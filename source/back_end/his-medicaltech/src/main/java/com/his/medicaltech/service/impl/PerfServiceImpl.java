package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.PerfDTO;
import com.his.medicaltech.entity.BizDeptCostMonth;
import com.his.medicaltech.entity.BizPerfResult;
import com.his.medicaltech.mapper.BizDeptCostMonthMapper;
import com.his.medicaltech.mapper.BizPerfResultMapper;
import com.his.medicaltech.mapper.PerfMapper;
import com.his.medicaltech.service.PerfService;
import com.his.medicaltech.vo.PerfVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 绩效成本核算服务。
 *
 * <p>口径：收入 = L1 费用记账流水月度净额（收正退负 SUM 现算，按记账行所属科室归属，
 * 账务归属月取 book_time）；
 * 药占比 = 药品收入/总收入；结余 = 收入 - 成本；绩效 = max(0, 结余) × 提成系数。
 * 成本同科室同月唯一（重复录入拒绝）；核算结果同科室同月唯一（重算覆盖）。
 */
@Service
@RequiredArgsConstructor
public class PerfServiceImpl implements PerfService {

    private static final BigDecimal DEFAULT_BONUS_RATE = new BigDecimal("0.06");

    private final BizDeptCostMonthMapper costMapper;
    private final BizPerfResultMapper perfMapper;
    private final PerfMapper perfMapper2;

    // 成本

    private static String normalizeMonth(String m) {
        if (!StringUtils.hasText(m)) {
            return null;
        }
        String t = m.trim();
        if (!t.matches("\\d{4}-\\d{2}")) {
            throw new BusinessException("核算月份格式应为 yyyy-MM：" + m);
        }
        return t;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    // 核算

    private static String asStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static BigDecimal asDecimal(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(o)).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(rollbackFor = Exception.class)
    public PerfVO.CostRow saveCost(PerfDTO.CostSave dto) {
        String month = normalizeMonth(dto.getCostMonth());
        BizDeptCostMonth exists = costMapper.selectOne(new LambdaQueryWrapper<BizDeptCostMonth>()
                .eq(BizDeptCostMonth::getDeptId, dto.getDeptId())
                .eq(BizDeptCostMonth::getCostMonth, month)
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException("该科室当月成本已录入（" + asStr(exists.getDeptName()) + " " + month + "），不能重复录入");
        }
        BizDeptCostMonth c = new BizDeptCostMonth();
        c.setDeptId(dto.getDeptId());
        c.setDeptName(StringUtils.hasText(dto.getDeptName()) ? dto.getDeptName().trim() : "科室" + dto.getDeptId());
        c.setCostMonth(month);
        c.setLaborCost(nz(dto.getLaborCost()));
        c.setDrugCost(nz(dto.getDrugCost()));
        c.setMaterialCost(nz(dto.getMaterialCost()));
        c.setDepreciation(nz(dto.getDepreciation()));
        c.setOtherCost(nz(dto.getOtherCost()));
        c.setTotalCost(c.getLaborCost().add(c.getDrugCost()).add(c.getMaterialCost())
                .add(c.getDepreciation()).add(c.getOtherCost()));
        c.setCreateBy(UserUtils.getCurrentUser().getUsername());
        c.setRemark(dto.getRemark());
        costMapper.insert(c);
        return toCostRow(c);
    }

    // 转换

    public IPage<PerfVO.CostRow> costPage(PerfDTO.CostQuery dto) {
        LambdaQueryWrapper<BizDeptCostMonth> qw = new LambdaQueryWrapper<BizDeptCostMonth>()
                .eq(dto.getDeptId() != null, BizDeptCostMonth::getDeptId, dto.getDeptId())
                .eq(StringUtils.hasText(dto.getCostMonth()), BizDeptCostMonth::getCostMonth,
                        normalizeMonth(dto.getCostMonth()))
                .orderByDesc(BizDeptCostMonth::getCostMonth)
                .orderByAsc(BizDeptCostMonth::getDeptId);
        IPage<BizDeptCostMonth> page = costMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toCostRow);
    }

    /**
     * 核算前预览：收入聚合 + 成本快照
     */
    public PerfVO.RevenueInfo revenueInfo(Long deptId, String month) {
        String m = normalizeMonth(month);
        Map<String, Object> r = perfMapper2.sumDeptRevenue(deptId, m);
        BizDeptCostMonth cost = costMapper.selectOne(new LambdaQueryWrapper<BizDeptCostMonth>()
                .eq(BizDeptCostMonth::getDeptId, deptId)
                .eq(BizDeptCostMonth::getCostMonth, m)
                .last("LIMIT 1"));
        PerfVO.RevenueInfo vo = new PerfVO.RevenueInfo();
        vo.setDeptId(deptId);
        vo.setCostMonth(m);
        vo.setRevenue(r == null ? BigDecimal.ZERO : asDecimal(r.get("revenue")));
        vo.setDrugRevenue(r == null ? BigDecimal.ZERO : asDecimal(r.get("drug_revenue")));
        vo.setDeptName(r == null ? "科室" + deptId : asStr(r.get("dept_name")));
        boolean costExists = cost != null;
        vo.setCostExists(costExists);
        vo.setTotalCost(costExists ? cost.getTotalCost() : BigDecimal.ZERO);
        vo.setSurplus(vo.getRevenue().subtract(vo.getTotalCost()));
        vo.setCostId(costExists ? cost.getId() : null);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public PerfVO.PerfRow calc(PerfDTO.PerfCalc dto) {
        String month = normalizeMonth(dto.getCostMonth());
        PerfVO.RevenueInfo info = revenueInfo(dto.getDeptId(), month);
        if (info.getRevenue().signum() == 0) {
            throw new BusinessException("该科室当月无收入记录，无需核算");
        }
        BigDecimal rate = dto.getBonusRate() == null ? DEFAULT_BONUS_RATE : dto.getBonusRate();
        if (rate.signum() < 0) {
            throw new BusinessException("提成系数不能为负数");
        }
        BigDecimal surplus = info.getRevenue().subtract(info.getTotalCost());
        BigDecimal perf = surplus.signum() > 0
                ? surplus.multiply(rate).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal ratio = info.getRevenue().signum() > 0
                ? info.getDrugRevenue().divide(info.getRevenue(), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BizPerfResult p = perfMapper.selectOne(new LambdaQueryWrapper<BizPerfResult>()
                .eq(BizPerfResult::getDeptId, dto.getDeptId())
                .eq(BizPerfResult::getCostMonth, month)
                .last("LIMIT 1"));
        boolean create = p == null;
        if (create) {
            p = new BizPerfResult();
            p.setDeptId(dto.getDeptId());
            p.setDeptName(info.getDeptName());
            p.setCostMonth(month);
            p.setPerfStatus(2);
            p.setCreateBy(UserUtils.getCurrentUser().getUsername());
        }
        p.setRevenue(info.getRevenue());
        p.setDrugRevenue(info.getDrugRevenue());
        p.setDrugRatio(ratio);
        p.setTotalCost(info.getTotalCost());
        p.setSurplus(surplus);
        p.setBonusRate(rate);
        p.setPerfAmount(perf);
        p.setPerfStatus(2);
        p.setCostId(info.getCostId());
        p.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        if (create) {
            perfMapper.insert(p);
        } else {
            perfMapper.updateById(p);
        }
        return toPerfRow(p);
    }

    public IPage<PerfVO.PerfRow> perfPage(PerfDTO.PerfQuery dto) {
        LambdaQueryWrapper<BizPerfResult> qw = new LambdaQueryWrapper<BizPerfResult>()
                .eq(dto.getDeptId() != null, BizPerfResult::getDeptId, dto.getDeptId())
                .eq(StringUtils.hasText(dto.getCostMonth()), BizPerfResult::getCostMonth,
                        normalizeMonth(dto.getCostMonth()))
                .orderByDesc(BizPerfResult::getCostMonth)
                .orderByDesc(BizPerfResult::getPerfAmount)
                .orderByDesc(BizPerfResult::getId);
        IPage<BizPerfResult> page = perfMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toPerfRow);
    }

    private PerfVO.CostRow toCostRow(BizDeptCostMonth c) {
        PerfVO.CostRow vo = new PerfVO.CostRow();
        vo.setId(c.getId());
        vo.setDeptId(c.getDeptId());
        vo.setDeptName(c.getDeptName());
        vo.setCostMonth(c.getCostMonth());
        vo.setLaborCost(c.getLaborCost());
        vo.setDrugCost(c.getDrugCost());
        vo.setMaterialCost(c.getMaterialCost());
        vo.setDepreciation(c.getDepreciation());
        vo.setOtherCost(c.getOtherCost());
        vo.setTotalCost(c.getTotalCost());
        vo.setRemark(c.getRemark());
        vo.setCreateBy(c.getCreateBy());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }

    private PerfVO.PerfRow toPerfRow(BizPerfResult p) {
        PerfVO.PerfRow vo = new PerfVO.PerfRow();
        vo.setId(p.getId());
        vo.setDeptId(p.getDeptId());
        vo.setDeptName(p.getDeptName());
        vo.setCostMonth(p.getCostMonth());
        vo.setRevenue(p.getRevenue());
        vo.setDrugRevenue(p.getDrugRevenue());
        vo.setDrugRatio(p.getDrugRatio());
        vo.setTotalCost(p.getTotalCost());
        vo.setSurplus(p.getSurplus());
        vo.setBonusRate(p.getBonusRate());
        vo.setPerfAmount(p.getPerfAmount());
        vo.setPerfStatus(p.getPerfStatus());
        vo.setRemark(p.getRemark());
        vo.setUpdateTime(p.getUpdateTime());
        return vo;
    }
}
