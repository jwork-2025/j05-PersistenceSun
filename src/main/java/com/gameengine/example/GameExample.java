package com.gameengine.example;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameEngine;
import com.gameengine.core.GameLogic;
import com.gameengine.entities.EntityFactory;
import com.gameengine.graphics.Renderer;
import com.gameengine.math.Vector2;
import com.gameengine.recording.RecordingConfig;
import com.gameengine.recording.RecordingService;
import com.gameengine.scene.GridSystem;
import com.gameengine.scene.Scene;
import com.gameengine.net.NioServer;
import com.gameengine.actor.ActorSystem;
import com.gameengine.actor.actors.NetworkBroadcastActor;
import com.gameengine.actor.messages.GameStateSnapshotMessage;

import java.util.ArrayList;

/**
 * 葫芦娃大战妖精 - 游戏示例（支持Actor优化开关）
 * 使用Actor模型异步处理网络广播，提升性能
 * 
 * 运行方式：
 *   - 默认（Actor优化）: java -cp build/classes com.gameengine.example.GameExample
 *   - 禁用优化: java -Dactor.disabled=true -cp build/classes com.gameengine.example.GameExample
 */
public class GameExample {
    // 游戏状态枚举
    private enum GameState {
        MENU,       // 主界面
        PLAYING,    // 游戏进行中
        VICTORY,    // 胜利
        DEFEAT      // 失败
    }
    
    // Actor优化开关（通过系统属性控制）
    private static final boolean ACTOR_ENABLED = !"true".equals(System.getProperty("actor.disabled"));
    
    public static void main(String[] args) {
        System.out.println("启动葫芦娃大战妖精...");
        
        // 启动网络服务器（监听7777端口）
        NioServer networkServer = new NioServer(7777);
        networkServer.start();
        System.out.println("✓ 网络服务器已启动（端口: 7777）");
        System.out.println("  客户端可以连接到此服务器观看游戏");
        
        // 初始化Actor系统用于异步网络广播（如果启用）
        if (ACTOR_ENABLED) {
            ActorSystem actorSystem = ActorSystem.getInstance();
            NetworkBroadcastActor networkActor = new NetworkBroadcastActor();
            actorSystem.registerActor(networkActor);
            System.out.println("✓ Actor优化已启用（异步网络广播）");
        } else {
            System.out.println("✓ Actor优化已禁用（同步网络广播）");
        }
        
        try {
            // 创建游戏引擎
            String title = ACTOR_ENABLED ? "葫芦娃大战妖精 [Actor优化]" : "葫芦娃大战妖精 [原始版本]";
            GameEngine engine = new GameEngine(800, 600, title);
            
            // 创建游戏场景
            Scene gameScene = new Scene("BattleScene") {
                private Renderer renderer;
                private GridSystem gridSystem;
                private EntityFactory entityFactory;
                private GameLogic gameLogic;
                private RecordingService recordingService;
                
                // 游戏状态
                private GameState gameState = GameState.MENU;
                
                // UI按钮区域
                private float startButtonX = 300;
                private float startButtonY = 280;
                private float startButtonWidth = 200;
                private float startButtonHeight = 60;
                
                private float replayButtonX = 300;
                private float replayButtonY = 360;
                private float replayButtonWidth = 200;
                private float replayButtonHeight = 60;
                
                private float restartButtonX = 250;
                private float restartButtonY = 300;
                private float restartButtonWidth = 150;
                private float restartButtonHeight = 50;
                
                private float menuButtonX = 450;
                private float menuButtonY = 300;
                private float menuButtonWidth = 150;
                private float menuButtonHeight = 50;
                
                @Override
                public void initialize() {
                    super.initialize();
                    this.renderer = engine.getRenderer();
                    
                    // 初始化网格系统
                    this.gridSystem = new GridSystem();
                    
                    // 初始化实体工厂
                    this.entityFactory = new EntityFactory(gridSystem, renderer);
                    
                    // 初始化游戏逻辑
                    this.gameLogic = new GameLogic(this, gridSystem, entityFactory);
                    
                    System.out.println("游戏初始化完成！");
                }
                
                /**
                 * 开始新游戏
                 */
                private void startNewGame() {
                    // 清空所有对象
                    for (GameObject obj : getGameObjects()) {
                        obj.setActive(false);
                    }
                    getGameObjects().clear();
                    gridSystem.clear();
                    
                    // 创建初始葫芦娃
                    createInitialHuluwas();
                    
                    // 重置游戏逻辑
                    gameLogic = new GameLogic(this, gridSystem, entityFactory);
                    
                    // 初始化录制服务
                    recordingService = new RecordingService(RecordingConfig.createDefault());
                    
                    // 切换到游戏状态
                    gameState = GameState.PLAYING;
                    
                    // 开始录制
                    try {
                        recordingService.start(this, 800, 600);
                        System.out.println("已开始录制游戏...");
                    } catch (Exception e) {
                        System.err.println("录制启动失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    System.out.println("开始新游戏！");
                    System.out.println("操作说明：");
                    System.out.println("- 鼠标左键点击选中葫芦娃");
                    System.out.println("- 方向键移动选中的葫芦娃");
                    System.out.println("- 按R键切换录制状态");
                    System.out.println("- 消灭所有妖精获胜！");
                }
                
                /**
                 * 创建初始葫芦娃（7个葫芦娃）
                 */
                private void createInitialHuluwas() {
                    // 创建七个葫芦娃，放置在中间行
                    GameObject dawa = entityFactory.createDawa(2, 1);    // 大娃
                    GameObject erwa = entityFactory.createErwa(2, 2);    // 二娃
                    GameObject sanwa = entityFactory.createSanwa(2, 3);  // 三娃
                    GameObject siwa = entityFactory.createSiwa(2, 4);    // 四娃
                    GameObject wuwa = entityFactory.createWuwa(2, 5);    // 五娃
                    GameObject liuwa = entityFactory.createLiuwa(2, 6);  // 六娃
                    GameObject qiwa = entityFactory.createQiwa(2, 7);    // 七娃
                    
                    addGameObject(dawa);
                    addGameObject(erwa);
                    addGameObject(sanwa);
                    addGameObject(siwa);
                    addGameObject(wuwa);
                    addGameObject(liuwa);
                    addGameObject(qiwa);
                    
                    // 在网格中标记占用
                    gridSystem.placeObject(2, 1, dawa);
                    gridSystem.placeObject(2, 2, erwa);
                    gridSystem.placeObject(2, 3, sanwa);
                    gridSystem.placeObject(2, 4, siwa);
                    gridSystem.placeObject(2, 5, wuwa);
                    gridSystem.placeObject(2, 6, liuwa);
                    gridSystem.placeObject(2, 7, qiwa);
                    
                    System.out.println("已放置7个葫芦娃：大娃、二娃、三娃、四娃、五娃、六娃、七娃");
                }
                
                @Override
                public void update(float deltaTime) {
                    super.update(deltaTime);
                    
                    // 根据游戏状态更新
                    switch (gameState) {
                        case MENU:
                            // 主界面：检测开始按钮点击
                            handleMenuInput();
                            break;
                        case PLAYING:
                            // 游戏进行中：使用游戏逻辑处理
                            gameLogic.update(deltaTime);
                            
                            // 更新录制服务
                            if (recordingService != null) {
                                recordingService.update(deltaTime, this, engine.getInputManager());
                            }
                            
                            // 网络广播：生成JSON关键帧并设置到NetState
                            broadcastNetworkKeyframe();
                            
                            // 检查R键切换录制状态
                            if (engine.getInputManager().isKeyJustPressed(java.awt.event.KeyEvent.VK_R)) {
                                toggleRecording();
                            }
                            
                            // 检查胜利/失败条件
                            checkGameEnd();
                            break;
                        case VICTORY:
                        case DEFEAT:
                            // 结束界面：检测按钮点击
                            handleEndInput();
                            break;
                    }
                }
                
                /**
                 * 处理主界面输入
                 */
                private void handleMenuInput() {
                    if (engine.getInputManager().isMouseButtonJustPressed(1)) {  // 左键是1
                        float mouseX = engine.getInputManager().getMouseX();
                        float mouseY = engine.getInputManager().getMouseY();
                        
                        System.out.println("点击位置: (" + mouseX + ", " + mouseY + ")");
                        System.out.println("按钮区域: (" + startButtonX + ", " + startButtonY + ", " + startButtonWidth + ", " + startButtonHeight + ")");
                        
                        // 检测开始按钮
                        if (isPointInRect(mouseX, mouseY, startButtonX, startButtonY, startButtonWidth, startButtonHeight)) {
                            System.out.println("点击了开始按钮！");
                            startNewGame();
                        }
                        
                        // 检测回放按钮
                        if (isPointInRect(mouseX, mouseY, replayButtonX, replayButtonY, replayButtonWidth, replayButtonHeight)) {
                            System.out.println("点击了回放按钮！");
                            startReplay();
                        }
                    }
                }
                
                /**
                 * 处理结束界面输入
                 */
                private void handleEndInput() {
                    if (engine.getInputManager().isMouseButtonJustPressed(1)) {  // 左键是1
                        float mouseX = engine.getInputManager().getMouseX();
                        float mouseY = engine.getInputManager().getMouseY();
                        
                        // 检测"再来一局"按钮（按钮位置下移50像素）
                        if (isPointInRect(mouseX, mouseY, restartButtonX, restartButtonY + 50, restartButtonWidth, restartButtonHeight)) {
                            startNewGame();
                        }
                        
                        // 检测"返回主界面"按钮
                        if (isPointInRect(mouseX, mouseY, menuButtonX, menuButtonY + 50, menuButtonWidth, menuButtonHeight)) {
                            gameState = GameState.MENU;
                        }
                    }
                }
                
                /**
                 * 检查点是否在矩形内
                 */
                private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
                    return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
                }
                
                /**
                 * 检查游戏是否结束
                 */
                private void checkGameEnd() {
                    if (gameLogic.isGameWon()) {
                        gameState = GameState.VICTORY;
                        stopRecording();
                        System.out.println("游戏胜利！");
                    } else if (gameLogic.isGameLost()) {
                        gameState = GameState.DEFEAT;
                        stopRecording();
                        System.out.println("游戏失败！");
                    }
                }
                
                /**
                 * 切换录制状态
                 */
                private void toggleRecording() {
                    if (recordingService == null) {
                        return;
                    }
                    
                    if (recordingService.isRecording()) {
                        stopRecording();
                        System.out.println("已暂停录制");
                    } else {
                        try {
                            recordingService.start(this, 800, 600);
                            System.out.println("已恢复录制");
                        } catch (Exception e) {
                            System.err.println("录制启动失败: " + e.getMessage());
                        }
                    }
                }
                
                /**
                 * 网络广播：生成JSON关键帧（支持Actor优化开关）
                 */
                private void broadcastNetworkKeyframe() {
                    if (ACTOR_ENABLED) {
                        // Actor优化版 - 异步处理
                        java.util.List<GameObject> snapshot = new java.util.ArrayList<>(getGameObjects());
                        GameStateSnapshotMessage msg = new GameStateSnapshotMessage(snapshot, gameState.name());
                        ActorSystem.getInstance().send("NetworkBroadcast", msg);
                    } else {
                        // 原始版 - 同步处理
                        StringBuilder js = new StringBuilder();
                        js.append('{').append("\"type\":\"kf\",")
                          .append("\"t\":").append(System.currentTimeMillis()/1000.0).append(',')
                          .append("\"state\":\"").append(gameState.name()).append("\",")
                          .append("\"entities\":[");
                        
                        boolean first = true;
                        for (GameObject obj : getGameObjects()) {
                            if (!obj.isActive()) continue;
                            
                            TransformComponent tc = obj.getComponent(TransformComponent.class);
                            RenderComponent rc = obj.getComponent(RenderComponent.class);
                            if (tc == null) continue;
                            
                            Vector2 p = tc.getPosition();
                            if (!first) js.append(',');
                            
                            String id = obj.getName() + "#" + obj.getInstanceId();
                            js.append('{')
                              .append("\"id\":\"").append(id).append("\",")
                              .append("\"x\":").append((int)p.x).append(',')
                              .append("\"y\":").append((int)p.y);
                            
                            // 添加颜色信息
                            if (rc != null) {
                                RenderComponent.Color color = rc.getColor();
                                js.append(",\"color\":[")
                                  .append(color.r).append(',')
                                  .append(color.g).append(',')
                                  .append(color.b).append(',')
                                  .append(color.a).append(']');
                            }
                            
                            js.append('}');
                            first = false;
                        }
                        js.append(']').append('}');
                        
                        String jsonStr = js.toString();
                        com.gameengine.net.NetState.setLastKeyframeJson(jsonStr);
                    }
                }
                
                /**
                 * 停止录制
                 */
                private void stopRecording() {
                    if (recordingService != null && recordingService.isRecording()) {
                        try {
                            recordingService.stop();
                            System.out.println("录制已停止");
                        } catch (Exception e) {
                            System.err.println("停止录制失败: " + e.getMessage());
                        }
                    }
                }
                
                /**
                 * 启动回放
                 */
                private void startReplay() {
                    // 切换到回放场景（传null进入文件选择模式）
                    engine.setScene(new ReplayScene(engine, null));
                }
                
                @Override
                public void render() {
                    // 根据游戏状态渲染
                    switch (gameState) {
                        case MENU:
                            renderMenu();
                            break;
                        case PLAYING:
                            renderGame();
                            break;
                        case VICTORY:
                            renderVictory();
                            break;
                        case DEFEAT:
                            renderDefeat();
                            break;
                    }
                }
                
                /**
                 * 渲染主界面
                 */
                private void renderMenu() {
                    // 深蓝色背景
                    renderer.drawRect(0, 0, 800, 600, 0.05f, 0.05f, 0.2f, 1.0f);
                    
                    // 标题文字区域（使用矩形模拟）
                    renderer.drawRect(200, 150, 400, 80, 0.2f, 0.2f, 0.4f, 0.8f);
                    renderer.drawText("葫芦娃大战妖精", 250, 200, 36, 1.0f, 1.0f, 0.0f, 1.0f);
                    
                    // 开始按钮
                    renderer.drawRect(startButtonX, startButtonY, startButtonWidth, startButtonHeight, 0.2f, 0.6f, 0.2f, 0.9f);
                    renderer.drawRect(startButtonX + 5, startButtonY + 5, startButtonWidth - 10, startButtonHeight - 10, 0.3f, 0.8f, 0.3f, 1.0f);
                    renderer.drawText("开始游戏", startButtonX + 55, startButtonY + 40, 24, 1.0f, 1.0f, 1.0f, 1.0f);
                    
                    // 回放按钮
                    renderer.drawRect(replayButtonX, replayButtonY, replayButtonWidth, replayButtonHeight, 0.2f, 0.2f, 0.6f, 0.9f);
                    renderer.drawRect(replayButtonX + 5, replayButtonY + 5, replayButtonWidth - 10, replayButtonHeight - 10, 0.3f, 0.3f, 0.8f, 1.0f);
                    renderer.drawText("观看回放", replayButtonX + 55, replayButtonY + 40, 24, 1.0f, 1.0f, 1.0f, 1.0f);
                }
                
                /**
                 * 渲染游戏画面
                 */
                private void renderGame() {
                    // 绘制背景
                    renderer.drawRect(0, 0, 800, 600, 0.1f, 0.1f, 0.2f, 1.0f);
                    
                    // 渲染网格线
                    gridSystem.render(renderer);
                    
                    // 渲染所有对象
                    super.render();
                    
                    // 渲染血条和选中高亮
                    renderHealthBars();
                    renderSelection();
                    
                    // 渲染游戏时间和难度等级（在屏幕右上角）
                    renderGameStats();
                }
                
                /**
                 * 渲染游戏统计信息（时间和难度）
                 */
                private void renderGameStats() {
                    float time = gameLogic.getGameTime();
                    int difficulty = gameLogic.getDifficultyLevel();
                    
                    // 时间显示（格式：00:00.0）
                    int minutes = (int)(time / 60);
                    float seconds = time % 60;
                    String timeStr = String.format("时间: %02d:%04.1f", minutes, seconds);
                    
                    // 难度显示
                    String diffStr = "难度: Lv." + difficulty;
                    
                    // 背景框
                    renderer.drawRect(600, 10, 190, 70, 0.0f, 0.0f, 0.0f, 0.6f);
                    
                    // 显示文字
                    renderer.drawText(timeStr, 615, 35, 18, 1.0f, 1.0f, 1.0f, 1.0f);
                    renderer.drawText(diffStr, 615, 60, 18, 1.0f, 0.8f, 0.2f, 1.0f);
                }
                
                /**
                 * 渲染胜利界面
                 */
                private void renderVictory() {
                    // 绿色背景
                    renderer.drawRect(0, 0, 800, 600, 0.1f, 0.3f, 0.1f, 1.0f);
                    
                    // 胜利文字区域
                    renderer.drawRect(250, 150, 300, 100, 0.2f, 0.6f, 0.2f, 0.9f);
                    renderer.drawText("胜利！", 330, 210, 48, 1.0f, 1.0f, 0.0f, 1.0f);
                    
                    // "再来一局"按钮
                    renderer.drawRect(restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight, 0.3f, 0.7f, 0.3f, 0.9f);
                    renderer.drawText("再来一局", restartButtonX + 25, restartButtonY + 33, 20, 1.0f, 1.0f, 1.0f, 1.0f);
                    
                    // "返回主界面"按钮
                    renderer.drawRect(menuButtonX, menuButtonY, menuButtonWidth, menuButtonHeight, 0.3f, 0.5f, 0.7f, 0.9f);
                    renderer.drawText("返回主界面", menuButtonX + 15, menuButtonY + 33, 20, 1.0f, 1.0f, 1.0f, 1.0f);
                }
                
                /**
                 * 渲染失败界面
                 */
                private void renderDefeat() {
                    // 红色背景
                    renderer.drawRect(0, 0, 800, 600, 0.3f, 0.1f, 0.1f, 1.0f);
                    
                    // 失败文字区域
                    renderer.drawRect(250, 100, 300, 100, 0.6f, 0.2f, 0.2f, 0.9f);
                    renderer.drawText("游戏结束！", 310, 160, 48, 1.0f, 1.0f, 1.0f, 1.0f);
                    
                    // 最终成绩显示
                    float finalTime = gameLogic.getGameTime();
                    int minutes = (int)(finalTime / 60);
                    float seconds = finalTime % 60;
                    String scoreStr = String.format("坚持时间: %02d:%04.1f", minutes, seconds);
                    
                    renderer.drawRect(250, 220, 300, 60, 0.5f, 0.2f, 0.2f, 0.8f);
                    renderer.drawText(scoreStr, 270, 260, 24, 1.0f, 1.0f, 0.0f, 1.0f);
                    
                    // "再来一局"按钮
                    renderer.drawRect(restartButtonX, restartButtonY + 50, restartButtonWidth, restartButtonHeight, 0.3f, 0.7f, 0.3f, 0.9f);
                    renderer.drawText("再来一局", restartButtonX + 25, restartButtonY + 83, 20, 1.0f, 1.0f, 1.0f, 1.0f);
                    
                    // "返回主界面"按钮
                    renderer.drawRect(menuButtonX, menuButtonY + 50, menuButtonWidth, menuButtonHeight, 0.3f, 0.5f, 0.7f, 0.9f);
                    renderer.drawText("返回主界面", menuButtonX + 15, menuButtonY + 83, 20, 1.0f, 1.0f, 1.0f, 1.0f);
                }
                
                /**
                 * 渲染所有单位的血条
                 */
                private void renderHealthBars() {
                    for (GameObject obj : getGameObjects()) {
                        if (!obj.isActive()) continue;
                        
                        HealthComponent health = obj.getComponent(HealthComponent.class);
                        TransformComponent transform = obj.getComponent(TransformComponent.class);
                        
                        if (health != null && transform != null) {
                            Vector2 pos = transform.getPosition();
                            float healthPercent = health.getHealthPercentage();
                            
                            // 血条位置（在单位上方）
                            float barWidth = 40;
                            float barHeight = 5;
                            float barX = pos.x - barWidth / 2;
                            float barY = pos.y - 30;
                            
                            // 背景（红色）
                            renderer.drawRect(barX, barY, barWidth, barHeight, 0.3f, 0.0f, 0.0f, 0.8f);
                            
                            // 当前血量（绿色到黄色到红色渐变）
                            float r = healthPercent < 0.5f ? 1.0f : 1.0f - (healthPercent - 0.5f) * 2.0f;
                            float g = healthPercent > 0.5f ? 1.0f : healthPercent * 2.0f;
                            renderer.drawRect(barX, barY, barWidth * healthPercent, barHeight, r, g, 0.0f, 1.0f);
                            
                            // 边框
                            renderer.drawLine(barX, barY, barX + barWidth, barY, 1.0f, 1.0f, 1.0f, 0.5f);
                            renderer.drawLine(barX, barY + barHeight, barX + barWidth, barY + barHeight, 1.0f, 1.0f, 1.0f, 0.5f);
                            renderer.drawLine(barX, barY, barX, barY + barHeight, 1.0f, 1.0f, 1.0f, 0.5f);
                            renderer.drawLine(barX + barWidth, barY, barX + barWidth, barY + barHeight, 1.0f, 1.0f, 1.0f, 0.5f);
                        }
                    }
                }
                
                /**
                 * 渲染选中葫芦娃的高亮框
                 */
                private void renderSelection() {
                    GameObject selected = gameLogic.getSelectedHuluwa();
                    if (selected != null && selected.isActive()) {
                        TransformComponent transform = selected.getComponent(TransformComponent.class);
                        if (transform != null) {
                            Vector2 pos = transform.getPosition();
                            float size = 25; // 高亮框大小
                            
                            // 绘制黄色高亮框（闪烁效果）
                            float alpha = (float)(0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 200.0));
                            
                            // 四个角的线段
                            float cornerSize = 8;
                            // 左上角
                            renderer.drawLine(pos.x - size, pos.y - size, pos.x - size + cornerSize, pos.y - size, 1.0f, 1.0f, 0.0f, alpha);
                            renderer.drawLine(pos.x - size, pos.y - size, pos.x - size, pos.y - size + cornerSize, 1.0f, 1.0f, 0.0f, alpha);
                            // 右上角
                            renderer.drawLine(pos.x + size, pos.y - size, pos.x + size - cornerSize, pos.y - size, 1.0f, 1.0f, 0.0f, alpha);
                            renderer.drawLine(pos.x + size, pos.y - size, pos.x + size, pos.y - size + cornerSize, 1.0f, 1.0f, 0.0f, alpha);
                            // 左下角
                            renderer.drawLine(pos.x - size, pos.y + size, pos.x - size + cornerSize, pos.y + size, 1.0f, 1.0f, 0.0f, alpha);
                            renderer.drawLine(pos.x - size, pos.y + size, pos.x - size, pos.y + size - cornerSize, 1.0f, 1.0f, 0.0f, alpha);
                            // 右下角
                            renderer.drawLine(pos.x + size, pos.y + size, pos.x + size - cornerSize, pos.y + size, 1.0f, 1.0f, 0.0f, alpha);
                            renderer.drawLine(pos.x + size, pos.y + size, pos.x + size, pos.y + size - cornerSize, 1.0f, 1.0f, 0.0f, alpha);
                        }
                    }
                }
            };
            
            // 设置场景
            engine.setScene(gameScene);
            
            // 运行游戏
            engine.run();
            
        } catch (Exception e) {
            System.err.println("游戏运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
