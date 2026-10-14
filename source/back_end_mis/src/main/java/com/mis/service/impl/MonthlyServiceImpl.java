package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageResult;
import com.mis.common.exception.BusinessException;
import com.mis.config.MisProperties;
import com.mis.engine.PolicyEngine;
import com.mis.entity.MisAllocation;
import com.mis.entity.JinbMonthly;
import com.mis.entity.JinbMonthlyDeduct;
import com.mis.entity.JinbSettlement;
import com.mis.mapper.JinbAllocationMapper;
import com.mis.mapper.JinbMonthlyDeductMapper;
import com.mis.mapper.JinbMonthlyMapper;
import com.mis.mapper.JinbSettlementMapper;
import com.mis.service.MonthlyService;
import com.mis.vo.MonthlyResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyServiceImpl implements MonthlyService {

    private final JinbMonthlyMapper monthlyMapper;
    private final JinbMonthlyDeductMapper deductMapper;
    private final JinbAllocationMapper allocationMapper;
    private final JinbSettlementMapper settlementMapper;
    private final PolicyEngine policyEngine;
    private final MisProperties properties;

    public MonthlyServiceImpl(JinbMonthlyMapper monthlyMapper,
                              JinbMonthlyDeductMapper deductMapper,
                              JinbAllocationMapper allocationMapper,
                              JinbSettlementMapper settlementMapper,
                              PolicyEngine policyEngine,
                              MisProperties properties) {
        this.monthlyMapper = monthlyMapper;
        this.deductMapper = deductMapper;
        this.allocationMapper = allocationMapper;
        this.settlementMapper = settlementMapper;
        this.policyEngine = policyEngine;
        this.properties = properties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyResultVO declare(String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<JinbSettlement> settlements = validSettlements(start, end);
        int count = settlements.size();
        BigDecimal declareAmount = settlements.stream()
                .map(s -> nz(s.getInsurancePay()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        declareAmount = scale(declareAmount);

        JinbMonthly m = requireByMonth(month);
        if (m == null) {
            m = new JinbMonthly();
            m.setMonth(month);
            m.setFixMedinsCode(properties.getMedins().getCode());
            m.setStatus(1);
            m.setDeductAmount(BigDecimal.ZERO);
            m.setReserveAmount(BigDecimal.ZERO);
            m.setPayableAmount(BigDecimal.ZERO);
            m.setAllocatedAmount(BigDecimal.ZERO);
        }
        if (m.getStatus() != null && m.getStatus() >= 2) {
            throw new BusinessException(month + " 已完成审核/拨付，不能重新申报");
        }
        m.setDeclareCount(count);
        m.setDeclareAmount(declareAmount);
        m.setDeclareTime(LocalDateTime.now());
        m.setStatus(1);
        if (m.getId() == null) {
            monthlyMapper.insert(m);
        } else {
            monthlyMapper.updateById(m);
        }
        return result(month);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyResultVO audit(String month) {
        JinbMonthly m = requireByMonth(month);
        if (m == null) {
            throw new BusinessException(month + " 尚未申报，请先申报");
        }
        if (m.getStatus() != 1) {
            throw new BusinessException(month + " 当前状态为「" + statusDesc(m.getStatus()) + "」，不能重复审核");
        }

        YearMonth ym = YearMonth.parse(month);
        List<JinbSettlement> settlements = validSettlements(ym.atDay(1), ym.atEndOfMonth());

        BigDecimal deductAmount = BigDecimal.ZERO;
        BigDecimal ratio = properties.getAudit().getRefuseRatio();
        for (JinbSettlement s : settlements) {
            if (policyEngine.isViolation(s.getSettlementNo())) {
                BigDecimal amount = scale(nz(s.getInsurancePay()).multiply(ratio));
                if (amount.signum() <= 0) {
                    continue;
                }
                deductAmount = deductAmount.add(amount);
                JinbMonthlyDeduct d = new JinbMonthlyDeduct();
                d.setMonth(month);
                d.setTradeNo(s.getTradeNo());
                d.setSettlementNo(s.getSettlementNo());
                d.setDeductType(1);
                d.setDeductAmount(amount);
                d.setReason("智能审核命中规则（模拟超限价/不合理诊疗），按规则拒付");
                deductMapper.insert(d);
            }
        }
        deductAmount = scale(deductAmount);

        BigDecimal net = scale(nz(m.getDeclareAmount()).subtract(deductAmount));
        BigDecimal reserve = scale(net.multiply(properties.getAudit().getReserveRatio()));
        BigDecimal payable = scale(net.subtract(reserve));

        m.setDeductAmount(deductAmount);
        m.setReserveAmount(reserve);
        m.setPayableAmount(payable);
        m.setStatus(2);
        m.setAuditTime(LocalDateTime.now());
        monthlyMapper.updateById(m);
        return result(month);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyResultVO allocate(String month) {
        JinbMonthly m = requireByMonth(month);
        if (m == null) {
            throw new BusinessException(month + " 尚未申报，请先申报");
        }
        if (m.getStatus() != 2) {
            throw new BusinessException(month + " 当前状态为「" + statusDesc(m.getStatus()) + "」，请先审核再拨付");
        }
        BigDecimal payable = nz(m.getPayableAmount());
        if (payable.signum() > 0) {
            MisAllocation a = new MisAllocation();
            a.setMonth(month);
            a.setAllocNo("YBB" + month.replace("-", "") + "01");
            a.setAllocType(1);
            a.setAmount(payable);
            a.setRemark("月度结算基金拨付");
            allocationMapper.insert(a);
        }
        m.setAllocatedAmount(payable);
        m.setStatus(3);
        m.setAllocateTime(LocalDateTime.now());
        monthlyMapper.updateById(m);
        return result(month);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyResultVO yearSettle(String month) {
        JinbMonthly m = requireByMonth(month);
        if (m == null) {
            throw new BusinessException(month + " 尚未申报，请先申报");
        }
        if (m.getStatus() != 3) {
            throw new BusinessException(month + " 当前状态为「" + statusDesc(m.getStatus()) + "」，需先完成月度拨付");
        }
        // 模拟年度考核合格：全额返还质量保证金
        BigDecimal reserve = nz(m.getReserveAmount());
        if (reserve.signum() > 0) {
            MisAllocation a = new MisAllocation();
            a.setMonth(month);
            a.setAllocNo("YBN" + month.replace("-", "") + "01");
            a.setAllocType(2);
            a.setAmount(reserve);
            a.setRemark("年终清算：年度考核合格，质量保证金返还");
            allocationMapper.insert(a);
            m.setAllocatedAmount(scale(nz(m.getAllocatedAmount()).add(reserve)));
        }
        m.setStatus(4);
        m.setSettleTime(LocalDateTime.now());
        monthlyMapper.updateById(m);
        return result(month);
    }

    @Override
    public MonthlyResultVO result(String month) {
        JinbMonthly m = requireByMonth(month);
        if (m == null) {
            throw new BusinessException(month + " 尚未申报");
        }
        List<JinbMonthlyDeduct> deducts = deductMapper.selectList(new LambdaQueryWrapper<JinbMonthlyDeduct>()
                .eq(JinbMonthlyDeduct::getMonth, month)
                .orderByAsc(JinbMonthlyDeduct::getId));
        List<MisAllocation> allocations = allocationMapper.selectList(new LambdaQueryWrapper<MisAllocation>()
                .eq(MisAllocation::getMonth, month)
                .orderByAsc(MisAllocation::getId));

        MonthlyResultVO vo = new MonthlyResultVO();
        vo.setMonth(month);
        vo.setStatus(m.getStatus());
        vo.setStatusDesc(statusDesc(m.getStatus()));
        vo.setFixMedinsCode(m.getFixMedinsCode());
        vo.setDeclareCount(m.getDeclareCount());
        vo.setDeclareAmount(nz(m.getDeclareAmount()));
        vo.setDeductAmount(nz(m.getDeductAmount()));
        vo.setReserveAmount(nz(m.getReserveAmount()));
        vo.setPayableAmount(nz(m.getPayableAmount()));
        vo.setAllocatedAmount(nz(m.getAllocatedAmount()));
        vo.setDeducts(deducts);
        vo.setAllocations(allocations);
        return vo;
    }

    @Override
    public PageResult<JinbMonthly> page(int pageNum, int pageSize) {
        Page<JinbMonthly> page = monthlyMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<JinbMonthly>().orderByDesc(JinbMonthly::getMonth));
        return PageResult.of(page.getTotal(), pageNum, pageSize, page.getPages(), page.getRecords());
    }

    // ===== internal =====

    private List<JinbSettlement> validSettlements(LocalDate start, LocalDate end) {
        return settlementMapper.selectList(new LambdaQueryWrapper<JinbSettlement>()
                .eq(JinbSettlement::getStatus, 1)
                .between(JinbSettlement::getBillDate, start, end)
                .orderByAsc(JinbSettlement::getId));
    }

    private JinbMonthly requireByMonth(String month) {
        return monthlyMapper.selectOne(new LambdaQueryWrapper<JinbMonthly>()
                .eq(JinbMonthly::getMonth, month).last("limit 1"));
    }

    private String statusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "已申报";
            case 2 -> "已审核";
            case 3 -> "已拨付";
            case 4 -> "已清算";
            default -> "未知";
        };
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
