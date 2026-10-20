package com.his.consumer;

import com.his.common.mq.HisMqTopics;
import com.his.common.mq.event.AuditLogEvent;
import com.his.system.service.SysAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 审计日志异步落库消费者。
 * <p>
 * 单条消费（RocketMQ push 模式），每条收到就 insert；
 * 后续如果审计量大，可以改成批量消费（consumeMessageBatchMaxSize）。
 * 失败时 RocketMQ 自动重试（默认 16 次后进死信队列），不丢消息。
 * <p>
 * 注意：MQ 消费线程没有 SecurityContext，{@code MetaObjectHandler.insertFill}
 * 对 createById/createBy 会拿到 null 直接 return。所以生产者侧拍好的操作人快照
 * 由 his-system 的审计服务在落库时应用，不能靠自动填充。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = HisMqTopics.AUDIT_LOG,
        consumerGroup = HisMqTopics.CG_AUDIT_LOG,
        selectorExpression = "audit"
)
public class AuditLogMqConsumer implements RocketMQListener<AuditLogEvent> {

    private final SysAuditLogService sysAuditLogService;

    @Override
    public void onMessage(AuditLogEvent event) {
        try {
            sysAuditLogService.saveFromEvent(event);
        } catch (Exception e) {
            log.error("[审计] 落库失败 event={}", event, e);
            throw new RuntimeException("审计日志落库失败，等待 MQ 重试", e);
        }
    }
}
