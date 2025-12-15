package com.gameengine.input;

import com.gameengine.math.Vector2;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 输入管理器，处理键盘和鼠标输入
 */
public class InputManager {
    private static InputManager instance;
    private Set<Integer> pressedKeys;
    private Set<Integer> justPressedKeys;
    private Map<Integer, Boolean> keyStates;
    private Vector2 mousePosition;
    private boolean[] mouseButtons;
    private boolean[] mouseButtonsJustPressed;
    
    private InputManager() {
        pressedKeys = new HashSet<>();
        justPressedKeys = new HashSet<>();
        keyStates = new HashMap<>();
        mousePosition = new Vector2();
        mouseButtons = new boolean[3]; // 左键、右键、中键
        mouseButtonsJustPressed = new boolean[3];
    }
    
    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }
    
    /**
     * 更新输入状态
     */
    public void update() {
        justPressedKeys.clear();
        for (int i = 0; i < mouseButtonsJustPressed.length; i++) {
            mouseButtonsJustPressed[i] = false;
        }
    }
    
    /**
     * 处理键盘按下事件
     */
    public void onKeyPressed(int keyCode) {
        if (!pressedKeys.contains(keyCode)) {
            justPressedKeys.add(keyCode);
        }
        pressedKeys.add(keyCode);
        keyStates.put(keyCode, true);
    }
    
    /**
     * 处理键盘释放事件
     */
    public void onKeyReleased(int keyCode) {
        pressedKeys.remove(keyCode);
        keyStates.put(keyCode, false);
    }
    
    /**
     * 处理鼠标移动事件
     */
    public void onMouseMoved(float x, float y) {
        mousePosition.x = x;
        mousePosition.y = y;
    }
    
    /**
     * 处理鼠标按下事件
     */
    public void onMousePressed(int button) {
        // Java MouseEvent的button从1开始，需要减1映射到数组索引
        int index = button - 1;
        if (index >= 0 && index < mouseButtons.length) {
            if (!mouseButtons[index]) {
                mouseButtonsJustPressed[index] = true;
            }
            mouseButtons[index] = true;
        }
    }
    
    /**
     * 处理鼠标释放事件
     */
    public void onMouseReleased(int button) {
        // Java MouseEvent的button从1开始，需要减1映射到数组索引
        int index = button - 1;
        if (index >= 0 && index < mouseButtons.length) {
            mouseButtons[index] = false;
        }
    }
    
    /**
     * 检查按键是否被按下
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    /**
     * 检查按键是否刚刚被按下（只在这一帧为true）
     */
    public boolean isKeyJustPressed(int keyCode) {
        return justPressedKeys.contains(keyCode);
    }
    
    /**
     * 检查鼠标按键是否被按下
     */
    public boolean isMouseButtonPressed(int button) {
        int index = button - 1;
        if (index >= 0 && index < mouseButtons.length) {
            return mouseButtons[index];
        }
        return false;
    }
    
    /**
     * 检查鼠标按键是否刚刚被按下
     */
    public boolean isMouseButtonJustPressed(int button) {
        int index = button - 1;
        if (index >= 0 && index < mouseButtons.length) {
            return mouseButtonsJustPressed[index];
        }
        return false;
    }
    
    /**
     * 获取鼠标位置
     */
    public Vector2 getMousePosition() {
        return new Vector2(mousePosition);
    }
    
    /**
     * 获取鼠标X坐标
     */
    public float getMouseX() {
        return mousePosition.x;
    }
    
    /**
     * 获取鼠标Y坐标
     */
    public float getMouseY() {
        return mousePosition.y;
    }
    
    /**
     * 获取刚按下的按键快照（用于录制）
     */
    public Set<Integer> getJustPressedKeysSnapshot() {
        return new HashSet<>(justPressedKeys);
    }
}
