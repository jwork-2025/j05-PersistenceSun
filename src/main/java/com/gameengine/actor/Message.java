package com.gameengine.actor;

/**
 * Actor消息基类
 * 所有在Actor之间传递的消息都应继承此类
 */
public abstract class Message {
    private final long timestamp;
    private final String senderId;
    
    public Message(String senderId) {
        this.timestamp = System.currentTimeMillis();
        this.senderId = senderId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    /**
     * 获取消息类型名称（用于日志和调试）
     */
    public String getTypeName() {
        return this.getClass().getSimpleName();
    }
}
