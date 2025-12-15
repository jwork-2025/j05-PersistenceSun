package com.gameengine.actor.actors;

import com.gameengine.actor.Actor;
import com.gameengine.actor.Message;
import com.gameengine.actor.messages.GameStateSnapshotMessage;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

import java.util.List;

/**
 * 网络广播Actor
 * 负责将游戏状态序列化并广播给所有客户端
 */
public class NetworkBroadcastActor extends Actor {
    
    private long lastBroadcastTime = 0;
    private final long broadcastInterval = 50; // 50ms = 20Hz
    
    public NetworkBroadcastActor() {
        super("NetworkBroadcast", 5000);
    }
    
    @Override
    protected void onReceive(Message message) {
        if (message instanceof GameStateSnapshotMessage) {
            handleGameStateSnapshot((GameStateSnapshotMessage) message);
        }
    }
    
    private void handleGameStateSnapshot(GameStateSnapshotMessage msg) {
        long now = System.currentTimeMillis();
        
        // 限流：确保广播间隔不小于50ms
        if (now - lastBroadcastTime < broadcastInterval) {
            return;
        }
        lastBroadcastTime = now;
        
        // 序列化游戏状态为JSON
        String json = serializeGameState(msg.getGameObjects(), msg.getGameState());
        
        // 更新到NetState（由NioServer读取并广播）
        com.gameengine.net.NetState.setLastKeyframeJson(json);
    }
    
    private String serializeGameState(List<GameObject> objects, String gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"kf\",\"t\":").append(System.currentTimeMillis())
          .append(",\"state\":\"").append(gameState).append("\",\"entities\":[");
        
        boolean first = true;
        for (GameObject obj : objects) {
            if (!obj.isActive()) continue;
            
            TransformComponent tc = obj.getComponent(TransformComponent.class);
            RenderComponent rc = obj.getComponent(RenderComponent.class);
            
            if (tc == null) continue;
            
            if (!first) sb.append(',');
            
            Vector2 pos = tc.getPosition();
            sb.append("{\"id\":\"").append(obj.getName()).append("#").append(obj.getInstanceId())
              .append("\",\"x\":").append(pos.x)
              .append(",\"y\":").append(pos.y);
            
            // 颜色信息
            if (rc != null) {
                RenderComponent.Color color = rc.getColor();
                sb.append(",\"color\":[")
                  .append(color.r).append(',')
                  .append(color.g).append(',')
                  .append(color.b).append(',')
                  .append(color.a).append(']');
            }
            
            sb.append('}');
            first = false;
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    @Override
    protected void onStart() {
        System.out.println("[NetworkBroadcastActor] 已启动，广播频率: " + (1000 / broadcastInterval) + "Hz");
    }
    
    @Override
    protected void onMessageDropped(Message message) {
        // 网络广播消息丢弃是可接受的（降级处理）
    }
}
