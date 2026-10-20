package com.his.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * MQ 发送统一封装。
 * <p>
 * 所有业务模块发消息都走这里，统一：
 * <ul>
 *   <li>发送失败只打日志、不抛异常（MQ 是旁路，不能反过来影响主业务）；</li>
 *   <li>按业务键做哈希选择，保证同一患者/账单消息落到同一队列（顺序消费的基础）；</li>
 *   <li>日志可追溯。</li>
 * </ul>
 * 注意：事务内调用发送方法是安全的——这里不参与事务提交，
 * 真正的「事务提交后再发」由调用方用 {@code TransactionSynchronizationManager} 控制。
 * <p>
 * 操作人（createById/createBy）由调用方在事件对象上显式 set，publisher 不自动注入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HisMqPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送一个事件，默认 Tag=default。
     */
    public void send(String topic, Object payload) {
        send(topic, "default", payload, null);
    }

    /**
     * 同步发送一个事件，带 Tag。
     */
    public void send(String topic, String tag, Object payload) {
        send(topic, tag, payload, null);
    }

    /**
     * 同步发送一个事件，带 Tag 与哈希键。
     *
     * @param hashKey 相同 hashKey 的消息会落到同一 queue；为 null 时轮询
     */
    public void send(String topic, String tag, Object payload, String hashKey) {
        String destination = topic + ":" + tag;
        try {
            var builder = MessageBuilder.withPayload(payload);
            if (hashKey != null && !hashKey.isEmpty()) {
                builder.setHeader(RocketMQHeaders.KEYS, hashKey);
            }
            rocketMQTemplate.syncSend(destination, builder.build());
            log.debug("[MQ] send topic={} tag={} hashKey={} payload={}", topic, tag, hashKey,
                    payload.getClass().getSimpleName());
        } catch (Exception e) {
            // MQ 挂了不能让主业务失败——但要打 error 级日志，便于排查漏发
            log.error("[MQ] send FAILED topic={} tag={} hashKey={}", topic, tag, hashKey, e);
        }
    }

    /**
     * 发送顺序消息（同一 hashKey 严格 FIFO）。
     */
    public void sendOrderly(String topic, String tag, Object payload, String hashKey) {
        String destination = topic + ":" + tag;
        try {
            rocketMQTemplate.syncSendOrderly(destination,
                    MessageBuilder.withPayload(payload).build(),
                    hashKey == null ? "default" : hashKey);
            log.debug("[MQ] orderly send topic={} tag={} hashKey={}", topic, tag, hashKey);
        } catch (Exception e) {
            log.error("[MQ] orderly send FAILED topic={} tag={} hashKey={}", topic, tag, hashKey, e);
        }
    }
}
