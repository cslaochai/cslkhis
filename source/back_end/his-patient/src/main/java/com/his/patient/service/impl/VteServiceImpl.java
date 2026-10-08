package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.*;
import com.his.patient.mapper.*;
import com.his.patient.service.VteService;
import com.his.patient.support.VteRules;
import com.his.patient.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * VTE 防控服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VteServiceImpl extends ServiceImpl<BizVteEventMapper, BizVteEvent> implements VteService {
    private static final int EXPORT_MAX = 5000;
    /**
     * 评审/VTE 防治中心建设常用阈值，只作提示不判定
     */
    private static final BigDecimal TARGET_ASSESS_RATE = new BigDecimal("90.00");
    private static final BigDecimal TARGET_PREVENT_RATE = new BigDecimal("90.00");

    private final VteStatMapper vteStatMapper;

    private final RedisSequenceService redisSequenceService;

    private final BizVtePreventMapper bizVtePreventMapper;

    private final BizVteEventMapper bizVteEventMapper;

    private final BizVteStatsMapper bizVteStatsMapper;

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizPatientMapper bizPatientMapper;

    private final BizNursingAssessmentMapper bizNursingAssessmentMapper;

    private final SysBedMapper sysBedMapper;

    private final DictCacheService dictCacheService;

    @Override
    public VteOverviewVO overview() {
        VteOverviewVO vo = new VteOverviewVO();
        long inHospital = vteStatMapper.countInHospital();
        long assessed = vteStatMapper.countInHospitalAssessed();
        long highRisk = vteStatMapper.countInHospitalHighRisk();
        long pending = vteStatMapper.countHighRiskPending();
        LocalDate now = LocalDate.now();
        LocalDate monthBegin = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        vo.setInHospitalCount((int) inHospital);
        vo.setInHospitalAssessedCount((int) assessed);
        vo.setInHospitalHighRiskCount((int) highRisk);
        vo.setHighRiskPendingCount((int) pending);
        vo.setHighRiskPreventRate(rate(highRisk - pending, highRisk));
        vo.setMissedAssessCount((int) Math.max(0, inHospital - assessed));
        vo.setMonthVteEventCount((int) vteStatMapper.countVteEventByDiagnoseDate(monthBegin, monthEnd));
        vo.setMonthBleedCount((int) vteStatMapper.countBleedByDiagnoseDate(monthBegin, monthEnd));
        return vo;
    }

    // 中高危名单

    @Override
    public PageResult<VteRiskListVO> riskListPage(VteRiskQueryPageDTO query) {
        if (query.getOnlyHighRisk() == null) {
            query.setOnlyHighRisk(Boolean.TRUE); // 名单页默认只看中高危——低危患者不需要占护士的视线
        }
        String kw = query.getKeyword() == null ? null : query.getKeyword().trim();
        query.setKeyword(kw);
        Page<VteRiskListVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<VteRiskListVO> result = (Page<VteRiskListVO>) bizVtePreventMapper.selectRiskPage(page, query);
        List<VteRiskListVO> records = result.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> ids = records.stream().map(VteRiskListVO::getAdmissionId).filter(Objects::nonNull).toList();
            Map<Long, List<VteMeasureStateVO>> states = new LinkedHashMap<>();
            for (Long id : ids) {
                states.put(id, new ArrayList<>());
            }
            if (!CollectionUtils.isEmpty(ids)) {
                for (VteMeasureStateVO s : bizVtePreventMapper.selectMeasureStates(ids)) {
                    states.computeIfAbsent(s.getAdmissionId(), k -> new ArrayList<>()).add(s);
                }
            }
            for (VteRiskListVO row : records) {
                decorate(row, states.getOrDefault(row.getAdmissionId(), Collections.emptyList()));
            }
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(), records);
    }

    /**
     * 补算推荐措施、落实状态（推荐条数由 SQL 带出，这里只做拼装与文案）
     */
    private void decorate(VteRiskListVO row, List<VteMeasureStateVO> states) {
        List<String> codes = VteRules.codesOf(row.getRiskLevel());
        row.setRecommendCodes(codes);
        row.setRecommendCount(codes.size());
        row.setRecommendText(codes.stream().map(VteRules::measureCodeText).reduce((a, b) -> a + " + " + b).orElse("-"));
        row.setMeasures(states);
        row.setDoneCount(row.getDoneCount() == null ? 0 : row.getDoneCount());
        if (row.getDoneCount() == 0) {
            row.setPreventStatus(0);
            row.setPreventStatusText("未落实");
        } else if (row.getDoneCount() >= row.getRecommendCount()) {
            row.setPreventStatus(2);
            row.setPreventStatusText("已落实");
        } else {
            row.setPreventStatus(1);
            row.setPreventStatusText("部分落实");
        }
    }

    // 预防措施

    @Override
    public PageResult<VtePreventVO> preventListPage(VtePreventQueryPageDTO query) {
        String kw = query.getKeyword() == null ? null : query.getKeyword().trim();
        query.setKeyword(kw);
        Page<VtePreventVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<VtePreventVO> result = (Page<VtePreventVO>) bizVtePreventMapper.selectPreventPage(page, query);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public List<VtePreventVO> preventListByAdmission(Long admissionId) {
        return bizVtePreventMapper.selectByAdmission(admissionId);
    }

    @Override
    public List<VteMeasureOptionVO> measureOptions(Integer riskLevel) {
        List<String> recommend = VteRules.codesOf(riskLevel);
        List<VteMeasureOptionVO> list = new ArrayList<>();
        for (VteRules.Measure m : VteRules.MEASURES) {
            VteMeasureOptionVO vo = new VteMeasureOptionVO();
            vo.setMeasureCode(m.code());
            vo.setMeasureType(m.type());
            vo.setMeasureName(m.name());
            vo.setDesc(m.desc());
            vo.setRecommend(recommend.contains(m.code()));
            list.add(vo);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VtePreventVO preventUpsert(VtePreventUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        VteRules.Measure measure = VteRules.measureOf(dto.getMeasureCode());
        // 保留（类别③）：措施码必须是字典里的三个码之一（能不能解析成措施是业务规则，不是「是否为空」）
        if (measure == null) {
            throw new BusinessException("措施码不合法（BASIC-基础预防 / PHYSICAL-物理预防 / DRUG-药物预防）");
        }
        Integer status = dto.getExecuteStatus();
        // 禁忌/拒绝必须给理由：否则"未落实"和"有原因未落实"在系统里是一回事，落实率就说不清
        boolean notDone = status == VtePreventStatusEnum.CONTRAINDICATION.getCode() || status == VtePreventStatusEnum.REFUSED.getCode();
        String reason = dto.getReason() == null ? null : dto.getReason().trim();
        // 保留（类别①条件必填）：reason 只在「禁忌未用 / 患者拒绝」时必填，@NotBlank 会把合法的已落实登记挡成 400
        if (notDone && !TextUtil.hasText(reason)) {
            throw new BusinessException("禁忌未用/患者拒绝必须填写原因");
        }
        if (reason != null && reason.length() > 500) {
            reason = reason.substring(0, 500);
        }

        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        NursingAssessmentVO latest = latestCaprini(dto.getAdmissionId());
        Integer riskLevel = latest == null ? null : latest.getRiskLevel();
        // 闸：低危患者不该上药物预防（出血风险大于血栓获益）—— 真要用，先把评估单改准
        if (VteRules.CODE_DRUG.equals(measure.code()) && riskLevel != null && riskLevel <= 1) {
            throw new BusinessException("该患者最新 Caprini 评估为低危，不建议使用药物预防（出血风险大于获益）；"
                    + "如病情变化请先重评 Caprini");
        }

        BizPatient patient = admission.getPatientId() == null ? null : bizPatientMapper.selectById(admission.getPatientId());
        LocalDateTime executeTime = dto.getExecuteTime();
        if (status == VtePreventStatusEnum.DONE.getCode() && executeTime == null) {
            executeTime = TimeUtil.nowSeconds();
        }
        if (status != VtePreventStatusEnum.DONE.getCode()) {
            executeTime = null;
        }

        BizVtePrevent row;
        boolean insert;
        if (dto.getId() == null) {
            row = new BizVtePrevent();
            row.setPreventNo(redisSequenceService.generateVtePreventNo());
            insert = true;
        } else {
            row = bizVtePreventMapper.selectById(dto.getId());
            if (row == null || row.getDelFlag() == null || row.getDelFlag() == 1) {
                throw new BusinessException("措施记录不存在或已删除");
            }
            insert = false;
        }
        row.setAdmissionId(admission.getAdmissionId());
        row.setPatientId(admission.getPatientId());
        row.setPatientNo(patient == null ? null : patient.getPatientNo());
        row.setPatientName(patient == null ? null : patient.getPatientName());
        row.setDeptId(admission.getDeptId());
        row.setDeptName(admission.getDeptId() == null ? null : vteStatMapper.selectDeptName(admission.getDeptId()));
        row.setWardId(admission.getWardId());
        row.setWardName(wardName(admission.getWardId()));
        row.setBedNo(bedNo(admission.getBedId()));
        row.setAssessmentId(latest == null ? null : latest.getId());
        row.setCapriniScore(latest == null ? null : latest.getTotalScore());
        row.setRiskLevel(riskLevel);
        row.setMeasureCode(measure.code());
        row.setMeasureType(measure.type());
        row.setMeasureName(TextUtil.hasText(dto.getMeasureName()) ? dto.getMeasureName().trim() : measure.name());
        row.setPlanDate(dto.getPlanDate() == null ? LocalDate.now() : dto.getPlanDate());
        row.setExecuteStatus(status);
        row.setExecuteTime(executeTime);
        row.setExecutorId(operatorUser.getEmployeeId());
        row.setExecutorName(operatorUser.getRealName());
        row.setReason(reason);
        row.setRemark(dto.getRemark() == null ? null : dto.getRemark().trim());

        if (insert) {
            try {
                bizVtePreventMapper.insert(row);
            } catch (DuplicateKeyException e) {
                throw new BusinessException("该患者已登记过「" + measure.name() + "」，请改为修改已有记录");
            }
            log.info("VTE 措施登记 住院={} 措施={} 状态={} 操作人={}", admission.getAdmissionNo(),
                    measure.code(), status, operatorUser.getRealName());
        } else {
            bizVtePreventMapper.updateById(row);
            log.info("VTE 措施修改 id={} 措施={} 状态={} 操作人={}", row.getId(), measure.code(), status, operatorUser.getRealName());
        }
        return toPreventVO(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int preventDeleteById(Long id) {
        BizVtePrevent row = bizVtePreventMapper.selectById(id);
        if (row == null || row.getDelFlag() == null || row.getDelFlag() == 1) {
            throw new BusinessException("措施记录不存在或已删除");
        }
        // 物理删：uk_vte_prevent 不含 del_flag，软删会占住键位（二次删除/重登记必撞唯一键）
        return bizVtePreventMapper.purgeById(id);
    }

    // VTE 事件

    @Override
    public PageResult<VteEventVO> eventListPage(VteEventQueryPageDTO query) {
        String kw = query.getKeyword() == null ? null : query.getKeyword().trim();
        query.setKeyword(kw);
        Page<VteEventVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<VteEventVO> result = (Page<VteEventVO>) bizVteEventMapper.selectEventPage(page, query);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public List<VteEventVO> eventListByAdmission(Long admissionId) {
        return bizVteEventMapper.selectByAdmission(admissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VteEventVO eventUpsert(VteEventUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        // 保留（类别③）：确诊日期不能落在未来（是否必填由入参注解负责）
        if (dto.getDiagnoseDate().isAfter(LocalDate.now())) {
            throw new BusinessException("确诊日期不能晚于今天");
        }
        // 入院带入的不会是"预防相关出血"——出血是预防之后才有的概念，选错会把发生率算歪
        if (dto.getEventType() == VteEventTypeEnum.BLEED.getCode() && dto.getOnsetType() == VteOnsetEnum.PRE_EXISTING.getCode()) {
            throw new BusinessException("预防相关出血不存在「入院时已存在」的情况，请改为院内发生");
        }

        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        BizPatient patient = admission.getPatientId() == null ? null : bizPatientMapper.selectById(admission.getPatientId());

        BizVteEvent row;
        boolean insert;
        if (dto.getId() == null) {
            row = new BizVteEvent();
            row.setEventNo(redisSequenceService.generateVteEventNo());
            insert = true;
        } else {
            row = bizVteEventMapper.selectById(dto.getId());
            if (row == null || row.getDelFlag() == null || row.getDelFlag() == 1) {
                throw new BusinessException("事件记录不存在或已删除");
            }
            insert = false;
        }
        row.setAdmissionId(admission.getAdmissionId());
        row.setPatientId(admission.getPatientId());
        row.setPatientNo(patient == null ? null : patient.getPatientNo());
        row.setPatientName(patient == null ? null : patient.getPatientName());
        row.setDeptId(admission.getDeptId());
        row.setDeptName(admission.getDeptId() == null ? null : vteStatMapper.selectDeptName(admission.getDeptId()));
        row.setWardId(admission.getWardId());
        row.setWardName(wardName(admission.getWardId()));
        row.setEventType(dto.getEventType());
        row.setOnsetType(dto.getOnsetType());
        row.setDiagnoseDate(dto.getDiagnoseDate());
        row.setDiagnosisBasis(dto.getDiagnosisBasis());
        row.setThrombusSite(dto.getThrombusSite() == null ? null : dto.getThrombusSite().trim());
        row.setOutcome(dto.getOutcome());
        row.setDrugPreventFlag(dto.getDrugPreventFlag() == null ? 0 : dto.getDrugPreventFlag());
        row.setReporterId(operatorUser.getEmployeeId());
        row.setReporterName(operatorUser.getRealName());
        row.setReportTime(TimeUtil.nowSeconds());
        row.setRemark(dto.getRemark() == null ? null : dto.getRemark().trim());

        if (insert) {
            bizVteEventMapper.insert(row);
            log.info("VTE 事件登记 住院={} 类型={} 时机={} 操作人={}", admission.getAdmissionNo(),
                    dto.getEventType(), dto.getOnsetType(), operatorUser.getRealName());
        } else {
            bizVteEventMapper.updateById(row);
        }
        VteEventVO vo = toEventVO(row);
        vo.setCounted(counted(row));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int eventDeleteById(Long id) {
        BizVteEvent row = bizVteEventMapper.selectById(id);
        if (row == null || row.getDelFlag() == null || row.getDelFlag() == 1) {
            throw new BusinessException("事件记录不存在或已删除");
        }
        return bizVteEventMapper.deleteById(id);
    }

    // 月度指标

    @Override
    public VteStatsVO previewStats(String statMonth) {
        YearMonth ym = requireMonth(statMonth);
        BizVteStats row = compute(statMonth, TimeUtil.dayStart(ym.atDay(1)),
                TimeUtil.dayEnd(ym.atEndOfMonth()), StatsScopeEnum.HOSPITAL.getCode(), null, null);
        VteStatsVO vo = toStatsVO(row);
        vo.setRemark("实时试算（未落库）：与已生成快照可能存在差异，报数请以快照为准");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<VteStatsVO> generateStats(VteStatsGenerateDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        YearMonth ym = requireMonth(dto.getStatMonth());
        LocalDateTime from = TimeUtil.dayStart(ym.atDay(1));
        LocalDateTime to = TimeUtil.dayEnd(ym.atEndOfMonth());
        String operator = operatorUser.getRealName();

        List<VteStatsVO> result = new ArrayList<>();
        // scopeType 合法性由 DTO 的 @InEnum 把关（1-全院 2-科室），这里只分派
        if (Objects.equals(StatsScopeEnum.DEPT.getCode(), dto.getScopeType())) {
            List<DeptCountRowVO> depts = vteStatMapper.selectDischargeDepts(from, to);
            if (CollectionUtils.isEmpty(depts)) {
                throw new BusinessException(ym + " 没有已出院患者，无法按科室生成快照");
            }
            for (DeptCountRowVO d : depts) {
                BizVteStats row = compute(dto.getStatMonth(), from, to, StatsScopeEnum.DEPT.getCode(),
                        d.getDeptId(), d.getDeptName());
                result.add(toStatsVO(upsertRow(row, operator, null)));
            }
        } else {
            BizVteStats row = compute(dto.getStatMonth(), from, to, StatsScopeEnum.HOSPITAL.getCode(), null, null);
            result.add(toStatsVO(upsertRow(row, operator, null)));
        }
        log.info("VTE 防控指标生成 月份={} 范围={} 行数={} 操作人={}", dto.getStatMonth(), dto.getScopeType(),
                result.size(), operator);
        return result;
    }

    @Override
    public PageResult<VteStatsVO> statsListPage(VteStatsQueryPageDTO query) {
        LambdaQueryWrapper<BizVteStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TextUtil.hasText(query.getStatMonth()), BizVteStats::getStatMonth, query.getStatMonth())
                .eq(query.getScopeType() != null, BizVteStats::getScopeType, query.getScopeType())
                .orderByDesc(BizVteStats::getStatMonth)
                .orderByAsc(BizVteStats::getScopeType)
                .orderByAsc(BizVteStats::getId);
        Page<BizVteStats> page = bizVteStatsMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                    Collections.emptyList());
        }
        List<VteStatsVO> vos = page.getRecords().stream().map(this::toStatsVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public String statsExportCsv(VteStatsQueryPageDTO query) {
        LambdaQueryWrapper<BizVteStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TextUtil.hasText(query.getStatMonth()), BizVteStats::getStatMonth, query.getStatMonth())
                .eq(query.getScopeType() != null, BizVteStats::getScopeType, query.getScopeType())
                .orderByDesc(BizVteStats::getStatMonth)
                .orderByAsc(BizVteStats::getScopeType)
                .orderByAsc(BizVteStats::getId)
                .last("LIMIT " + EXPORT_MAX);
        List<BizVteStats> rows = bizVteStatsMapper.selectList(wrapper);

        StringBuilder sb = new StringBuilder(1024);
        sb.append('\uFEFF'); // BOM：Excel 打开中文不乱码
        sb.append("统计月份,范围,科室,出院患者数,已评估人数,评估率(%),中高危人数,中高危占比(%),"
                + "措施已落实人数,措施落实率(%),院内新发VTE人数,院内VTE发生率(%),预防相关出血人数,生成人,生成时间\n");
        for (BizVteStats r : rows) {
            sb.append(csv(r.getStatMonth())).append(',')
                    .append(r.getScopeType() != null && r.getScopeType() == StatsScopeEnum.DEPT.getCode() ? "科室" : "全院")
                    .append(',')
                    .append(csv(r.getDeptName())).append(',')
                    .append(r.getDischargeCount()).append(',')
                    .append(r.getAssessedCount()).append(',')
                    .append(r.getAssessRate()).append(',')
                    .append(r.getHighRiskCount()).append(',')
                    .append(r.getHighRiskRate()).append(',')
                    .append(r.getPreventDoneCount()).append(',')
                    .append(r.getPreventRate()).append(',')
                    .append(r.getVteEventCount()).append(',')
                    .append(r.getVteIncidenceRate()).append(',')
                    .append(r.getBleedCount()).append(',')
                    .append(csv(r.getGenerateBy())).append(',')
                    .append(r.getGenerateTime() == null ? "" : DateFormats.DATETIME.format(r.getGenerateTime()))
                    .append('\n');
        }
        return sb.toString();
    }

    /**
     * 指标复算（试算与落库共用，保证两处口径一致）。
     */
    private BizVteStats compute(String statMonth, LocalDateTime from, LocalDateTime to,
                                int scopeType, Long deptId, String deptName) {
        BizVteStats row = new BizVteStats();
        row.setStatMonth(statMonth);
        row.setScopeType(scopeType);
        row.setDeptId(deptId);
        row.setDeptName(deptName);

        long discharge = vteStatMapper.countDischarge(from, to, deptId);
        long assessed = vteStatMapper.countAssessed(from, to, deptId);
        long highRisk = vteStatMapper.countHighRisk(from, to, deptId);
        long preventDone = vteStatMapper.countPreventDone(from, to, deptId);
        long vteEvent = vteStatMapper.countVteEvent(from, to, deptId);
        long bleed = vteStatMapper.countBleed(from, to, deptId);

        row.setDischargeCount((int) discharge);
        row.setAssessedCount((int) assessed);
        row.setAssessRate(rate(assessed, discharge));
        row.setHighRiskCount((int) highRisk);
        row.setHighRiskRate(rate(highRisk, assessed));
        row.setPreventDoneCount((int) preventDone);
        row.setPreventRate(rate(preventDone, highRisk));
        row.setVteEventCount((int) vteEvent);
        row.setVteIncidenceRate(rate(vteEvent, discharge));
        row.setBleedCount((int) bleed);
        return row;
    }

    /**
     * 同月同范围覆盖（唯一键 uk_vte_stats，不含 del_flag，不走软删）
     */
    private BizVteStats upsertRow(BizVteStats row, String operator, String remark) {
        BizVteStats exist = bizVteStatsMapper.selectOne(new LambdaQueryWrapper<BizVteStats>()
                .eq(BizVteStats::getStatMonth, row.getStatMonth())
                .eq(BizVteStats::getScopeType, row.getScopeType())
                .eq(row.getDeptId() != null, BizVteStats::getDeptId, row.getDeptId())
                .isNull(row.getDeptId() == null, BizVteStats::getDeptId)
                .last("LIMIT 1"));
        row.setGenerateBy(operator);
        row.setGenerateTime(TimeUtil.nowSeconds());
        row.setRemark(TextUtil.hasText(remark) ? remark.trim() : null);
        if (exist == null) {
            bizVteStatsMapper.insert(row);
            return row;
        }
        row.setId(exist.getId());
        bizVteStatsMapper.updateById(row);
        return row;
    }

    // 工具

    private YearMonth requireMonth(String statMonth) {
        // C-非 web 入参：私有 requireXxx helper，除 DTO 入口外还被 previewStats 的 GET 标量参数复用
        // （@RequestParam String 只保证「带了参数」，空串照样进来），Bean Validation 不覆盖，保留
        if (!TextUtil.hasText(statMonth)) {
            throw new BusinessException("统计月份不能为空");
        }
        try {
            return YearMonth.parse(statMonth.trim());
        } catch (Exception e) {
            throw new BusinessException("统计月份格式不正确（yyyy-MM）");
        }
    }

    /**
     * 百分比（分母为 0 返回 0.00，不返回 NaN —— 空表跑出 NaN 会让人以为系统坏了）
     */
    private BigDecimal rate(long num, long den) {
        if (den <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(num).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(den), 2, RoundingMode.HALF_UP);
    }

    private NursingAssessmentVO latestCaprini(Long admissionId) {
        List<NursingAssessmentVO> list = bizNursingAssessmentMapper.selectLatestByAdmission(admissionId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        for (NursingAssessmentVO vo : list) {
            if (vo.getAssessType() != null && vo.getAssessType() == 4) {
                return vo;
            }
        }
        return null;
    }

    private String wardName(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = sysBedMapper.selectWardById(wardId);
        return ward == null ? null : ward.getWardName();
    }

    private String bedNo(Long bedId) {
        if (bedId == null) {
            return null;
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        return bed == null ? null : bed.getBedNo();
    }

    private VtePreventVO toPreventVO(BizVtePrevent r) {
        VtePreventVO vo = new VtePreventVO();
        vo.setId(r.getId());
        vo.setPreventNo(r.getPreventNo());
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setPatientNo(r.getPatientNo());
        vo.setDeptName(r.getDeptName());
        vo.setWardName(r.getWardName());
        vo.setBedNo(r.getBedNo());
        vo.setAssessmentId(r.getAssessmentId());
        vo.setCapriniScore(r.getCapriniScore());
        vo.setRiskLevel(r.getRiskLevel());
        vo.setRiskLevelText(r.getRiskLevel() == null ? "未评" : dictCacheService.getDicDataLabel(DictType.ASSESS_RISK_LEVEL, r.getRiskLevel()));
        vo.setMeasureCode(r.getMeasureCode());
        vo.setMeasureCodeText(VteRules.measureCodeText(r.getMeasureCode()));
        vo.setMeasureType(r.getMeasureType());
        vo.setMeasureTypeText(VteMeasureTypeEnum.getText(r.getMeasureType()));
        vo.setMeasureName(r.getMeasureName());
        vo.setPlanDate(r.getPlanDate());
        vo.setExecuteStatus(r.getExecuteStatus());
        vo.setExecuteStatusText(VtePreventStatusEnum.getText(r.getExecuteStatus()));
        vo.setExecuteTime(r.getExecuteTime());
        vo.setExecutorName(r.getExecutorName());
        vo.setReason(r.getReason());
        vo.setRemark(r.getRemark());
        return vo;
    }

    private VteEventVO toEventVO(BizVteEvent r) {
        VteEventVO vo = new VteEventVO();
        vo.setId(r.getId());
        vo.setEventNo(r.getEventNo());
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setPatientNo(r.getPatientNo());
        vo.setDeptName(r.getDeptName());
        vo.setWardName(r.getWardName());
        vo.setEventType(r.getEventType());
        vo.setEventTypeText(VteEventTypeEnum.getText(r.getEventType()));
        vo.setOnsetType(r.getOnsetType());
        vo.setOnsetTypeText(VteOnsetEnum.getText(r.getOnsetType()));
        vo.setDiagnoseDate(r.getDiagnoseDate());
        vo.setDiagnosisBasis(r.getDiagnosisBasis());
        vo.setDiagnosisBasisText(dictCacheService.getDicDataLabel(DictType.VTE_BASIS, r.getDiagnosisBasis()));
        vo.setThrombusSite(r.getThrombusSite());
        vo.setOutcome(r.getOutcome());
        vo.setOutcomeText(dictCacheService.getDicDataLabel(DictType.VTE_OUTCOME, r.getOutcome()));
        vo.setDrugPreventFlag(r.getDrugPreventFlag());
        vo.setReporterName(r.getReporterName());
        vo.setReportTime(r.getReportTime());
        vo.setCounted(counted(r));
        vo.setRemark(r.getRemark());
        return vo;
    }

    private VteStatsVO toStatsVO(BizVteStats r) {
        VteStatsVO vo = new VteStatsVO();
        vo.setId(r.getId());
        vo.setStatMonth(r.getStatMonth());
        vo.setScopeType(r.getScopeType());
        vo.setScopeTypeText(r.getScopeType() != null && r.getScopeType() == StatsScopeEnum.DEPT.getCode() ? "科室" : "全院");
        vo.setDeptId(r.getDeptId());
        vo.setDeptName(r.getDeptName());
        vo.setDischargeCount(r.getDischargeCount());
        vo.setAssessedCount(r.getAssessedCount());
        vo.setAssessRate(r.getAssessRate());
        vo.setHighRiskCount(r.getHighRiskCount());
        vo.setHighRiskRate(r.getHighRiskRate());
        vo.setPreventDoneCount(r.getPreventDoneCount());
        vo.setPreventRate(r.getPreventRate());
        vo.setVteEventCount(r.getVteEventCount());
        vo.setVteIncidenceRate(r.getVteIncidenceRate());
        vo.setBleedCount(r.getBleedCount());
        vo.setAssessRateTarget(TARGET_ASSESS_RATE);
        vo.setPreventRateTarget(TARGET_PREVENT_RATE);
        vo.setGenerateBy(r.getGenerateBy());
        vo.setGenerateTime(r.getGenerateTime());
        vo.setRemark(r.getRemark());
        return vo;
    }

    /**
     * 是否计入院内 VTE 发生率：DVT/PE 且院内发生。
     * 入院带入(2)与预防相关出血(3)都不算 —— 这是发生率口径的唯一判定点。
     */
    private boolean counted(BizVteEvent r) {
        return r.getEventType() != null && r.getEventType() != VteEventTypeEnum.BLEED.getCode()
                && r.getOnsetType() != null && r.getOnsetType() == VteOnsetEnum.IN_HOSPITAL.getCode();
    }

    private String csv(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}