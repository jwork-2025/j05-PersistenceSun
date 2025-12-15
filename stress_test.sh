#!/bin/bash

# 压力测试脚本
# 用法：./stress_test.sh [客户端数量] [启动间隔ms]

CLIENTS=${1:-10}
DELAY=${2:-100}

echo "=========================================="
echo "  启动网络压力测试"
echo "=========================================="
echo "客户端数量: $CLIENTS"
echo "启动间隔: ${DELAY}ms"
echo "=========================================="
echo ""
echo "提示: 先在另一个终端运行服务器："
echo "  ./run.sh"
echo ""
echo "按 Ctrl+C 停止测试"
echo ""

java -cp build/classes com.gameengine.example.StressTest -n $CLIENTS -d $DELAY
