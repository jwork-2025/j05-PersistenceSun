#!/bin/bash
# 网络联机测试脚本 - 自动启动服务器和多个客户端

echo "=================================="
echo "  网络联机测试"
echo "=================================="
echo ""
echo "即将启动："
echo "  - 1个服务器窗口"
echo "  - 2个客户端窗口"
echo ""
echo "请确保已安装并可以使用以下命令打开新终端："
echo "  - Windows: start (cmd)"
echo "  - Git Bash: 手动打开新窗口"
echo ""
read -p "按Enter继续..."

# 检测操作系统
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    echo "检测到Windows系统"
    echo ""
    echo "请手动操作："
    echo "1. 当前窗口运行服务器（按Enter后自动启动）"
    echo "2. 打开新的Git Bash窗口，运行: cd /f/NJU/JAVA/j03-PersistenceSun-main && ./run_client.sh"
    echo "3. 再打开一个Git Bash窗口，运行: cd /f/NJU/JAVA/j03-PersistenceSun-main && ./run_client.sh"
    echo ""
    read -p "准备好后按Enter启动服务器..."
    ./run.sh
else
    # Linux/Mac可以用xterm或gnome-terminal
    echo "启动服务器..."
    ./run.sh &
    sleep 2
    
    echo "启动客户端1..."
    xterm -e "./run_client.sh" &
    sleep 1
    
    echo "启动客户端2..."
    xterm -e "./run_client.sh" &
fi
