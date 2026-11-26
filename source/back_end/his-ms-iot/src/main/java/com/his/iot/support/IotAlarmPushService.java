package com.his.iot.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 设备告警 SSE 推送（按科室广播）。
 * <p>
 * 与站内信按「收件人」推不同，病区监护大屏是「一个科室一个订阅方」：护士站开几台屏幕都该收到本科室的告警，
 * 但没有「谁收谁不收」的业务概念，所以注册维度用 deptId。
 * <p>
 * 告警内容含患者床号，属于本科室数据：订阅时 deptId 必须过数据权限（见 IotEventServiceImpl.subscribeAlarm），
 * 不允许前端传个别的科室就推送。
 */
@Slf4j
@Component
public class IotAlarmPushService {

    /**
     * 0L = 不超时：连接生命周期由心跳与显式清理管理（护士站大屏可能挂一整个班）
     */
    private static final long TIMEOUT_MILLIS = 0L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 注册一个科室告警连接。必须在请求线程调用（deptId 已在 service 层过完权限）。
     */
    public SseEmitter register(Long deptId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        CopyOnWriteArrayList<SseEmitter> list = emitters.computeIfAbsent(deptId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);
        Runnable cleanup = () -> list.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        try {
            // 注释帧：让前端 fetch 流尽早收到字节，确认连接已建立
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            cleanup.run();
        }
        return emitter;
    }

    /**
     * 向该科室的全部在线连接广播一帧告警。
     * 单连接失败只摘除该连接，不影响其他屏幕。
     */
    public void pushAlarm(Long deptId, Object data) {
        if (deptId == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(deptId);
        if (list == null || list.isEmpty()) {
            return; // 没有大屏在线是常态，不是错误
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                list.remove(emitter);
            }
        }
        if (list.isEmpty()) {
            emitters.remove(deptId, list);
        }
    }

    /**
     * 心跳：30 秒一次注释帧，防代理/浏览器静默掐掉空闲连接。
     */
    @Scheduled(fixedDelay = 30_000)
    public void heartbeat() {
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            if (entry.getValue().isEmpty()) {
                emitters.remove(entry.getKey(), entry.getValue());
                continue;
            }
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
        return emitters.values().stream().mapToInt(list -> list.size()).sum();
    }
}
