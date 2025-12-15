package com.gameengine.example;

import com.gameengine.components.RenderComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameEngine;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.Renderer;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.recording.FileRecordingStorage;
import com.gameengine.recording.RecordingJson;
import com.gameengine.recording.RecordingStorage;
import com.gameengine.scene.GridSystem;
import com.gameengine.scene.Scene;

import java.io.File;
import java.util.*;

/**
 * 回放场景 - 用于播放录制的游戏
 */
public class ReplayScene extends Scene {
    private final GameEngine engine;
    private String recordingPath;
    private Renderer renderer;
    private InputManager input;
    private float time;
    private GridSystem gridSystem;

    // 关键帧数据结构
    private static class Keyframe {
        static class EntityInfo {
            Vector2 pos;
            String rt; // RECTANGLE/CIRCLE
            float w, h;
            float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f; // 默认白色
            String id;
        }
        double t;
        List<EntityInfo> entities = new ArrayList<>();
    }

    private final List<Keyframe> keyframes = new ArrayList<>();
    private final Map<String, GameObject> objectMap = new HashMap<>();  // 按ID管理对象

    // 文件选择模式
    private List<File> recordingFiles;
    private int selectedIndex = 0;

    /**
     * 构造函数
     * @param engine 游戏引擎
     * @param path 录制文件路径，如果为null则显示文件列表
     */
    public ReplayScene(GameEngine engine, String path) {
        super("Replay");
        this.engine = engine;
        this.recordingPath = path;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.input = engine.getInputManager();
        this.gridSystem = new GridSystem();
        
        // 重置状态
        this.time = 0f;
        this.keyframes.clear();
        this.objectMap.clear();
        
        if (recordingPath != null) {
            // 加载录制文件
            loadRecording(recordingPath);
            buildObjectsFromFirstKeyframe();
        } else {
            // 进入文件选择模式
            this.recordingFiles = null;
            this.selectedIndex = 0;
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // ESC键返回主菜单
        if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            returnToMenu();
            return;
        }
        
        // 文件选择模式
        if (recordingPath == null) {
            handleFileSelection();
            return;
        }

        if (keyframes.isEmpty()) return;
        
        // 更新时间
        time += deltaTime;
        
        // 限制在最后关键帧处停止
        double lastT = keyframes.get(keyframes.size() - 1).t;
        if (time > lastT) {
            time = (float) lastT;
        }

        // 查找当前时间所在的关键帧区间
        Keyframe a = keyframes.get(0);
        Keyframe b = keyframes.get(keyframes.size() - 1);
        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe k1 = keyframes.get(i);
            Keyframe k2 = keyframes.get(i + 1);
            if (time >= k1.t && time <= k2.t) {
                a = k1;
                b = k2;
                break;
            }
        }
        
        // 计算插值参数
        double span = Math.max(1e-6, b.t - a.t);
        double u = Math.min(1.0, Math.max(0.0, (time - a.t) / span));
        
        // 更新插值位置
        updateInterpolatedPositions(a, b, (float) u);
    }

    @Override
    public void render() {
        if (recordingPath == null) {
            // 绘制深蓝色背景（文件列表模式）
            renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 
                0.06f, 0.06f, 0.08f, 1.0f);
            // 渲染文件列表
            renderFileList();
            return;
        }
        
        // 绘制游戏背景（与游戏中一致）
        renderer.drawRect(0, 0, 800, 600, 0.1f, 0.1f, 0.2f, 1.0f);
        
        // 渲染网格线
        if (gridSystem != null) {
            gridSystem.render(renderer);
        }
        
        // 渲染回放对象
        super.render();
        
        // 绘制提示信息
        String hint = "REPLAY: ESC to return";
        float w = hint.length() * 12.0f;
        renderer.drawText(hint, renderer.getWidth() / 2.0f - w / 2.0f, 30, 18, 
            0.8f, 0.8f, 0.8f, 1.0f);
        
        // 绘制时间信息
        if (!keyframes.isEmpty()) {
            double lastT = keyframes.get(keyframes.size() - 1).t;
            String timeInfo = String.format("Time: %.1fs / %.1fs", time, lastT);
            renderer.drawText(timeInfo, 10, 30, 16, 0.7f, 0.7f, 0.7f, 1.0f);
        }
    }

    /**
     * 加载录制文件
     */
    private void loadRecording(String path) {
        keyframes.clear();
        RecordingStorage storage = new FileRecordingStorage();
        
        try {
            for (String line : storage.readLines(path)) {
                if (line.contains("\"type\":\"keyframe\"")) {
                    Keyframe kf = new Keyframe();
                    kf.t = RecordingJson.parseDouble(RecordingJson.field(line, "t"));
                    
                    // 解析entities数组
                    int idx = line.indexOf("\"entities\":[");
                    if (idx >= 0) {
                        int bracket = line.indexOf('[', idx);
                        String arr = bracket >= 0 ? RecordingJson.extractArray(line, bracket) : "";
                        String[] parts = RecordingJson.splitTopLevel(arr);
                        
                        for (String p : parts) {
                            Keyframe.EntityInfo ei = new Keyframe.EntityInfo();
                            ei.id = RecordingJson.stripQuotes(RecordingJson.field(p, "id"));
                            double x = RecordingJson.parseDouble(RecordingJson.field(p, "x"));
                            double y = RecordingJson.parseDouble(RecordingJson.field(p, "y"));
                            ei.pos = new Vector2((float) x, (float) y);
                            
                            String rt = RecordingJson.stripQuotes(RecordingJson.field(p, "rt"));
                            ei.rt = rt;
                            ei.w = (float) RecordingJson.parseDouble(RecordingJson.field(p, "w"));
                            ei.h = (float) RecordingJson.parseDouble(RecordingJson.field(p, "h"));
                            
                            // 解析颜色
                            String colorArr = RecordingJson.field(p, "color");
                            if (colorArr != null && colorArr.startsWith("[")) {
                                String c = colorArr.substring(1, Math.max(1, colorArr.indexOf(']', 1)));
                                String[] cs = c.split(",");
                                if (cs.length >= 3) {
                                    try {
                                        ei.r = Float.parseFloat(cs[0].trim());
                                        ei.g = Float.parseFloat(cs[1].trim());
                                        ei.b = Float.parseFloat(cs[2].trim());
                                        if (cs.length >= 4) ei.a = Float.parseFloat(cs[3].trim());
                                    } catch (Exception ignored) {}
                                }
                            }
                            
                            kf.entities.add(ei);
                        }
                    }
                    keyframes.add(kf);
                }
            }
        } catch (Exception e) {
            System.err.println("加载录制文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 按时间排序
        keyframes.sort(Comparator.comparingDouble(k -> k.t));
        System.out.println("加载了 " + keyframes.size() + " 个关键帧");
        if (!keyframes.isEmpty()) {
            Keyframe first = keyframes.get(0);
            System.out.println("第一帧包含 " + first.entities.size() + " 个实体");
            for (Keyframe.EntityInfo ei : first.entities) {
                System.out.println("  - " + ei.id + " at (" + ei.pos.x + ", " + ei.pos.y + ") " + ei.rt + " [" + ei.r + "," + ei.g + "," + ei.b + "]");
            }
        }
    }

    /**
     * 从第一个关键帧构建对象
     */
    private void buildObjectsFromFirstKeyframe() {
        if (keyframes.isEmpty()) return;
        
        Keyframe kf0 = keyframes.get(0);
        objectMap.clear();
        clear();
        
        for (int i = 0; i < kf0.entities.size(); i++) {
            Keyframe.EntityInfo ei = kf0.entities.get(i);
            GameObject obj = buildObjectFromEntity(ei, i);
            addGameObject(obj);
            objectMap.put(ei.id, obj);
        }
        
        time = 0f;
    }

    /**
     * 更新插值位置（按ID匹配实体）
     */
    private void updateInterpolatedPositions(Keyframe a, Keyframe b, float u) {
        // 收集两帧中所有实体ID
        Set<String> allIds = new HashSet<>();
        Map<String, Keyframe.EntityInfo> mapA = new HashMap<>();
        Map<String, Keyframe.EntityInfo> mapB = new HashMap<>();
        
        for (Keyframe.EntityInfo ei : a.entities) {
            mapA.put(ei.id, ei);
            allIds.add(ei.id);
        }
        for (Keyframe.EntityInfo ei : b.entities) {
            mapB.put(ei.id, ei);
            allIds.add(ei.id);
        }
        
        // 逐个处理实体
        for (String id : allIds) {
            Keyframe.EntityInfo eiA = mapA.get(id);
            Keyframe.EntityInfo eiB = mapB.get(id);
            
            // 如果对象不存在，创建它
            if (!objectMap.containsKey(id)) {
                // 使用eiA或eiB来创建（优先eiA）
                Keyframe.EntityInfo template = eiA != null ? eiA : eiB;
                if (template != null) {
                    GameObject obj = buildObjectFromEntity(template, objectMap.size());
                    addGameObject(obj);
                    objectMap.put(id, obj);
                }
            }
            
            GameObject obj = objectMap.get(id);
            if (obj == null) continue;
            
            // 选择要使用的EntityInfo（优先使用B帧以获取最新数据）
            Keyframe.EntityInfo currentInfo = eiB != null ? eiB : eiA;
            
            // 如果实体在两帧中都存在，进行插值
            if (eiA != null && eiB != null) {
                float x = (1.0f - u) * eiA.pos.x + u * eiB.pos.x;
                float y = (1.0f - u) * eiA.pos.y + u * eiB.pos.y;
                
                TransformComponent tc = obj.getComponent(TransformComponent.class);
                if (tc != null) {
                    tc.setPosition(new Vector2(x, y));
                }
                
                // 更新渲染信息（颜色、大小）
                updateRenderComponent(obj, currentInfo);
                obj.setActive(true);
            }
            // 如果只在帧A中存在（正在消失）
            else if (eiA != null) {
                TransformComponent tc = obj.getComponent(TransformComponent.class);
                if (tc != null) {
                    tc.setPosition(new Vector2(eiA.pos));
                }
                
                // 更新渲染信息
                updateRenderComponent(obj, eiA);
                obj.setActive(true);
            }
            // 如果只在帧B中存在（正在出现）
            else if (eiB != null) {
                TransformComponent tc = obj.getComponent(TransformComponent.class);
                if (tc != null) {
                    tc.setPosition(new Vector2(eiB.pos));
                }
                
                // 更新渲染信息
                updateRenderComponent(obj, eiB);
                obj.setActive(true);
            }
        }
        
        // 隐藏不在当前帧中的对象
        for (Map.Entry<String, GameObject> entry : objectMap.entrySet()) {
            if (!allIds.contains(entry.getKey())) {
                entry.getValue().setActive(false);
            }
        }
    }

    /**
     * 更新对象的渲染组件（颜色、大小等）
     */
    private void updateRenderComponent(GameObject obj, Keyframe.EntityInfo ei) {
        RenderComponent rc = obj.getComponent(RenderComponent.class);
        if (rc == null) {
            System.err.println("警告: 对象 " + obj.getName() + " 没有RenderComponent!");
            return;
        }
        
        // 确保Renderer被设置
        if (renderer != null) {
            rc.setRenderer(renderer);
        }
        
        // 更新颜色
        rc.setColor(new RenderComponent.Color(ei.r, ei.g, ei.b, ei.a));
        
        // 更新大小
        float width = Math.max(1, ei.w > 0 ? ei.w : 30);
        float height = Math.max(1, ei.h > 0 ? ei.h : 30);
        rc.setSize(new Vector2(width, height));
        
        // 更新渲染类型（如果需要）
        if (ei.rt != null) {
            RenderComponent.RenderType newType = "CIRCLE".equals(ei.rt) 
                ? RenderComponent.RenderType.CIRCLE 
                : RenderComponent.RenderType.RECTANGLE;
            if (rc.getRenderType() != newType) {
                // 如果类型改变了，需要重新创建RenderComponent
                // 这里简单处理，保持原类型
            }
        }
    }

    /**
     * 从实体信息构建游戏对象
     */
    private GameObject buildObjectFromEntity(Keyframe.EntityInfo ei, int index) {
        String name = ei.id != null ? ei.id : ("ReplayObj#" + index);
        GameObject obj = new GameObject(name);
        
        // 添加Transform组件
        obj.addComponent(new TransformComponent(new Vector2(ei.pos)));
        
        // 添加Render组件
        RenderComponent.RenderType renderType;
        if ("CIRCLE".equals(ei.rt)) {
            renderType = RenderComponent.RenderType.CIRCLE;
        } else {
            renderType = RenderComponent.RenderType.RECTANGLE;
        }
        
        float width = Math.max(1, ei.w > 0 ? ei.w : 30);
        float height = Math.max(1, ei.h > 0 ? ei.h : 30);
        
        RenderComponent rc = new RenderComponent(
            renderType,
            new Vector2(width, height),
            new RenderComponent.Color(ei.r, ei.g, ei.b, ei.a)
        );
        rc.setRenderer(renderer);
        obj.addComponent(rc);
        
        return obj;
    }

    /**
     * 返回主菜单
     */
    private void returnToMenu() {
        // 重新创建主游戏场景（回到菜单状态）
        try {
            // 通过反射或者直接访问引擎来切换回主场景
            // 这里简单处理：退出程序或重启
            System.out.println("按ESC返回主菜单（需要重启游戏）");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== 文件选择模式 ==========

    /**
     * 确保文件列表已加载
     */
    private void ensureFilesListed() {
        if (recordingFiles != null) return;
        RecordingStorage storage = new FileRecordingStorage();
        recordingFiles = storage.listRecordings();
    }

    /**
     * 处理文件选择输入
     */
    private void handleFileSelection() {
        ensureFilesListed();
        
        // 上箭头
        if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_UP)) {
            selectedIndex = (selectedIndex - 1 + Math.max(1, recordingFiles.size())) 
                % Math.max(1, recordingFiles.size());
        }
        // 下箭头
        else if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_DOWN)) {
            selectedIndex = (selectedIndex + 1) % Math.max(1, recordingFiles.size());
        }
        // 回车或空格
        else if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_ENTER) || 
                 input.isKeyJustPressed(java.awt.event.KeyEvent.VK_SPACE)) {
            if (!recordingFiles.isEmpty()) {
                String path = recordingFiles.get(selectedIndex).getAbsolutePath();
                this.recordingPath = path;
                clear();
                initialize();
            }
        }
    }

    /**
     * 渲染文件列表
     */
    private void renderFileList() {
        ensureFilesListed();
        
        int w = renderer.getWidth();
        int h = renderer.getHeight();
        
        // 标题
        String title = "选择录制文件";
        float tw = title.length() * 16f;
        renderer.drawText(title, w / 2f - tw / 2f, 80, 24, 1f, 1f, 1f, 1f);

        if (recordingFiles.isEmpty()) {
            String none = "未找到录制文件";
            float nw = none.length() * 14f;
            renderer.drawText(none, w / 2f - nw / 2f, h / 2f, 20, 0.9f, 0.8f, 0.2f, 1f);
            
            String back = "ESC 返回";
            float bw = back.length() * 12f;
            renderer.drawText(back, w / 2f - bw / 2f, h - 60, 18, 0.7f, 0.7f, 0.7f, 1f);
            return;
        }

        // 文件列表
        float startY = 140f;
        float itemH = 28f;
        for (int i = 0; i < recordingFiles.size(); i++) {
            String name = recordingFiles.get(i).getName();
            float x = 100f;
            float y = startY + i * itemH;
            
            // 高亮选中项
            if (i == selectedIndex) {
                renderer.drawRect(x - 10, y - 6, 600, 24, 0.3f, 0.3f, 0.4f, 0.8f);
            }
            
            renderer.drawText(name, x, y, 18, 0.9f, 0.9f, 0.9f, 1f);
        }

        // 提示信息
        String hint = "上下键选择, 回车播放, ESC返回";
        float hw = hint.length() * 12f;
        renderer.drawText(hint, w / 2f - hw / 2f, h - 60, 18, 0.7f, 0.7f, 0.7f, 1f);
    }
}
