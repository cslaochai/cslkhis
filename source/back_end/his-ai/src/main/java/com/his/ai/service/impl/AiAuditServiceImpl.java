package com.his.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.ai.entity.SysAiCallLog;
import com.his.ai.mapper.SysAiCallLogMapper;
import com.his.ai.service.AiAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * AI 调用审计服务。
 * <p>
 * 关键约定：<b>写审计日志失败绝不能影响主流程</b>。
 * 审计是「事后追溯」的手段，不是「事前拦截」的手段 —— 让它把正常业务搞挂是本末倒置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAuditServiceImpl extends ServiceImpl<SysAiCallLogMapper, SysAiCallLog> implements AiAuditService {

    private final SysAiCallLogMapper sysAiCallLogMapper;

    public void record(SysAiCallLog entity) {
        try {
            if (entity == null) {
                return;
            }
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(LocalDateTime.now());
            }
            if (!StringUtils.hasText(entity.getCreateBy())) {
                if (!StringUtils.hasText(entity.getOperator())) {
                    // 调用方没给 operator 就等于「这次 AI 调用不知道谁触发的」，
                    // 塞system 等于伪造审计痕迹；宁可丢这条日志也要让调用方补上
                    log.warn("[AI] 审计日志缺少 operator（capabilityKey={}），本次不落库", entity.getCapabilityKey());
                    return;
                }
                entity.setCreateBy(entity.getOperator());
            }
            sysAiCallLogMapper.insert(entity);
        } catch (Exception ex) {
            log.error("[AI] 写审计日志失败，已忽略（不影响业务）：{}", ex.getMessage());
        }
    }
}
