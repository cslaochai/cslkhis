package com.his.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysAuditLog;
import com.his.system.mapper.SysAuditLogMapper;
import com.his.system.service.SysAuditLogService;
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
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements SysAuditLogService {

    private final SysAuditLogMapper sysAuditLogMapper;

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

    /**
     * 记一条审计。content/errorMsg 由调用方负责先做 PII 打码，这里只保证列宽截断。
     */
    public void record(Long userId, String userName, String module, String operation,
                       String targetType, Object targetId, String content,
                       boolean success, String errorMsg) {
        try {
            SysAuditLog row = new SysAuditLog();
            row.setUserId(userId);
            row.setUserName(TextUtil.cut(userName, 64));
            row.setModule(TextUtil.cut(module, 50));
            row.setOperation(TextUtil.cut(operation, 50));
            row.setTargetType(TextUtil.cut(targetType, 50));
            row.setTargetId(TextUtil.cut(targetId == null ? null : String.valueOf(targetId), 50));
            row.setContent(content);
            row.setIp(TextUtil.cut(currentIp(), 50));
            row.setStatus(success ? 1 : 0);
            row.setErrorMsg(TextUtil.cut(errorMsg, 500));
            row.setCreateBy(TextUtil.cut(userName, 64));
            sysAuditLogMapper.insert(row);
        } catch (Exception e) {
            log.error("审计日志写入失败 module={} operation={} userId={}", module, operation, userId, e);
        }
    }

}
