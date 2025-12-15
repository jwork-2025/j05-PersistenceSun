package com.gameengine.example;

import com.gameengine.net.NioClient;
import com.gameengine.net.NetworkBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网络性能压力测试工具
 * 用于模拟大量客户端同时连接，测试服务器性能
 */
public class StressTest {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    
    // 统计信息
    private static final AtomicInteger connectedCount = new AtomicInteger(0);
    private static final AtomicInteger failedCount = new AtomicInteger(0);
    private static final AtomicInteger totalFramesReceived = new AtomicInteger(0);
    
    /**
     * 模拟客户端线程
     */
    static class SimulatedClient implements Runnable {
        private final int clientId;
        private final boolean verbose;
        
        public SimulatedClient(int clientId, boolean verbose) {
            this.clientId = clientId;
            this.verbose = verbose;
        }
        
        @Override
        public void run() {
            NioClient client = null;
            try {
                // 连接到服务器
                client = new NioClient();
                boolean connected = client.connect(SERVER_HOST, SERVER_PORT);
                
                if (!connected) {
                    if (verbose) {
                        System.err.println("[Client-" + clientId + "] 连接失败");
                    }
                    failedCount.incrementAndGet();
                    return;
                }
                
                boolean joined = client.join("StressClient" + clientId);
                
                if (!joined) {
                    if (verbose) {
                        System.err.println("[Client-" + clientId + "] 加入失败");
                    }
                    failedCount.incrementAndGet();
                    return;
                }
                
                connectedCount.incrementAndGet();
                if (verbose) {
                    System.out.println("[Client-" + clientId + "] 已连接");
                }
                
                // 持续接收数据
                long startTime = System.currentTimeMillis();
                int frameCount = 0;
                
                while (true) {
                    NetworkBuffer.Keyframe kf = NetworkBuffer.sampleKeyframe();
                    if (kf != null) {
                        frameCount++;
                        totalFramesReceived.incrementAndGet();
                        
                        // 每100帧输出一次统计（可选）
                        if (verbose && frameCount % 100 == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            double fps = (frameCount * 1000.0) / elapsed;
                            System.out.printf("[Client-%d] 已接收 %d 帧, FPS: %.2f, 实体数: %d%n",
                                    clientId, frameCount, fps, kf.entities.size());
                        }
                    }
                    
                    // 短暂休眠，避免占用过多CPU
                    Thread.sleep(10);
                }
                
            } catch (Exception e) {
                if (verbose) {
                    System.err.println("[Client-" + clientId + "] 异常: " + e.getMessage());
                }
                failedCount.incrementAndGet();
            }
            // NioClient没有close方法，连接会在线程结束时自动关闭
        }
    }
    
    /**
     * 监控线程，定期输出统计信息
     */
    static class MonitorThread implements Runnable {
        private final int totalClients;
        private final long startTime;
        
        public MonitorThread(int totalClients) {
            this.totalClients = totalClients;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(5000); // 每5秒输出一次
                    
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    int connected = connectedCount.get();
                    int failed = failedCount.get();
                    int totalFrames = totalFramesReceived.get();
                    
                    System.out.println("\n========== 压力测试统计 (运行时间: " + elapsed + "秒) ==========");
                    System.out.println("目标客户端数: " + totalClients);
                    System.out.println("成功连接: " + connected);
                    System.out.println("连接失败: " + failed);
                    System.out.println("总接收帧数: " + totalFrames);
                    if (connected > 0) {
                        System.out.println("平均每客户端帧数: " + (totalFrames / connected));
                    }
                    System.out.println("=======================================================\n");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void main(String[] args) {
        // 解析命令行参数
        int numClients = 10; // 默认10个客户端
        boolean verbose = false;
        int delayMs = 100; // 客户端启动间隔（毫秒）
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                case "--clients":
                    if (i + 1 < args.length) {
                        numClients = Integer.parseInt(args[++i]);
                    }
                    break;
                case "-v":
                case "--verbose":
                    verbose = true;
                    break;
                case "-d":
                case "--delay":
                    if (i + 1 < args.length) {
                        delayMs = Integer.parseInt(args[++i]);
                    }
                    break;
                case "-h":
                case "--help":
                    printUsage();
                    return;
            }
        }
        
        System.out.println("========================================");
        System.out.println("  网络性能压力测试工具");
        System.out.println("========================================");
        System.out.println("服务器地址: " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("客户端数量: " + numClients);
        System.out.println("启动间隔: " + delayMs + "ms");
        System.out.println("详细输出: " + (verbose ? "开启" : "关闭"));
        System.out.println("========================================\n");
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(numClients + 1);
        
        // 启动监控线程
        executor.submit(new MonitorThread(numClients));
        
        // 逐个启动客户端
        List<SimulatedClient> clients = new ArrayList<>();
        for (int i = 0; i < numClients; i++) {
            SimulatedClient client = new SimulatedClient(i + 1, verbose);
            clients.add(client);
            executor.submit(client);
            
            // 间隔一段时间再启动下一个客户端
            if (delayMs > 0 && i < numClients - 1) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        System.out.println("\n所有客户端已启动，正在连接...");
        System.out.println("按 Ctrl+C 停止测试\n");
        
        // 主线程等待（直到被中断）
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("\n测试结束");
            executor.shutdownNow();
        }
    }
    
    private static void printUsage() {
        System.out.println("用法: java -cp build/classes com.gameengine.example.StressTest [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -n, --clients <数量>   模拟客户端数量 (默认: 10)");
        System.out.println("  -d, --delay <毫秒>     客户端启动间隔 (默认: 100ms)");
        System.out.println("  -v, --verbose          显示详细输出");
        System.out.println("  -h, --help             显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  # 模拟50个客户端");
        System.out.println("  java -cp build/classes com.gameengine.example.StressTest -n 50");
        System.out.println();
        System.out.println("  # 模拟100个客户端，启动间隔50ms，显示详细输出");
        System.out.println("  java -cp build/classes com.gameengine.example.StressTest -n 100 -d 50 -v");
    }
}
