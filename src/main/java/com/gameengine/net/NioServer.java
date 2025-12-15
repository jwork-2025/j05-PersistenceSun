package com.gameengine.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NioServer implements Runnable {
    private final int port;
    private volatile boolean running = true;
    private Thread thread;

    public NioServer(int port) { this.port = port; }

    public void start() {
        if (thread != null) return;
        thread = new Thread(this, "nio-server");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() { running = false; if (thread!=null) thread.interrupt(); }

    @Override public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(port));
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buf = ByteBuffer.allocate(1024);
            List<SocketChannel> conns = new ArrayList<>();
            long lastBroadcast = System.currentTimeMillis();
            while (running) {
                selector.select(250);
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next(); it.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) {
                        SocketChannel ch = ssc.accept();
                        if (ch != null) {
                            ch.configureBlocking(false);
                            ch.register(selector, SelectionKey.OP_READ);
                            NetState.clientConnected();
                            conns.add(ch);
                        }
                    } else if (key.isReadable()) {
                        SocketChannel ch = (SocketChannel) key.channel();
                        buf.clear();
                        int n = ch.read(buf);
                        if (n <= 0) { 
                            key.cancel(); 
                            ch.close(); 
                            conns.remove(ch); 
                            NetState.clientDisconnected();
                            continue; 
                        }
                        buf.flip();
                        String s = new String(buf.array(), 0, buf.limit());
                        
                        // 处理JOIN消息
                        if (s.contains("JOIN:")) {
                            System.out.println("收到JOIN请求: " + s.trim());
                            ByteBuffer out = ByteBuffer.wrap("JOIN-ACK\n".getBytes());
                            while (out.hasRemaining()) ch.write(out);
                            System.out.println("已发送JOIN-ACK");
                        } 
                        // 处理INPUT消息
                        if (s.contains("INPUT:")) {
                            try {
                                int idx = s.indexOf("INPUT:");
                                String payload = s.substring(idx + 6).trim();
                                int newline = payload.indexOf('\n');
                                if (newline > 0) payload = payload.substring(0, newline);
                                String[] kv = payload.split(",");
                                if (kv.length >= 2) {
                                    float vx = Float.parseFloat(kv[0]);
                                    float vy = Float.parseFloat(kv[1]);
                                    NetState.setP2Velocity(vx, vy);
                                }
                            } catch (Exception e) {
                                // 忽略解析错误
                            }
                        }
                    }
                }
                long now = System.currentTimeMillis();
                if (now - lastBroadcast >= 50) {
                    lastBroadcast = now;
                    String json = NetState.getLastKeyframeJson();
                    if (json != null && !json.isEmpty()) {
                        ByteBuffer out = ByteBuffer.wrap((json + "\n").getBytes());
                        for (int i = conns.size() - 1; i >= 0; i--) {
                            SocketChannel ch = conns.get(i);
                            if (!ch.isOpen()) { conns.remove(i); continue; }
                            out.rewind();
                            try { while (out.hasRemaining()) ch.write(out); } catch (IOException ignored) {}
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
