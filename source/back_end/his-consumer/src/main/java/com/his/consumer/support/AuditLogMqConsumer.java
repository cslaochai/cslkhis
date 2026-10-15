package com.his.consumer.support;

import com.his.common.mq.HisMqTopics;
import com.his.common.mq.event.AuditLogEvent;
import com.his.system.entity.SysAuditLog;
import com.his.system.mapper.SysAuditLogMapper;
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
 * 对 createById/createBy 会拿到 null 直接 return。所以这里必须从事件里取
 * 生产者侧拍好的快照手动 set 进去，不能靠自动填充。
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

    private final SysAuditLogMapper sysAuditLogMapper;

    @Override
    public void onMessage(AuditLogEvent event) {
        try {
            SysAuditLog row = new SysAuditLog();
            row.setUserId(event.getUserId());
            row.setUserName(event.getUserName());
            row.setModule(event.getModule());
            row.setOperation(event.getOperation());
            row.setTargetType(event.getTargetType());
            row.setTargetId(event.getTargetId());
            row.setContent(event.getContent());
            row.setIp(event.getIp());
            row.setStatus(event.getStatus());
            row.setErrorMsg(event.getErrorMsg());
            row.setCreateById(event.getCreateById());
            row.setCreateBy(event.getCreateBy());
            row.setUpdateById(event.getCreateById());
            row.setUpdateBy(event.getCreateBy());
            sysAuditLogMapper.insert(row);
        } catch (Exception e) {
            log.error("[审计] 落库失败 event={}", event, e);
            throw new RuntimeException("审计日志落库失败，等待 MQ 重试", e);
        }
    }
}
