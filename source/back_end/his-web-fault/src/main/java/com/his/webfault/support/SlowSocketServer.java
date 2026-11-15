package com.his.webfault.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 一个「只收连接、永不响应」的假下游（127.0.0.1:18091）。
 * <p>
 * 为什么要单独起一个 socket 服务：如果下游还是本进程的 Tomcat 接口，
 * 入站线程会去抢同一个线程池，线程栈里看到的是「等线程池」而不是「等下游」，
 * 与线上现象不一致。这里用裸 socket 挂住连接，jstack 才能看到
 * {@code sun.nio.ch.SocketDispatcher.read0} —— 医保前置机/CA/支付网关超时的真实形态。
 */
@Component
public class SlowSocketServer {

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private final List<Socket> held = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void start() {
        try {
            serverSocket = new ServerSocket(18091, 1024, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            // 端口被占（比如上次演练进程没退干净）时不阻断应用启动，线程场景退化为快速失败
            System.out.println("[fault-lab] 18091 不可用，线程耗尽场景将快速失败: " + e.getMessage());
            return;
        }
        acceptThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    held.add(socket);
                    // 故意什么都不写：调用方 read() 会一直阻塞，直到它自己的 soTimeout
                } catch (IOException e) {
                    // 瞬时异常（如连接在 accept 前被重置）不能让 accept 循环退出，
                    // 否则后续连接全部被拒、调用方立刻失败——线上「下游越慢、报错越快」就是这个原因
                    if (serverSocket.isClosed()) {
                        return;
                    }
                    System.out.println("[fault-lab] accept retry: " + e.getMessage());
                }
            }
        }, "fault-slow-downstream");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    @PreDestroy
    public void stop() {
        if (serverSocket == null) {
            return;
        }
        held.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException ignored) {
                // 演练进程退出，忽略
            }
        });
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
            // 演练进程退出，忽略
        }
    }

    public int heldConnections() {
        return held.size();
    }
}
