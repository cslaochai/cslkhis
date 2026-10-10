package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.PerfDTO;
import com.his.medicaltech.entity.BizDeptCostMonth;
import com.his.medicaltech.entity.BizPerfResult;
import com.his.medicaltech.mapper.BizDeptCostMonthMapper;
import com.his.medicaltech.mapper.BizPerfResultMapper;
import com.his.medicaltech.mapper.PerfMapper;
import com.his.medicaltech.service.PerfService;
import com.his.medicaltech.vo.PerfDeptRevenueRowVO;
import com.his.medicaltech.vo.PerfVO;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 绩效成本核算服务。
 */
@Service
@RequiredArgsConstructor
public class PerfServiceImpl extends ServiceImpl<BizPerfResultMapper, BizPerfResult> implements PerfService {

    private static final BigDecimal DEFAULT_BONUS_RATE = new BigDecimal("0.06");

    private final BizDeptCostMonthMapper bizDeptCostMonthMapper;
    private final BizPerfResultMapper bizPerfResultMapper;
    private final PerfMapper perfMapper;
    private final DeptScopeService deptScopeService;

    // 成本

    private static String normalizeMonth(String m) {
        if (!TextUtil.hasText(m)) {
            return null;
        }
        String t = m.trim();
        if (!t.matches("\\d{4}-\\d{2}")) {
            throw new BusinessException("核算月份格式应为 yyyy-MM：" + m);
        }
        return t;
    }

    // 核算

    @Transactional(rollbackFor = Exception.class)
    public PerfVO.CostRow saveCost(PerfDTO.CostSave dto) {
        String month = normalizeMonth(dto.getCostMonth());
        // 科室数据权限：向指定科室写入成本前校验越权
        deptScopeService.resolveDeptId(dto.getDeptId());
        BizDeptCostMonth exists = bizDeptCostMonthMapper.selectOne(new LambdaQueryWrapper<BizDeptCostMonth>()
                .eq(BizDeptCostMonth::getDeptId, dto.getDeptId())
                .eq(BizDeptCostMonth::getCostMonth, month)
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException("该科室当月成本已录入（"
                    + (exists.getDeptName() == null ? "" : exists.getDeptName()) + " " + month + "），不能重复录入");
        }
        BizDeptCostMonth c = new BizDeptCostMonth();
        c.setDeptId(dto.getDeptId());
        c.setDeptName(TextUtil.hasText(dto.getDeptName()) ? dto.getDeptName().trim() : "科室" + dto.getDeptId());
        c.setCostMonth(month);
        c.setLaborCost(NumUtil.orZero(dto.getLaborCost()));
        c.setDrugCost(NumUtil.orZero(dto.getDrugCost()));
        c.setMaterialCost(NumUtil.orZero(dto.getMaterialCost()));
        c.setDepreciation(NumUtil.orZero(dto.getDepreciation()));
        c.setOtherCost(NumUtil.orZero(dto.getOtherCost()));
        c.setTotalCost(c.getLaborCost().add(c.getDrugCost()).add(c.getMaterialCost())
                .add(c.getDepreciation()).add(c.getOtherCost()));
        c.setCreateBy(UserUtils.getCurrentUser().getUsername());
        c.setRemark(dto.getRemark());
        bizDeptCostMonthMapper.insert(c);
        return toCostRow(c);
    }

    // 转换

    public IPage<PerfVO.CostRow> costPage(PerfDTO.CostQuery dto) {
        // 科室数据权限收口：列表按岗位可见科室集合过滤
        List<Long> deptIds = deptScopeService.scopedDeptIds(dto.getDeptId());
        LambdaQueryWrapper<BizDeptCostMonth> qw = new LambdaQueryWrapper<BizDeptCostMonth>()
                .eq(dto.getDeptId() != null, BizDeptCostMonth::getDeptId, dto.getDeptId())
                .in(deptIds != null, BizDeptCostMonth::getDeptId, deptIds)
                .eq(TextUtil.hasText(dto.getCostMonth()), BizDeptCostMonth::getCostMonth,
                        normalizeMonth(dto.getCostMonth()))
                .orderByDesc(BizDeptCostMonth::getCostMonth)
                .orderByAsc(BizDeptCostMonth::getDeptId);
        IPage<BizDeptCostMonth> page = bizDeptCostMonthMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toCostRow);
    }

    /**
     * 核算前预览：收入聚合 + 成本快照
     */
    public PerfVO.RevenueInfo revenueInfo(Long deptId, String month) {
        // 科室数据权限：核算/预览都按指定科室取数，先校验越权（calc 经此复用）
        deptScopeService.resolveDeptId(deptId);
        String m = normalizeMonth(month);
        PerfDeptRevenueRowVO r = perfMapper.sumDeptRevenue(deptId, m);
        BizDeptCostMonth cost = bizDeptCostMonthMapper.selectOne(new LambdaQueryWrapper<BizDeptCostMonth>()
                .eq(BizDeptCostMonth::getDeptId, deptId)
                .eq(BizDeptCostMonth::getCostMonth, m)
                .last("LIMIT 1"));
        PerfVO.RevenueInfo vo = new PerfVO.RevenueInfo();
        vo.setDeptId(deptId);
        vo.setCostMonth(m);
        vo.setRevenue(r == null ? BigDecimal.ZERO : r.getRevenue());
        vo.setDrugRevenue(r == null ? BigDecimal.ZERO : r.getDrugRevenue());
        vo.setDeptName(r == null ? "科室" + deptId : r.getDeptName());
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

        BizPerfResult p = bizPerfResultMapper.selectOne(new LambdaQueryWrapper<BizPerfResult>()
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
            bizPerfResultMapper.insert(p);
        } else {
            bizPerfResultMapper.updateById(p);
        }
        return toPerfRow(p);
    }

    public IPage<PerfVO.PerfRow> perfPage(PerfDTO.PerfQuery dto) {
        // 科室数据权限收口：列表按岗位可见科室集合过滤
        List<Long> deptIds = deptScopeService.scopedDeptIds(dto.getDeptId());
        LambdaQueryWrapper<BizPerfResult> qw = new LambdaQueryWrapper<BizPerfResult>()
                .eq(dto.getDeptId() != null, BizPerfResult::getDeptId, dto.getDeptId())
                .in(deptIds != null, BizPerfResult::getDeptId, deptIds)
                .eq(TextUtil.hasText(dto.getCostMonth()), BizPerfResult::getCostMonth,
                        normalizeMonth(dto.getCostMonth()))
                .orderByDesc(BizPerfResult::getCostMonth)
                .orderByDesc(BizPerfResult::getPerfAmount)
                .orderByDesc(BizPerfResult::getId);
        IPage<BizPerfResult> page = bizPerfResultMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
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
