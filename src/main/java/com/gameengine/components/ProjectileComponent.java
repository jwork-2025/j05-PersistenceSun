package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

/**
 * 子弹组件，处理子弹飞行和碰撞
 */
public class ProjectileComponent extends Component<ProjectileComponent> {
    private float speed;            // 子弹速度（像素/秒）
    private Vector2 direction;      // 飞行方向
    private float damage;           // 伤害值
    private GameObject target;      // 目标对象
    private float maxDistance;      // 最大飞行距离
    private float traveledDistance; // 已飞行距离
    private Vector2 startPosition;  // 起始位置
    
    /**
     * 构造函数
     * @param speed 飞行速度
     * @param target 目标对象
     * @param damage 伤害值
     */
    public ProjectileComponent(float speed, GameObject target, float damage) {
        this.speed = speed;
        this.target = target;
        this.damage = damage;
        this.maxDistance = 1000.0f;  // 默认最大飞行距离
        this.traveledDistance = 0;
        this.name = "ProjectileComponent";
    }
    
    @Override
    public void initialize() {
        // 记录起始位置
        if (owner != null) {
            TransformComponent transform = owner.getComponent(TransformComponent.class);
            if (transform != null) {
                startPosition = new Vector2(transform.getPosition().x, transform.getPosition().y);
            }
        }
        
        // 计算飞行方向
        if (target != null && owner != null) {
            TransformComponent myTransform = owner.getComponent(TransformComponent.class);
            TransformComponent targetTransform = target.getComponent(TransformComponent.class);
            
            if (myTransform != null && targetTransform != null) {
                Vector2 myPos = myTransform.getPosition();
                Vector2 targetPos = targetTransform.getPosition();
                direction = targetPos.subtract(myPos).normalize();
            }
        }
        
        if (direction == null) {
            direction = new Vector2(1, 0);  // 默认向右
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (owner == null) return;
        
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform == null) return;
        
        // 移动子弹
        Vector2 velocity = direction.multiply(speed * deltaTime);
        Vector2 currentPos = transform.getPosition();
        Vector2 newPos = currentPos.add(velocity);
        transform.setPosition(newPos);
        
        // 更新已飞行距离
        float moveDistance = velocity.magnitude();
        traveledDistance += moveDistance;
        
        // 检查是否超过最大距离
        if (traveledDistance >= maxDistance) {
            owner.destroy();
            return;
        }
        
        // 检查与目标的碰撞
        if (target != null && target.isActive()) {
            if (checkCollision(target)) {
                // 击中目标，造成伤害
                HealthComponent targetHealth = target.getComponent(HealthComponent.class);
                if (targetHealth != null && targetHealth.isAlive()) {
                    targetHealth.takeDamage(damage);
                }
                // 销毁子弹
                owner.destroy();
            }
        } else {
            // 目标已被销毁，销毁子弹
            owner.destroy();
        }
    }
    
    @Override
    public void render() {
        // 渲染由RenderComponent处理
    }
    
    /**
     * 检查与目标的碰撞
     */
    public boolean checkCollision(GameObject target) {
        if (target == null || owner == null) return false;
        
        TransformComponent myTransform = owner.getComponent(TransformComponent.class);
        TransformComponent targetTransform = target.getComponent(TransformComponent.class);
        
        if (myTransform == null || targetTransform == null) return false;
        
        Vector2 myPos = myTransform.getPosition();
        Vector2 targetPos = targetTransform.getPosition();
        
        // 简单的圆形碰撞检测
        float collisionDistance = 20.0f;  // 碰撞半径
        float distance = myPos.distance(targetPos);
        
        return distance <= collisionDistance;
    }
    
    // Getters and Setters
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public Vector2 getDirection() {
        return direction;
    }
    
    public void setDirection(Vector2 direction) {
        this.direction = direction;
    }
    
    public float getDamage() {
        return damage;
    }
    
    public void setDamage(float damage) {
        this.damage = damage;
    }
    
    public GameObject getTarget() {
        return target;
    }
    
    public void setTarget(GameObject target) {
        this.target = target;
    }
    
    public float getMaxDistance() {
        return maxDistance;
    }
    
    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }
    
    @Override
    public Class<ProjectileComponent> getComponentType() {
        return ProjectileComponent.class;
    }
}
