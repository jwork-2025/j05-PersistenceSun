package com.gameengine.actor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Actor系统管理器
 * 负责Actor的注册、查找和生命周期管理
 */
public class ActorSystem {
    private static final ActorSystem INSTANCE = new ActorSystem();
    
    private final ConcurrentHashMap<String, Actor> actors;
    private final ScheduledExecutorService scheduler;
    private volatile boolean started = false;
    
    private ActorSystem() {
        this.actors = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    public static ActorSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册并启动Actor
     */
    public void registerActor(Actor actor) {
        if (actors.putIfAbsent(actor.getActorId(), actor) == null) {
            actor.start();
        }
    }
    
    /**
     * 根据ID获取Actor
     */
    public Actor getActor(String actorId) {
        return actors.get(actorId);
    }
    
    /**
     * 移除并停止Actor
     */
    public void removeActor(String actorId) {
        Actor actor = actors.remove(actorId);
        if (actor != null) {
            actor.stop();
        }
    }
    
    /**
     * 发送消息到指定Actor
     */
    public boolean send(String targetActorId, Message message) {
        Actor actor = actors.get(targetActorId);
        if (actor != null) {
            return actor.tell(message);
        }
        return false;
    }
    
    /**
     * 广播消息到所有Actor
     */
    public void broadcast(Message message) {
        for (Actor actor : actors.values()) {
            actor.tell(message);
        }
    }
    
    /**
     * 定时任务调度
     */
    public void scheduleTask(Runnable task, long delayMs, long periodMs) {
        scheduler.scheduleAtFixedRate(task, delayMs, periodMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 启动监控任务，定期输出Actor统计信息
     */
    public void startMonitoring(long intervalMs) {
        if (!started) {
            started = true;
            scheduleTask(() -> {
                System.out.println("\n========== Actor System Stats ==========");
                for (Actor actor : actors.values()) {
                    System.out.println(actor.getStats());
                }
                System.out.println("========================================\n");
            }, intervalMs, intervalMs);
        }
    }
    
    /**
     * 停止所有Actor
     */
    public void shutdown() {
        for (Actor actor : actors.values()) {
            actor.stop();
        }
        actors.clear();
        scheduler.shutdown();
    }
    
    /**
     * 获取Actor数量
     */
    public int getActorCount() {
        return actors.size();
    }
}
