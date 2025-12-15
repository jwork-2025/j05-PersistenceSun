package com.gameengine.scene;

import com.gameengine.core.GameObject;
import com.gameengine.graphics.Renderer;
import com.gameengine.math.Vector2;

/**
 * 格子系统，管理游戏战场的网格布局
 * 类似"植物大战僵尸"的网格战场
 */
public class GridSystem {
    // 网格配置
    private final int rows;           // 行数
    private final int cols;           // 列数
    private final float cellWidth;    // 格子宽度
    private final float cellHeight;   // 格子高度
    private final float offsetX;      // 网格起始X偏移
    private final float offsetY;      // 网格起始Y偏移
    
    // 格子占用状态
    private final GameObject[][] grid;
    
    // 默认配置常量
    public static final int DEFAULT_ROWS = 5;
    public static final int DEFAULT_COLS = 9;
    public static final float DEFAULT_CELL_WIDTH = 80f;
    public static final float DEFAULT_CELL_HEIGHT = 80f;
    public static final float DEFAULT_OFFSET_X = 40f;
    public static final float DEFAULT_OFFSET_Y = 100f;
    
    /**
     * 使用默认配置创建格子系统
     */
    public GridSystem() {
        this(DEFAULT_ROWS, DEFAULT_COLS, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, 
             DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);
    }
    
    /**
     * 使用自定义配置创建格子系统
     */
    public GridSystem(int rows, int cols, float cellWidth, float cellHeight, 
                      float offsetX, float offsetY) {
        this.rows = rows;
        this.cols = cols;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.grid = new GameObject[rows][cols];
    }
    
    /**
     * 将格子坐标转换为屏幕世界坐标（返回格子中心点）
     * @param row 行索引 (0 ~ rows-1)
     * @param col 列索引 (0 ~ cols-1)
     * @return 格子中心的屏幕坐标
     */
    public Vector2 gridToWorld(int row, int col) {
        float x = offsetX + col * cellWidth + cellWidth / 2;
        float y = offsetY + row * cellHeight + cellHeight / 2;
        return new Vector2(x, y);
    }
    
    /**
     * 将屏幕世界坐标转换为格子坐标
     * @param worldX 屏幕X坐标
     * @param worldY 屏幕Y坐标
     * @return 格子坐标 [row, col]，如果超出边界返回 null
     */
    public int[] worldToGrid(float worldX, float worldY) {
        int col = (int) ((worldX - offsetX) / cellWidth);
        int row = (int) ((worldY - offsetY) / cellHeight);
        
        if (isValidCell(row, col)) {
            return new int[]{row, col};
        }
        return null;
    }
    
    /**
     * 检查格子坐标是否有效
     */
    public boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    /**
     * 检查格子是否被占用
     */
    public boolean isOccupied(int row, int col) {
        if (!isValidCell(row, col)) {
            return true; // 无效格子视为已占用
        }
        GameObject obj = grid[row][col];
        // 如果格子中有对象，但对象已经不活跃，视为未占用
        if (obj != null && !obj.isActive()) {
            grid[row][col] = null;  // 顺便清理
            return false;
        }
        return obj != null;
    }
    
    /**
     * 检查是否可以移动到指定格子
     */
    public boolean canMoveTo(int row, int col) {
        return isValidCell(row, col) && !isOccupied(row, col);
    }
    
    /**
     * 在指定格子放置游戏对象
     * @return 是否放置成功
     */
    public boolean placeObject(int row, int col, GameObject object) {
        if (!canMoveTo(row, col)) {
            return false;
        }
        grid[row][col] = object;
        return true;
    }
    
    /**
     * 从指定格子移除游戏对象
     * @return 被移除的对象，如果格子为空返回 null
     */
    public GameObject removeObject(int row, int col) {
        if (!isValidCell(row, col)) {
            return null;
        }
        GameObject obj = grid[row][col];
        grid[row][col] = null;
        return obj;
    }
    
    /**
     * 获取指定格子的游戏对象
     */
    public GameObject getObject(int row, int col) {
        if (!isValidCell(row, col)) {
            return null;
        }
        return grid[row][col];
    }
    
    /**
     * 移动游戏对象从一个格子到另一个格子
     * @return 是否移动成功
     */
    public boolean moveObject(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidCell(fromRow, fromCol) || !canMoveTo(toRow, toCol)) {
            return false;
        }
        
        GameObject obj = grid[fromRow][fromCol];
        if (obj == null) {
            return false;
        }
        
        grid[fromRow][fromCol] = null;
        grid[toRow][toCol] = obj;
        return true;
    }
    
    /**
     * 查找游戏对象所在的格子坐标
     * @return 格子坐标 [row, col]，如果未找到返回 null
     */
    public int[] findObject(GameObject object) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] == object) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }
    
    /**
     * 清空所有格子
     */
    public void clear() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = null;
            }
        }
    }
    
    /**
     * 渲染网格线（用于调试）
     */
    public void render(Renderer renderer) {
        // 绘制网格线颜色：灰色半透明
        float r = 0.5f, g = 0.5f, b = 0.5f, a = 0.5f;
        
        // 绘制水平线
        for (int row = 0; row <= rows; row++) {
            float y = offsetY + row * cellHeight;
            float x1 = offsetX;
            float x2 = offsetX + cols * cellWidth;
            renderer.drawLine(x1, y, x2, y, r, g, b, a);
        }
        
        // 绘制垂直线
        for (int col = 0; col <= cols; col++) {
            float x = offsetX + col * cellWidth;
            float y1 = offsetY;
            float y2 = offsetY + rows * cellHeight;
            renderer.drawLine(x, y1, x, y2, r, g, b, a);
        }
        
        // 高亮显示被占用的格子
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] != null) {
                    float x = offsetX + col * cellWidth;
                    float y = offsetY + row * cellHeight;
                    // 用淡黄色标记占用的格子
                    renderer.drawRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 
                                     1.0f, 1.0f, 0.5f, 0.2f);
                }
            }
        }
    }
    
    /**
     * 获取妖精生成列（最右侧）
     */
    public int getSpawnColumn() {
        return cols - 1;
    }
    
    /**
     * 获取失败列（最左侧）
     */
    public int getFailColumn() {
        return 0;
    }
    
    /**
     * 获取随机行（用于妖精生成）
     */
    public int getRandomRow() {
        return (int) (Math.random() * rows);
    }
    
    // Getters
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public float getCellWidth() {
        return cellWidth;
    }
    
    public float getCellHeight() {
        return cellHeight;
    }
    
    public float getOffsetX() {
        return offsetX;
    }
    
    public float getOffsetY() {
        return offsetY;
    }
    
    /**
     * 获取网格总宽度
     */
    public float getTotalWidth() {
        return cols * cellWidth;
    }
    
    /**
     * 获取网格总高度
     */
    public float getTotalHeight() {
        return rows * cellHeight;
    }
}
