#!/bin/bash

# Actor模型性能对比测试脚本

echo "=========================================="
echo "  Actor模型性能对比测试"
echo "=========================================="
echo ""
echo "此测试将对比两种模式的性能："
echo "  1. 原始版本（单线程处理）"
echo "  2. Actor优化版（多Actor并发处理）"
echo ""
echo "测试指标："
echo "  - CPU使用率"
echo "  - 内存占用"
echo "  - 消息处理延迟"
echo "  - 网络广播吞吐量"
echo""
echo "=========================================="
echo ""

read -p "按Enter开始测试..."

echo ""
echo "[1/2] 启动压力测试（200个客户端）..."
echo "请在另一终端运行服务器，然后按Enter继续"
read

./stress_test.sh 200 20

