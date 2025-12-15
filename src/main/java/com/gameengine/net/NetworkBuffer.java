package com.gameengine.net;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NetworkBuffer {
    private static final Deque<Keyframe> buffer = new ArrayDeque<>();
    private static final Object lock = new Object();
    private static final double MAX_AGE_SEC = 2.0;
    private static final double INTERP_DELAY_SEC = 0.12; // 120ms 缓冲

    public static class Entity {
        public String id; 
        public float x; 
        public float y;
        public float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f; // 默认白色
    }
    public static class Keyframe {
        public double t;
        public String state = "PLAYING"; // MENU, PLAYING, VICTORY, DEFEAT
        public List<Entity> entities = new ArrayList<>();
    }

    public static void push(Keyframe kf) {
        synchronized (lock) {
            buffer.addLast(kf);
            // 修剪老帧
            double now = kf.t;
            while (!buffer.isEmpty() && now - buffer.peekFirst().t > MAX_AGE_SEC) buffer.pollFirst();
        }
    }

    public static Keyframe parseJsonLine(String line) {
        // 极简 JSON 解析（假设格式固定）：{"type":"kf","t":X,"state":"PLAYING","entities":[{"id":"...","x":N,"y":N},...]}
        if (line == null || !line.contains("\"type\":\"kf\"")) return null;
        Keyframe kf = new Keyframe();
        try {
            String ts = com.gameengine.recording.RecordingJson.field(line, "t");
            kf.t = com.gameengine.recording.RecordingJson.parseDouble(ts);
            
            // 解析state
            String stateStr = com.gameengine.recording.RecordingJson.field(line, "state");
            if (stateStr != null) {
                kf.state = com.gameengine.recording.RecordingJson.stripQuotes(stateStr);
            }
            
            int idx = line.indexOf("\"entities\":[");
            if (idx >= 0) {
                int bracket = line.indexOf('[', idx);
                String arr = bracket >= 0 ? com.gameengine.recording.RecordingJson.extractArray(line, bracket) : "";
                String[] parts = com.gameengine.recording.RecordingJson.splitTopLevel(arr);
                for (String p : parts) {
                    Entity e = new Entity();
                    e.id = com.gameengine.recording.RecordingJson.stripQuotes(com.gameengine.recording.RecordingJson.field(p, "id"));
                    e.x = (float)com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "x"));
                    e.y = (float)com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "y"));
                    
                    // 解析颜色（如果有）
                    String colorStr = com.gameengine.recording.RecordingJson.field(p, "color");
                    if (colorStr != null && colorStr.startsWith("[")) {
                        String[] rgba = colorStr.substring(1, colorStr.length()-1).split(",");
                        if (rgba.length >= 4) {
                            e.r = Float.parseFloat(rgba[0].trim());
                            e.g = Float.parseFloat(rgba[1].trim());
                            e.b = Float.parseFloat(rgba[2].trim());
                            e.a = Float.parseFloat(rgba[3].trim());
                        }
                    }
                    
                    kf.entities.add(e);
                }
            }
        } catch (Exception ignored) {}
        return kf;
    }

    public static Map<String, float[]> sample() {
        double now = System.currentTimeMillis() / 1000.0;
        double target = now - INTERP_DELAY_SEC;
        Keyframe a = null, b = null;
        synchronized (lock) {
            if (buffer.isEmpty()) return new HashMap<>();
            a = buffer.peekFirst();
            b = buffer.peekLast();
            for (Keyframe k : buffer) {
                if (k.t <= target) a = k; else { b = k; break; }
            }
        }
        if (a == null) return new HashMap<>();
        if (b == null) b = a;
        double span = Math.max(1e-6, b.t - a.t);
        double u = Math.max(0.0, Math.min(1.0, (target - a.t) / span));
        Map<String, float[]> out = new HashMap<>();
        int n = Math.min(a.entities.size(), b.entities.size());
        for (int i = 0; i < n; i++) {
            Entity ea = a.entities.get(i);
            Entity eb = b.entities.get(i);
            if (ea == null || eb == null || ea.id == null) continue;
            float x = (float)((1.0 - u) * ea.x + u * eb.x);
            float y = (float)((1.0 - u) * ea.y + u * eb.y);
            out.put(ea.id, new float[]{x, y});
        }
        return out;
    }
    
    public static Keyframe sampleKeyframe() {
        double now = System.currentTimeMillis() / 1000.0;
        double target = now - INTERP_DELAY_SEC;
        Keyframe a = null, b = null;
        synchronized (lock) {
            if (buffer.isEmpty()) return null;
            a = buffer.peekFirst();
            b = buffer.peekLast();
            for (Keyframe k : buffer) {
                if (k.t <= target) a = k; else { b = k; break; }
            }
        }
        if (a == null) return null;
        if (b == null) b = a;
        
        // 返回插值后的关键帧
        double span = Math.max(1e-6, b.t - a.t);
        double u = Math.max(0.0, Math.min(1.0, (target - a.t) / span));
        
        Keyframe result = new Keyframe();
        result.t = target;
        int n = Math.min(a.entities.size(), b.entities.size());
        for (int i = 0; i < n; i++) {
            Entity ea = a.entities.get(i);
            Entity eb = b.entities.get(i);
            if (ea == null || eb == null || ea.id == null) continue;
            
            Entity e = new Entity();
            e.id = ea.id;
            e.x = (float)((1.0 - u) * ea.x + u * eb.x);
            e.y = (float)((1.0 - u) * ea.y + u * eb.y);
            e.r = ea.r; e.g = ea.g; e.b = ea.b; e.a = ea.a; // 颜色不插值
            result.entities.add(e);
        }
        return result;
    }
}
