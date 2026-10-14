package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.entity.MssBill;
import com.mis.entity.MssFeeAudit;
import com.mis.entity.MssPendingSettle;
import com.mis.entity.MssRecon;
import com.mis.entity.MssRecover;
import com.mis.entity.MssSuspicion;
import com.mis.mapper.MssBillMapper;
import com.mis.mapper.MssFeeAuditMapper;
import com.mis.mapper.MssPendingSettleMapper;
import com.mis.mapper.MssReconMapper;
import com.mis.mapper.MssRecoverMapper;
import com.mis.mapper.MssSuspicionMapper;
import com.mis.service.WorkbenchService;
import com.mis.vo.BillVO;
import com.mis.vo.StatsVO;
import com.mis.vo.WorkbenchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkbenchServiceImpl implements WorkbenchService {

    private final MssBillMapper billMapper;
    private final MssPendingSettleMapper pendingMapper;
    private final MssFeeAuditMapper auditMapper;
    private final MssReconMapper reconMapper;
    private final MssSuspicionMapper suspicionMapper;
    private final MssRecoverMapper recoverMapper;

    @Override
    public WorkbenchVO summary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<MssBill> todays = billMapper.selectList(new LambdaQueryWrapper<MssBill>()
                .ge(MssBill::getSettleTime, start)
                .lt(MssBill::getSettleTime, end));

        WorkbenchVO.Kpi kpi = new WorkbenchVO.Kpi();
        kpi.setTodayCount(todays.size());
        kpi.setTodayAmount(sum(todays, MssBill::getTotalAmount));
        kpi.setInsurancePay(sum(todays, MssBill::getInsurancePay));
        kpi.setPersonalAccount(sum(todays, MssBill::getPersonalAccount));
        kpi.setCashPay(sum(todays, MssBill::getCashPay));

        long pending = pendingMapper.selectCount(new LambdaQueryWrapper<MssPendingSettle>()
                .eq(MssPendingSettle::getStatus, 1));
        long audit = auditMapper.selectCount(new LambdaQueryWrapper<MssFeeAudit>()
                .eq(MssFeeAudit::getStatus, "待审核"));
        long reconcile = reconMapper.selectCount(new LambdaQueryWrapper<MssRecon>()
                .eq(MssRecon::getStatus, "待对账"));
        long refund = billMapper.selectCount(new LambdaQueryWrapper<MssBill>()
                .eq(MssBill::getStatus, "已退费"));
        long suspicion = suspicionMapper.selectCount(new LambdaQueryWrapper<MssSuspicion>()
                .eq(MssSuspicion::getStatus, "待核实"));
        long recover = recoverMapper.selectCount(new LambdaQueryWrapper<MssRecover>()
                .eq(MssRecover::getStatus, "待追回"));

        WorkbenchVO.Todo todo = new WorkbenchVO.Todo();
        todo.setPendingSettle((int) pending);
        todo.setAudit((int) audit);
        todo.setReconcile((int) reconcile);
        todo.setRefund((int) refund);
        todo.setSuspicion((int) suspicion);
        todo.setRecover((int) recover);
        todo.setTotal((int) (pending + audit + reconcile + suspicion + recover));

        List<MssBill> recent = billMapper.selectPage(new Page<>(1, 6),
                new LambdaQueryWrapper<MssBill>().orderByDesc(MssBill::getSettleTime)).getRecords();
        List<BillVO> recentVO = new ArrayList<>();
        for (MssBill b : recent) {
            BillVO vo = new BillVO();
            BeanUtils.copyProperties(b, vo);
            recentVO.add(vo);
        }

        WorkbenchVO vo = new WorkbenchVO();
        vo.setKpi(kpi);
        vo.setTodo(todo);
        vo.setRecentBills(recentVO);
        return vo;
    }

    @Override
    public StatsVO stats() {
        List<MssBill> all = billMapper.selectList(null);

        Map<String, StatsVO.InsStat> byIns = new LinkedHashMap<>();
        Map<String, StatsVO.DeptStat> byDept = new LinkedHashMap<>();
        for (MssBill b : all) {
            StatsVO.InsStat ins = byIns.computeIfAbsent(b.getInsuranceType(), k -> {
                StatsVO.InsStat s = new StatsVO.InsStat();
                s.setAmount(z());
                s.setInsurancePay(z());
                return s;
            });
            ins.setCount(ins.getCount() + 1);
            ins.setAmount(nz(ins.getAmount()).add(nz(b.getTotalAmount())));
            ins.setInsurancePay(nz(ins.getInsurancePay()).add(nz(b.getInsurancePay())));

            StatsVO.DeptStat dept = byDept.computeIfAbsent(b.getDept(), k -> {
                StatsVO.DeptStat s = new StatsVO.DeptStat();
                s.setAmount(z());
                return s;
            });
            dept.setCount(dept.getCount() + 1);
            dept.setAmount(nz(dept.getAmount()).add(nz(b.getTotalAmount())));
        }

        LocalDate today = LocalDate.now();
        List<StatsVO.TrendPoint> trend = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime s = d.atStartOfDay();
            LocalDateTime e = d.plusDays(1).atStartOfDay();
            List<MssBill> dayBills = billMapper.selectList(new LambdaQueryWrapper<MssBill>()
                    .ge(MssBill::getSettleTime, s).lt(MssBill::getSettleTime, e));
            StatsVO.TrendPoint p = new StatsVO.TrendPoint();
            p.setDay(d.toString());
            p.setCount(dayBills.size());
            p.setAmount(sum(dayBills, MssBill::getTotalAmount));
            trend.add(p);
        }

        StatsVO vo = new StatsVO();
        vo.setByIns(byIns);
        vo.setByDept(byDept);
        vo.setTrend(trend);
        return vo;
    }

    private BigDecimal sum(List<MssBill> list, java.util.function.Function<MssBill, BigDecimal> getter) {
        BigDecimal total = z();
        for (MssBill b : list) {
            total = total.add(nz(getter.apply(b)));
        }
        return total;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? z() : v;
    }

    private static BigDecimal z() {
        return BigDecimal.ZERO.setScale(2);
    }
}
