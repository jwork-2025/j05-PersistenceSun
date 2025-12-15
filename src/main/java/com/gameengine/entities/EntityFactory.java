package com.gameengine.entities;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.Renderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.GridSystem;

/**
 * 实体工厂，统一创建游戏实体
 */
public class EntityFactory {
    private GridSystem gridSystem;
    private Renderer renderer;
    
    // 默认属性值（基础葫芦娃）
    private static final float HULUWA_MAX_HEALTH = 100.0f;
    private static final float HULUWA_ATTACK_DAMAGE = 20.0f;
    private static final float HULUWA_ATTACK_RANGE = 3.0f;
    private static final float HULUWA_ATTACK_COOLDOWN = 1.0f;
    
    // 大娃：生命值较高，伤害非常高，范围小
    private static final float DAWA_MAX_HEALTH = 200.0f;
    private static final float DAWA_ATTACK_DAMAGE = 60.0f;  // 非常高
    private static final float DAWA_ATTACK_RANGE = 1.5f;    // 只有1.5格
    private static final float DAWA_ATTACK_COOLDOWN = 0.3f;
    
    // 二娃：攻击范围覆盖全屏
    private static final float ERWA_MAX_HEALTH = 100.0f;
    private static final float ERWA_ATTACK_DAMAGE = 20.0f;
    private static final float ERWA_ATTACK_RANGE = 99.0f;   // 全屏
    private static final float ERWA_ATTACK_COOLDOWN = 0.6f;
    
    // 三娃：生命值非常高，伤害低，带阻挡
    private static final float SANWA_MAX_HEALTH = 1000.0f;    // 非常高
    private static final float SANWA_ATTACK_DAMAGE = 5.0f;   // 很低
    private static final float SANWA_ATTACK_RANGE = 2.0f;
    private static final float SANWA_ATTACK_COOLDOWN = 0.4f;
    
    // 四娃：火焰子弹，伤害高
    private static final float SIWA_MAX_HEALTH = 100.0f;
    private static final float SIWA_ATTACK_DAMAGE = 40.0f;   // 高伤害
    private static final float SIWA_ATTACK_RANGE = 5.5f;
    private static final float SIWA_ATTACK_COOLDOWN = 1.3f;
    
    // 五娃：同时攻击多个目标
    private static final float WUWA_MAX_HEALTH = 90.0f;
    private static final float WUWA_ATTACK_DAMAGE = 20.0f;
    private static final float WUWA_ATTACK_RANGE = 5.5f;
    private static final float WUWA_ATTACK_COOLDOWN = 1.8f;  
    private static final int WUWA_MAX_TARGETS = 3;           // 最多3个目标
    
    // 六娃：隐身，不被攻击
    private static final float LIUWA_MAX_HEALTH = 4000.0f;
    private static final float LIUWA_ATTACK_DAMAGE = 18.0f;
    private static final float LIUWA_ATTACK_RANGE = 3.0f;
    private static final float LIUWA_ATTACK_COOLDOWN = 1.0f;
    
    // 七娃：大招技能，25秒冷却
    private static final float QIWA_MAX_HEALTH = 100.0f;
    private static final float QIWA_ATTACK_DAMAGE = 150.0f;
    private static final float QIWA_ATTACK_RANGE = 2.5f;
    private static final float QIWA_ATTACK_COOLDOWN = 15.0f;
    private static final float QIWA_SKILL_COOLDOWN = 25.0f;  // 技能冷却
    
    private static final float MONSTER_MAX_HEALTH = 60.0f;
    private static final float MONSTER_ATTACK_DAMAGE = 10.0f;
    private static final float MONSTER_ATTACK_RANGE = 0.5f;
    private static final float MONSTER_ATTACK_COOLDOWN = 2.0f;
    private static final float MONSTER_MOVE_SPEED = 0.5f;
    
    // 熊精：生命值非常高
    private static final float BEAR_MAX_HEALTH = 200.0f;
    private static final float BEAR_ATTACK_DAMAGE = 10.0f;
    private static final float BEAR_ATTACK_RANGE = 2.0f;
    private static final float BEAR_ATTACK_COOLDOWN = 2.0f;
    private static final float BEAR_MOVE_SPEED = 0.3f;  // 较慢
    
    // 豹子精：移动速度非常快
    private static final float LEOPARD_MAX_HEALTH = 50.0f;
    private static final float LEOPARD_ATTACK_DAMAGE = 8.0f;
    private static final float LEOPARD_ATTACK_RANGE = 2.0f;
    private static final float LEOPARD_ATTACK_COOLDOWN = 1.8f;
    private static final float LEOPARD_MOVE_SPEED = 1.2f;  // 非常快
    
    // 老虎精：伤害非常高
    private static final float TIGER_MAX_HEALTH = 80.0f;
    private static final float TIGER_ATTACK_DAMAGE = 30.0f;  // 高伤害
    private static final float TIGER_ATTACK_RANGE = 2.0f;
    private static final float TIGER_ATTACK_COOLDOWN = 2.5f;
    private static final float TIGER_MOVE_SPEED = 0.5f;
    
    // 鹰精：攻击范围非常大
    private static final float EAGLE_MAX_HEALTH = 60.0f;
    private static final float EAGLE_ATTACK_DAMAGE = 12.0f;
    private static final float EAGLE_ATTACK_RANGE = 5.0f;  // 超大范围
    private static final float EAGLE_ATTACK_COOLDOWN = 1.5f;
    private static final float EAGLE_MOVE_SPEED = 0.6f;
    
    private static final float BULLET_SPEED = 400.0f;
    
    /**
     * 构造函数
     * @param gridSystem 网格系统
     * @param renderer 渲染器
     */
    public EntityFactory(GridSystem gridSystem, Renderer renderer) {
        this.gridSystem = gridSystem;
        this.renderer = renderer;
    }
    
    /**
     * 创建葫芦娃
     * @param row 行
     * @param col 列
     * @return 葫芦娃GameObject
     */
    public GameObject createHuluwa(int row, int col) {
        GameObject huluwa = new GameObject("Huluwa");
        
        // 计算世界坐标（格子中心）
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        // 添加Transform组件
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        huluwa.addComponent(transform);
        
        // 添加Render组件（红色方形）
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(30, 30),
            new RenderComponent.Color(1.0f, 0.0f, 0.0f, 1.0f)  // 红色
        );
        render.setRenderer(renderer);
        huluwa.addComponent(render);
        
        // 添加Health组件
        HealthComponent health = new HealthComponent(HULUWA_MAX_HEALTH);
        huluwa.addComponent(health);
        
        // 添加Team组件（友军）
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        huluwa.addComponent(team);
        
        // 添加Combat组件
        CombatComponent combat = new CombatComponent(
            HULUWA_ATTACK_RANGE, 
            HULUWA_ATTACK_DAMAGE, 
            HULUWA_ATTACK_COOLDOWN
        );
        huluwa.addComponent(combat);
        
        return huluwa;
    }
    
    /**
     * 创建大娃（生命值较高，伤害非常高，范围小）
     */
    public GameObject createDawa(int row, int col) {
        GameObject dawa = new GameObject("Dawa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        dawa.addComponent(transform);
        
        // 红色大型方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(35, 35),
            new RenderComponent.Color(0.8f, 0.0f, 0.0f, 1.0f)
        );
        render.setRenderer(renderer);
        dawa.addComponent(render);
        
        HealthComponent health = new HealthComponent(DAWA_MAX_HEALTH);
        dawa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        dawa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            DAWA_ATTACK_RANGE, DAWA_ATTACK_DAMAGE, DAWA_ATTACK_COOLDOWN
        );
        dawa.addComponent(combat);
        
        return dawa;
    }
    
    /**
     * 创建二娃（攻击范围覆盖全屏）
     */
    public GameObject createErwa(int row, int col) {
        GameObject erwa = new GameObject("Erwa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        erwa.addComponent(transform);
        
        // 橙色方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(30, 30),
            new RenderComponent.Color(1.0f, 0.5f, 0.0f, 1.0f)  // 橙色
        );
        render.setRenderer(renderer);
        erwa.addComponent(render);
        
        HealthComponent health = new HealthComponent(ERWA_MAX_HEALTH);
        erwa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        erwa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            ERWA_ATTACK_RANGE, ERWA_ATTACK_DAMAGE, ERWA_ATTACK_COOLDOWN
        );
        erwa.addComponent(combat);
        
        return erwa;
    }
    
    /**
     * 创建三娃（生命值非常高，伤害低，带阻挡效果）
     */
    public GameObject createSanwa(int row, int col) {
        GameObject sanwa = new GameObject("Sanwa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        sanwa.addComponent(transform);
        
        // 黄色大型方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(38, 38),
            new RenderComponent.Color(1.0f, 1.0f, 0.0f, 1.0f)  // 黄色
        );
        render.setRenderer(renderer);
        sanwa.addComponent(render);
        
        HealthComponent health = new HealthComponent(SANWA_MAX_HEALTH);
        sanwa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        sanwa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            SANWA_ATTACK_RANGE, SANWA_ATTACK_DAMAGE, SANWA_ATTACK_COOLDOWN
        );
        sanwa.addComponent(combat);
        
        // TODO: 添加阻挡组件
        
        return sanwa;
    }
    
    /**
     * 创建四娃（火焰子弹，伤害高）
     */
    public GameObject createSiwa(int row, int col) {
        GameObject siwa = new GameObject("Siwa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        siwa.addComponent(transform);
        
        // 绿色方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(30, 30),
            new RenderComponent.Color(0.0f, 1.0f, 0.0f, 1.0f)  // 绿色
        );
        render.setRenderer(renderer);
        siwa.addComponent(render);
        
        HealthComponent health = new HealthComponent(SIWA_MAX_HEALTH);
        siwa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        siwa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            SIWA_ATTACK_RANGE, SIWA_ATTACK_DAMAGE, SIWA_ATTACK_COOLDOWN
        );
        siwa.addComponent(combat);
        
        return siwa;
    }
    
    /**
     * 创建五娃（同时攻击多个目标）
     */
    public GameObject createWuwa(int row, int col) {
        GameObject wuwa = new GameObject("Wuwa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        wuwa.addComponent(transform);
        
        // 青色方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(30, 30),
            new RenderComponent.Color(0.0f, 1.0f, 1.0f, 1.0f)  // 青色
        );
        render.setRenderer(renderer);
        wuwa.addComponent(render);
        
        HealthComponent health = new HealthComponent(WUWA_MAX_HEALTH);
        wuwa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        wuwa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            WUWA_ATTACK_RANGE, WUWA_ATTACK_DAMAGE, WUWA_ATTACK_COOLDOWN
        );
        wuwa.addComponent(combat);
        
        // TODO: 添加多目标攻击标记
        
        return wuwa;
    }
    
    /**
     * 创建六娃（隐身，不被攻击）
     */
    public GameObject createLiuwa(int row, int col) {
        GameObject liuwa = new GameObject("Liuwa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        liuwa.addComponent(transform);
        
        // 蓝色半透明方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(30, 30),
            new RenderComponent.Color(0.0f, 0.0f, 1.0f, 0.5f)  // 蓝色半透明
        );
        render.setRenderer(renderer);
        liuwa.addComponent(render);
        
        HealthComponent health = new HealthComponent(LIUWA_MAX_HEALTH);
        liuwa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        liuwa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            LIUWA_ATTACK_RANGE, LIUWA_ATTACK_DAMAGE, LIUWA_ATTACK_COOLDOWN
        );
        liuwa.addComponent(combat);
        
        // TODO: 添加隐身标记
        
        return liuwa;
    }
    
    /**
     * 创建七娃（大招技能，25秒冷却）
     */
    public GameObject createQiwa(int row, int col) {
        GameObject qiwa = new GameObject("Qiwa");
        
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        qiwa.addComponent(transform);
        
        // 紫色方形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(30, 30),
            new RenderComponent.Color(0.6f, 0.0f, 1.0f, 1.0f)  // 紫色
        );
        render.setRenderer(renderer);
        qiwa.addComponent(render);
        
        HealthComponent health = new HealthComponent(QIWA_MAX_HEALTH);
        qiwa.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.FRIENDLY);
        qiwa.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            QIWA_ATTACK_RANGE, QIWA_ATTACK_DAMAGE, QIWA_ATTACK_COOLDOWN
        );
        qiwa.addComponent(combat);
        
        // TODO: 添加技能组件
        
        return qiwa;
    }
    
    /**
     * 创建妖精
     * @param row 行
     * @return 妖精GameObject
     */
    public GameObject createMonster(int row) {
        GameObject monster = new GameObject("Monster");
        
        // 妖精从最右列出现
        int col = gridSystem.getSpawnColumn();
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        // 添加Transform组件
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        monster.addComponent(transform);
        
        // 添加Render组件（绿色圆形）
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(25, 25),
            new RenderComponent.Color(0.0f, 1.0f, 0.0f, 1.0f)  // 绿色
        );
        render.setRenderer(renderer);
        monster.addComponent(render);
        
        // 添加Health组件
        HealthComponent health = new HealthComponent(MONSTER_MAX_HEALTH);
        monster.addComponent(health);
        
        // 添加Team组件（敌军）
        TeamComponent team = new TeamComponent(TeamComponent.Team.ENEMY);
        monster.addComponent(team);
        
        // 添加Combat组件（近战攻击）
        CombatComponent combat = new CombatComponent(
            MONSTER_ATTACK_RANGE, 
            MONSTER_ATTACK_DAMAGE, 
            MONSTER_ATTACK_COOLDOWN
        );
        monster.addComponent(combat);
        
        // 添加Movement组件（向左移动）
        MovementComponent movement = new MovementComponent(
            MONSTER_MOVE_SPEED, 
            new Vector2(-1, 0)  // 向左
        );
        movement.setGridSystem(gridSystem);
        monster.addComponent(movement);
        
        return monster;
    }
    
    /**
     * 创建子弹
     * @param startPos 起始位置
     * @param target 目标对象
     * @param damage 伤害值
     * @return 子弹GameObject
     */
    public GameObject createBullet(Vector2 startPos, GameObject target, float damage) {
        GameObject bullet = new GameObject("Bullet");
        
        // 添加Transform组件
        TransformComponent transform = new TransformComponent();
        transform.setPosition(startPos);
        bullet.addComponent(transform);
        
        // 添加Render组件（黄色小圆形）
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(5, 5),
            new RenderComponent.Color(1.0f, 1.0f, 0.0f, 1.0f)  // 黄色
        );
        render.setRenderer(renderer);
        bullet.addComponent(render);
        
        // 添加Projectile组件
        ProjectileComponent projectile = new ProjectileComponent(BULLET_SPEED, target, damage);
        bullet.addComponent(projectile);
        
        return bullet;
    }
    
    /**
     * 创建火焰子弹（四娃专用，红色，伤害高）
     */
    public GameObject createFireBullet(Vector2 startPos, GameObject target, float damage) {
        GameObject bullet = new GameObject("FireBullet");
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(startPos);
        bullet.addComponent(transform);
        
        // 火焰子弹：红色较大圆形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(8, 8),
            new RenderComponent.Color(1.0f, 0.3f, 0.0f, 1.0f)  // 橙红色
        );
        render.setRenderer(renderer);
        bullet.addComponent(render);
        
        // 速度稍快
        ProjectileComponent projectile = new ProjectileComponent(450.0f, target, damage);
        bullet.addComponent(projectile);
        
        return bullet;
    }
    
    /**
     * 创建妖精子弹（紫色，速度较慢，射程较近）
     * @param startPos 起始位置
     * @param target 目标对象
     * @param damage 伤害值
     * @return 妖精子弹GameObject
     */
    public GameObject createMonsterBullet(Vector2 startPos, GameObject target, float damage) {
        GameObject bullet = new GameObject("MonsterBullet");
        
        // 添加Transform组件
        TransformComponent transform = new TransformComponent();
        transform.setPosition(startPos);
        bullet.addComponent(transform);
        
        // 添加Render组件（紫色小圆形，比葫芦娃子弹稍大）
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(6, 6),
            new RenderComponent.Color(0.8f, 0.0f, 0.8f, 1.0f)  // 紫色
        );
        render.setRenderer(renderer);
        bullet.addComponent(render);
        
        // 添加Projectile组件（速度300，比葫芦娃子弹慢）
        ProjectileComponent projectile = new ProjectileComponent(300.0f, target, damage);
        bullet.addComponent(projectile);
        
        return bullet;
    }
    
    /**
     * 创建熊精（生命值非常高）
     */
    public GameObject createBearMonster(int row) {
        GameObject bear = new GameObject("BearMonster");
        
        int col = gridSystem.getSpawnColumn();
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        bear.addComponent(transform);
        
        // 棕色大型圆形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(35, 35),  // 比普通妖精大
            new RenderComponent.Color(0.6f, 0.4f, 0.2f, 1.0f)  // 棕色
        );
        render.setRenderer(renderer);
        bear.addComponent(render);
        
        HealthComponent health = new HealthComponent(BEAR_MAX_HEALTH);
        bear.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.ENEMY);
        bear.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            BEAR_ATTACK_RANGE, BEAR_ATTACK_DAMAGE, BEAR_ATTACK_COOLDOWN
        );
        bear.addComponent(combat);
        
        MovementComponent movement = new MovementComponent(
            BEAR_MOVE_SPEED, new Vector2(-1, 0)
        );
        movement.setGridSystem(gridSystem);
        bear.addComponent(movement);
        
        return bear;
    }
    
    /**
     * 创建豹子精（移动速度非常快）
     */
    public GameObject createLeopardMonster(int row) {
        GameObject leopard = new GameObject("LeopardMonster");
        
        int col = gridSystem.getSpawnColumn();
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        leopard.addComponent(transform);
        
        // 黄色圆形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(22, 22),
            new RenderComponent.Color(1.0f, 0.8f, 0.0f, 1.0f)  // 金黄色
        );
        render.setRenderer(renderer);
        leopard.addComponent(render);
        
        HealthComponent health = new HealthComponent(LEOPARD_MAX_HEALTH);
        leopard.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.ENEMY);
        leopard.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            LEOPARD_ATTACK_RANGE, LEOPARD_ATTACK_DAMAGE, LEOPARD_ATTACK_COOLDOWN
        );
        leopard.addComponent(combat);
        
        MovementComponent movement = new MovementComponent(
            LEOPARD_MOVE_SPEED, new Vector2(-1, 0)
        );
        movement.setGridSystem(gridSystem);
        leopard.addComponent(movement);
        
        return leopard;
    }
    
    /**
     * 创建老虎精（伤害非常高）
     */
    public GameObject createTigerMonster(int row) {
        GameObject tiger = new GameObject("TigerMonster");
        
        int col = gridSystem.getSpawnColumn();
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        tiger.addComponent(transform);
        
        // 橙红色圆形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(28, 28),
            new RenderComponent.Color(1.0f, 0.4f, 0.0f, 1.0f)  // 橙红色
        );
        render.setRenderer(renderer);
        tiger.addComponent(render);
        
        HealthComponent health = new HealthComponent(TIGER_MAX_HEALTH);
        tiger.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.ENEMY);
        tiger.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            TIGER_ATTACK_RANGE, TIGER_ATTACK_DAMAGE, TIGER_ATTACK_COOLDOWN
        );
        tiger.addComponent(combat);
        
        MovementComponent movement = new MovementComponent(
            TIGER_MOVE_SPEED, new Vector2(-1, 0)
        );
        movement.setGridSystem(gridSystem);
        tiger.addComponent(movement);
        
        return tiger;
    }
    
    /**
     * 创建鹰精（攻击范围非常大）
     */
    public GameObject createEagleMonster(int row) {
        GameObject eagle = new GameObject("EagleMonster");
        
        int col = gridSystem.getSpawnColumn();
        Vector2 position = gridSystem.gridToWorld(row, col);
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(position);
        eagle.addComponent(transform);
        
        // 蓝色圆形
        RenderComponent render = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(24, 24),
            new RenderComponent.Color(0.3f, 0.6f, 1.0f, 1.0f)  // 天蓝色
        );
        render.setRenderer(renderer);
        eagle.addComponent(render);
        
        HealthComponent health = new HealthComponent(EAGLE_MAX_HEALTH);
        eagle.addComponent(health);
        
        TeamComponent team = new TeamComponent(TeamComponent.Team.ENEMY);
        eagle.addComponent(team);
        
        CombatComponent combat = new CombatComponent(
            EAGLE_ATTACK_RANGE, EAGLE_ATTACK_DAMAGE, EAGLE_ATTACK_COOLDOWN
        );
        eagle.addComponent(combat);
        
        MovementComponent movement = new MovementComponent(
            EAGLE_MOVE_SPEED, new Vector2(-1, 0)
        );
        movement.setGridSystem(gridSystem);
        eagle.addComponent(movement);
        
        return eagle;
    }
    
    // Getters for default values (可用于调整参数)
    public static float getHuluwaMaxHealth() {
        return HULUWA_MAX_HEALTH;
    }
    
    public static float getMonsterMaxHealth() {
        return MONSTER_MAX_HEALTH;
    }
    
    public static float getBulletSpeed() {
        return BULLET_SPEED;
    }
}
