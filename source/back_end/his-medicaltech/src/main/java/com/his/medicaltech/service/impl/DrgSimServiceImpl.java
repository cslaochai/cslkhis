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
import com.his.medicaltech.support.DrgFacts;
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
 */
@Service
@RequiredArgsConstructor
public class DrgSimServiceImpl extends ServiceImpl<DrgSimMapper, DrgSimResult> implements DrgSimService {

    private final DrgSimMapper drgSimMapper;
    private final DrgGrouper grouper;

    /**
     * 金额归一到两位小数；没有值就是 null，不兜 0。
     *
     * <p>权重与支付标准一律不走「兜 0」：官方方案包只下发目录结构，这两列在组表里就是空的，
     * 兜成 0.00 等于把「统筹区还没定标准」显示成「标准是 0 元」，跟着算出的
     * 「超支 = 全部实际费用」是一句编出来的话。null 原样落库，页面渲染成 —。
     */
    private static BigDecimal money(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
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
        // 分组事实全量取自首页明细：主手术可有多条，其他手术（QTSS）与联合手术判定有关，不能只取第一条
        List<String> mainOpers = List.of();
        List<String> otherOpers = List.of();
        List<String> otherDiags = List.of();
        if (s.getAdmissionId() != null) {
            mainOpers = drgSimMapper.selectMainOperCodes(s.getAdmissionId());
            otherOpers = drgSimMapper.selectOtherOperCodes(s.getAdmissionId());
            otherDiags = drgSimMapper.selectOtherDiagCodes(s.getAdmissionId());
        }
        BigDecimal actual = money(s.getActualAmount());

        DrgGrouper.GroupResult g = grouper.group(new DrgFacts(icd, mainOpers, otherDiags, otherOpers,
                s.getGender(), s.getAge(), s.getAgeUnit(), s.getBirthWeight()));
        boolean grouped = g.grouped();
        BigDecimal pay = g.payStandard();
        // 支付标准未下发时盈亏算不出来，留 null，不拿 0 减实际费用冒充超支
        BigDecimal profit = grouped && pay != null ? money(pay.subtract(actual)) : null;

        // upsert：每首页一条，重跑覆盖（含软删行复活）
        DrgSimResult r = drgSimMapper.selectOne(new LambdaQueryWrapper<DrgSimResult>()
                .eq(DrgSimResult::getSummaryId, dto.getSummaryId())
                .last("LIMIT 1"));
        boolean create = r == null;
        if (create) {
            r = new DrgSimResult();
            r.setSummaryId(dto.getSummaryId());
            r.setCreateBy(UserUtils.getCurrentUser().getRealName());
        }
        r.setPatientName(s.getPatientName());
        r.setMainDiagCode(icd);
        r.setMainDiagName(icdName);
        r.setIsSurgery(s.getIsSurgery());
        r.setInpatientDays(s.getInpatientDays());
        r.setDrgCode(g.drgCode());
        r.setDrgName(g.drgName());
        r.setMdcCode(g.mdcCode());
        r.setWeight(g.weight());
        r.setPayStandard(pay);
        r.setActualAmount(actual);
        r.setProfitAmount(profit);
        r.setSimStatus(grouped ? DrgSimStatusEnum.GROUPED.getCode() : DrgSimStatusEnum.UNGROUPED.getCode());
        r.setRuleNote(g.ruleNote());
        r.setUpdateBy(UserUtils.getCurrentUser().getRealName());
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
        List<Long> ids = dto.getSummaryIds();
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

    /**
     * 细分组目录列表：与分组器可返回的组号一一对应，所以官方表里名字为空的「其他手术」歧义档与末组也照列
     * （名称列留空，编码员按编码认）。
     *
     * <p>权重与支付标准原样透出（官方包未含，值为 null），页面渲染成 —。
     */
    public List<DrgSimVO.GroupRow> groupList() {
        return drgSimMapper.groupList().stream()
                .map(m -> {
                    DrgSimVO.GroupRow r = new DrgSimVO.GroupRow();
                    r.setId(m.getId());
                    r.setDrgCode(m.getDrgCode());
                    r.setDrgName(m.getDrgName());
                    r.setMdcCode(m.getMdcCode());
                    r.setAdrgCode(m.getAdrgCode());
                    r.setWeight(m.getWeight());
                    r.setPayStandard(m.getPayStandard());
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
