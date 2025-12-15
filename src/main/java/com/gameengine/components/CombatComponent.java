package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.List;

/**
 * 战斗组件，处理攻击逻辑
 */
public class CombatComponent extends Component<CombatComponent> {
    private float attackRange;      // 攻击范围（格子单位）
    private float attackDamage;     // 攻击伤害
    private float attackCooldown;   // 攻击冷却时间（秒）
    private float currentCooldown;  // 当前冷却计时
    private GameObject target;      // 当前目标
    private Scene scene;            // 场景引用（用于查找目标）
    
    // 攻击回调接口
    public interface AttackCallback {
        void onAttack(GameObject attacker, GameObject target);
    }
    
    private AttackCallback attackCallback;
    
    /**
     * 构造函数
     * @param attackRange 攻击范围
     * @param attackDamage 攻击伤害
     * @param attackCooldown 攻击冷却时间
     */
    public CombatComponent(float attackRange, float attackDamage, float attackCooldown) {
        this.attackRange = attackRange;
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.currentCooldown = 0;
        this.name = "CombatComponent";
    }
    
    @Override
    public void initialize() {
        // 初始化逻辑
    }
    
    @Override
    public void update(float deltaTime) {
        // 更新冷却时间
        if (currentCooldown > 0) {
            currentCooldown -= deltaTime;
        }
        
        // 如果场景引用存在，自动查找和攻击目标
        if (scene != null && canAttack()) {
            target = findTarget();
            if (target != null) {
                attack();
            }
        }
    }
    
    @Override
    public void render() {
        // 可选：渲染攻击范围指示器
    }
    
    /**
     * 是否可以攻击
     */
    public boolean canAttack() {
        return currentCooldown <= 0;
    }
    
    /**
     * 查找范围内的敌人目标
     * @return 找到的目标，如果没有返回null
     */
    public GameObject findTarget() {
        if (scene == null || owner == null) return null;
        
        TransformComponent myTransform = owner.getComponent(TransformComponent.class);
        TeamComponent myTeam = owner.getComponent(TeamComponent.class);
        
        if (myTransform == null || myTeam == null) return null;
        
        Vector2 myPos = myTransform.getPosition();
        GameObject closestEnemy = null;
        float closestDistance = Float.MAX_VALUE;
        
        // 遍历场景中所有对象
        List<GameObject> allObjects = scene.getGameObjects();
        for (GameObject obj : allObjects) {
            if (obj == owner || !obj.isActive()) continue;
            
            TeamComponent objTeam = obj.getComponent(TeamComponent.class);
            TransformComponent objTransform = obj.getComponent(TransformComponent.class);
            HealthComponent objHealth = obj.getComponent(HealthComponent.class);
            
            // 检查是否是敌人且存活
            if (objTeam != null && myTeam.isEnemy(objTeam) && 
                objTransform != null && objHealth != null && objHealth.isAlive()) {
                
                Vector2 objPos = objTransform.getPosition();
                float distance = myPos.distance(objPos);
                
                // 将像素距离转换为格子单位（假设每格80像素）
                float gridDistance = distance / 80.0f;
                
                if (gridDistance <= attackRange && distance < closestDistance) {
                    closestEnemy = obj;
                    closestDistance = distance;
                }
            }
        }
        
        return closestEnemy;
    }
    
    /**
     * 执行攻击
     */
    public void attack() {
        if (target == null || !canAttack()) return;
        
        // 重置冷却
        currentCooldown = attackCooldown;
        
        // 触发攻击回调（用于发射子弹等）
        if (attackCallback != null) {
            attackCallback.onAttack(owner, target);
        }
    }
    
    /**
     * 直接对目标造成伤害（近战攻击用）
     */
    public void dealDamage(GameObject target) {
        if (target == null) return;
        
        HealthComponent targetHealth = target.getComponent(HealthComponent.class);
        if (targetHealth != null && targetHealth.isAlive()) {
            targetHealth.takeDamage(attackDamage);
        }
    }
    
    /**
     * 检查目标是否在攻击范围内
     */
    public boolean isInRange(GameObject target) {
        if (target == null || owner == null) return false;
        
        TransformComponent myTransform = owner.getComponent(TransformComponent.class);
        TransformComponent targetTransform = target.getComponent(TransformComponent.class);
        
        if (myTransform == null || targetTransform == null) return false;
        
        float distance = myTransform.getPosition().distance(targetTransform.getPosition());
        float gridDistance = distance / 80.0f;
        
        return gridDistance <= attackRange;
    }
    
    // Getters and Setters
    public float getAttackRange() {
        return attackRange;
    }
    
    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }
    
    public float getAttackDamage() {
        return attackDamage;
    }
    
    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }
    
    public float getAttackCooldown() {
        return attackCooldown;
    }
    
    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }
    
    public GameObject getTarget() {
        return target;
    }
    
    public void setTarget(GameObject target) {
        this.target = target;
    }
    
    public void setScene(Scene scene) {
        this.scene = scene;
    }
    
    public void setAttackCallback(AttackCallback callback) {
        this.attackCallback = callback;
    }
    
    @Override
    public Class<CombatComponent> getComponentType() {
        return CombatComponent.class;
    }
}
