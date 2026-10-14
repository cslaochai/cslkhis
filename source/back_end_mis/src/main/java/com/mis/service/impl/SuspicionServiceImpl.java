package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.config.MisProperties;
import com.mis.config.UserContext;
import com.mis.dto.SuspicionHandleDTO;
import com.mis.dto.SuspicionQueryDTO;
import com.mis.engine.SuperviseRuleEngine;
import com.mis.entity.MssRecover;
import com.mis.entity.MssSuspicion;
import com.mis.mapper.MssRecoverMapper;
import com.mis.mapper.MssSuspicionMapper;
import com.mis.service.AuditRuleService;
import com.mis.service.SuspicionService;
import com.mis.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuspicionServiceImpl implements SuspicionService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MssSuspicionMapper suspicionMapper;
    private final MssRecoverMapper recoverMapper;
    private final AuditRuleService ruleService;
    private final SuperviseRuleEngine ruleEngine;
    private final MisProperties properties;

    @Override
    public PageVO<MssSuspicion> suspicionPage(SuspicionQueryDTO q) {
        Page<MssSuspicion> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssSuspicion> w = new LambdaQueryWrapper<>();
        if (hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(MssSuspicion::getSuspicionNo, kw)
                    .or().like(MssSuspicion::getPatientName, kw)
                    .or().like(MssSuspicion::getBizNo, kw));
        }
        if (hasText(q.getRuleType())) {
            w.eq(MssSuspicion::getRuleType, q.getRuleType());
        }
        if (hasText(q.getStatus())) {
            w.eq(MssSuspicion::getStatus, q.getStatus());
        }
        if (q.getBizSource() != null) {
            w.eq(MssSuspicion::getBizSource, q.getBizSource());
        }
        w.orderByDesc(MssSuspicion::getId);
        Page<MssSuspicion> res = suspicionMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public ActionVO runAudit() {
        List<SuperviseRuleEngine.Draft> drafts = ruleEngine.scan(ruleService.enabledRules());
        int inserted = 0;
        for (SuperviseRuleEngine.Draft d : drafts) {
            Long exists = suspicionMapper.selectCount(new LambdaQueryWrapper<MssSuspicion>()
                    .eq(MssSuspicion::getBizSource, d.getBizSource())
                    .eq(MssSuspicion::getBizId, d.getBizId())
                    .eq(MssSuspicion::getRuleId, d.getRuleId()));
            if (exists != null && exists > 0) {
                continue;
            }
            MssSuspicion s = new MssSuspicion();
            s.setSuspicionNo(nextNo("YZ", suspicionMapper.selectCount(new LambdaQueryWrapper<MssSuspicion>()
                    .ge(MssSuspicion::getCreateTime, LocalDate.now().atStartOfDay()))));
            s.setRuleId(d.getRuleId());
            s.setRuleName(d.getRuleName());
            s.setRuleType(d.getRuleType());
            s.setBizSource(d.getBizSource());
            s.setBizId(d.getBizId());
            s.setBizNo(d.getBizNo());
            s.setPatientName(d.getPatientName());
            s.setIdCardMask(d.getIdCardMask());
            s.setInsuranceType(d.getInsuranceType());
            s.setEncounterType(d.getEncounterType());
            s.setDept(d.getDept());
            s.setDoctor(d.getDoctor());
            s.setBizDate(d.getBizDate());
            s.setInvolvedAmount(d.getInvolvedAmount());
            s.setReason(d.getReason());
            s.setStatus("待核实");
            suspicionMapper.insert(s);
            inserted++;
        }
        return ActionVO.matched(inserted);
    }

    @Override
    public MssSuspicion getDetail(Long id) {
        return suspicionMapper.selectById(id);
    }

    @Override
    public ActionVO confirm(SuspicionHandleDTO dto) {
        MssSuspicion s = suspicionMapper.selectById(dto.getId());
        if (s == null) {
            return ActionVO.fail("疑点不存在");
        }
        if (!"待核实".equals(s.getStatus())) {
            return ActionVO.fail("该疑点已处置，不可重复确认");
        }
        BigDecimal amount = dto.getAmount() != null ? dto.getAmount() : s.getInvolvedAmount();
        s.setStatus("已确认");
        s.setConfirmAmount(amount);
        s.setHandleUser(UserContext.username());
        s.setHandleTime(LocalDateTime.now());
        s.setRemark(dto.getRemark());
        suspicionMapper.updateById(s);

        // 自动生成追回单（来源：疑点稽核）
        MssRecover r = new MssRecover();
        r.setRecoverNo(nextNo("ZJ", recoverMapper.selectCount(new LambdaQueryWrapper<MssRecover>()
                .ge(MssRecover::getCreateTime, LocalDate.now().atStartOfDay()))));
        r.setSourceType(1);
        r.setSourceNo(s.getSuspicionNo());
        r.setTargetOrgCode(properties.getMedins().getCode());
        r.setTargetOrgName(properties.getMedins().getName());
        r.setMonth(s.getBizDate() == null ? null : YearMonth.from(s.getBizDate()).toString());
        r.setRecoverAmount(amount);
        r.setReason(cut(s.getReason(), 500));
        r.setStatus("待追回");
        recoverMapper.insert(r);
        return ActionVO.ok();
    }

    @Override
    public ActionVO exclude(SuspicionHandleDTO dto) {
        MssSuspicion s = suspicionMapper.selectById(dto.getId());
        if (s == null) {
            return ActionVO.fail("疑点不存在");
        }
        if (!"待核实".equals(s.getStatus())) {
            return ActionVO.fail("该疑点已处置，不可重复排除");
        }
        s.setStatus("已排除");
        s.setHandleUser(UserContext.username());
        s.setHandleTime(LocalDateTime.now());
        s.setRemark(dto.getRemark());
        suspicionMapper.updateById(s);
        return ActionVO.ok();
    }

    private String nextNo(String prefix, long todayCount) {
        return prefix + LocalDate.now().format(DAY) + String.format("%03d", todayCount + 1);
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
