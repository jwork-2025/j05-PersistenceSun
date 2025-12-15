package com.gameengine.example;

import com.gameengine.core.GameEngine;
import com.gameengine.net.NioClient;

/**
 * 客户端启动器 - 连接到服务器并观看游戏
 */
public class ClientLauncher {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 7777;
        
        // 如果提供了命令行参数，使用自定义服务器地址
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("无效的端口号，使用默认端口 7777");
            }
        }
        
        System.out.println("======================");
        System.out.println("葫芦娃大战妖精 - 客户端");
        System.out.println("======================");
        System.out.println("连接到: " + host + ":" + port);
        
        // 创建NIO客户端并连接
        NioClient client = new NioClient();
        if (!client.connect(host, port)) {
            System.err.println("✗ 连接失败！");
            System.err.println("  请确保服务器已启动");
            return;
        }
        
        System.out.println("✓ 已连接到服务器");
        
        // 发送JOIN消息
        if (!client.join("Client_" + System.currentTimeMillis())) {
            System.err.println("✗ 加入失败！");
            return;
        }
        
        System.out.println("✓ 已加入游戏");
        
        // 启动接收循环（在后台线程接收服务器广播）
        client.startReceiveLoop();
        System.out.println("✓ 开始接收游戏状态");
        
        try {
            // 创建游戏引擎并运行客户端场景
            GameEngine engine = new GameEngine(800, 600, "葫芦娃大战妖精 [客户端]");
            ClientScene clientScene = new ClientScene(engine);
            engine.setScene(clientScene);
            
            System.out.println("✓ 客户端场景已启动");
            System.out.println();
            System.out.println("现在可以观看服务器游戏画面！");
            System.out.println();
            
            engine.run();
        } catch (Exception e) {
            System.err.println("客户端启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
