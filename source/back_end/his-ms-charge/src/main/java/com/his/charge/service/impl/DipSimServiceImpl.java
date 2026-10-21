package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.DipSimStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.charge.dto.DipSimDTO;
import com.his.charge.entity.DipSimResult;
import com.his.charge.mapper.DipSimMapper;
import com.his.charge.service.DipSimService;
import com.his.charge.support.DipFacts;
import com.his.charge.support.DipGrouper;
import com.his.charge.support.DipSchemeCache;
import com.his.charge.vo.DipSimVO;
import com.his.charge.vo.DipSummaryListRowVO;
import com.his.charge.vo.DipSummaryRowVO;
import com.his.spi.service.SystemConfigFacadeService;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DIP 分组模拟服务。
 *
 * <p>金额链路：结算分值 = 基准分值 × 病例组合系数；预测结算金额 = 结算分值 × 点值；
 * 盈亏 = 结算金额 - 实际费用。基准分值 / 点值任一未下发（统筹区未接入）时，后续金额一律 NULL，
 * 页面渲染成 —，不拿 0 冒充。
 */
@Service
@RequiredArgsConstructor
public class DipSimServiceImpl extends ServiceImpl<DipSimMapper, DipSimResult> implements DipSimService {

    private final DipSimMapper dipSimMapper;
    private final DipGrouper grouper;
    private final DipSchemeCache dipSchemeCache;
    private final SystemConfigFacadeService sysConfigService;

    /**
     * 金额归一到两位小数；没有值就是 null，不兜 0。
     */
    private static BigDecimal money(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 结算分值归一到四位小数（biz_dip_sim_result.score decimal(10,4)）。
     */
    private static BigDecimal score(BigDecimal base, BigDecimal coeff) {
        if (base == null || coeff == null) {
            return null;
        }
        return base.multiply(coeff).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 点值（元/分值）：系统参数 dip.point_value，未配置或非法时返回 null（结算金额不计算）。
     */
    private BigDecimal pointValue() {
        String v = sysConfigService.getConfigValue(SystemConfigKeyConst.DIP_POINT_VALUE, null);
        if (!TextUtil.hasText(v)) {
            return null;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DipSimVO.SimResult simulate(DipSimDTO.Simulate dto) {
        DipSummaryRowVO s = dipSimMapper.selectSummary(dto.getSummaryId());
        if (s == null) {
            throw new BusinessException("病案首页不存在");
        }
        String icd = TextUtil.hasText(dto.getIcdCode()) ? dto.getIcdCode().trim() : s.getMainDiagnosisCode();
        String icdName = TextUtil.hasText(dto.getIcdName()) ? dto.getIcdName().trim() : s.getMainDiagnosisName();
        if (!TextUtil.hasText(icd)) {
            throw new BusinessException("该首页主诊断编码为空，请先补录 ICD 编码再模拟");
        }
        // DIP 病种 = 主诊断 + 手术操作组合：首页主手术可有多条，全部参与匹配；
        // 入参补录的操作覆盖首页（首页缺操作时用）
        List<String> mainOpers;
        String operName;
        if (TextUtil.hasText(dto.getOperCode())) {
            mainOpers = List.of(dto.getOperCode().trim());
            operName = TextUtil.trimToNull(dto.getOperName());
        } else {
            mainOpers = s.getAdmissionId() == null ? List.of() : dipSimMapper.selectMainOperCodes(s.getAdmissionId());
            // 首页查询不带手术名称快照，操作名称留空（biz_dip_sim_result.oper_name 允许 NULL）
            operName = null;
        }
        List<String> otherDiags = s.getAdmissionId() == null ? List.of() : dipSimMapper.selectOtherDiagCodes(s.getAdmissionId());
        BigDecimal actual = money(s.getActualAmount());

        String version = TextUtil.hasText(dto.getVersion()) ? dto.getVersion().trim() : dipSchemeCache.getDefaultVersion();
        DipGrouper.GroupResult g = grouper.group(new DipFacts(icd, mainOpers, otherDiags, s.getAge(), s.getAgeUnit()), version);
        boolean grouped = g.grouped();
        BigDecimal base = g.baseScore();
        BigDecimal coeff = g.coeff();
        BigDecimal sc = score(base, coeff);
        BigDecimal point = pointValue();
        // 结算金额与盈亏：分值、点值任一未下发就留 null，不拿 0 减实际费用冒充结余/超支
        BigDecimal settle = grouped && sc != null && point != null ? money(sc.multiply(point)) : null;
        BigDecimal profit = grouped && settle != null && actual != null ? money(settle.subtract(actual)) : null;

        // upsert：每首页每版本一条，重跑覆盖（含软删行复活）
        DipSimResult r = dipSimMapper.selectOne(new LambdaQueryWrapper<DipSimResult>()
                .eq(DipSimResult::getSummaryId, dto.getSummaryId())
                .eq(DipSimResult::getVersion, version)
                .last("LIMIT 1"));
        boolean create = r == null;
        if (create) {
            r = new DipSimResult();
            r.setSummaryId(dto.getSummaryId());
            r.setCreateBy(UserUtils.getCurrentUser().getRealName());
        }
        r.setPatientName(s.getPatientName());
        r.setMainDiagCode(icd);
        r.setMainDiagName(icdName);
        r.setOperCode(mainOpers.isEmpty() ? null : mainOpers.get(0));
        r.setOperName(operName);
        r.setDiseaseType(g.diseaseType());
        r.setDipCode(g.dipCode());
        r.setDipName(g.dipName());
        r.setVersion(version);
        r.setBaseScore(base);
        r.setCoeff(coeff);
        r.setScore(sc);
        r.setPointValue(point);
        r.setSettleAmount(settle);
        r.setActualAmount(actual);
        r.setProfitAmount(profit);
        r.setSimStatus(grouped ? DipSimStatusEnum.GROUPED.getCode() : DipSimStatusEnum.UNGROUPED.getCode());
        r.setRuleNote(g.ruleNote());
        r.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (create) {
            try {
                dipSimMapper.insert(r);
            } catch (DuplicateKeyException e) {
                // 并发 upsert 兜底：uk_summary(summary_id, version) 拦截了同键并发插入，
                // 说明另一事务已建行 —— 改为按 (summary_id, version) 覆盖，与「重跑覆盖」语义一致
                int rows = dipSimMapper.update(r, new LambdaUpdateWrapper<DipSimResult>()
                        .eq(DipSimResult::getSummaryId, r.getSummaryId())
                        .eq(DipSimResult::getVersion, version));
                if (rows == 0) {
                    throw e;
                }
            }
        } else {
            dipSimMapper.updateById(r);
        }
        return toSimVo(r);
    }

    /**
     * 批量模拟：入参为空取最近 50 条；主诊断编码为空的行跳过并在结果中标注
     */
    @Transactional(rollbackFor = Exception.class)
    public DipSimVO.SummaryListVO simulateBatch(DipSimDTO.SimulateBatch dto) {
        List<Long> ids = dto.getSummaryIds();
        List<DipSummaryRowVO> summaries;
        if (ids != null && !ids.isEmpty()) {
            summaries = new ArrayList<>();
            for (Long id : ids) {
                DipSummaryRowVO s = dipSimMapper.selectSummary(id);
                if (s == null) {
                    throw new BusinessException("病案首页不存在：" + id);
                }
                summaries.add(s);
            }
        } else {
            summaries = dipSimMapper.selectSummaries(50);
        }
        int skipped = 0;
        for (DipSummaryRowVO s : summaries) {
            if (!TextUtil.hasText(s.getMainDiagnosisCode())) {
                skipped++;
                continue;
            }
            DipSimDTO.Simulate one = new DipSimDTO.Simulate();
            one.setSummaryId(s.getSummaryId());
            one.setVersion(dto.getVersion());
            simulate(one);
        }
        return summaryList(null, dto.getVersion(), skipped);
    }

    public IPage<DipSimVO.ResultRow> resultPage(DipSimDTO.ResultQuery dto) {
        LambdaQueryWrapper<DipSimResult> qw = new LambdaQueryWrapper<DipSimResult>()
                .eq(dto.getSimStatus() != null, DipSimResult::getSimStatus, dto.getSimStatus())
                .eq(TextUtil.hasText(dto.getVersion()), DipSimResult::getVersion, dto.getVersion())
                .and(TextUtil.hasText(dto.getKeyword()), w -> w
                        .like(DipSimResult::getPatientName, TextUtil.trimToEmpty(dto.getKeyword()))
                        .or().like(DipSimResult::getDipCode, TextUtil.trimToEmpty(dto.getKeyword())))
                .orderByDesc(DipSimResult::getUpdateTime)
                .orderByDesc(DipSimResult::getId);
        IPage<DipSimResult> page = dipSimMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toRow);
    }

    /**
     * 可模拟首页列表 + 汇总统计
     */
    public DipSimVO.SummaryListVO summaryList(Integer limit, String version, Integer extraSkipped) {
        String v = TextUtil.hasText(version) ? version.trim() : dipSchemeCache.getDefaultVersion();
        List<DipSummaryListRowVO> rows = dipSimMapper.summaryList(limit == null ? 50 : limit, v);
        List<DipSimVO.SummaryRow> list = rows.stream().map(m -> {
            DipSimVO.SummaryRow r = new DipSimVO.SummaryRow();
            r.setSummaryId(m.getSummaryId());
            r.setPatientName(m.getPatientName());
            r.setDeptName(m.getDeptName());
            r.setDischargeTime(m.getDischargeTime());
            r.setMainDiagCode(m.getMainDiagnosisCode());
            r.setMainDiagName(m.getMainDiagnosisName());
            r.setIsSurgery(m.getIsSurgery());
            r.setSimDipCode(m.getDipCode());
            r.setSimProfit(money(m.getProfitAmount()));
            return r;
        }).collect(Collectors.toList());

        // 统计只算当前版本（结果按版本隔离），否则切版本时汇总卡会把其他版本的计数也并进来
        List<DipSimResult> all = dipSimMapper.selectList(new LambdaQueryWrapper<DipSimResult>()
                .eq(DipSimResult::getDelFlag, 0)
                .eq(DipSimResult::getVersion, v));
        DipSimVO.SimStat stat = new DipSimVO.SimStat();
        stat.setTotal((long) all.size());
        stat.setGrouped(all.stream().filter(x -> DipSimStatusEnum.GROUPED.is(x.getSimStatus())).count());
        stat.setUngrouped(all.stream().filter(x -> DipSimStatusEnum.UNGROUPED.is(x.getSimStatus())).count());
        stat.setTotalProfit(all.stream()
                .filter(x -> x.getProfitAmount() != null && x.getProfitAmount().signum() > 0)
                .map(DipSimResult::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
        stat.setTotalOverrun(all.stream()
                .filter(x -> x.getProfitAmount() != null && x.getProfitAmount().signum() < 0)
                .map(DipSimResult::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs().setScale(2, RoundingMode.HALF_UP));
        stat.setTotal(stat.getTotal() + (extraSkipped == null ? 0 : extraSkipped));
        DipSimVO.SummaryListVO vo = new DipSimVO.SummaryListVO();
        vo.setRows(list);
        vo.setStat(stat);
        return vo;
    }

    /**
     * 病种目录列表：与分组器可返回的病种码一一对应。
     * 基准分值原样透出（统筹区未下发时为 null），页面渲染成 —。
     */
    public List<DipSimVO.DiseaseRow> diseaseList(String version) {
        String v = TextUtil.hasText(version) ? version.trim() : dipSchemeCache.getDefaultVersion();
        return dipSimMapper.diseaseList(v).stream()
                .map(m -> {
                    DipSimVO.DiseaseRow r = new DipSimVO.DiseaseRow();
                    r.setId(m.getId());
                    r.setDiseaseCode(m.getDiseaseCode());
                    r.setDiseaseName(m.getDiseaseName());
                    r.setDiseaseType(m.getDiseaseType());
                    r.setMainDiagCode(m.getMainDiagCode());
                    r.setMainDiagName(m.getMainDiagName());
                    r.setOperCode(m.getOperCode());
                    r.setOperName(m.getOperName());
                    r.setDiagMatch(m.getDiagMatch());
                    r.setBaseScore(m.getBaseScore());
                    r.setSource(m.getSource());
                    r.setVersion(m.getVersion());
                    r.setStatus(m.getStatus());
                    return r;
                }).collect(Collectors.toList());
    }

    private DipSimVO.SimResult toSimVo(DipSimResult r) {
        DipSimVO.SimResult vo = new DipSimVO.SimResult();
        vo.setSummaryId(r.getSummaryId());
        vo.setPatientName(r.getPatientName());
        vo.setMainDiagCode(r.getMainDiagCode());
        vo.setMainDiagName(r.getMainDiagName());
        vo.setOperCode(r.getOperCode());
        vo.setOperName(r.getOperName());
        vo.setDipCode(r.getDipCode());
        vo.setDipName(r.getDipName());
        vo.setDiseaseType(r.getDiseaseType());
        vo.setVersion(r.getVersion());
        vo.setBaseScore(r.getBaseScore());
        vo.setCoeff(r.getCoeff());
        vo.setScore(r.getScore());
        vo.setPointValue(r.getPointValue());
        vo.setSettleAmount(r.getSettleAmount());
        vo.setActualAmount(r.getActualAmount());
        vo.setProfitAmount(r.getProfitAmount());
        vo.setSimStatus(r.getSimStatus());
        vo.setRuleNote(r.getRuleNote());
        return vo;
    }

    private DipSimVO.ResultRow toRow(DipSimResult r) {
        DipSimVO.ResultRow vo = new DipSimVO.ResultRow();
        vo.setId(r.getId());
        vo.setSummaryId(r.getSummaryId());
        vo.setPatientName(r.getPatientName());
        vo.setMainDiagCode(r.getMainDiagCode());
        vo.setMainDiagName(r.getMainDiagName());
        vo.setOperCode(r.getOperCode());
        vo.setOperName(r.getOperName());
        vo.setDiseaseType(r.getDiseaseType());
        vo.setDipCode(r.getDipCode());
        vo.setDipName(r.getDipName());
        vo.setVersion(r.getVersion());
        vo.setBaseScore(r.getBaseScore());
        vo.setCoeff(r.getCoeff());
        vo.setScore(r.getScore());
        vo.setPointValue(r.getPointValue());
        vo.setSettleAmount(r.getSettleAmount());
        vo.setActualAmount(r.getActualAmount());
        vo.setProfitAmount(r.getProfitAmount());
        vo.setSimStatus(r.getSimStatus());
        vo.setRuleNote(r.getRuleNote());
        vo.setUpdateTime(r.getUpdateTime());
        return vo;
    }
}
