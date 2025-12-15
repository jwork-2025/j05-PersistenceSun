#!/bin/bash
# 启动客户端（连接到服务器）

cd "$(dirname "$0")"

HOST=${1:-localhost}
PORT=${2:-7777}

echo "==================================="
echo "  启动客户端（葫芦娃大战妖精）"
echo "==================================="
echo ""
echo "连接到: $HOST:$PORT"
echo ""

# 编译项目
bash compile.sh > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi

echo "启动客户端..."
echo ""

java -cp build/classes com.gameengine.example.ClientLauncher $HOST $PORT
