package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.AntibioticStatsGenerateDTO;
import com.his.pharmacy.dto.AntibioticStatsQueryPageDTO;
import com.his.pharmacy.dto.IncisionReviewQueryPageDTO;
import com.his.pharmacy.dto.IncisionReviewUpsertDTO;
import com.his.pharmacy.entity.BizAntibioticIncisionReview;
import com.his.pharmacy.entity.BizAntibioticStats;
import com.his.pharmacy.enums.AntibioticProblemTypeEnum;
import com.his.pharmacy.mapper.AntibioticCatalogMapper;
import com.his.pharmacy.mapper.AntibioticStatMapper;
import com.his.pharmacy.mapper.BizAntibioticIncisionReviewMapper;
import com.his.pharmacy.mapper.BizAntibioticStatsMapper;
import com.his.pharmacy.service.AntibioticMonitorService;
import com.his.pharmacy.vo.AntibioticStatsVO;
import com.his.pharmacy.vo.DeptCountRowVO;
import com.his.pharmacy.vo.IncisionCandidateVO;
import com.his.pharmacy.vo.IncisionDrugCandidateVO;
import com.his.pharmacy.vo.IncisionReviewVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抗菌药物使用监测与 I 类切口预防用药点评。
 *
 * <p>要点：
 * ① 指标计算在 {@link #compute} 一处，试算与落库共用 —— 试算对不上快照就是 bug，不是"口径不同"；
 * ② 送检率是<b>简化口径</b>（同次住院是否有微生物送检，不做时序比对），VO 的 remark 每次都带上这句，
 *    防止有人拿着简化口径的数去报严格口径的表；
 * ③ I 类切口点评的结论由药师下，但服务端做一致性校验（不合理必填问题码、联合用药必填理由、
 *    特殊使用级无会诊必须挂 47）—— 药师也是人，表单不校验迟早出现"结论不合理但问题码空着"。
 */
@Service
@RequiredArgsConstructor
public class AntibioticMonitorServiceImpl implements AntibioticMonitorService {
    @Autowired
    private DictCacheService dictText;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter CSV_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int EXPORT_MAX = 5000;

    /** 评审/专项整治常用阈值，只作提示不判定（写在 VO 里给前端对照） */
    private static final BigDecimal TARGET_AUD = new BigDecimal("40.00");
    private static final BigDecimal TARGET_OP_USAGE_RATE = new BigDecimal("20.00");
    private static final BigDecimal TARGET_IP_USAGE_RATE = new BigDecimal("60.00");
    private static final BigDecimal TARGET_MICRO_RATE = new BigDecimal("50.00");

    private final AntibioticStatMapper statMapper;
    private final BizAntibioticStatsMapper statsMapper;
    private final BizAntibioticIncisionReviewMapper incisionMapper;
    private final AntibioticCatalogMapper catalogMapper;

    // 监测指标

    @Override
    public PageResult<AntibioticStatsVO> statsListPage(AntibioticStatsQueryPageDTO query) {
        LambdaQueryWrapper<BizAntibioticStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getStatMonth()), BizAntibioticStats::getStatMonth, query.getStatMonth())
                .eq(query.getScopeType() != null, BizAntibioticStats::getScopeType, query.getScopeType())
                .orderByDesc(BizAntibioticStats::getStatMonth)
                .orderByAsc(BizAntibioticStats::getScopeType)
                .orderByAsc(BizAntibioticStats::getId);
        Page<BizAntibioticStats> page = statsMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        List<AntibioticStatsVO> vos = page.getRecords().stream().map(this::toStatsVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public AntibioticStatsVO previewStats(String statMonth) {
        YearMonth ym = requireMonth(statMonth);
        BizAntibioticStats row = compute(statMonth, ym.atDay(1), ym.atEndOfMonth(),
                BizAntibioticStats.SCOPE_HOSPITAL, null, null);
        AntibioticStatsVO vo = toStatsVO(row);
        vo.setRemark("实时试算（未落库）：与已生成快照可能存在差异，报数请以快照为准");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AntibioticStatsVO> generateStats(AntibioticStatsGenerateDTO dto) {
        YearMonth ym = requireMonth(dto.getStatMonth());
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        String operator = StringUtils.hasText(UserUtils.getCurrentEmployeeName())
                ? UserUtils.getCurrentEmployeeName() : UserUtils.getCurrentEmployeeId() == null
                ? "system" : String.valueOf(UserUtils.getCurrentEmployeeId());

        List<AntibioticStatsVO> result = new ArrayList<>();
        if (dto.getScopeType() == BizAntibioticStats.SCOPE_DEPT) {
            List<DeptCountRowVO> depts = statMapper.selectDischargeDepts(from, to);
            if (CollectionUtils.isEmpty(depts)) {
                throw new BusinessException(ym + " 没有出院患者，无法按科室生成监测指标");
            }
            for (DeptCountRowVO dept : depts) {
                BizAntibioticStats row = compute(dto.getStatMonth(), from, to,
                        BizAntibioticStats.SCOPE_DEPT, dept.getDeptId(), dept.getDeptName());
                result.add(toStatsVO(upsertRow(row, operator, dto.getRemark())));
            }
        } else {
            BizAntibioticStats row = compute(dto.getStatMonth(), from, to,
                    BizAntibioticStats.SCOPE_HOSPITAL, null, "全院");
            result.add(toStatsVO(upsertRow(row, operator, dto.getRemark())));
        }
        return result;
    }

    @Override
    public String statsExportCsv(AntibioticStatsQueryPageDTO query) {
        LambdaQueryWrapper<BizAntibioticStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getStatMonth()), BizAntibioticStats::getStatMonth, query.getStatMonth())
                .eq(query.getScopeType() != null, BizAntibioticStats::getScopeType, query.getScopeType())
                .orderByDesc(BizAntibioticStats::getStatMonth)
                .orderByAsc(BizAntibioticStats::getId);
        long total = statsMapper.selectCount(wrapper);
        if (total > EXPORT_MAX) {
            throw new BusinessException("导出上限 " + EXPORT_MAX + " 行，当前 " + total + " 行，请先按月筛选");
        }
        List<BizAntibioticStats> rows = statsMapper.selectList(wrapper);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM：Excel 打开中文不乱码
        sb.append("统计月份,范围,科室,门急诊处方数,抗菌药处方数,门诊使用率(%),出院患者数,使用抗菌药人数,住院使用率(%),"
                + "人天数,累计DDD数,使用强度AUD,使用抗菌药住院人数,微生物送检人数,送检率(%),未匹配医嘱数,生成人,生成时间\n");
        for (BizAntibioticStats r : rows) {
            sb.append(csv(r.getStatMonth())).append(',')
                    .append(r.getScopeType() != null && r.getScopeType() == BizAntibioticStats.SCOPE_DEPT ? "科室" : "全院").append(',')
                    .append(csv(r.getDeptName())).append(',')
                    .append(r.getOpRxCount()).append(',')
                    .append(r.getOpAbxRxCount()).append(',')
                    .append(r.getOpUsageRate()).append(',')
                    .append(r.getIpDischargeCount()).append(',')
                    .append(r.getIpAbxPatientCount()).append(',')
                    .append(r.getIpUsageRate()).append(',')
                    .append(r.getPatientDays()).append(',')
                    .append(r.getDdds()).append(',')
                    .append(r.getAud()).append(',')
                    .append(r.getAbxTreatCount()).append(',')
                    .append(r.getMicroSubmitCount()).append(',')
                    .append(r.getMicroSubmitRate()).append(',')
                    .append(r.getUnmatchedOrderCount()).append(',')
                    .append(csv(r.getGenerateBy())).append(',')
                    .append(r.getGenerateTime() == null ? "" : CSV_TIME_FMT.format(r.getGenerateTime()))
                    .append('\n');
        }
        return sb.toString();
    }

    /**
     * 指标复算（试算与落库共用，保证两处口径一致）。
     *
     * <p>deptId 为 null = 全院；非 null 时所有查询按科室过滤。
     */
    private BizAntibioticStats compute(String statMonth, LocalDate from, LocalDate to,
                                       int scopeType, Long deptId, String deptName) {
        BizAntibioticStats row = new BizAntibioticStats();
        row.setStatMonth(statMonth);
        row.setScopeType(scopeType);
        row.setDeptId(deptId);
        row.setDeptName(deptName);

        long opRx = statMapper.countOpRx(from, to, deptId);
        long opAbxRx = statMapper.countOpAbxRx(from, to, deptId);
        long ipDischarge = statMapper.countIpDischarge(from, to, deptId);
        long ipAbx = statMapper.countIpAbxPatient(from, to, deptId);
        long patientDays = statMapper.sumPatientDays(from, to, deptId);
        BigDecimal ddds = statMapper.sumIpDdds(from, to, deptId);
        long micro = statMapper.countMicroSubmit(from, to, deptId);
        long unmatched = statMapper.countUnmatchedOrders(from, to, deptId);

        row.setOpRxCount((int) opRx);
        row.setOpAbxRxCount((int) opAbxRx);
        row.setOpUsageRate(rate(opAbxRx, opRx));
        row.setIpDischargeCount((int) ipDischarge);
        row.setIpAbxPatientCount((int) ipAbx);
        row.setIpUsageRate(rate(ipAbx, ipDischarge));
        row.setPatientDays((int) patientDays);
        row.setDdds(ddds == null ? BigDecimal.ZERO : ddds.setScale(2, RoundingMode.HALF_UP));
        row.setAud(aud(row.getDdds(), patientDays));
        row.setAbxTreatCount((int) ipAbx);
        row.setMicroSubmitCount((int) micro);
        row.setMicroSubmitRate(rate(micro, ipAbx));
        row.setUnmatchedOrderCount((int) unmatched);
        return row;
    }

    /** 同月同范围覆盖（唯一键 uk_antibiotic_stats，不含 del_flag，不走软删） */
    private BizAntibioticStats upsertRow(BizAntibioticStats row, String operator, String remark) {
        BizAntibioticStats exist = statsMapper.selectOne(new LambdaQueryWrapper<BizAntibioticStats>()
                .eq(BizAntibioticStats::getStatMonth, row.getStatMonth())
                .eq(BizAntibioticStats::getScopeType, row.getScopeType())
                .eq(row.getDeptId() != null, BizAntibioticStats::getDeptId, row.getDeptId())
                .isNull(row.getDeptId() == null, BizAntibioticStats::getDeptId)
                .last("LIMIT 1"));
        row.setGenerateBy(operator);
        row.setGenerateTime(LocalDateTime.now());
        row.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
        if (exist == null) {
            statsMapper.insert(row);
            return row;
        }
        row.setId(exist.getId());
        statsMapper.updateById(row);
        return row;
    }

    // I 类切口预防用药点评

    @Override
    public List<IncisionCandidateVO> incisionCandidates() {
        List<IncisionCandidateVO> list = statMapper.selectIncisionCandidates(200);
        for (IncisionCandidateVO vo : list) {
            if (vo.getAdmissionId() != null && vo.getOperationTime() != null) {
                List<IncisionDrugCandidateVO> drugs = statMapper.selectPeriopAntibioticOrders(
                        vo.getAdmissionId(), vo.getOperationTime());
                for (IncisionDrugCandidateVO d : drugs) {
                    d.setAntibioticLevelText(AntibioticLevelEnum.getText(d.getAntibioticLevel()));
                    d.setMinutesFromIncision(d.getStartTime() == null ? null
                            : java.time.Duration.between(vo.getOperationTime(), d.getStartTime()).toMinutes());
                }
                vo.setDrugCandidates(drugs);
            } else {
                vo.setDrugCandidates(Collections.emptyList());
            }
        }
        return list;
    }

    @Override
    public PageResult<IncisionReviewVO> incisionReviewListPage(IncisionReviewQueryPageDTO query) {
        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        LambdaQueryWrapper<BizAntibioticIncisionReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w
                        .like(BizAntibioticIncisionReview::getPatientName, keyword)
                        .or().like(BizAntibioticIncisionReview::getOperationName, keyword))
                .eq(query.getReviewResult() != null, BizAntibioticIncisionReview::getReviewResult, query.getReviewResult())
                .orderByDesc(BizAntibioticIncisionReview::getId);
        Page<BizAntibioticIncisionReview> page = incisionMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        List<IncisionReviewVO> vos = page.getRecords().stream().map(this::toIncisionVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IncisionReviewVO incisionReviewUpsert(IncisionReviewUpsertDTO dto) {
        boolean unreasonable = dto.getReviewResult() == BizAntibioticIncisionReview.RESULT_UNREASONABLE;
        // B 类：问题码与点评意见只在「结论=不合理」时必填，条件必填留在 service
        if (unreasonable) {
            if (!StringUtils.hasText(dto.getProblemTypes())) {
                throw new BusinessException("结论为不合理时必须选择问题码");
            }
            List<String> codes = Arrays.stream(dto.getProblemTypes().split(","))
                    .map(String::trim).filter(StringUtils::hasText).toList();
            if (codes.isEmpty()) {
                throw new BusinessException("结论为不合理时必须选择问题码");
            }
            for (String c : codes) {
                if (!AntibioticProblemTypeEnum.isValid(c)) {
                    throw new BusinessException("问题码非法：" + c + "（允许 41~48）");
                }
            }
            if (!StringUtils.hasText(dto.getReviewOpinion())) {
                throw new BusinessException("结论为不合理时必须填写点评意见");
            }
        }
        // B 类：只有勾了联合用药才必填理由，条件必填
        if (dto.getComboFlag() != null && dto.getComboFlag() == 1 && !StringUtils.hasText(dto.getComboReason())) {
            throw new BusinessException("联合用药必须填写联合理由");
        }

        BizAntibioticIncisionReview entity;
        if (dto.getId() != null) {
            entity = incisionMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("点评记录不存在");
            }
        } else {
            BizAntibioticIncisionReview exist = incisionMapper.selectOne(
                    new LambdaQueryWrapper<BizAntibioticIncisionReview>()
                            .eq(BizAntibioticIncisionReview::getOperationApplyId, dto.getOperationApplyId())
                            .last("LIMIT 1"));
            if (exist != null) {
                throw new BusinessException("该手术已点评过（" + exist.getReviewNo() + "），请直接修改那条");
            }
            IncisionCandidateVO candidate = statMapper.selectIncisionCandidates(200).stream()
                    .filter(c -> c.getOperationApplyId().equals(dto.getOperationApplyId()))
                    .findFirst().orElse(null);
            entity = new BizAntibioticIncisionReview();
            entity.setReviewNo(nextReviewNo());
            entity.setOperationApplyId(dto.getOperationApplyId());
            if (candidate != null) {
                entity.setApplyNo(candidate.getApplyNo());
                entity.setAdmissionId(candidate.getAdmissionId());
                entity.setPatientId(candidate.getPatientId());
                entity.setPatientName(candidate.getPatientName());
                entity.setDeptName(candidate.getDeptName());
                entity.setOperationName(candidate.getOperationName());
                entity.setOperationCode(candidate.getOperationCode());
                entity.setOperationTime(candidate.getOperationTime());
                entity.setSurgeonName(candidate.getSurgeonName());
            } else {
                throw new BusinessException("该手术不在待点评范围内（仅已完成且切口等级为 I 类的手术）");
            }
            entity.setIncisionLevel(1);
            entity.setCreateBy(UserUtils.getCurrentEmployeeName());
        }

        entity.setDrugId(dto.getDrugId());
        if (dto.getDrugId() != null) {
            var drug = catalogMapper.selectAntibioticDrugs().stream()
                    .filter(d -> d.getId().equals(dto.getDrugId())).findFirst().orElse(null);
            entity.setDrugName(drug == null ? null : drug.getDrugName());
            entity.setAntibioticLevel(drug == null ? null : drug.getAntibioticLevel());
        } else {
            entity.setDrugName(null);
            entity.setAntibioticLevel(null);
        }
        entity.setIndicationFlag(dto.getIndicationFlag() == null ? 0 : dto.getIndicationFlag());
        entity.setTimingType(dto.getTimingType());
        entity.setCourseHours(dto.getCourseHours());
        entity.setComboFlag(dto.getComboFlag() == null ? 0 : dto.getComboFlag());
        entity.setComboReason(StringUtils.hasText(dto.getComboReason()) ? dto.getComboReason().trim() : null);
        entity.setConsultFlag(dto.getConsultFlag() == null ? 0 : dto.getConsultFlag());
        entity.setReviewResult(dto.getReviewResult());
        entity.setProblemTypes(unreasonable ? dto.getProblemTypes().trim() : null);
        entity.setReviewOpinion(StringUtils.hasText(dto.getReviewOpinion()) ? dto.getReviewOpinion().trim() : null);
        entity.setReviewerId(UserUtils.getCurrentEmployeeId());
        entity.setReviewerName(UserUtils.getCurrentEmployeeName());
        entity.setReviewTime(LocalDateTime.now());
        entity.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);

        // 一致性兜底：用了特殊使用级却没会诊同意 → 问题码必须挂 47
        if (unreasonable && entity.getAntibioticLevel() != null && entity.getAntibioticLevel() == 3
                && entity.getConsultFlag() != null && entity.getConsultFlag() == 0
                && (entity.getProblemTypes() == null
                || !entity.getProblemTypes().contains(AntibioticProblemTypeEnum.SPECIAL_USE_NO_CONSULT.getCode()))) {
            throw new BusinessException("使用特殊使用级抗菌药物且未经会诊同意时，问题码必须包含「47 特殊使用级无会诊」");
        }

        if (entity.getId() == null) {
            incisionMapper.insert(entity);
        } else {
            incisionMapper.updateById(entity);
        }
        return toIncisionVO(entity);
    }

    // 内部

    private YearMonth requireMonth(String month) {
        // C 类：实时试算入口只有 @RequestParam 的裸字符串，没有 DTO 可挂注解；生成入口的必填已由 DTO 兜住
        if (!StringUtils.hasText(month)) {
            throw new BusinessException("统计月份不能为空");
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (Exception e) {
            throw new BusinessException("统计月份格式应为 yyyy-MM");
        }
    }

    private String nextReviewNo() {
        String day = LocalDate.now().format(DAY_FMT);
        String max = incisionMapper.selectMaxReviewNo(day);
        int seq = 1;
        if (StringUtils.hasText(max) && max.length() >= 4) {
            try {
                seq = Integer.parseInt(max.substring(max.length() - 4)) + 1;
            } catch (NumberFormatException ignored) {
                seq = 1;
            }
        }
        return "KQI" + day + String.format("%04d", seq);
    }

    /** 百分比（分母为 0 记 0，不除） */
    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    /** AUD = DDDs × 100 / 人天数（人天数为 0 记 0，不除） */
    private BigDecimal aud(BigDecimal ddds, long patientDays) {
        if (patientDays <= 0 || ddds == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return ddds.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(patientDays), 2, RoundingMode.HALF_UP);
    }

    private AntibioticStatsVO toStatsVO(BizAntibioticStats e) {
        AntibioticStatsVO vo = new AntibioticStatsVO();
        vo.setId(e.getId());
        vo.setStatMonth(e.getStatMonth());
        vo.setScopeType(e.getScopeType());
        vo.setScopeTypeText(e.getScopeType() != null && e.getScopeType() == BizAntibioticStats.SCOPE_DEPT ? "科室" : "全院");
        vo.setDeptId(e.getDeptId());
        vo.setDeptName(e.getDeptName());
        vo.setOpRxCount(e.getOpRxCount());
        vo.setOpAbxRxCount(e.getOpAbxRxCount());
        vo.setOpUsageRate(e.getOpUsageRate());
        vo.setIpDischargeCount(e.getIpDischargeCount());
        vo.setIpAbxPatientCount(e.getIpAbxPatientCount());
        vo.setIpUsageRate(e.getIpUsageRate());
        vo.setPatientDays(e.getPatientDays());
        vo.setDdds(e.getDdds());
        vo.setAud(e.getAud());
        vo.setAbxTreatCount(e.getAbxTreatCount());
        vo.setMicroSubmitCount(e.getMicroSubmitCount());
        vo.setMicroSubmitRate(e.getMicroSubmitRate());
        vo.setUnmatchedOrderCount(e.getUnmatchedOrderCount());
        vo.setAudTarget(TARGET_AUD);
        vo.setOpUsageRateTarget(TARGET_OP_USAGE_RATE);
        vo.setIpUsageRateTarget(TARGET_IP_USAGE_RATE);
        vo.setMicroSubmitRateTarget(TARGET_MICRO_RATE);
        vo.setGenerateBy(e.getGenerateBy());
        vo.setGenerateTime(e.getGenerateTime());
        vo.setRemark(e.getRemark());
        return vo;
    }

    private IncisionReviewVO toIncisionVO(BizAntibioticIncisionReview e) {
        IncisionReviewVO vo = new IncisionReviewVO();
        vo.setId(e.getId());
        vo.setReviewNo(e.getReviewNo());
        vo.setOperationApplyId(e.getOperationApplyId());
        vo.setApplyNo(e.getApplyNo());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setPatientId(e.getPatientId());
        vo.setPatientName(e.getPatientName());
        vo.setDeptName(e.getDeptName());
        vo.setOperationName(e.getOperationName());
        vo.setOperationCode(e.getOperationCode());
        vo.setOperationTime(e.getOperationTime());
        vo.setSurgeonName(e.getSurgeonName());
        vo.setIncisionLevel(e.getIncisionLevel());
        vo.setDrugId(e.getDrugId());
        vo.setDrugName(e.getDrugName());
        vo.setAntibioticLevel(e.getAntibioticLevel());
        vo.setAntibioticLevelText(AntibioticLevelEnum.getText(e.getAntibioticLevel()));
        vo.setIndicationFlag(e.getIndicationFlag());
        vo.setTimingType(e.getTimingType());
        vo.setTimingTypeText(AntibioticTimingEnum.getText(e.getTimingType()));
        vo.setCourseHours(e.getCourseHours());
        vo.setComboFlag(e.getComboFlag());
        vo.setComboReason(e.getComboReason());
        vo.setConsultFlag(e.getConsultFlag());
        vo.setReviewResult(e.getReviewResult());
        vo.setReviewResultText(e.getReviewResult() == null ? null
                : e.getReviewResult() == BizAntibioticIncisionReview.RESULT_REASONABLE ? "合理" : "不合理");
        vo.setProblemTypes(e.getProblemTypes());
        vo.setProblemTypesText(problemText(e.getProblemTypes()));
        vo.setReviewOpinion(e.getReviewOpinion());
        vo.setReviewerId(e.getReviewerId());
        vo.setReviewerName(e.getReviewerName());
        vo.setReviewTime(e.getReviewTime());
        vo.setCreateBy(e.getCreateBy());
        vo.setCreateTime(e.getCreateTime());
        vo.setRemark(e.getRemark());
        return vo;
    }

    private String problemText(String codes) {
        if (!StringUtils.hasText(codes)) {
            return null;
        }
        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(AntibioticProblemTypeEnum::getText)
                .filter(StringUtils::hasText)
                .reduce((a, b) -> a + "、" + b).orElse(null);
    }

    private String csv(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        return s.contains(",") || s.contains("\"") || s.contains("\n") ? "\"" + s + "\"" : s;
    }
}