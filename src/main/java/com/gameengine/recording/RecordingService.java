package com.gameengine.recording;

import com.gameengine.components.RenderComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 录制服务 - 异步录制游戏状态
 * 录制内容：header（窗口信息） + input（输入事件） + keyframe（关键帧）
 */
public class RecordingService {
    private final RecordingConfig config;
    private final BlockingQueue<String> lineQueue;
    private volatile boolean recording;
    private Thread writerThread;
    private RecordingStorage storage = new FileRecordingStorage();
    private double elapsed;
    private double keyframeElapsed;
    private final double warmupSec = 0.1; // 等待初始化完成
    private final DecimalFormat qfmt;
    private Scene lastScene;

    public RecordingService(RecordingConfig config) {
        this.config = config;
        this.lineQueue = new ArrayBlockingQueue<>(config.queueCapacity);
        this.recording = false;
        this.elapsed = 0.0;
        this.keyframeElapsed = 0.0;
        this.qfmt = new DecimalFormat();
        this.qfmt.setMaximumFractionDigits(Math.max(0, config.quantizeDecimals));
        this.qfmt.setGroupingUsed(false);
    }

    public boolean isRecording() {
        return recording;
    }

    /**
     * 开始录制
     */
    public void start(Scene scene, int width, int height) throws IOException {
        if (recording) return;
        storage.openWriter(config.outputPath);
        
        // 启动异步写入线程
        writerThread = new Thread(() -> {
            try {
                while (recording || !lineQueue.isEmpty()) {
                    String s = lineQueue.poll();
                    if (s == null) {
                        try { 
                            Thread.sleep(2); 
                        } catch (InterruptedException ignored) {}
                        continue;
                    }
                    storage.writeLine(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { 
                    storage.closeWriter(); 
                } catch (Exception ignored) {}
            }
        }, "record-writer");
        
        recording = true;
        writerThread.start();

        // 写入header
        enqueue("{\"type\":\"header\",\"version\":1,\"w\":" + width + ",\"h\":" + height + "}");
        keyframeElapsed = 0.0;
        System.out.println("录制开始: " + config.outputPath);
    }

    /**
     * 停止录制
     */
    public void stop() {
        if (!recording) return;
        try {
            if (lastScene != null) {
                writeKeyframe(lastScene);
            }
        } catch (Exception ignored) {}
        recording = false;
        try { 
            writerThread.join(500); 
        } catch (InterruptedException ignored) {}
        System.out.println("录制结束");
    }

    /**
     * 更新录制状态
     */
    public void update(double deltaTime, Scene scene, InputManager input) {
        if (!recording) return;
        elapsed += deltaTime;
        keyframeElapsed += deltaTime;
        lastScene = scene;

        // 录制输入事件（只记录刚按下的键）
        Set<Integer> just = input.getJustPressedKeysSnapshot();
        if (!just.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"type\":\"input\",\"t\":")
              .append(qfmt.format(elapsed))
              .append(",\"keys\":[");
            boolean first = true;
            for (Integer k : just) {
                if (!first) sb.append(',');
                sb.append(k);
                first = false;
            }
            sb.append("]}");
            enqueue(sb.toString());
        }

        // 周期性写入关键帧（跳过暖机阶段）
        if (elapsed >= warmupSec && keyframeElapsed >= config.keyframeIntervalSec) {
            if (writeKeyframe(scene)) {
                keyframeElapsed = 0.0;
            }
        }
    }

    /**
     * 写入关键帧
     */
    private boolean writeKeyframe(Scene scene) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"keyframe\",\"t\":")
          .append(qfmt.format(elapsed))
          .append(",\"entities\":[");
        
        List<GameObject> objs = scene.getGameObjects();
        boolean first = true;
        int count = 0;
        
        for (GameObject obj : objs) {
            if (!obj.isActive()) continue;
            
            TransformComponent tc = obj.getComponent(TransformComponent.class);
            if (tc == null) continue;
            
            Vector2 pos = tc.getPosition();
            if (!first) sb.append(',');
            
            // 使用对象的instanceId作为唯一标识
            String uniqueId = obj.getName() + "#" + obj.getInstanceId();
            
            sb.append('{')
              .append("\"id\":\"").append(uniqueId).append("\",")
              .append("\"x\":").append(qfmt.format(pos.x)).append(',')
              .append("\"y\":").append(qfmt.format(pos.y));

            // 录制渲染信息（形状、尺寸、颜色）
            RenderComponent rc = obj.getComponent(RenderComponent.class);
            if (rc != null) {
                RenderComponent.RenderType rt = rc.getRenderType();
                Vector2 sz = rc.getSize();
                RenderComponent.Color col = rc.getColor();
                
                sb.append(',')
                  .append("\"rt\":\"").append(rt.name()).append("\",")
                  .append("\"w\":").append(qfmt.format(sz.x)).append(',')
                  .append("\"h\":").append(qfmt.format(sz.y)).append(',')
                  .append("\"color\":[")
                  .append(qfmt.format(col.r)).append(',')
                  .append(qfmt.format(col.g)).append(',')
                  .append(qfmt.format(col.b)).append(',')
                  .append(qfmt.format(col.a)).append(']');
            }

            sb.append('}');
            first = false;
            count++;
        }
        
        sb.append("]}");
        if (count == 0) return false;
        
        enqueue(sb.toString());
        return true;
    }

    /**
     * 将数据加入队列
     */
    private void enqueue(String line) {
        if (!lineQueue.offer(line)) {
            // 队列满时丢弃（简单策略）
            System.err.println("Recording queue full, dropping data");
        }
    }
}
