#!/bin/bash

# 简单编译脚本
echo "编译游戏引擎..."

# 创建输出目录
mkdir -p build/classes

# 编译所有Java文件
javac -d build/classes \
    -cp . \
    src/main/java/com/gameengine/math/Vector2.java \
    src/main/java/com/gameengine/input/InputManager.java \
    src/main/java/com/gameengine/core/Component.java \
    src/main/java/com/gameengine/core/GameObject.java \
    src/main/java/com/gameengine/components/TransformComponent.java \
    src/main/java/com/gameengine/components/PhysicsComponent.java \
    src/main/java/com/gameengine/components/RenderComponent.java \
    src/main/java/com/gameengine/components/HealthComponent.java \
    src/main/java/com/gameengine/components/TeamComponent.java \
    src/main/java/com/gameengine/components/CombatComponent.java \
    src/main/java/com/gameengine/components/MovementComponent.java \
    src/main/java/com/gameengine/components/ProjectileComponent.java \
    src/main/java/com/gameengine/graphics/Renderer.java \
    src/main/java/com/gameengine/scene/GridSystem.java \
    src/main/java/com/gameengine/scene/Scene.java \
    src/main/java/com/gameengine/recording/RecordingStorage.java \
    src/main/java/com/gameengine/recording/FileRecordingStorage.java \
    src/main/java/com/gameengine/recording/RecordingConfig.java \
    src/main/java/com/gameengine/recording/RecordingJson.java \
    src/main/java/com/gameengine/recording/RecordingService.java \
    src/main/java/com/gameengine/net/NetState.java \
    src/main/java/com/gameengine/net/NioServer.java \
    src/main/java/com/gameengine/net/NioClient.java \
    src/main/java/com/gameengine/net/NetworkBuffer.java \
    src/main/java/com/gameengine/entities/EntityFactory.java \
    src/main/java/com/gameengine/core/GameEngine.java \
    src/main/java/com/gameengine/core/GameLogic.java \
    src/main/java/com/gameengine/actor/Message.java \
    src/main/java/com/gameengine/actor/Actor.java \
    src/main/java/com/gameengine/actor/ActorSystem.java \
    src/main/java/com/gameengine/actor/messages/GameStateSnapshotMessage.java \
    src/main/java/com/gameengine/actor/actors/NetworkBroadcastActor.java \
    src/main/java/com/gameengine/example/ReplayScene.java \
    src/main/java/com/gameengine/example/ClientScene.java \
    src/main/java/com/gameengine/example/ClientLauncher.java \
    src/main/java/com/gameengine/example/StressTest.java \
    src/main/java/com/gameengine/example/ServerLauncher.java \
    src/main/java/com/gameengine/example/GameExample.java

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo "运行游戏: java -cp build/classes com.gameengine.example.GameExample"
else
    echo "编译失败！"
    exit 1
fi
