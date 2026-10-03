package com.his.system.service.impl;

import com.his.system.service.SysAuditLogService;
import com.his.system.entity.SysAuditLog;
import com.his.system.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作审计日志落库服务（审计日志）。
 *
 * <p><b>旁路写入：</b>审计失败只记 error 日志，绝不让业务操作跟着失败 ——
 * 但也不静默吞，出问题时日志里能看出来。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAuditLogServiceImpl implements SysAuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    /**
     * 记一条审计。content/errorMsg 由调用方负责先做 PII 打码，这里只保证列宽截断。
     */
    public void record(Long userId, String userName, String module, String operation,
                       String targetType, Object targetId, String content,
                       boolean success, String errorMsg) {
        try {
            SysAuditLog row = new SysAuditLog();
            row.setUserId(userId);
            row.setUserName(cut(userName, 64));
            row.setModule(cut(module, 50));
            row.setOperation(cut(operation, 50));
            row.setTargetType(cut(targetType, 50));
            row.setTargetId(cut(targetId == null ? null : String.valueOf(targetId), 50));
            row.setContent(content);
            row.setIp(cut(currentIp(), 50));
            row.setStatus(success ? 1 : 0);
            row.setErrorMsg(cut(errorMsg, 500));
            row.setCreateBy(cut(userName, 64));
            auditLogMapper.insert(row);
        } catch (Exception e) {
            log.error("审计日志写入失败 module={} operation={} userId={}", module, operation, userId, e);
        }
    }

    private static String currentIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        return StringUtils.hasText(real) ? real : request.getRemoteAddr();
    }

    private static String cut(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }
}
