package com.gameengine.actor.messages;

import com.gameengine.actor.Message;
import com.gameengine.core.GameObject;

import java.util.List;

/**
 * 游戏状态快照消息
 */
public class GameStateSnapshotMessage extends Message {
    private final List<GameObject> gameObjects;
    private final String gameState;
    
    public GameStateSnapshotMessage(List<GameObject> gameObjects, String gameState) {
        super("GameLogicActor");
        this.gameObjects = gameObjects;
        this.gameState = gameState;
    }
    
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }
    
    public String getGameState() {
        return gameState;
    }
}
