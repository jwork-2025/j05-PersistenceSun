package com.gameengine.components;

import com.gameengine.core.Component;

/**
 * 阵营组件，标识实体所属阵营
 */
public class TeamComponent extends Component<TeamComponent> {
    
    /**
     * 阵营枚举
     */
    public enum Team {
        FRIENDLY,  // 友军（葫芦娃）
        ENEMY      // 敌军（妖精）
    }
    
    private Team team;
    
    /**
     * 构造函数
     * @param team 阵营
     */
    public TeamComponent(Team team) {
        this.team = team;
        this.name = "TeamComponent";
    }
    
    @Override
    public void initialize() {
        // 初始化逻辑（如果需要）
    }
    
    @Override
    public void update(float deltaTime) {
        // 阵营组件通常不需要更新
    }
    
    @Override
    public void render() {
        // 不需要渲染
    }
    
    /**
     * 判断是否是敌对阵营
     * @param other 另一个阵营组件
     * @return 是否敌对
     */
    public boolean isEnemy(TeamComponent other) {
        if (other == null) return false;
        return this.team != other.team;
    }
    
    /**
     * 判断是否是友军
     * @param other 另一个阵营组件
     * @return 是否友军
     */
    public boolean isFriendly(TeamComponent other) {
        if (other == null) return false;
        return this.team == other.team;
    }
    
    // Getters
    public Team getTeam() {
        return team;
    }
    
    public boolean isFriendly() {
        return team == Team.FRIENDLY;
    }
    
    public boolean isEnemy() {
        return team == Team.ENEMY;
    }
    
    // Setters
    public void setTeam(Team team) {
        this.team = team;
    }
    
    @Override
    public Class<TeamComponent> getComponentType() {
        return TeamComponent.class;
    }
}
