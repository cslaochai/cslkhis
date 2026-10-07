package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.DrgSimDTO;
import com.his.medicaltech.entity.DrgSimResult;
import com.his.medicaltech.enums.DrgSimStatusEnum;
import com.his.medicaltech.mapper.DrgSimMapper;
import com.his.medicaltech.service.DrgSimService;
import com.his.medicaltech.support.DrgGrouper;
import com.his.medicaltech.vo.DrgGroupRowVO;
import com.his.medicaltech.vo.DrgSimVO;
import com.his.medicaltech.vo.DrgSummaryListRowVO;
import com.his.medicaltech.vo.DrgSummaryRowVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DRG 分组模拟服务。
 *
 * <p>分组器为院内简化模拟（DrgGrouper 单点），组表 DRG 分组与权重为 CHS-DRG 1.1 模拟种子；
 * 每个首页一条模拟结果（uk_summary），重跑覆盖；实际费用取结算单，结算缺失按 0 计（首页 total_amount 兜底）。
 */
@Service
@RequiredArgsConstructor
public class DrgSimServiceImpl extends ServiceImpl<DrgSimMapper, DrgSimResult> implements DrgSimService {

    private final DrgSimMapper drgSimMapper;
    private final DrgGrouper grouper;

    /**
     * 金额归一到两位小数，空值按 0 计。
     *
     * <p>为什么空值兜0 而不是保留 null：盈亏 = 支付标准 - 实际费用，
     * 组表里某组没维护支付标准时若传null，盈亏这一步会直接 NPE；
     * 兜 0 算出来的"亏全部实际费用"虽然难看，但至少让页面能显示这组没维护标准。
     */
    private static BigDecimal money(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(rollbackFor = Exception.class)
    public DrgSimVO.SimResult simulate(DrgSimDTO.Simulate dto) {
        DrgSummaryRowVO s = drgSimMapper.selectSummary(dto.getSummaryId());
        if (s == null) {
            throw new BusinessException("病案首页不存在");
        }
        String icd = TextUtil.hasText(dto.getIcdCode()) ? dto.getIcdCode().trim() : s.getMainDiagnosisCode();
        String icdName = TextUtil.hasText(dto.getIcdName()) ? dto.getIcdName().trim() : s.getMainDiagnosisName();
        if (!TextUtil.hasText(icd)) {
            throw new BusinessException("该首页主诊断编码为空，请先补录 ICD 编码再模拟");
        }
        boolean surgery = Integer.valueOf(1).equals(s.getIsSurgery());
        Integer days = s.getInpatientDays();
        boolean death = Integer.valueOf(1).equals(s.getDeathFlag());
        BigDecimal actual = money(s.getActualAmount());

        DrgGrouper.GroupResult g = grouper.group(icd, surgery, days, death);
        boolean grouped = !DrgGrouper.QY_CODE.equals(g.drgCode());
        DrgGroupRowVO groupRow = null;
        BigDecimal weight = null;
        BigDecimal pay = null;
        String drgName = null;
        if (grouped) {
            groupRow = drgSimMapper.groupList().stream()
                    .filter(x -> g.drgCode().equals(x.getDrgCode()))
                    .findFirst().orElse(null);
            if (groupRow == null) {
                throw new BusinessException("组表缺少 " + g.drgCode() + "，请检查 sys_drg_group 种子数据");
            }
            weight = money(groupRow.getWeight());
            pay = money(groupRow.getPayStandard());
            drgName = groupRow.getDrgName();
        }
        BigDecimal profit = grouped ? pay.subtract(actual) : null;

        // upsert：每首页一条，重跑覆盖（含软删行复活）
        DrgSimResult r = drgSimMapper.selectOne(new LambdaQueryWrapper<DrgSimResult>()
                .eq(DrgSimResult::getSummaryId, dto.getSummaryId())
                .last("LIMIT 1"));
        boolean create = r == null;
        if (create) {
            r = new DrgSimResult();
            r.setSummaryId(dto.getSummaryId());
            r.setCreateBy(UserUtils.getCurrentUser().getUsername());
        }
        r.setPatientName(s.getPatientName());
        r.setMainDiagCode(icd);
        r.setMainDiagName(icdName);
        r.setIsSurgery(surgery ? 1 : 0);
        r.setInpatientDays(days);
        r.setDrgCode(g.drgCode());
        r.setDrgName(drgName);
        r.setMdcCode(g.mdc());
        r.setWeight(weight);
        r.setPayStandard(pay);
        r.setActualAmount(actual);
        r.setProfitAmount(profit);
        r.setSimStatus(grouped ? DrgSimStatusEnum.GROUPED.getCode() : DrgSimStatusEnum.UNGROUPED.getCode());
        r.setRuleNote(g.ruleNote());
        r.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        if (create) {
            drgSimMapper.insert(r);
        } else {
            drgSimMapper.updateById(r);
        }
        return toSimVo(r);
    }

    /**
     * 批量模拟：入参为空取最近 50 条；主诊断编码为空的行跳过并在结果中标注
     */
    @Transactional(rollbackFor = Exception.class)
    public DrgSimVO.SummaryListVO simulateBatch(DrgSimDTO.SimulateBatch dto) {
        List<Long> ids = dto == null ? null : dto.getSummaryIds();
        List<DrgSummaryRowVO> summaries;
        if (ids != null && !ids.isEmpty()) {
            summaries = new ArrayList<>();
            for (Long id : ids) {
                DrgSummaryRowVO s = drgSimMapper.selectSummary(id);
                if (s == null) {
                    throw new BusinessException("病案首页不存在：" + id);
                }
                summaries.add(s);
            }
        } else {
            summaries = drgSimMapper.selectSummaries(50);
        }
        int skipped = 0;
        for (DrgSummaryRowVO s : summaries) {
            if (!TextUtil.hasText(s.getMainDiagnosisCode())) {
                skipped++;
                continue;
            }
            DrgSimDTO.Simulate one = new DrgSimDTO.Simulate();
            one.setSummaryId(s.getSummaryId());
            simulate(one);
        }
        return summaryList(null, skipped);
    }

    public IPage<DrgSimVO.ResultRow> resultPage(DrgSimDTO.ResultQuery dto) {
        LambdaQueryWrapper<DrgSimResult> qw = new LambdaQueryWrapper<DrgSimResult>()
                .eq(dto.getSimStatus() != null, DrgSimResult::getSimStatus, dto.getSimStatus())
                .and(TextUtil.hasText(dto.getKeyword()), w -> w
                        .like(DrgSimResult::getPatientName, TextUtil.trimToEmpty(dto.getKeyword()))
                        .or().like(DrgSimResult::getDrgCode, TextUtil.trimToEmpty(dto.getKeyword())))
                .orderByDesc(DrgSimResult::getUpdateTime)
                .orderByDesc(DrgSimResult::getId);
        IPage<DrgSimResult> page = drgSimMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toRow);
    }

    /**
     * 可模拟首页列表 + 汇总统计
     */
    public DrgSimVO.SummaryListVO summaryList(Integer limit, Integer extraSkipped) {
        List<DrgSummaryListRowVO> rows = drgSimMapper.summaryList(limit == null ? 50 : limit);
        List<DrgSimVO.SummaryRow> list = rows.stream().map(m -> {
            DrgSimVO.SummaryRow r = new DrgSimVO.SummaryRow();
            r.setSummaryId(m.getSummaryId());
            r.setPatientName(m.getPatientName());
            r.setDeptName(m.getDeptName());
            r.setDischargeTime(m.getDischargeTime());
            r.setMainDiagCode(m.getMainDiagnosisCode());
            r.setMainDiagName(m.getMainDiagnosisName());
            r.setIsSurgery(m.getIsSurgery());
            r.setSimDrgCode(m.getDrgCode());
            r.setSimProfit(money(m.getProfitAmount()));
            return r;
        }).collect(Collectors.toList());

        List<DrgSimResult> all = drgSimMapper.selectList(new LambdaQueryWrapper<DrgSimResult>()
                .eq(DrgSimResult::getDelFlag, 0));
        DrgSimVO.SimStat stat = new DrgSimVO.SimStat();
        stat.setTotal((long) all.size());
        stat.setGrouped(all.stream().filter(x -> DrgSimStatusEnum.GROUPED.is(x.getSimStatus())).count());
        stat.setUngrouped(all.stream().filter(x -> DrgSimStatusEnum.UNGROUPED.is(x.getSimStatus())).count());
        stat.setTotalProfit(all.stream()
                .filter(x -> x.getProfitAmount() != null && x.getProfitAmount().signum() > 0)
                .map(DrgSimResult::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
        stat.setTotalOverrun(all.stream()
                .filter(x -> x.getProfitAmount() != null && x.getProfitAmount().signum() < 0)
                .map(DrgSimResult::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs().setScale(2, RoundingMode.HALF_UP));
        stat.setTotal(stat.getTotal() + (extraSkipped == null ? 0 : extraSkipped));
        DrgSimVO.SummaryListVO vo = new DrgSimVO.SummaryListVO();
        vo.setRows(list);
        vo.setStat(stat);
        return vo;
    }

    public List<DrgSimVO.GroupRow> groupList() {
        return drgSimMapper.groupList().stream().map(m -> {
            DrgSimVO.GroupRow r = new DrgSimVO.GroupRow();
            r.setId(m.getId());
            r.setDrgCode(m.getDrgCode());
            r.setDrgName(m.getDrgName());
            r.setMdcCode(m.getMdcCode());
            r.setAdrgCode(m.getAdrgCode());
            r.setWeight(money(m.getWeight()));
            r.setPayStandard(money(m.getPayStandard()));
            r.setSource(m.getSource());
            r.setVersion(m.getVersion());
            r.setStatus(m.getStatus());
            return r;
        }).collect(Collectors.toList());
    }

    private DrgSimVO.SimResult toSimVo(DrgSimResult r) {
        DrgSimVO.SimResult vo = new DrgSimVO.SimResult();
        vo.setSummaryId(r.getSummaryId());
        vo.setPatientName(r.getPatientName());
        vo.setMainDiagCode(r.getMainDiagCode());
        vo.setMainDiagName(r.getMainDiagName());
        vo.setSurgery(r.getIsSurgery() != null && r.getIsSurgery() == 1);
        vo.setInpatientDays(r.getInpatientDays());
        vo.setDrgCode(r.getDrgCode());
        vo.setDrgName(r.getDrgName());
        vo.setMdcCode(r.getMdcCode());
        vo.setWeight(r.getWeight());
        vo.setPayStandard(r.getPayStandard());
        vo.setActualAmount(r.getActualAmount());
        vo.setProfitAmount(r.getProfitAmount());
        vo.setSimStatus(r.getSimStatus());
        vo.setRuleNote(r.getRuleNote());
        return vo;
    }

    private DrgSimVO.ResultRow toRow(DrgSimResult r) {
        DrgSimVO.ResultRow vo = new DrgSimVO.ResultRow();
        vo.setId(r.getId());
        vo.setSummaryId(r.getSummaryId());
        vo.setPatientName(r.getPatientName());
        vo.setMainDiagCode(r.getMainDiagCode());
        vo.setMainDiagName(r.getMainDiagName());
        vo.setIsSurgery(r.getIsSurgery());
        vo.setInpatientDays(r.getInpatientDays());
        vo.setDrgCode(r.getDrgCode());
        vo.setDrgName(r.getDrgName());
        vo.setWeight(r.getWeight());
        vo.setPayStandard(r.getPayStandard());
        vo.setActualAmount(r.getActualAmount());
        vo.setProfitAmount(r.getProfitAmount());
        vo.setSimStatus(r.getSimStatus());
        vo.setRuleNote(r.getRuleNote());
        vo.setUpdateTime(r.getUpdateTime());
        return vo;
    }
}
