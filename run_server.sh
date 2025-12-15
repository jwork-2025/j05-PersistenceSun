#!/bin/bash
# 启动服务器（运行游戏）

cd "$(dirname "$0")"

echo "==================================="
echo "  启动服务器（葫芦娃大战妖精）"
echo "==================================="
echo ""

# 编译项目
bash compile.sh

if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi

echo ""
echo "启动游戏服务器..."
echo "客户端可以运行 ./run_client.sh 连接"
echo ""

java -cp build/classes com.gameengine.example.GameExample
