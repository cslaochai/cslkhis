package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.*;
import com.his.emr.entity.BizPathway;
import com.his.emr.entity.BizPathwayEnroll;
import com.his.emr.entity.BizPathwayStep;
import com.his.emr.entity.BizPathwayVariance;
import com.his.emr.enums.PathwayEnrollStatusEnum;
import com.his.emr.enums.PathwayStatusEnum;
import com.his.emr.mapper.BizPathwayEnrollMapper;
import com.his.emr.mapper.BizPathwayMapper;
import com.his.emr.mapper.BizPathwayStepMapper;
import com.his.emr.mapper.BizPathwayVarianceMapper;
import com.his.emr.service.PathwayService;
import com.his.emr.vo.*;
import com.his.security.DeptScopeGuard;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 临床路径服务实现。
 *
 * <p>口径：
 * <ol>
 *   <li>模板 = 定义 + 路径日步骤（文书，不生成医嘱不计费）；1草稿（可编辑）→ 2使用中
 *       （锁定，同码仅一张，发布时回算 total_days）→ 3已停用（不再可入径，存量入径不受影响）。</li>
 *   <li>入径：一次住院仅一条在径；患者/科室/诊断/模板全快照，快照以服务端按 admissionId
 *       重查为准；入径日不早于入院时间、不晚于今天。</li>
 *   <li>变异：追加式台账，仅在径可登，day_no ∈ [1, total_days]，原因必填截 200；
 *       登记后回算 enroll.variance_count。有变异不挡完成，偏离大走退径（原因必填）。</li>
 *   <li>路径日 currentDay 派生不落库：min(total_days, 结束日 - 入径日 + 1)。</li>
 *   <li>操作人一律服务端取当前登录人。跨模块读（入院记录/患者基本信息/科室）走裸 SQL。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PathwayServiceImpl implements PathwayService {

    /**
     * 原因类文本统一截 200（列宽 255，留余量，超长会把业务失败升级成 Data too long 500）
     */
    private static final int REASON_MAX = 200;

    private final BizPathwayMapper pathwayMapper;
    private final BizPathwayStepMapper stepMapper;
    private final BizPathwayEnrollMapper enrollMapper;
    private final BizPathwayVarianceMapper varianceMapper;
    private final RedisSequenceService sequenceService;

    // 模板

    /**
     * 路径日派生：在径按今天封顶；终态按 finish_date 定格。下限 1（入径当天=第 1 路径日）。
     */
    private static Integer deriveCurrentDay(Integer status, LocalDate enrollDate, LocalDate finishDate, Integer totalDays) {
        if (enrollDate == null || totalDays == null || totalDays <= 0) {
            return null;
        }
        LocalDate end = Objects.equals(status, PathwayEnrollStatusEnum.ENROLLED.getCode()) || finishDate == null
                ? LocalDate.now() : finishDate;
        long day = ChronoUnit.DAYS.between(enrollDate, end) + 1;
        if (day < 1) {
            day = 1;
        }
        return (int) Math.min(day, totalDays);
    }

    private static Double rate(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0) {
            return null;
        }
        return BigDecimal.valueOf(numerator == null ? 0 : numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static Long sum(List<Long> values) {
        return values.stream().filter(Objects::nonNull).mapToLong(Long::longValue).sum();
    }

    private static LocalDate parseDate(String dateTimeText) {
        if (!StringUtils.hasText(dateTimeText) || dateTimeText.length() < 10) {
            return null;
        }
        return LocalDate.parse(dateTimeText.substring(0, 10));
    }

    private static void validateEnrollDate(LocalDate enrollDate, LocalDate admitDate) {
        if (enrollDate.isAfter(LocalDate.now())) {
            throw new BusinessException("入径日期不能是未来日期");
        }
        if (admitDate != null && enrollDate.isBefore(admitDate)) {
            throw new BusinessException("入径日期不能早于入院日期（" + admitDate + "）");
        }
    }

    private static String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    // 入径 / 变异 / 终态

    private static String cutToNull(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return cut(text.trim(), max);
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public PageResult<PathwayVO> listPage(PathwayQueryPageDTO dto) {
        Page<PathwayVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        // mapper 返回 List 时结果只在返回值里，page.getRecords() 不会被 MP 回填
        List<PathwayVO> records = pathwayMapper.selectPathwayPage(page, trimToNull(dto.getKeyword()),
                dto.getStatus(), dto.getDeptId());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public PathwayVO getDetailById(Long id) {
        PathwayVO vo = pathwayMapper.selectPathwayById(id);
        if (vo == null) {
            throw new BusinessException("路径模板不存在或已删除");
        }
        vo.setSteps(stepMapper.selectStepsByPathwayId(id));
        return vo;
    }

    @Override
    public List<PathwayVO> activeSelectList(Long deptId) {
        return pathwayMapper.selectActiveList(deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayVO pathwayUpsert(PathwayUpsertDTO dto) {
        String code = trimToNull(dto.getPathwayCode());
        BizPathway entity;
        if (dto.getId() == null) {
            entity = new BizPathway();
            entity.setPathwayCode(code);
            entity.setStatus(PathwayStatusEnum.DRAFT.getCode());
            entity.setTotalDays(0);
        } else {
            entity = requirePathway(dto.getId());
            if (!Objects.equals(entity.getStatus(), PathwayStatusEnum.DRAFT.getCode())) {
                throw new BusinessException("仅草稿模板允许编辑（使用中/已停用已锁定）");
            }
        }
        entity.setPathwayName(dto.getPathwayName().trim());
        entity.setPathwayCode(code);
        entity.setDiagnosis(trimToNull(dto.getDiagnosis()));
        entity.setVersion(StringUtils.hasText(dto.getVersion()) ? dto.getVersion().trim() : "V1");
        entity.setRemark(dto.getRemark());
        // updateById 跳过 null 字段：科室只支持改值，不支持清空
        if (dto.getDeptId() != null) {
            entity.setDeptId(dto.getDeptId());
            entity.setDeptName(pathwayMapper.selectDeptName(dto.getDeptId()));
        }
        if (entity.getId() == null) {
            pathwayMapper.insert(entity);
        } else {
            pathwayMapper.updateById(entity);
        }

        // 步骤整组替换（逻辑删旧行再插新行），草稿同步回算总日数供预览
        stepMapper.delete(new LambdaQueryWrapper<BizPathwayStep>()
                .eq(BizPathwayStep::getPathwayId, entity.getId()));
        if (dto.getSteps() != null) {
            int sort = 0;
            for (PathwayUpsertDTO.StepItem s : dto.getSteps()) {
                BizPathwayStep step = new BizPathwayStep();
                step.setPathwayId(entity.getId());
                step.setDayNo(s.getDayNo());
                step.setItemType(s.getItemType());
                step.setItemName(s.getItemName().trim());
                step.setItemCode(trimToNull(s.getItemCode()));
                step.setContent(trimToNull(s.getContent()));
                step.setSortNo(s.getSortNo() != null ? s.getSortNo() : ++sort);
                stepMapper.insert(step);
            }
        }
        applyTotalDays(entity);
        return getDetailById(entity.getId());
    }

    // 变异分析

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayVO publishPathway(PathwayActionDTO dto) {
        BizPathway pathway = requirePathway(dto.getId());
        if (!Objects.equals(pathway.getStatus(), PathwayStatusEnum.DRAFT.getCode())) {
            throw new BusinessException("仅草稿模板允许发布");
        }
        Integer maxDay = stepMapper.selectMaxDayNo(pathway.getId());
        if (maxDay == null || maxDay <= 0) {
            throw new BusinessException("模板还没有路径步骤，请先维护步骤再发布");
        }
        if (pathwayMapper.countOtherActiveByCode(pathway.getPathwayCode(), pathway.getId()) > 0) {
            throw new BusinessException("编码 " + pathway.getPathwayCode() + " 已有使用中版本，请先停用或升版本号");
        }
        pathway.setTotalDays(maxDay);
        pathway.setStatus(PathwayStatusEnum.ACTIVE.getCode());
        pathway.setPublishBy(currentOperator());
        pathway.setPublishTime(now());
        if (pathwayMapper.updateById(pathway) <= 0) {
            throw new BusinessException("发布失败");
        }
        return getDetailById(pathway.getId());
    }

    // 医生站软约束（只读，不拦截）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayVO deprecatePathway(PathwayActionDTO dto) {
        BizPathway pathway = requirePathway(dto.getId());
        if (!Objects.equals(pathway.getStatus(), PathwayStatusEnum.ACTIVE.getCode())) {
            throw new BusinessException("仅使用中模板允许停用");
        }
        pathway.setStatus(PathwayStatusEnum.DEPRECATED.getCode());
        if (pathwayMapper.updateById(pathway) <= 0) {
            throw new BusinessException("停用失败");
        }
        return getDetailById(pathway.getId());
    }

    @Override
    public PageResult<PathwayEnrollVO> enrollListPage(EnrollQueryPageDTO dto) {
        Page<PathwayEnrollVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<PathwayEnrollVO> records = enrollMapper.selectEnrollPage(page, dto.getPathwayId(),
                dto.getDeptId(), dto.getStatus(), dto.getEnrollDate(), trimToNull(dto.getPatientName()));
        records.forEach(v -> v.setCurrentDay(deriveCurrentDay(v.getStatus(), v.getEnrollDate(),
                v.getFinishDate(), v.getTotalDays())));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 内部

    @Override
    public PathwayEnrollVO enrollGetDetailById(Long id) {
        PathwayEnrollVO vo = enrollMapper.selectEnrollById(id);
        if (vo == null) {
            throw new BusinessException("入径记录不存在或已删除");
        }
        vo.setCurrentDay(deriveCurrentDay(vo.getStatus(), vo.getEnrollDate(), vo.getFinishDate(), vo.getTotalDays()));
        vo.setSteps(stepMapper.selectStepsByPathwayId(vo.getPathwayId()));
        vo.setVariances(varianceMapper.selectVariancesByEnrollId(id));
        return vo;
    }

    @Override
    public List<PathwayAdmissionVO> admissionsForEnroll(String keyword, Integer limit) {
        int n = limit != null && limit > 0 && limit <= 200 ? limit : 20;
        return enrollMapper.selectAdmissionCandidates(trimToNull(keyword), n);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayEnrollVO enrollUpsert(EnrollUpsertDTO dto) {
        BizPathwayEnroll enroll;
        if (dto.getId() == null) {
            BizPathway pathway = pathwayMapper.selectById(dto.getPathwayId());
            if (pathway == null) {
                throw new BusinessException("路径模板不存在或已删除");
            }
            if (!Objects.equals(pathway.getStatus(), PathwayStatusEnum.ACTIVE.getCode())) {
                throw new BusinessException("仅「使用中」模板允许入径");
            }
            PathwayAdmissionVO snap = enrollMapper.selectAdmissionSnapshot(dto.getAdmissionId());
            if (snap == null) {
                throw new BusinessException("入院记录不存在");
            }
            if (!Objects.equals(snap.getAdmitStatus(), 1)) {
                throw new BusinessException("患者不在院，不能入径");
            }
            if (enrollMapper.countActiveByAdmission(dto.getAdmissionId()) > 0) {
                throw new BusinessException("该次住院已有在径记录（一次住院同时仅一条）");
            }
            LocalDate admitDate = parseDate(snap.getAdmitTime());
            validateEnrollDate(dto.getEnrollDate(), admitDate);

            enroll = new BizPathwayEnroll();
            enroll.setEnrollNo(sequenceService.generatePathwayNo());
            enroll.setPathwayId(pathway.getId());
            enroll.setPathwayCode(pathway.getPathwayCode());
            enroll.setPathwayName(pathway.getPathwayName());
            enroll.setVersion(pathway.getVersion());
            enroll.setTotalDays(pathway.getTotalDays());
            enroll.setAdmissionId(snap.getAdmissionId());
            enroll.setPatientId(snap.getPatientId());
            enroll.setPatientNo(snap.getPatientNo());
            enroll.setPatientName(snap.getPatientName());
            enroll.setDeptId(snap.getDeptId());
            enroll.setDeptName(snap.getDeptName());
            enroll.setDiagnosis(snap.getDiagnosis());
            enroll.setEnrollDate(dto.getEnrollDate());
            enroll.setEnrollBy(currentOperator());
            enroll.setEnrollTime(now());
            enroll.setStatus(PathwayEnrollStatusEnum.ENROLLED.getCode());
            enroll.setVarianceCount(0);
            enroll.setRemark(dto.getRemark());
            enrollMapper.insert(enroll);
        } else {
            enroll = requireEnroll(dto.getId());
            if (!Objects.equals(enroll.getStatus(), PathwayEnrollStatusEnum.ENROLLED.getCode())) {
                throw new BusinessException("仅在径记录允许修改");
            }
            PathwayAdmissionVO snap = enrollMapper.selectAdmissionSnapshot(enroll.getAdmissionId());
            validateEnrollDate(dto.getEnrollDate(), snap != null ? parseDate(snap.getAdmitTime()) : null);
            enroll.setEnrollDate(dto.getEnrollDate());
            if (StringUtils.hasText(dto.getRemark())) {
                enroll.setRemark(dto.getRemark());
            }
            if (enrollMapper.updateById(enroll) <= 0) {
                throw new BusinessException("入径更新失败");
            }
        }
        return enrollGetDetailById(enroll.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayEnrollVO varianceUpsert(VarianceUpsertDTO dto) {
        BizPathwayEnroll enroll = requireEnroll(dto.getEnrollId());
        // 医生站开单入口也调本接口（ipd:order:add），科室越权在此收口
        checkDeptAccess(enroll.getDeptId());
        if (!Objects.equals(enroll.getStatus(), PathwayEnrollStatusEnum.ENROLLED.getCode())) {
            throw new BusinessException("入径已结束（已完成/已退径），不能再登记变异");
        }
        if (dto.getDayNo() <= 0 || dto.getDayNo() > enroll.getTotalDays()) {
            throw new BusinessException("路径日须在 1~" + enroll.getTotalDays() + " 之间");
        }
        if (dto.getOccurredDate().isAfter(LocalDate.now())) {
            throw new BusinessException("变异发生日期不能是未来日期");
        }
        BizPathwayVariance variance = new BizPathwayVariance();
        variance.setEnrollId(enroll.getId());
        variance.setDayNo(dto.getDayNo());
        variance.setVarianceType(dto.getVarianceType());
        // 原因截到列宽内：超长会把"登记失败"升级成 Data too long 的 500
        variance.setVarianceReason(cut(dto.getVarianceReason().trim(), REASON_MAX));
        variance.setHandling(cutToNull(dto.getHandling(), REASON_MAX));
        variance.setOccurredDate(dto.getOccurredDate());
        variance.setRecorderId(UserUtils.getCurrentEmployeeId());
        variance.setRecorderName(currentOperator());
        variance.setRecordTime(now());
        varianceMapper.insert(variance);

        recountVariance(enroll);
        return enrollGetDetailById(enroll.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayEnrollVO finishEnroll(EnrollActionDTO dto) {
        BizPathwayEnroll enroll = requireEnroll(dto.getId());
        if (!Objects.equals(enroll.getStatus(), PathwayEnrollStatusEnum.ENROLLED.getCode())) {
            throw new BusinessException("仅在径记录允许完成");
        }
        // 有变异不挡完成：完成=按计划走完或临床认可的结局
        enroll.setStatus(PathwayEnrollStatusEnum.FINISHED.getCode());
        enroll.setFinishDate(LocalDate.now());
        enroll.setFinishBy(currentOperator());
        if (StringUtils.hasText(dto.getReason())) {
            enroll.setRemark(cut(dto.getReason().trim(), 500));
        }
        if (enrollMapper.updateById(enroll) <= 0) {
            throw new BusinessException("完成更新失败");
        }
        return enrollGetDetailById(enroll.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PathwayEnrollVO abortEnroll(EnrollActionDTO dto) {
        BizPathwayEnroll enroll = requireEnroll(dto.getId());
        if (!Objects.equals(enroll.getStatus(), PathwayEnrollStatusEnum.ENROLLED.getCode())) {
            throw new BusinessException("仅在径记录允许退径");
        }
        // B 类保留：条件必填——动作 DTO 为在径多个动作共用，仅退径要求原因
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("退径必须填写原因");
        }
        enroll.setStatus(PathwayEnrollStatusEnum.ABORTED.getCode());
        enroll.setFinishDate(LocalDate.now());
        enroll.setFinishBy(currentOperator());
        enroll.setAbortReason(cut(dto.getReason().trim(), REASON_MAX));
        if (enrollMapper.updateById(enroll) <= 0) {
            throw new BusinessException("退径更新失败");
        }
        return enrollGetDetailById(enroll.getId());
    }

    @Override
    public PathwayAnalysisVO analysis(Long pathwayId) {
        PathwayAnalysisVO vo = new PathwayAnalysisVO();
        List<PathwayAnalysisVO.Row> rows = varianceMapper.selectEnrollStats(pathwayId);
        rows.forEach(r -> r.setFinishRate(rate(r.getFinishCount(), r.getEnrollCount())));
        vo.setRows(rows);
        vo.setEnrollCount(sum(rows.stream().map(PathwayAnalysisVO.Row::getEnrollCount).toList()));
        vo.setFinishCount(sum(rows.stream().map(PathwayAnalysisVO.Row::getFinishCount).toList()));
        vo.setAbortCount(sum(rows.stream().map(PathwayAnalysisVO.Row::getAbortCount).toList()));
        vo.setVarianceCount(sum(rows.stream().map(PathwayAnalysisVO.Row::getVarianceCount).toList()));
        vo.setFinishRate(rate(vo.getFinishCount(), vo.getEnrollCount()));
        vo.setTypeStats(varianceMapper.selectVarianceTypeStats(pathwayId));
        vo.setTopReasons(varianceMapper.selectTopReasons(pathwayId, 10));
        return vo;
    }

    @Override
    public PathwayEnrollVO activeEnrollByAdmission(Long admissionId) {
        BizPathwayEnroll enroll = enrollMapper.selectActiveByAdmission(admissionId);
        if (enroll == null) {
            return null;
        }
        checkDeptAccess(enroll.getDeptId());
        PathwayEnrollVO vo = enrollMapper.selectEnrollById(enroll.getId());
        vo.setCurrentDay(deriveCurrentDay(vo.getStatus(), vo.getEnrollDate(), vo.getFinishDate(), vo.getTotalDays()));
        vo.setSteps(stepMapper.selectStepsByPathwayId(vo.getPathwayId()));
        return vo;
    }

    @Override
    public OrderCheckVO orderCheck(OrderCheckDTO dto) {
        OrderCheckVO vo = new OrderCheckVO();
        BizPathwayEnroll enroll = enrollMapper.selectActiveByAdmission(dto.getAdmissionId());
        if (enroll == null) {
            vo.setEnrolled(false);
            return vo;
        }
        checkDeptAccess(enroll.getDeptId());
        vo.setEnrolled(true);
        vo.setEnrollId(enroll.getId());
        vo.setPathwayName(enroll.getPathwayName());
        vo.setVersion(enroll.getVersion());
        vo.setTotalDays(enroll.getTotalDays());
        Integer day = deriveCurrentDay(enroll.getStatus(), enroll.getEnrollDate(), enroll.getFinishDate(),
                enroll.getTotalDays());
        vo.setDayNo(day);

        List<PathwayStepVO> steps = stepMapper.selectStepsByPathwayId(enroll.getPathwayId());
        vo.setPlanSteps(steps.stream().filter(s -> Objects.equals(s.getDayNo(), day)).toList());
        // 纯文书模板（没有任何带编码步骤）不做偏离比对，避免自由文本模糊匹配误报刷屏
        List<PathwayStepVO> coded = steps.stream().filter(s -> StringUtils.hasText(s.getItemCode())).toList();
        vo.setComparable(!coded.isEmpty());
        if (vo.getComparable() && dto.getItems() != null) {
            Set<String> seen = new HashSet<>();
            for (OrderCheckDTO.CheckItem item : dto.getItems()) {
                String code = trimToNull(item.getItemCode());
                String name = trimToNull(item.getItemName());
                if (code == null && name == null) {
                    continue;
                }
                boolean onPath = coded.stream().anyMatch(s ->
                        (code != null && code.equals(s.getItemCode()))
                                || (name != null && name.equals(s.getItemName())));
                if (!onPath && seen.add(name != null ? name : code)) {
                    vo.getDeviations().add(new OrderCheckVO.Deviation(code, name));
                }
            }
        }
        return vo;
    }

    /**
     * 受限账号（如病区医生）只能碰授权科室患者的路径数据
     */
    private void checkDeptAccess(Long deptId) {
        Set<Long> allowed = DeptScopeGuard.allowedDeptIds();
        if (allowed != null && (deptId == null || !allowed.contains(deptId))) {
            throw new BusinessException("无权访问该科室患者的路径数据");
        }
    }

    /**
     * 草稿步骤变化后回算总日数（发布时再固化一次）
     */
    private void applyTotalDays(BizPathway pathway) {
        Integer maxDay = stepMapper.selectMaxDayNo(pathway.getId());
        int total = maxDay != null && maxDay > 0 ? maxDay : 0;
        if (!Objects.equals(pathway.getTotalDays(), total)) {
            pathway.setTotalDays(total);
            pathwayMapper.updateById(pathway);
        }
    }

    private void recountVariance(BizPathwayEnroll enroll) {
        long cnt = varianceMapper.selectCount(new LambdaQueryWrapper<BizPathwayVariance>()
                .eq(BizPathwayVariance::getEnrollId, enroll.getId()));
        if (!Objects.equals(enroll.getVarianceCount(), (int) cnt)) {
            enroll.setVarianceCount((int) cnt);
            enrollMapper.updateById(enroll);
        }
    }

    private BizPathway requirePathway(Long id) {
        BizPathway pathway = pathwayMapper.selectById(id);
        if (pathway == null) {
            throw new BusinessException("路径模板不存在或已删除");
        }
        return pathway;
    }

    private BizPathwayEnroll requireEnroll(Long id) {
        BizPathwayEnroll enroll = enrollMapper.selectById(id);
        if (enroll == null) {
            throw new BusinessException("入径记录不存在或已删除");
        }
        return enroll;
    }

    private String currentOperator() {
        String name = UserUtils.getCurrentEmployeeName();
        return StringUtils.hasText(name) ? name : "system";
    }
}
