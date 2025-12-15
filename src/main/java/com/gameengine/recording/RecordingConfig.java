package com.gameengine.recording;

/**
 * 录制配置
 */
public class RecordingConfig {
    /** 关键帧间隔（秒） */
    public final double keyframeIntervalSec;
    
    /** 队列容量 */
    public final int queueCapacity;
    
    /** 输出路径 */
    public final String outputPath;
    
    /** 数值精度（小数位数） */
    public final int quantizeDecimals;

    public RecordingConfig(double keyframeIntervalSec, int queueCapacity, 
                          String outputPath, int quantizeDecimals) {
        this.keyframeIntervalSec = keyframeIntervalSec;
        this.queueCapacity = queueCapacity;
        this.outputPath = outputPath;
        this.quantizeDecimals = quantizeDecimals;
    }

    /**
     * 默认配置：每0.1秒一个关键帧，队列1000，保留2位小数
     */
    public static RecordingConfig createDefault() {
        long timestamp = System.currentTimeMillis();
        String path = "recordings/session_" + timestamp + ".jsonl";
        return new RecordingConfig(0.1, 1000, path, 2);
    }
}
