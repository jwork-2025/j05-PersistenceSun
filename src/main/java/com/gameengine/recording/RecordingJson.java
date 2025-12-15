package com.gameengine.recording;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的JSON解析工具
 * 用于解析录制文件中的JSON数据
 */
public final class RecordingJson {
    private RecordingJson() {}

    /**
     * 从JSON字符串中提取指定字段的值
     */
    public static String field(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i < 0) return null;
        int c = json.indexOf(':', i);
        if (c < 0) return null;
        int end = c + 1;
        
        // 跳过空白
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) {
            end++;
        }
        
        // 如果值是数组，找到对应的 ]
        if (end < json.length() && json.charAt(end) == '[') {
            int depth = 1;
            int j = end + 1;
            while (j < json.length() && depth > 0) {
                if (json.charAt(j) == '[') depth++;
                else if (json.charAt(j) == ']') depth--;
                j++;
            }
            return json.substring(end, j).trim();
        }
        
        // 否则使用逗号或花括号作为结束标记
        int comma = json.indexOf(',', end);
        int brace = json.indexOf('}', end);
        int j = (comma < 0) ? brace : (brace < 0 ? comma : Math.min(comma, brace));
        if (j < 0) j = json.length();
        return json.substring(end, j).trim();
    }

    /**
     * 去除字符串两端的引号
     */
    public static String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * 解析double值
     */
    public static double parseDouble(String s) {
        if (s == null) return 0.0;
        try { 
            return Double.parseDouble(stripQuotes(s)); 
        } catch (Exception e) { 
            return 0.0; 
        }
    }

    /**
     * 分割顶层数组元素
     */
    public static String[] splitTopLevel(String arr) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < arr.length(); i++) {
            char ch = arr.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
            } else if (ch == ',' && depth == 0) {
                out.add(arr.substring(start, i));
                start = i + 1;
            }
        }
        if (start < arr.length()) {
            out.add(arr.substring(start));
        }
        return out.stream()
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .toArray(String[]::new);
    }

    /**
     * 提取数组内容
     */
    public static String extractArray(String json, int startIdx) {
        int i = startIdx;
        if (i >= json.length() || json.charAt(i) != '[') {
            return "";
        }
        int depth = 1;
        int begin = i + 1;
        i++;
        for (; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '[') {
                depth++;
            } else if (ch == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(begin, i);
                }
            }
        }
        return "";
    }
}
