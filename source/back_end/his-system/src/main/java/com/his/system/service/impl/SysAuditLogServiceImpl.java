package com.his.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.mq.HisMqPublisher;
import com.his.common.mq.HisMqTopics;
import com.his.common.mq.event.AuditLogEvent;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysAuditLog;
import com.his.system.mapper.SysAuditLogMapper;
import com.his.system.service.SysAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作审计日志落库服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements SysAuditLogService {

    private final HisMqPublisher mqPublisher;

    /**
     * 消费端落库：事件字段逐个搬到审计行
     */
    @Override
    public void saveFromEvent(AuditLogEvent event) {
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
        this.save(row);
    }

    private static String currentIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (TextUtil.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        return TextUtil.hasText(real) ? real : request.getRemoteAddr();
    }

    /**
     * 记一条审计
     */
    @Override
    public void record(Long userId, String userName, String module, String operation,
                       String targetType, Object targetId, String content,
                       boolean success, String errorMsg) {
        try {
            AuditLogEvent event = AuditLogEvent.builder()
                    .userId(userId)
                    .userName(TextUtil.cut(userName, 64))
                    .module(TextUtil.cut(module, 50))
                    .operation(TextUtil.cut(operation, 50))
                    .targetType(TextUtil.cut(targetType, 50))
                    .targetId(TextUtil.cut(targetId == null ? null : String.valueOf(targetId), 50))
                    .content(content)
                    .ip(TextUtil.cut(currentIp(), 50))
                    .status(success ? 1 : 0)
                    .errorMsg(TextUtil.cut(errorMsg, 500))
                    .createById(userId)
                    .createBy(TextUtil.cut(userName, 64))
                    .build();
            mqPublisher.send(HisMqTopics.AUDIT_LOG, "audit", event,
                    userId == null ? null : String.valueOf(userId));
        } catch (Exception e) {
            // 双保险：MQ 都发不出去时至少留日志，不能让审计动作本身把业务带崩
            log.error("审计日志事件投递失败 module={} operation={} userId={}", module, operation, userId, e);
        }
    }
}
