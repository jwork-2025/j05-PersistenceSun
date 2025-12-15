package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.GridSystem;

/**
 * 移动组件，处理网格移动逻辑
 */
public class MovementComponent extends Component<MovementComponent> {
    private float moveSpeed;        // 移动速度(格子/秒)
    private Vector2 direction;      // 移动方向
    private int currentRow;         // 当前格子行
    private int currentCol;         // 当前格子列
    private GridSystem gridSystem;  // 网格系统引用
    private float moveTimer;        // 移动计时器
    
    /**
     * 构造函数
     * @param moveSpeed 移动速度（格子/秒）
     * @param direction 移动方向
     */
    public MovementComponent(float moveSpeed, Vector2 direction) {
        this.moveSpeed = moveSpeed;
        this.direction = direction != null ? direction : new Vector2(0, 0);
        this.moveTimer = 0;
        this.name = "MovementComponent";
    }
    
    @Override
    public void initialize() {
        // 初始化时获取当前格子位置
        if (owner != null && gridSystem != null) {
            TransformComponent transform = owner.getComponent(TransformComponent.class);
            if (transform != null) {
                Vector2 pos = transform.getPosition();
                int[] gridPos = gridSystem.worldToGrid(pos.x, pos.y);
                if (gridPos != null) {
                    currentRow = gridPos[0];
                    currentCol = gridPos[1];
                }
            }
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (gridSystem == null || owner == null) return;
        if (direction.magnitude() == 0) return;  // 没有移动方向
        
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform == null) return;
        
        // 检查是否已经在移动中
        if (transform.isMoving()) {
            return;  // 等待当前移动完成
        }
        
        // 累加移动时间
        moveTimer += deltaTime * moveSpeed;
        
        // 当累计时间达到1秒时，移动一格
        if (moveTimer >= 1.0f) {
            moveTimer = 0;
            
            // 计算目标格子
            int targetRow = currentRow + (int)direction.y;
            int targetCol = currentCol + (int)direction.x;
            
            // 检查是否到达边界
            if (!gridSystem.isValidCell(targetRow, targetCol)) {
                // 到达边界，触发边界事件
                onReachedBoundary(targetRow, targetCol);
                return;
            }
            
            // 使用平滑移动到新格子
            Vector2 newPos = gridSystem.gridToWorld(targetRow, targetCol);
            transform.moveTo(newPos);  // 使用平滑移动
            
            currentRow = targetRow;
            currentCol = targetCol;
        }
    }
    
    @Override
    public void render() {
        // 不需要渲染
    }
    
    /**
     * 到达边界时的回调
     */
    private void onReachedBoundary(int targetRow, int targetCol) {
        // 检查是否到达最左列（失败条件）
        if (targetCol < 0) {
            // 妖精到达最左边，触发游戏失败
            TeamComponent team = owner.getComponent(TeamComponent.class);
            if (team != null && team.isEnemy()) {
                // 标记游戏失败（由GameLogic处理）
                owner.destroy();  // 移除该妖精
            }
        }
    }
    
    /**
     * 移动到指定格子
     */
    public void moveToGrid(int row, int col) {
        if (gridSystem == null || owner == null) return;
        if (!gridSystem.isValidCell(row, col)) return;
        
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform != null) {
            Vector2 newPos = gridSystem.gridToWorld(row, col);
            transform.setPosition(newPos);
            currentRow = row;
            currentCol = col;
        }
    }
    
    /**
     * 设置移动方向
     */
    public void setDirection(Vector2 dir) {
        this.direction = dir;
    }
    
    /**
     * 设置移动速度
     */
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }
    
    /**
     * 设置网格系统引用
     */
    public void setGridSystem(GridSystem gridSystem) {
        this.gridSystem = gridSystem;
    }
    
    // Getters
    public float getMoveSpeed() {
        return moveSpeed;
    }
    
    public Vector2 getDirection() {
        return direction;
    }
    
    public int getCurrentRow() {
        return currentRow;
    }
    
    public int getCurrentCol() {
        return currentCol;
    }
    
    @Override
    public Class<MovementComponent> getComponentType() {
        return MovementComponent.class;
    }
}
