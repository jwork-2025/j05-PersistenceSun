package com.gameengine.example;

import com.gameengine.core.GameEngine;
import com.gameengine.net.NioServer;

/**
 * 服务器启动器 - 运行游戏服务器并接受客户端连接
 */
public class ServerLauncher {
    public static void main(String[] args) {
        System.out.println("======================");
        System.out.println("葫芦娃大战妖精 - 服务器");
        System.out.println("======================");
        
        // 启动NIO服务器（端口7777）
        NioServer server = new NioServer(7777);
        server.start();
        System.out.println("✓ 服务器已启动，监听端口: 7777");
        System.out.println("  等待客户端连接...");
        
        try {
            // 创建游戏引擎并运行游戏（直接使用GameExample的main逻辑）
            GameEngine engine = new GameEngine(800, 600, "葫芦娃大战妖精 [服务器]");
            
            // 这里需要手动创建一个场景，暂时使用简单方式
            // TODO: 将来可以提取GameExample的场景逻辑
            System.out.println("✓ 游戏引擎已启动");
            System.out.println();
            System.out.println("操作说明：");
            System.out.println("  - 请直接运行 GameExample 作为服务器");
            System.out.println("  - 此启动器仅用于演示，实际使用时在GameExample启动时会自动启动服务器");
            System.out.println();
            
            // 简单等待，不运行完整游戏
            System.out.println("服务器运行中，按Ctrl+C停止...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.err.println("服务器启动失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            server.stop();
            System.out.println("服务器已关闭");
        }
    }
}
