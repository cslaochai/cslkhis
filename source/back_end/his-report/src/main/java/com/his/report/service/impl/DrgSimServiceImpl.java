package com.his.report.service.impl;

import com.his.report.service.DrgSimService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.report.dto.DrgSimDTO;
import com.his.report.entity.DrgSimResult;
import com.his.report.enums.DrgSimStatusEnum;
import com.his.report.mapper.DrgSimMapper;
import com.his.report.support.DrgGrouper;
import com.his.report.vo.DrgSimVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DRG 分组模拟服务。
 *
 * <p>分组器为院内简化模拟（DrgGrouper 单点），组表 DRG 分组与权重为 CHS-DRG 1.1 模拟种子；
 * 每个首页一条模拟结果（uk_summary），重跑覆盖；实际费用取结算单，结算缺失按 0 计（首页 total_amount 兜底）。
 */
@Service
@RequiredArgsConstructor
public class DrgSimServiceImpl implements DrgSimService {

    private final DrgSimMapper simMapper;
    private final DrgGrouper grouper;

    @Transactional(rollbackFor = Exception.class)
    public DrgSimVO.SimResult simulate(DrgSimDTO.Simulate dto) {
        Map<String, Object> s = simMapper.selectSummary(dto.getSummaryId());
        if (s == null) {
            throw new BusinessException("病案首页不存在");
        }
        String icd = StringUtils.hasText(dto.getIcdCode()) ? dto.getIcdCode().trim() : asStr(s.get("main_diagnosis_code"));
        String icdName = StringUtils.hasText(dto.getIcdName()) ? dto.getIcdName().trim() : asStr(s.get("main_diagnosis_name"));
        if (!StringUtils.hasText(icd)) {
            throw new BusinessException("该首页主诊断编码为空，请先补录 ICD 编码再模拟");
        }
        boolean surgery = asInt(s.get("is_surgery")) == 1;
        Integer days = (Integer) s.get("inpatient_days");
        boolean death = asInt(s.get("death_flag")) == 1;
        BigDecimal actual = asDecimal(s.get("actual_amount"));

        DrgGrouper.GroupResult g = grouper.group(icd, surgery, days, death);
        boolean grouped = !DrgGrouper.QY_CODE.equals(g.drgCode());
        Map<String, Object> groupRow = null;
        BigDecimal weight = null;
        BigDecimal pay = null;
        String drgName = null;
        if (grouped) {
            groupRow = simMapper.groupList().stream()
                    .filter(x -> g.drgCode().equals(String.valueOf(x.get("drg_code"))))
                    .findFirst().orElse(null);
            if (groupRow == null) {
                throw new BusinessException("组表缺少 " + g.drgCode() + "，请检查 sys_drg_group 种子数据");
            }
            weight = asDecimal(groupRow.get("weight"));
            pay = asDecimal(groupRow.get("pay_standard"));
            drgName = String.valueOf(groupRow.get("drg_name"));
        }
        BigDecimal profit = grouped ? pay.subtract(actual) : null;

        // upsert：每首页一条，重跑覆盖（含软删行复活）
        DrgSimResult r = simMapper.selectOne(new LambdaQueryWrapper<DrgSimResult>()
                .eq(DrgSimResult::getSummaryId, dto.getSummaryId())
                .last("LIMIT 1"));
        boolean create = r == null;
        if (create) {
            r = new DrgSimResult();
            r.setSummaryId(dto.getSummaryId());
            r.setCreateBy(UserUtils.getCurrentUser().getUsername());
        }
        r.setPatientName(asStr(s.get("patient_name")));
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
            simMapper.insert(r);
        } else {
            simMapper.updateById(r);
        }
        return toSimVo(r);
    }

    /** 批量模拟：入参为空取最近 50 条；主诊断编码为空的行跳过并在结果中标注 */
    @Transactional(rollbackFor = Exception.class)
    public DrgSimVO.SummaryListVO simulateBatch(DrgSimDTO.SimulateBatch dto) {
        List<Long> ids = dto == null ? null : dto.getSummaryIds();
        List<Map<String, Object>> summaries;
        if (ids != null && !ids.isEmpty()) {
            summaries = new ArrayList<>();
            for (Long id : ids) {
                Map<String, Object> s = simMapper.selectSummary(id);
                if (s == null) {
                    throw new BusinessException("病案首页不存在：" + id);
                }
                summaries.add(s);
            }
        } else {
            summaries = simMapper.selectSummaries(50);
        }
        int skipped = 0;
        for (Map<String, Object> s : summaries) {
            if (!StringUtils.hasText(asStr(s.get("main_diagnosis_code")))) {
                skipped++;
                continue;
            }
            DrgSimDTO.Simulate one = new DrgSimDTO.Simulate();
            one.setSummaryId(((Number) s.get("summary_id")).longValue());
            simulate(one);
        }
        return summaryList(null, skipped);
    }

    public IPage<DrgSimVO.ResultRow> resultPage(DrgSimDTO.ResultQuery dto) {
        LambdaQueryWrapper<DrgSimResult> qw = new LambdaQueryWrapper<DrgSimResult>()
                .eq(dto.getSimStatus() != null, DrgSimResult::getSimStatus, dto.getSimStatus())
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(DrgSimResult::getPatientName, tr(dto.getKeyword()))
                        .or().like(DrgSimResult::getDrgCode, tr(dto.getKeyword())))
                .orderByDesc(DrgSimResult::getUpdateTime)
                .orderByDesc(DrgSimResult::getId);
        IPage<DrgSimResult> page = simMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toRow);
    }

    /** 可模拟首页列表 + 汇总统计 */
    public DrgSimVO.SummaryListVO summaryList(Integer limit, Integer extraSkipped) {
        List<Map<String, Object>> rows = simMapper.summaryList(limit == null ? 50 : limit);
        List<DrgSimVO.SummaryRow> list = rows.stream().map(m -> {
            DrgSimVO.SummaryRow r = new DrgSimVO.SummaryRow();
            r.setSummaryId(((Number) m.get("summary_id")).longValue());
            r.setPatientName(asStr(m.get("patient_name")));
            r.setDeptName(asStr(m.get("dept_name")));
            r.setDischargeTime((LocalDateTime) m.get("discharge_time"));
            r.setMainDiagCode(asStr(m.get("main_diagnosis_code")));
            r.setMainDiagName(asStr(m.get("main_diagnosis_name")));
            r.setIsSurgery(asInt(m.get("is_surgery")));
            r.setSimDrgCode(asStr(m.get("drg_code")));
            r.setSimProfit(asDecimal(m.get("profit_amount")));
            return r;
        }).collect(Collectors.toList());

        List<DrgSimResult> all = simMapper.selectList(new LambdaQueryWrapper<DrgSimResult>()
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
        return simMapper.groupList().stream().map(m -> {
            DrgSimVO.GroupRow r = new DrgSimVO.GroupRow();
            r.setId(((Number) m.get("id")).longValue());
            r.setDrgCode(asStr(m.get("drg_code")));
            r.setDrgName(asStr(m.get("drg_name")));
            r.setMdcCode(asStr(m.get("mdc_code")));
            r.setAdrgCode(asStr(m.get("adrg_code")));
            r.setWeight(asDecimal(m.get("weight")));
            r.setPayStandard(asDecimal(m.get("pay_standard")));
            r.setSource(asStr(m.get("source")));
            r.setVersion(asStr(m.get("version")));
            r.setStatus(asInt(m.get("status")));
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

    private static String asStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Integer asInt(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }

    private static BigDecimal asDecimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(o)).setScale(2, RoundingMode.HALF_UP);
    }

    private static String tr(String s) {
        return s == null ? "" : s.trim();
    }
}
