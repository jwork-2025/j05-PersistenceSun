package com.gameengine.core;

import com.gameengine.components.*;
import com.gameengine.entities.EntityFactory;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.GridSystem;
import com.gameengine.scene.Scene;

import java.util.List;

/**
 * 游戏逻辑类，处理具体的游戏规则
 */
public class GameLogic {
    private Scene scene;
    private InputManager inputManager;
    private GridSystem gridSystem;
    private EntityFactory entityFactory;
    
    // 游戏状态
    public enum GameState { PLAYING, VICTORY, DEFEAT }
    private GameState gameState = GameState.PLAYING;
    
    // 妖精生成（无尽模式）
    private float monsterSpawnTimer = 0;
    private float spawnInterval = 3.0f;  // 初始3秒生成一个妖精
    private float batchSpawnTimer = 0;   // 批量生成计时器
    private float batchSpawnInterval = 10.0f;  // 初始10秒批量生成
    private int batchSpawnCount = 5;     // 批量生成数量
    private float gameTime = 0;          // 游戏时间（作为最终成绩）
    
    // 动态难度参数
    private static final float DIFFICULTY_INCREASE_INTERVAL = 20.0f;  // 每20秒提升一次难度
    private float difficultyTimer = 0;
    private int difficultyLevel = 1;     // 当前难度等级
    
    // 选中的葫芦娃
    private GameObject selectedHuluwa = null;
    
    public GameLogic(Scene scene, GridSystem gridSystem, EntityFactory entityFactory) {
        this.scene = scene;
        this.gridSystem = gridSystem;
        this.entityFactory = entityFactory;
        this.inputManager = InputManager.getInstance();
    }
    
    /**
     * 主更新方法
     */
    public void update(float deltaTime) {
        if (gameState != GameState.PLAYING) return;
        
        gameTime += deltaTime;
        handlePlayerInput();
        spawnMonster(deltaTime);
        handleCombat();
        updateProjectiles();
        cleanupDeadEntities();
        updatePhysics();
        
        // 检查胜负
        GameState newState = checkGameOver();
        if (newState != gameState) {
            gameState = newState;
            // 只在状态改变时打印一次
            if (gameState == GameState.VICTORY) {
                System.out.println("=== 胜利！===");
            } else if (gameState == GameState.DEFEAT) {
                System.out.println("=== 失败！===");
            }
        }
    }
    
    /**
     * 处理玩家输入（鼠标选中 + 方向键移动）
     */
    public void handlePlayerInput() {
        // 1. 鼠标左键点击选中葫芦娃
        if (inputManager.isMouseButtonJustPressed(1)) {  // 左键
            Vector2 mousePos = inputManager.getMousePosition();
            int[] gridPos = gridSystem.worldToGrid(mousePos.x, mousePos.y);
            
            if (gridPos != null) {
                GameObject obj = gridSystem.getObject(gridPos[0], gridPos[1]);
                if (obj != null) {
                    TeamComponent team = obj.getComponent(TeamComponent.class);
                    if (team != null && team.isFriendly()) {
                        selectedHuluwa = obj;
                        System.out.println("选中葫芦娃: " + obj.getName());
                    }
                }
            }
        }
        
        // 2. 方向键移动选中的葫芦娃
        if (selectedHuluwa != null && selectedHuluwa.isActive()) {
            TransformComponent transform = selectedHuluwa.getComponent(TransformComponent.class);
            if (transform == null) return;
            
            Vector2 currentPos = transform.getPosition();
            int[] currentGrid = gridSystem.worldToGrid(currentPos.x, currentPos.y);
            if (currentGrid == null) return;
            
            int targetRow = currentGrid[0];
            int targetCol = currentGrid[1];
            boolean moved = false;
            
            if (inputManager.isKeyJustPressed(38)) { // 上箭头
                targetRow--;
                moved = true;
            }
            if (inputManager.isKeyJustPressed(40)) { // 下箭头
                targetRow++;
                moved = true;
            }
            if (inputManager.isKeyJustPressed(37)) { // 左箭头
                targetCol--;
                moved = true;
            }
            if (inputManager.isKeyJustPressed(39)) { // 右箭头
                targetCol++;
                moved = true;
            }
            
            // 执行移动
            if (moved && gridSystem.canMoveTo(targetRow, targetCol)) {
                gridSystem.removeObject(currentGrid[0], currentGrid[1]);
                gridSystem.placeObject(targetRow, targetCol, selectedHuluwa);
                Vector2 newPos = gridSystem.gridToWorld(targetRow, targetCol);
                transform.moveTo(newPos);  // 使用平滑移动
                System.out.println("移动到: (" + targetRow + ", " + targetCol + ")");
            }
        }
    }
    
    /**
     * 生成妖精（无尽模式，难度递增）
     */
    private void spawnMonster(float deltaTime) {
        // 动态难度递增
        difficultyTimer += deltaTime;
        if (difficultyTimer >= DIFFICULTY_INCREASE_INTERVAL) {
            difficultyTimer = 0;
            difficultyLevel++;
            
            // 降低生成间隔（加快生成速度）
            spawnInterval = Math.max(1.0f, 3.0f - (difficultyLevel - 1) * 0.2f);
            batchSpawnInterval = Math.max(5.0f, 10.0f - (difficultyLevel - 1) * 0.3f);
            
            // 增加批量生成数量
            batchSpawnCount = 5 + (difficultyLevel - 1);
            
            System.out.println("=== 难度提升！等级: " + difficultyLevel + " | 生成间隔: " + String.format("%.1f", spawnInterval) + "s | 批量数量: " + batchSpawnCount + " ===");
        }
        
        // 每隔一定时间生成1只随机妖精
        monsterSpawnTimer += deltaTime;
        if (monsterSpawnTimer >= spawnInterval) {
            monsterSpawnTimer = 0;
            spawnRandomMonster();
        }
        
        // 批量生成随机妖精
        batchSpawnTimer += deltaTime;
        if (batchSpawnTimer >= batchSpawnInterval) {
            batchSpawnTimer = 0;
            System.out.println("=== 批量生成" + batchSpawnCount + "只妖精！===");
            for (int i = 0; i < batchSpawnCount; i++) {
                spawnRandomMonster();
            }
        }
    }
    
    /**
     * 生成一只随机类型的妖精
     */
    private void spawnRandomMonster() {
        int randomRow = gridSystem.getRandomRow();
        int monsterType = (int)(Math.random() * 5);  // 0-4随机选择妖精类型
        
        GameObject monster;
        String monsterName;
        
        switch (monsterType) {
            case 0:  // 普通妖精
                monster = entityFactory.createMonster(randomRow);
                monsterName = "普通妖精";
                break;
            case 1:  // 熊精（高血量）
                monster = entityFactory.createBearMonster(randomRow);
                monsterName = "熊精";
                break;
            case 2:  // 豹子精（高速度）
                monster = entityFactory.createLeopardMonster(randomRow);
                monsterName = "豹子精";
                break;
            case 3:  // 老虎精（高伤害）
                monster = entityFactory.createTigerMonster(randomRow);
                monsterName = "老虎精";
                break;
            case 4:  // 鹰精（大范围）
                monster = entityFactory.createEagleMonster(randomRow);
                monsterName = "鹰精";
                break;
            default:
                monster = entityFactory.createMonster(randomRow);
                monsterName = "普通妖精";
                break;
        }
        
        scene.addGameObject(monster);
        System.out.println("生成" + monsterName + "在第 " + randomRow + " 行");
    }
    
    /**
     * 处理战斗逻辑
     */
    private void handleCombat() {
        List<CombatComponent> combatComponents = scene.getComponents(CombatComponent.class);
        
        for (CombatComponent combat : combatComponents) {
            combat.setScene(scene);  // 设置场景引用
            
            // 为葫芦娃设置攻击回调（发射子弹）
            GameObject owner = combat.getOwner();
            TeamComponent team = owner.getComponent(TeamComponent.class);
            
            if (team != null && team.isFriendly()) {
                combat.setAttackCallback((attacker, target) -> {
                    // 发射子弹
                    TransformComponent attackerTransform = attacker.getComponent(TransformComponent.class);
                    if (attackerTransform != null) {
                        Vector2 startPos = attackerTransform.getPosition();
                        float damage = combat.getAttackDamage();
                        
                        // 五娃：多目标攻击（最多3个）
                        if ("Wuwa".equals(attacker.getName())) {
                            List<GameObject> targets = findNearestEnemies(attacker, 5, combat.getAttackRange());
                            if (targets.isEmpty()) {
                                // 如果没找到目标，对原始target发射
                                GameObject bullet = entityFactory.createBullet(startPos, target, damage);
                                scene.addGameObject(bullet);
                            } else {
                                // 对找到的所有目标发射子弹
                                for (GameObject t : targets) {
                                    GameObject bullet = entityFactory.createBullet(startPos, t, damage);
                                    scene.addGameObject(bullet);
                                }
                            }
                        }
                        // 四娃：发射火焰子弹
                        else if ("Siwa".equals(attacker.getName())) {
                            GameObject bullet = entityFactory.createFireBullet(startPos, target, damage);
                            scene.addGameObject(bullet);
                        }
                        // 其他葫芦娃：普通子弹
                        else {
                            GameObject bullet = entityFactory.createBullet(startPos, target, damage);
                            scene.addGameObject(bullet);
                        }
                    }
                });
            } else if (team != null && team.isEnemy()) {
                // 妖精也发射子弹（紫色），射程较短
                combat.setAttackCallback((attacker, target) -> {
                    TransformComponent attackerTransform = attacker.getComponent(TransformComponent.class);
                    if (attackerTransform != null) {
                        Vector2 startPos = attackerTransform.getPosition();
                        float damage = combat.getAttackDamage();
                        // 创建妖精的紫色子弹
                        GameObject bullet = entityFactory.createMonsterBullet(startPos, target, damage);
                        scene.addGameObject(bullet);
                    }
                });
            }
        }
    }
    
    /**
     * 更新子弹
     */
    private void updateProjectiles() {
        // 子弹由ProjectileComponent自己的update处理碰撞和移动
    }
    
    /**
     * 清理死亡实体
     */
    private void cleanupDeadEntities() {
        List<HealthComponent> healthComponents = scene.getComponents(HealthComponent.class);
        for (HealthComponent health : healthComponents) {
            if (health.isDead()) {
                GameObject owner = health.getOwner();
                if (owner != null && owner.isActive()) {  // 确保对象还在激活状态
                    // 从网格移除（必须在destroy之前）
                    TransformComponent transform = owner.getComponent(TransformComponent.class);
                    if (transform != null) {
                        Vector2 pos = transform.getPosition();
                        int[] gridPos = gridSystem.worldToGrid(pos.x, pos.y);
                        if (gridPos != null) {
                            GameObject gridObj = gridSystem.getObject(gridPos[0], gridPos[1]);
                            // 只有当格子中的对象确实是这个死亡对象时才移除
                            if (gridObj == owner) {
                                gridSystem.removeObject(gridPos[0], gridPos[1]);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 检查游戏是否失败（无尽模式，只有失败条件）
     */
    private GameState checkGameOver() {
        // 检查是否有妖精到达最左列（游戏失败）
        for (GameObject obj : scene.getGameObjects()) {
            if (!obj.isActive()) continue;
            
            TeamComponent team = obj.getComponent(TeamComponent.class);
            if (team != null && team.isEnemy()) {
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                if (transform != null) {
                    Vector2 pos = transform.getPosition();
                    int[] gridPos = gridSystem.worldToGrid(pos.x, pos.y);
                    if (gridPos != null && gridPos[1] <= 0) {
                        return GameState.DEFEAT;
                    }
                }
            }
        }
        
        // 无尽模式：没有胜利条件，游戏持续进行
        return GameState.PLAYING;
    }
    
    /**
     * 更新物理系统（保留用于子弹等）
     */
    public void updatePhysics() {
        // 物理组件由各自的update处理
        // 这里可以添加全局物理规则
    }
    
    // Getters
    public GameState getGameState() {
        return gameState;
    }
    
    public void setGameState(GameState state) {
        this.gameState = state;
    }
    
    public GameObject getSelectedHuluwa() {
        return selectedHuluwa;
    }
    
    /**
     * 检查游戏是否胜利
     */
    public boolean isGameWon() {
        return gameState == GameState.VICTORY;
    }
    
    /**
     * 检查游戏是否失败
     */
    public boolean isGameLost() {
        return gameState == GameState.DEFEAT;
    }
    
    /**
     * 获取游戏时间（最终成绩）
     */
    public float getGameTime() {
        return gameTime;
    }
    
    /**
     * 获取当前难度等级
     */
    public int getDifficultyLevel() {
        return difficultyLevel;
    }
    
    /**
     * 查找最近的N个敌人
     * @param attacker 攻击者
     * @param maxCount 最多返回的敌人数量
     * @param range 攻击范围
     * @return 敌人列表
     */
    private List<GameObject> findNearestEnemies(GameObject attacker, int maxCount, float range) {
        TransformComponent attackerTransform = attacker.getComponent(TransformComponent.class);
        if (attackerTransform == null) {
            System.out.println("五娃没有Transform组件！");
            return new java.util.ArrayList<>();
        }
        
        Vector2 attackerPos = attackerTransform.getPosition();
        List<GameObject> enemies = new java.util.ArrayList<>();
        
        // 将格子范围转换为像素距离（每格80像素）
        float pixelRange = range * 80.0f;
        
        // 找到所有范围内的敌人
        for (GameObject obj : scene.getGameObjects()) {
            if (!obj.isActive()) continue;
            
            TeamComponent objTeam = obj.getComponent(TeamComponent.class);
            if (objTeam == null || !objTeam.isEnemy()) continue;
            
            HealthComponent objHealth = obj.getComponent(HealthComponent.class);
            if (objHealth == null || objHealth.isDead()) continue;
            
            TransformComponent objTransform = obj.getComponent(TransformComponent.class);
            if (objTransform == null) continue;
            
            float distance = attackerPos.distance(objTransform.getPosition());
            if (distance <= pixelRange) {
                enemies.add(obj);
            }
        }
        
        // 按距离排序，取最近的N个
        enemies.sort((a, b) -> {
            TransformComponent ta = a.getComponent(TransformComponent.class);
            TransformComponent tb = b.getComponent(TransformComponent.class);
            float da = attackerPos.distance(ta.getPosition());
            float db = attackerPos.distance(tb.getPosition());
            return Float.compare(da, db);
        });
        
        // 返回最多maxCount个
        return enemies.subList(0, Math.min(maxCount, enemies.size()));
    }
}
