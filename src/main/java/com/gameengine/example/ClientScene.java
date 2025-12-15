package com.gameengine.example;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameEngine;
import com.gameengine.core.GameLogic;
import com.gameengine.entities.EntityFactory;
import com.gameengine.graphics.Renderer;
import com.gameengine.math.Vector2;
import com.gameengine.net.NetworkBuffer;
import com.gameengine.scene.GridSystem;
import com.gameengine.scene.Scene;

/**
 * 客户端场景 - 接收服务器广播并渲染游戏状态
 */
public class ClientScene extends Scene {
    private final GameEngine engine;
    private Renderer renderer;
    private GridSystem gridSystem;
    private EntityFactory entityFactory;
    private String currentGameState = "PLAYING"; // 当前游戏状态

    public ClientScene(GameEngine engine) {
        super("ClientScene");
        this.engine = engine;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.gridSystem = new GridSystem();
        this.entityFactory = new EntityFactory(gridSystem, renderer);
        
        System.out.println("客户端场景初始化完成");
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // 从网络缓冲区采样插值后的位置
        NetworkBuffer.Keyframe kf = NetworkBuffer.sampleKeyframe();
        if (kf == null) return;
        
        // 更新游戏状态
        currentGameState = kf.state;
        
        // 标记所有现有对象为"未更新"
        java.util.Set<String> receivedIds = new java.util.HashSet<>();
        
        for (NetworkBuffer.Entity entity : kf.entities) {
            String id = entity.id;
            receivedIds.add(id);
            
            // 查找或创建对应的镜像对象
            GameObject obj = findOrCreateMirror(id);
            
            // 更新位置
            TransformComponent tc = obj.getComponent(TransformComponent.class);
            if (tc != null) {
                tc.setPosition(new Vector2(entity.x, entity.y));
            }
            
            // 更新颜色
            RenderComponent rc = obj.getComponent(RenderComponent.class);
            if (rc != null) {
                rc.setColor(new RenderComponent.Color(entity.r, entity.g, entity.b, entity.a));
            }
        }
        
        // 清理不在快照中的对象（已死亡或被移除）
        java.util.List<GameObject> toRemove = new java.util.ArrayList<>();
        for (GameObject obj : getGameObjects()) {
            // 客户端镜像对象的名字就是完整ID
            String objId = obj.getName();
            if (!receivedIds.contains(objId)) {
                toRemove.add(obj);
            }
        }
        for (GameObject obj : toRemove) {
            obj.setActive(false);
            getGameObjects().remove(obj);
        }
    }

    @Override
    public void render() {
        // 绘制背景
        renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.1f, 0.1f, 0.15f, 1.0f);
        
        // 根据游戏状态绘制不同界面
        if ("MENU".equals(currentGameState)) {
            renderMenuState();
        } else if ("VICTORY".equals(currentGameState)) {
            renderVictoryState();
        } else if ("DEFEAT".equals(currentGameState)) {
            renderDefeatState();
        } else {
            // PLAYING状态：绘制游戏场景
            renderPlayingState();
        }
        
        // 绘制客户端提示
        renderer.drawText("CLIENT MODE - Viewing Server", 10, 10, 16, 0.5f, 1.0f, 0.5f, 1.0f);
        renderer.drawText("Connected clients: " + com.gameengine.net.NetState.getClientCount(), 10, 30, 14, 0.8f, 0.8f, 0.8f, 1.0f);
        renderer.drawText("State: " + currentGameState, 10, 50, 14, 0.8f, 0.8f, 0.8f, 1.0f);
    }
    
    private void renderMenuState() {
        float cx = renderer.getWidth() / 2.0f;
        float cy = renderer.getHeight() / 2.0f;
        renderer.drawText("MENU", cx - 30, cy - 20, 24, 1.0f, 1.0f, 1.0f, 1.0f);
        renderer.drawText("(Server is in menu)", cx - 80, cy + 20, 14, 0.7f, 0.7f, 0.7f, 1.0f);
    }
    
    private void renderVictoryState() {
        drawGrid();
        super.render();
        
        float cx = renderer.getWidth() / 2.0f;
        float cy = renderer.getHeight() / 2.0f;
        renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.0f, 0.0f, 0.0f, 0.5f);
        renderer.drawRect(cx - 200, cy - 80, 400, 160, 0.0f, 0.3f, 0.0f, 0.8f);
        renderer.drawText("VICTORY!", cx - 50, cy - 20, 24, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.drawText("Server won the game!", cx - 90, cy + 20, 16, 0.8f, 0.8f, 0.8f, 1.0f);
    }
    
    private void renderDefeatState() {
        drawGrid();
        super.render();
        
        float cx = renderer.getWidth() / 2.0f;
        float cy = renderer.getHeight() / 2.0f;
        renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.0f, 0.0f, 0.0f, 0.5f);
        renderer.drawRect(cx - 200, cy - 80, 400, 160, 0.3f, 0.0f, 0.0f, 0.8f);
        renderer.drawText("DEFEAT", cx - 40, cy - 20, 24, 1.0f, 0.0f, 0.0f, 1.0f);
        renderer.drawText("Server lost the game", cx - 90, cy + 20, 16, 0.8f, 0.8f, 0.8f, 1.0f);
    }
    
    private void renderPlayingState() {
        // 绘制网格线
        drawGrid();
        
        // 渲染所有游戏对象
        super.render();
    }

    /**
     * 绘制网格线
     */
    private void drawGrid() {
        float cellWidth = 80f;
        float cellHeight = 80f;
        
        // 垂直线
        for (int i = 0; i <= 9; i++) {
            float x = i * cellWidth;
            renderer.drawLine(x, 0, x, renderer.getHeight(), 0.2f, 0.2f, 0.25f, 0.3f);
        }
        
        // 水平线
        for (int i = 0; i <= 5; i++) {
            float y = i * cellHeight;
            renderer.drawLine(0, y, renderer.getWidth(), y, 0.2f, 0.2f, 0.25f, 0.3f);
        }
    }

    /**
     * 查找或创建镜像对象
     */
    private GameObject findOrCreateMirror(String id) {
        // 先查找已存在的对象（使用完整ID作为对象名）
        for (GameObject obj : getGameObjects()) {
            // 客户端镜像对象直接用完整ID作为名字
            if (id.equals(obj.getName())) {
                return obj;
            }
        }
        
        // 解析ID，提取基础名称
        String baseName = id;
        int hashIndex = id.indexOf('#');
        if (hashIndex >= 0) {
            baseName = id.substring(0, hashIndex);
        }
        
        // 根据名称创建对应的视觉对象（传入完整ID）
        GameObject obj = createVisualObject(baseName, id);
        addGameObject(obj);
        return obj;
    }

    /**
     * 根据名称创建视觉对象
     */
    private GameObject createVisualObject(String baseName, String fullId) {
        GameObject obj = new GameObject(fullId) {
            @Override
            public void update(float deltaTime) {
                super.update(deltaTime);
                updateComponents(deltaTime);
            }

            @Override
            public void render() {
                renderComponents();
            }
        };

        obj.addComponent(new TransformComponent(new Vector2(0, 0)));
        
        RenderComponent rc;
        
        // 根据名称判断类型并设置颜色
        if (baseName.contains("Dawa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(0.8f, 0.0f, 0.0f, 1.0f) // 红色
            ));
        } else if (baseName.contains("Erwa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(1.0f, 0.5f, 0.0f, 1.0f) // 橙色
            ));
        } else if (baseName.contains("Sanwa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(1.0f, 1.0f, 0.0f, 1.0f) // 黄色
            ));
        } else if (baseName.contains("Siwa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(0.0f, 0.8f, 0.0f, 1.0f) // 绿色
            ));
        } else if (baseName.contains("Wuwa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(0.0f, 1.0f, 1.0f, 1.0f) // 青色
            ));
        } else if (baseName.contains("Liuwa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(0.0f, 0.0f, 1.0f, 1.0f) // 蓝色
            ));
        } else if (baseName.contains("Qiwa")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(35, 35),
                new RenderComponent.Color(0.5f, 0.0f, 0.5f, 1.0f) // 紫色
            ));
        } else if (baseName.contains("Monster")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.CIRCLE,
                new Vector2(20, 20),
                new RenderComponent.Color(0.8f, 0.2f, 0.2f, 1.0f) // 暗红色
            ));
        } else if (baseName.contains("Bullet")) {
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.CIRCLE,
                new Vector2(8, 8),
                new RenderComponent.Color(1.0f, 1.0f, 0.5f, 1.0f) // 亮黄色
            ));
        } else {
            // 默认：灰色小方块
            rc = obj.addComponent(new RenderComponent(
                RenderComponent.RenderType.RECTANGLE,
                new Vector2(15, 15),
                new RenderComponent.Color(0.5f, 0.5f, 0.5f, 1.0f)
            ));
        }
        
        rc.setRenderer(renderer);
        return obj;
    }
}
