package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mis.entity.MssAuditRule;
import com.mis.entity.MssInspection;
import com.mis.entity.MssRecover;
import com.mis.entity.MssSuspicion;
import com.mis.mapper.MssAuditRuleMapper;
import com.mis.mapper.MssInspectionMapper;
import com.mis.mapper.MssRecoverMapper;
import com.mis.mapper.MssSuspicionMapper;
import com.mis.service.SuperviseStatsService;
import com.mis.vo.SuperviseStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SuperviseStatsServiceImpl implements SuperviseStatsService {

    private final MssAuditRuleMapper ruleMapper;
    private final MssSuspicionMapper suspicionMapper;
    private final MssInspectionMapper inspectionMapper;
    private final MssRecoverMapper recoverMapper;

    @Override
    public SuperviseStatsVO stats() {
        SuperviseStatsVO vo = new SuperviseStatsVO();

        vo.setRuleCount(ruleMapper.selectCount(new LambdaQueryWrapper<MssAuditRule>()
                .eq(MssAuditRule::getStatus, 1)));

        List<MssSuspicion> suspicions = suspicionMapper.selectList(null);
        long pending = 0, confirmed = 0, excluded = 0;
        BigDecimal confirmedAmount = z();
        for (MssSuspicion s : suspicions) {
            switch (nzStatus(s.getStatus(), "待核实")) {
                case "已确认" -> {
                    confirmed++;
                    confirmedAmount = confirmedAmount.add(nz(s.getConfirmAmount()));
                }
                case "已排除" -> excluded++;
                default -> pending++;
            }
        }
        vo.setSuspicionTotal(suspicions.size());
        vo.setSuspicionPending(pending);
        vo.setSuspicionConfirmed(confirmed);
        vo.setSuspicionExcluded(excluded);
        vo.setConfirmedAmount(confirmedAmount);

        List<MssRecover> recovers = recoverMapper.selectList(null);
        BigDecimal pendingRecover = z(), recovered = z(), writeOff = z();
        for (MssRecover r : recovers) {
            switch (nzStatus(r.getStatus(), "待追回")) {
                case "已追回" -> recovered = recovered.add(nz(r.getRecoverAmount()));
                case "已核销" -> writeOff = writeOff.add(nz(r.getRecoverAmount()));
                default -> pendingRecover = pendingRecover.add(nz(r.getRecoverAmount()));
            }
        }
        vo.setRecoverPendingAmount(pendingRecover);
        vo.setRecoveredAmount(recovered);
        vo.setWriteOffAmount(writeOff);

        vo.setInspectionRunning(inspectionMapper.selectCount(new LambdaQueryWrapper<MssInspection>()
                .eq(MssInspection::getStatus, "检查中")));
        vo.setInspectionDone(inspectionMapper.selectCount(new LambdaQueryWrapper<MssInspection>()
                .eq(MssInspection::getStatus, "已完成")));

        vo.setRuleDist(ruleDist(suspicions));
        vo.setTrend(trend(suspicions));
        return vo;
    }

    private List<SuperviseStatsVO.RuleDistVO> ruleDist(List<MssSuspicion> suspicions) {
        Map<String, SuperviseStatsVO.RuleDistVO> byRule = new LinkedHashMap<>();
        for (MssSuspicion s : suspicions) {
            String key = s.getRuleName() == null ? "未知规则" : s.getRuleName();
            SuperviseStatsVO.RuleDistVO v = byRule.computeIfAbsent(key, k -> {
                SuperviseStatsVO.RuleDistVO d = new SuperviseStatsVO.RuleDistVO();
                d.setRuleName(k);
                d.setRuleType(s.getRuleType());
                d.setAmount(z());
                return d;
            });
            v.setCount(v.getCount() + 1);
            v.setAmount(v.getAmount().add(nz(s.getInvolvedAmount())));
        }
        List<SuperviseStatsVO.RuleDistVO> list = new ArrayList<>(byRule.values());
        list.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return list.size() > 8 ? list.subList(0, 8) : list;
    }

    private List<SuperviseStatsVO.TrendPointVO> trend(List<MssSuspicion> suspicions) {
        Map<LocalDate, SuperviseStatsVO.TrendPointVO> byDay = new LinkedHashMap<>();
        for (MssSuspicion s : suspicions) {
            LocalDate day = s.getCreateTime() == null ? LocalDate.now() : s.getCreateTime().toLocalDate();
            SuperviseStatsVO.TrendPointVO p = byDay.computeIfAbsent(day, k -> {
                SuperviseStatsVO.TrendPointVO t = new SuperviseStatsVO.TrendPointVO();
                t.setDay(k.toString());
                t.setAmount(z());
                return t;
            });
            p.setCount(p.getCount() + 1);
            p.setAmount(p.getAmount().add(nz(s.getInvolvedAmount())));
        }
        LocalDate today = LocalDate.now();
        List<SuperviseStatsVO.TrendPointVO> out = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            SuperviseStatsVO.TrendPointVO p = byDay.get(d);
            if (p == null) {
                p = new SuperviseStatsVO.TrendPointVO();
                p.setDay(d.toString());
                p.setAmount(z());
            }
            out.add(p);
        }
        return out;
    }

    private static String nzStatus(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? z() : v;
    }

    private static BigDecimal z() {
        return BigDecimal.ZERO.setScale(2);
    }
}
