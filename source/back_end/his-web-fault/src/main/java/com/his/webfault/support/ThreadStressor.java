package com.his.webfault.support;

import com.his.webfault.config.FaultLabProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 线程耗尽：真实成因不是「线程池配小了」，而是某个下游变慢后，
 * Tomcat 的 maxThreads（默认 200）被同步调用一个个占住，新请求全排在 Accept 队列里。
 * <p>
 * 这里的下游是一条只收不发的 socket（127.0.0.1:18091），调用方 read() 一直阻塞，
 * jstack 里能抓到 {@code sun.nio.ch.SocketDispatcher.read0}，与线上抓到的线程栈一致。
 */
@Component
public class ThreadStressor {

    private static final int DOWNSTREAM_PORT = 18091;

    private final FaultLabProperties props;
    private final FaultExecutor executor;
    private final SlowSocketServer slowSocketServer;
    private final HttpClient client;
    private final String baseUrl;

    public ThreadStressor(FaultLabProperties props, FaultExecutor executor, SlowSocketServer slowSocketServer,
                          @Value("${server.port:18090}") int port) {
        this.props = props;
        this.executor = executor;
        this.slowSocketServer = slowSocketServer;
        this.baseUrl = "http://127.0.0.1:" + port;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    /** 慢接口本体：占住一个 Tomcat worker（纯 sleep，用于单请求演示） */
    public String slow(int delayMs) {
        try {
            Thread.sleep(Math.max(0, delayMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "SLEPT " + delayMs + "ms on " + Thread.currentThread().getName();
    }

    /** 当前请求线程同步等下游：卡在 socketRead 上 */
    public String downstream(int delayMs) {
        readFromSlowDownstream(delayMs);
        return "DOWNSTREAM TIMEOUT after " + delayMs + "ms";
    }

    /** 一次点火：并发发起 N 个入站请求，把 worker 占满 */
    public String load(int requests, int delayMs) {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        int count = Math.max(1, Math.min(props.getMaxConcurrency(), requests));
        for (int i = 0; i < count; i++) {
            executor.run("fault-load-" + i, () -> postToSlowEndpoint(delayMs));
        }
        return "STARTED: " + count + " inbound requests to /fault/thread/downstream, each blocks "
                + delayMs + "ms on 127.0.0.1:" + DOWNSTREAM_PORT;
    }

    /**
     * 入站请求（Tomcat worker）里的同步下游调用：连上假下游后一直读，服务端不写任何字节，
     * read() 阻塞到 soTimeout —— 线程栈就是 socketRead。
     */
    private void readFromSlowDownstream(int delayMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", DOWNSTREAM_PORT), 3000);
            socket.setSoTimeout(Math.max(1000, delayMs));
            InputStream input = socket.getInputStream();
            input.read();
        } catch (SocketTimeoutException e) {
            // 正常路径：下游超时，业务线程带着超时异常返回
        } catch (Exception e) {
            System.out.println("[fault-lab] downstream failed: " + e.getClass().getSimpleName());
        }
    }

    private void postToSlowEndpoint(int delayMs) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/fault/thread/downstream?delayMs=" + delayMs))
                    .timeout(Duration.ofSeconds(Math.max(10, delayMs / 1000 + 10)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("[fault-lab] inbound call failed: " + e.getClass().getSimpleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
