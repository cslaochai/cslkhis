package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.ai.dto.AiAuditLogQueryPageDTO;
import com.his.ai.entity.SysAiCallLog;
import com.his.ai.enums.AiCallStatusEnum;
import com.his.ai.mapper.SysAiCallLogMapper;
import com.his.ai.service.AiAuditQueryService;
import com.his.ai.vo.AiAuditLogVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 调用审计查询。
 * <p>
 * 只读服务：审计日志的写入由 {@link AiAuditService} 负责，
 * 且写入必须是「永不抛异常」的旁路 —— 审计失败不能影响业务。
 * 查出来给人看，所以状态码要翻译成中文，别把 1/2/3 直接甩给医务科。
 */
@Service
@RequiredArgsConstructor
public class AiAuditQueryServiceImpl extends ServiceImpl<SysAiCallLogMapper, SysAiCallLog> implements AiAuditQueryService {

    private final SysAiCallLogMapper sysAiCallLogMapper;

    private static AiAuditLogVO toVO(SysAiCallLog entity) {
        AiAuditLogVO vo = new AiAuditLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusText(AiCallStatusEnum.getText(entity.getStatus()));
        return vo;
    }

    /**
     * 取当日 00:00:00；入参为空时返回 null。
     * <p>
     * <b>这里踩过一个坑</b>：MyBatis-Plus 的 {@code ge(condition, column, value)} 会
     * <b>先求值 value 再判断 condition</b>，所以 {@code ge(hasText(date), ..., startOfDay(date))}
     * 在 date 为 null 时依然会执行 startOfDay(null) 并抛异常 ——
     * 空条件查询直接变成 500。凡是这种「带 condition 的查询方法」，参数构造函数必须自己判空。
     */
    private static LocalDateTime startOfDay(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return parseDate(date).atStartOfDay();
    }

    private static LocalDateTime endOfDay(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return parseDate(date).atTime(LocalTime.MAX);
    }

    private static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception ex) {
            // 非空但格式不对时明确报错，不要静默兜底成「今天」——
            // 那会让使用者以为筛选生效了，实际查的却是另一段时间的数据，比报错更糟。
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd：" + date);
        }
    }

    public PageResult<AiAuditLogVO> listPage(AiAuditLogQueryPageDTO dto) {
        LambdaQueryWrapper<SysAiCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getCapabilityKey()), SysAiCallLog::getCapabilityKey, dto.getCapabilityKey())
                .eq(dto.getStatus() != null, SysAiCallLog::getStatus, dto.getStatus())
                .eq(StringUtils.hasText(dto.getBizType()), SysAiCallLog::getBizType, dto.getBizType())
                .eq(dto.getBizId() != null, SysAiCallLog::getBizId, dto.getBizId())
                .like(StringUtils.hasText(dto.getOperator()), SysAiCallLog::getOperator, dto.getOperator())
                .ge(StringUtils.hasText(dto.getStartDate()), SysAiCallLog::getCreateTime, startOfDay(dto.getStartDate()))
                .le(StringUtils.hasText(dto.getEndDate()), SysAiCallLog::getCreateTime, endOfDay(dto.getEndDate()))
                .orderByDesc(SysAiCallLog::getCreateTime);

        Page<SysAiCallLog> page = sysAiCallLogMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<AiAuditLogVO> records = page.getRecords().stream().map(AiAuditQueryServiceImpl::toVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }
}
