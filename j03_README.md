
# 葫芦娃大战妖精 - Java游戏引擎

一个基于Java Swing的游戏引擎，采用ECS架构，支持完整的游戏录制与回放功能，测试录屏视频参见链接[j03&j05视频链接](https://www.bilibili.com/video/BV1Mk2yBxEsq/?share_source=copy_web&vd_source=09a928393c9ed4fbe22680d55d146b2f)

## 🎮 游戏简介

这是一款塔防类游戏，灵感源自于《植物大战僵尸》，玩家需要指挥七个葫芦娃守卫左侧阵地，阻止各自从右侧不断进攻的妖精靠近左侧。

## 🎯 功能特性

### 核心引擎
- **游戏循环**: 固定60 FPS渲染，可变时间步长更新
- **场景管理**: 支持多场景切换（菜单、游戏、回放）
- **组件系统**: 基于泛型的ECS架构
- **对象生命周期**: instanceId唯一标识，自动管理

### 渲染系统
- **Java Swing**: 基于JPanel的即时模式渲染
- **图形支持**: 矩形、圆形、线条、文本
- **网格系统**: 可视化战场网格
- **UI层**: 主菜单、按钮交互

### 游戏系统
- **战斗系统**: 自动瞄准、子弹飞行、伤害计算
- **移动系统**: 网格移动、碰撞检测
- **生成系统**: 定时波次、难度递增、批量生成
- **输入处理**: 鼠标选择、方向键移动、快捷键

### 录制回放系统 ⭐
- **异步录制**: 基于BlockingQueue的非阻塞写入
- **JSONL格式**: 每行一个JSON对象，易于解析
- **关键帧系统**: 0.1秒间隔，精确记录位置、颜色、类型
- **插值回放**: 平滑的帧间插值
- **唯一ID追踪**: 基于instanceId的对象识别
- **文件管理**: 列表选择、时间戳命名

## 📁 项目结构

```
src/main/java/com/gameengine/
├── core/                  # 核心引擎
│   ├── GameEngine.java    # 游戏主循环、窗口管理
│   ├── GameObject.java    # 游戏对象基类（含instanceId）
│   ├── Component.java     # 组件基类
│   └── GameLogic.java     # 游戏逻辑处理
├── components/            # ECS组件
│   ├── TransformComponent.java   # 位置、旋转、缩放
│   ├── RenderComponent.java      # 渲染类型、颜色、尺寸
│   ├── HealthComponent.java      # 生命值系统
│   ├── TeamComponent.java        # 阵营标识
│   ├── CombatComponent.java      # 战斗系统
│   ├── MovementComponent.java    # 网格移动（妖精）
│   └── ProjectileComponent.java  # 子弹飞行
├── entities/              # 实体工厂
│   └── EntityFactory.java # 创建葫芦娃、妖精、子弹
├── scene/                 # 场景系统
│   ├── Scene.java         # 场景基类
│   └── GridSystem.java    # 网格管理（5×9）
├── graphics/              # 渲染系统
│   └── Renderer.java      # Swing渲染器
├── input/                 # 输入管理
│   └── InputManager.java  # 键盘鼠标处理
├── math/                  # 数学工具
│   └── Vector2.java       # 2D向量运算
├── recording/             # 录制回放系统 ⭐
│   ├── RecordingService.java    # 异步录制服务
│   ├── RecordingStorage.java    # 存储接口
│   ├── FileRecordingStorage.java # 文件存储实现
│   ├── RecordingConfig.java     # 录制配置
│   └── RecordingJson.java       # 轻量JSON解析
└── example/               # 游戏实现
    ├── GameExample.java   # 主游戏场景
    └── ReplayScene.java   # 回放场景
```

## 🚀 快速开始

### 环境要求
- Java 11 或更高版本

### 编译运行
```bash
# 编译
bash compile.sh

# 运行
bash run.sh

# 或一步到位
bash run.sh
```

### 游戏操作

**主菜单**
- 鼠标左键点击"开始游戏"按钮
- 鼠标左键点击"回放录像"按钮

**游戏中**
- **鼠标左键**: 点击选中葫芦娃
- **方向键 ↑↓←→**: 移动选中的葫芦娃
- **R键**: 切换录制状态（开始/停止）
- **ESC键**: 返回主菜单

**回放模式**
- **方向键 ↑↓**: 选择录像文件
- **Enter/Space**: 播放选中的录像
- **ESC键**: 返回主菜单

## 🎲 游戏规则

### 游戏目标与成绩计算

- 游戏开始后，战场右侧会不断生成各种妖精，妖精自右向左移动并向攻击范围内的葫芦娃发射子弹，葫芦娃也会向攻击范围内的妖精发射子弹，子弹命中敌人时会造成伤害，**当有妖精抵达屏幕左侧时游戏结束**。
- **从游戏开始到游戏结束的时间即为本次游戏的成绩。**
- 玩家需要合理指挥各葫芦娃的移动，争取获得更高的分数。

### 角色属性

- **大娃**: 力大无穷，伤害值很高，但攻击范围近。
- **二娃**: 千里眼顺风耳，攻击范围覆盖全屏。
- **四娃**: 发射伤害值较高的火焰子弹。
- **五娃**: 能同时向攻击范围内多个目标发射子弹。
- **熊精**：皮糙肉厚，生命值超高。
- **老虎精**：百兽之王，伤害值很高。
- **豹子精**：移动速度超快。
- **鹰精**：鹰眼，攻击范围较大。
- **更多角色正在开发中……**


### 战场机制

- **网格系统**: 5行×9列，每格80×80像素


### 波次系统

每隔20秒提升一个难度级别，即提升生成妖精的频率和数量。



## 🎬 录制回放系统

### 录制功能

**自动录制：**
- 游戏开始时自动创建录像文件
- 文件存储在 `recordings/` 目录
- 命名格式：`session_<timestamp>.jsonl`

**手动控制：**
- 按 `R` 键切换录制状态
- 控制台会显示录制开始/结束提示

**录制内容：**
- Header（窗口尺寸、版本号）
- Input（按键事件、时间戳）
- Keyframe（对象位置、颜色、类型、ID）

**JSONL格式示例：**
```json
{"type":"header","version":1,"w":800,"h":600}
{"type":"input","t":1.23,"keys":[37,38]}
{"type":"keyframe","t":0.1,"entities":[
  {"id":"Dawa#1","x":160,"y":300,"rt":"RECTANGLE","w":35,"h":35,"color":[0.8,0,0,1]},
  {"id":"Bullet#15","x":200,"y":310,"rt":"CIRCLE","w":10,"h":10,"color":[1,1,0,1]}
]}
```

### 回放功能

**文件选择：**
- 回放场景会列出所有可用录像
- 使用方向键 ↑↓ 选择
- 按 Enter 或 Space 播放

**回放特性：**
- 关键帧插值：平滑的60 FPS回放
- 颜色还原：准确显示所有实体颜色
- 唯一ID追踪：每个对象独立识别（`名字#instanceId`）
- 对象池管理：动态创建/停用对象

**技术要点：**
- 解决了JSON数组解析bug（`RecordingJson.field()`）
- 使用instanceId替代不稳定的哈希码
- Map<String,GameObject>按ID管理对象
- 自动处理对象生命周期（子弹、妖精的创建/销毁）

## 🏗️ 架构设计

### ECS组件系统

```java
// 创建葫芦娃
GameObject huluwa = new GameObject("Dawa");

// 添加组件
huluwa.addComponent(new TransformComponent(new Vector2(160, 300)));
huluwa.addComponent(new RenderComponent(
    RenderComponent.RenderType.RECTANGLE,
    new Vector2(35, 35),
    new RenderComponent.Color(0.8f, 0f, 0f, 1f)  // 红色
));
huluwa.addComponent(new HealthComponent(100));
huluwa.addComponent(new TeamComponent(TeamComponent.Team.FRIENDLY));
huluwa.addComponent(new CombatComponent(20, 240, 1.0f));
```

### 场景架构

```
GameExample (主游戏场景)
    ├── 初始化
    │   ├── GridSystem (5×9网格)
    │   ├── EntityFactory (实体工厂)
    │   └── RecordingService (录制服务)
    ├── 更新循环
    │   ├── handlePlayerInput() - 鼠标选择、键盘移动
    │   ├── spawnMonster() - 定时波次生成
    │   ├── handleCombat() - 战斗逻辑
    │   ├── updateProjectiles() - 子弹飞行
    │   ├── cleanupDeadEntities() - 清理死亡对象
    │   └── checkGameOver() - 胜负判定
    └── 录制控制
        ├── recordingService.update() - 记录关键帧
        └── R键切换录制状态

ReplayScene (回放场景)
    ├── 文件选择模式
    │   └── 列出recordings/目录下所有.jsonl文件
    ├── 回放模式
    │   ├── loadRecording() - 解析JSONL文件
    │   ├── buildObjectsFromFirstKeyframe() - 创建初始对象
    │   ├── updateInterpolatedPositions() - 插值更新
    │   │   ├── Map<String,GameObject> - 按ID管理对象
    │   │   ├── 动态创建新对象（子弹、妖精）
    │   │   └── 停用不在当前帧的对象
    │   └── updateRenderComponent() - 更新颜色、尺寸
    └── 渲染
        ├── GridSystem - 网格背景
        └── 所有激活的GameObject
```

### 核心类职责

**GameEngine**
- 管理游戏主循环（60 FPS渲染）
- 场景切换（菜单→游戏→回放）
- 窗口管理（800×600）

**GameObject**
- instanceId（全局唯一，从1递增）
- 组件容器（List<Component>）
- 生命周期（active标志）

**RecordingService**
- 异步写入（BlockingQueue + 后台线程）
- 关键帧采样（0.1秒间隔）
- warmup机制（跳过前0.1秒空帧）

**ReplayScene**
- JSONL解析（RecordingJson工具类）
- 对象池（Map<String,GameObject>）
- 插值算法（线性插值位置）









