package com.gameengine.components;

import com.gameengine.core.Component;

/**
 * 生命值组件，管理实体的生命值
 */
public class HealthComponent extends Component<HealthComponent> {
    private float maxHealth;
    private float currentHealth;
    private boolean isDead;
    
    /**
     * 构造函数
     * @param maxHealth 最大生命值
     */
    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.isDead = false;
        this.name = "HealthComponent";
    }
    
    @Override
    public void initialize() {
        // 初始化逻辑（如果需要）
    }
    
    @Override
    public void update(float deltaTime) {
        // 检查死亡状态
        if (currentHealth <= 0 && !isDead) {
            isDead = true;
            onDeath();
        }
    }
    
    @Override
    public void render() {
        // 可选：渲染血条
    }
    
    /**
     * 受到伤害
     * @param damage 伤害值
     */
    public void takeDamage(float damage) {
        if (isDead) return;
        
        currentHealth -= damage;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }
    
    /**
     * 治疗
     * @param amount 治疗量
     */
    public void heal(float amount) {
        if (isDead) return;
        
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }
    
    /**
     * 死亡回调
     */
    private void onDeath() {
        // 标记GameObject为非激活状态
        if (owner != null) {
            owner.destroy();
        }
    }
    
    /**
     * 获取生命值百分比
     */
    public float getHealthPercentage() {
        return maxHealth > 0 ? currentHealth / maxHealth : 0;
    }
    
    /**
     * 是否存活
     */
    public boolean isAlive() {
        return !isDead && currentHealth > 0;
    }
    
    // Getters
    public float getMaxHealth() {
        return maxHealth;
    }
    
    public float getCurrentHealth() {
        return currentHealth;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    // Setters
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }
    
    public void setCurrentHealth(float currentHealth) {
        this.currentHealth = currentHealth;
        if (this.currentHealth > maxHealth) {
            this.currentHealth = maxHealth;
        }
        if (this.currentHealth < 0) {
            this.currentHealth = 0;
        }
    }
    
    @Override
    public Class<HealthComponent> getComponentType() {
        return HealthComponent.class;
    }
}
