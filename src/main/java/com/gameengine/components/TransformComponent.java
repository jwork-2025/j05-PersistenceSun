package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.math.Vector2;

/**
 * 变换组件，管理位置、旋转、缩放
 */
public class TransformComponent extends Component<TransformComponent> {
    private Vector2 position;
    private Vector2 scale;
    private float rotation;
    
    // 平滑移动相关
    private Vector2 targetPosition;
    private boolean isMoving;
    private float moveSpeed = 400.0f; // 像素/秒
    
    public TransformComponent() {
        this.position = new Vector2();
        this.targetPosition = new Vector2();
        this.scale = new Vector2(1, 1);
        this.rotation = 0;
        this.isMoving = false;
    }
    
    public TransformComponent(Vector2 position) {
        this();
        this.position = new Vector2(position);
        this.targetPosition = new Vector2(position);
    }
    
    public TransformComponent(Vector2 position, Vector2 scale, float rotation) {
        this.position = new Vector2(position);
        this.targetPosition = new Vector2(position);
        this.scale = new Vector2(scale);
        this.rotation = rotation;
        this.isMoving = false;
    }
    
    @Override
    public void initialize() {
        // 初始化变换组件
    }
    
    @Override
    public void update(float deltaTime) {
        // 平滑移动到目标位置
        if (isMoving) {
            Vector2 direction = targetPosition.subtract(position);
            float distance = direction.magnitude();
            
            if (distance < 1.0f) {
                // 到达目标位置
                position.x = targetPosition.x;
                position.y = targetPosition.y;
                isMoving = false;
            } else {
                // 继续移动
                Vector2 velocity = direction.normalize().multiply(moveSpeed * deltaTime);
                if (velocity.magnitude() > distance) {
                    position.x = targetPosition.x;
                    position.y = targetPosition.y;
                    isMoving = false;
                } else {
                    position = position.add(velocity);
                }
            }
        }
    }
    
    @Override
    public void render() {
        // 变换组件不直接渲染
    }
    
    /**
     * 平滑移动到指定位置
     */
    public void moveTo(Vector2 newPosition) {
        this.targetPosition = new Vector2(newPosition);
        this.isMoving = true;
    }
    
    /**
     * 瞬间移动到目标位置（不使用动画）
     */
    public void teleportTo(Vector2 target) {
        this.position = new Vector2(target);
        this.targetPosition = new Vector2(target);
        this.isMoving = false;
    }
    
    /**
     * 移动相对距离
     */
    public void translate(Vector2 delta) {
        this.position = position.add(delta);
        this.targetPosition = new Vector2(this.position);
    }
    
    /**
     * 旋转指定角度
     */
    public void rotate(float angle) {
        this.rotation += angle;
    }
    
    /**
     * 设置旋转角度
     */
    public void setRotation(float angle) {
        this.rotation = angle;
    }
    
    /**
     * 缩放
     */
    public void scale(Vector2 scaleFactor) {
        this.scale = new Vector2(this.scale.x * scaleFactor.x, this.scale.y * scaleFactor.y);
    }
    
    /**
     * 设置缩放
     */
    public void setScale(Vector2 newScale) {
        this.scale = new Vector2(newScale);
    }
    
    // Getters and Setters
    public Vector2 getPosition() {
        return new Vector2(position);
    }
    
    public void setPosition(Vector2 position) {
        this.position = new Vector2(position);
        this.targetPosition = new Vector2(position);
        this.isMoving = false;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public Vector2 getScale() {
        return new Vector2(scale);
    }
    
    public float getRotation() {
        return rotation;
    }
}
