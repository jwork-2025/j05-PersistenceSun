package com.gameengine.actor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Actor基类
 * 每个Actor拥有独立的消息队列和处理线程
 * 通过消息传递而非共享内存来通信
 */
public abstract class Actor {
    private final String actorId;
    private final BlockingQueue<Message> mailbox;
    private final AtomicBoolean running;
    private Thread workerThread;
    private final int mailboxCapacity;
    
    // 统计信息
    private long messagesProcessed = 0;
    private long messagesDropped = 0;
    
    public Actor(String actorId) {
        this(actorId, 10000); // 默认邮箱容量10000
    }
    
    public Actor(String actorId, int mailboxCapacity) {
        this.actorId = actorId;
        this.mailboxCapacity = mailboxCapacity;
        this.mailbox = new LinkedBlockingQueue<>(mailboxCapacity);
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * 启动Actor
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            workerThread = new Thread(this::processMessages, "Actor-" + actorId);
            workerThread.setDaemon(true);
            workerThread.start();
            onStart();
        }
    }
    
    /**
     * 停止Actor
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            onStop();
            if (workerThread != null) {
                workerThread.interrupt();
            }
        }
    }
    
    /**
     * 发送消息到此Actor（非阻塞）
     * @return true表示消息已入队，false表示邮箱已满
     */
    public boolean tell(Message message) {
        if (!running.get()) {
            return false;
        }
        
        boolean offered = mailbox.offer(message);
        if (!offered) {
            messagesDropped++;
            onMessageDropped(message);
        }
        return offered;
    }
    
    /**
     * 消息处理循环
     */
    private void processMessages() {
        while (running.get()) {
            try {
                Message message = mailbox.take();
                onReceive(message);
                messagesProcessed++;
            } catch (InterruptedException e) {
                if (!running.get()) {
                    break;
                }
            } catch (Exception e) {
                onError(e);
            }
        }
    }
    
    /**
     * 子类必须实现：处理接收到的消息
     */
    protected abstract void onReceive(Message message);
    
    /**
     * Actor启动时的回调（可选重写）
     */
    protected void onStart() {}
    
    /**
     * Actor停止时的回调（可选重写）
     */
    protected void onStop() {}
    
    /**
     * 消息被丢弃时的回调（可选重写）
     */
    protected void onMessageDropped(Message message) {}
    
    /**
     * 处理消息时发生异常的回调（可选重写）
     */
    protected void onError(Exception e) {
        System.err.println("[Actor-" + actorId + "] 错误: " + e.getMessage());
    }
    
    // Getters
    public String getActorId() {
        return actorId;
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public int getMailboxSize() {
        return mailbox.size();
    }
    
    public long getMessagesProcessed() {
        return messagesProcessed;
    }
    
    public long getMessagesDropped() {
        return messagesDropped;
    }
    
    /**
     * 获取统计信息
     */
    public String getStats() {
        return String.format("[%s] Processed: %d, Dropped: %d, Pending: %d/%d",
                actorId, messagesProcessed, messagesDropped, 
                mailbox.size(), mailboxCapacity);
    }
}
