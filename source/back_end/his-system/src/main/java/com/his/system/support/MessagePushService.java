package com.his.system.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 站内信 SSE 实时推送。
 */
@Slf4j
@Component
public class MessagePushService {

    /**
     * 0L = 不超时，连接生命周期由心跳与显式清理管理
     */
    private static final long TIMEOUT_MILLIS = 0L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 注册一个 SSE 连接。必须在请求线程调用（当前用户在 Controller 解出）。
     */
    public SseEmitter register(Long receiverId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        CopyOnWriteArrayList<SseEmitter> list = emitters.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);
        Runnable cleanup = () -> list.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        // 立即回一条注释帧：前端 fetch 流尽早收到字节，确认连接建立
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            cleanup.run();
        }
        return emitter;
    }

    /**
     * 新消息落库后推送：带最新未读数与消息摘要，前端直接更新角标并弹提醒。
     *
     * @param unread 该收件人最新未读数（调用方查询，本类不反查）
     */
    public void pushNewMessage(Long receiverId, String title, String bizType, String severity, int unread) {
        if (receiverId == null) {
            return;
        }
        push(receiverId, Map.of(
                "type", "new-message",
                "title", title == null ? "" : title,
                "bizType", bizType == null ? "" : bizType,
                "severity", severity == null ? "info" : severity,
                "unread", unread));
    }

    /**
     * 广播一条 data 帧（JSON）到该收件人的全部在线连接。
     * 单连接失败只摘除该连接，不影响其他连接与其他收件人。
     */
    public void push(Long receiverId, Object data) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(receiverId);
        if (list == null || list.isEmpty()) {
            return; // 不在线是常态，不是错误
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                list.remove(emitter);
            }
        }
        if (list.isEmpty()) {
            emitters.remove(receiverId, list);
        }
    }

    /**
     * 心跳：30 秒一次注释帧，防代理/浏览器静默掐掉空闲连接
     */
    @Scheduled(fixedDelay = 30_000)
    public void heartbeat() {
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (Exception e) {
                    entry.getValue().remove(emitter);
                }
            }
        }
    }

    /**
     * 供运维观察在线连接数
     */
    public int onlineCount() {
        return emitters.values().stream().mapToInt(List::size).sum();
    }
}
