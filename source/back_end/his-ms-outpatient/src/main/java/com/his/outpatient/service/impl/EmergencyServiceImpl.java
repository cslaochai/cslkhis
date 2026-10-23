package com.his.outpatient.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.outpatient.dto.*;
import com.his.outpatient.entity.*;
import com.his.outpatient.entity.Queue;
import com.his.common.enums.*;
import com.his.outpatient.mapper.*;
import com.his.outpatient.service.EmergencyService;
import com.his.outpatient.service.DutyRosterService;
import com.his.outpatient.support.EmergencyObservationPolicy;
import com.his.outpatient.support.EmergencyWaitPolicy;
import com.his.common.support.EmergencyMewsRules;
import com.his.common.support.EmergencyTriageRules;
import com.his.outpatient.vo.*;
import com.his.common.base.PageResult;
import com.his.common.enums.EmergencyAssignTypeEnum;
import com.his.common.enums.EmergencyStatusEnum;
import com.his.common.enums.EmergencyTriageLevelEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.spi.service.RedisSequenceService;
import com.his.common.support.EmpTitleCode;
import com.his.common.util.ShiftCoverUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.spi.dto.InpatientAdmitDTO;
import com.his.spi.dto.EmergencyPatientCreateDTO;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.service.InpatientFacadeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.PatientMasterVO;
import com.his.spi.vo.BedVO;
import com.his.spi.vo.WardVO;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.EmployeeBriefVO;
import com.his.spi.vo.UserBriefVO;
import com.his.common.enums.BizTypeEnum;
import com.his.spi.service.DeptScopeService;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.spi.vo.DutyOfficerBriefVO;
import com.his.spi.service.SysMessageService;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyServiceImpl extends ServiceImpl<EmergencyMapper, Emergency> implements EmergencyService {

    /**
     * 候诊是否超时的判定式（读时算，不落列）。target_see_minutes 为 NULL 的存量行按未定级时限收口。
     */
    private static final String OVERDUE_SQL = "admission_time IS NOT NULL AND TIMESTAMPDIFF(MINUTE, admission_time, NOW()) > IFNULL(target_see_minutes, {0})";

    /**
     * 留观已超该小时数（读时算，不落列）；状态=3 的收口写在各处 wrapper 里，本式只管时间
     */
    private static final String OBS_OVER_SQL = "TIMESTAMPDIFF(HOUR, observation_start_time, NOW()) >= {0}";

    /**
     * 待复评判定式（读时算，不落列）：候诊中 ∧ 入急诊已超过应接诊时限 ∧
     * 距上次复评（从未复评则从入急诊算）又已经过一个时限。
     * <p>分诊规范的原话是「候诊时间超过 X 后，应再次进行评估及分级」——所以两个条件都要：
     * 只判「超时」会催已经复评过的人，只判「复评间隔」会漏掉从没复评过的人。
     */
    private static final String REEVAL_DUE_SQL = "emergency_status = 1 "
            + "AND TIMESTAMPDIFF(MINUTE, admission_time, NOW()) > IFNULL(target_see_minutes, {0}) "
            + "AND (last_reeval_time IS NULL OR TIMESTAMPDIFF(MINUTE, last_reeval_time, NOW()) >= IFNULL(target_see_minutes, {0}))";

    // 无人可发待办时的兜底接收人（用户名或员工ID）的 key 收口在 SystemConfigConstKey.EMERGENCY_WAIT_FALLBACK_RECEIVER

    private final QueueMapper queueMapper;

    private final EmergencyReevalMapper emergencyReevalMapper;

    private final AppointInfoMapper appointInfoMapper;

    private final EmergencyWaitPolicy waitPolicy;

    private final EmergencyObservationPolicy obsPolicy;

    private final SystemFacadeService systemFacadeService;

    /**
     * 当日总值班（全院协调兜底人）
     */
    private final DutyRosterService dutyRosterService;

    private final EmergencyHandoverMapper emergencyHandoverMapper;

    private final EmergencyHandoverItemMapper emergencyHandoverItemMapper;

    private final RedisSequenceService redisSequenceService;

    private final PatientFacadeService patientFacadeService;
    private final InpatientFacadeService inpatientFacadeService;

    private final SysMessageService sysMessageService;

    private final DeptScopeService deptScopeService;

    private final com.his.spi.service.DutyRosterFacadeService dutyRosterFacadeService;

    private final com.his.spi.service.ClinicSourceFacadeService clinicSourceFacadeService;

    @Override
    public PageResult<BizEmergencyVO> listPage(EmergencyQueryDTO queryDTO) {
        LambdaQueryWrapper<Emergency> wrapper = new LambdaQueryWrapper<>();
        List<Long> scoped = deptScopeService.scopedDeptIds(null);
        if (scoped != null) {
            wrapper.in(Emergency::getDeptId, scoped);
        }
        wrapper.eq(queryDTO.getTriageLevel() != null, Emergency::getTriageLevel, queryDTO.getTriageLevel())
                .eq(queryDTO.getEmergencyStatus() != null, Emergency::getEmergencyStatus, queryDTO.getEmergencyStatus())
                .and(TextUtil.hasText(queryDTO.getKeyword()), w -> w
                        .like(Emergency::getPatientName, queryDTO.getKeyword())
                        .or().like(Emergency::getEmergencyNo, queryDTO.getKeyword())
                        .or().like(Emergency::getChiefComplaint, queryDTO.getKeyword()));

        if (Boolean.TRUE.equals(queryDTO.getUnassignedOnly())) {
            // 待派单池 = 候诊中且没有接诊医生；这就是「急诊池」在本系统里的唯一口径
            wrapper.eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode())
                    .isNull(Emergency::getDoctorId);
        }
        boolean overdueOnly = Boolean.TRUE.equals(queryDTO.getOverdueOnly());
        boolean reevalDueOnly = Boolean.TRUE.equals(queryDTO.getReevalDueOnly());
        boolean observationBoard = queryDTO.getObservationMinHours() != null;
        if (overdueOnly && observationBoard) {
            throw new BusinessException("「只看超时候诊」与「留观榜」不能同时开启，请只保留一个");
        }
        if (overdueOnly) {
            wrapper.eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode())
                    .apply(OVERDUE_SQL, waitPolicy.deadlineMinutes(null));
        }
        // 待复评是「超时且又超一个时限没复评」：严格子集，与「只看超时」并行不冲突，
        // 但单独勾选时也要按最老的排最前（它和救火队列一样是催命榜）
        if (reevalDueOnly) {
            wrapper.apply(REEVAL_DUE_SQL, waitPolicy.deadlineMinutes(null));
        }
        if (observationBoard) {
            wrapper.eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.OBSERVATION.getCode())
                    .apply(OBS_OVER_SQL, queryDTO.getObservationMinHours());
        }

        // 只看超时=救火队列、待复评=分诊台的催命榜、留观榜=占床最久优先，三者都是最老的排最前；其余保持新建在前
        if (overdueOnly || reevalDueOnly) {
            wrapper.orderByAsc(Emergency::getAdmissionTime);
        } else if (observationBoard) {
            wrapper.orderByAsc(Emergency::getObservationStartTime);
        } else {
            wrapper.orderByDesc(Emergency::getCreateTime);
        }

        Page<Emergency> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<BizEmergencyVO> records = page.getRecords() == null ? List.of()
                : page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    /**
     * 实体 → 出参，候诊时长与超时档位在这里现算
     */
    private BizEmergencyVO toVO(Emergency entity) {
        BizEmergencyVO vo = new BizEmergencyVO();
        BeanUtils.copyProperties(entity, vo);
        EmergencyAssignTypeEnum assignType = EmergencyAssignTypeEnum.fromCode(entity.getAssignType());
        vo.setAssignTypeText(assignType == null ? "" : assignType.getLabel());
        LocalDateTime endAt = waitingNow(entity)
                ? LocalDateTime.now()
                : (entity.getDiagnosisTime() == null ? null : entity.getDiagnosisTime());
        if (endAt != null && entity.getAdmissionTime() != null) {
            long minutes = Math.max(0, Duration.between(entity.getAdmissionTime(), endAt).toMinutes());
            vo.setWaitMinutes(minutes);
            // 超时只对「还在等」的人有意义：已经接诊的患者不该继续挂红标
            if (waitingNow(entity)) {
                int level = EmergencyWaitPolicy.overdueLevelOf(minutes, entity.getTargetSeeMinutes());
                vo.setOverdueLevel(level);
                vo.setOverdueText(EmergencyWaitPolicy.overdueText(level));
            } else {
                vo.setOverdueLevel(0);
                vo.setOverdueText("");
            }
        } else {
            vo.setOverdueLevel(0);
            vo.setOverdueText("");
        }
        vo.setObsHours(obsHoursOf(entity));
        vo.setObsLevel(obsLevelOf(entity));
        vo.setObsLevelText(EmergencyObservationPolicy.levelText(vo.getObsLevel()));
        // 分诊三件套：意识文案 / MEWS 快照 / 待复评标记。意识文案走枚举出口（null=「未采」），
        // 不许前端把 null 显示成空白——「没采」和「采了但正常」在看板上必须长得不一样
        vo.setConsciousnessText(EmergencyConsciousnessEnum.getLabel(entity.getConsciousness()));
        vo.setReevalDue(reevalDueOf(entity, vo.getWaitMinutes()));
        return vo;
    }

    /**
     * 待复评判定（读时算）：候诊中 ∧ 已过应接诊时限 ∧ 距上次复评（或入急诊）又已过一个时限。
     * 与 {@link #REEVAL_DUE_SQL} 同一口径——列表筛选、统计卡片、行内徽章三处必须同源，
     * 否则分诊台会看到「筛出来 3 个人、卡片说 5 个」。
     */
    private Boolean reevalDueOf(Emergency entity, Long waitMinutes) {
        if (!waitingNow(entity) || waitMinutes == null || entity.getAdmissionTime() == null) {
            return Boolean.FALSE;
        }
        int target = entity.getTargetSeeMinutes() == null
                ? waitPolicy.deadlineMinutes(null) : Math.max(entity.getTargetSeeMinutes(), 0);
        if (waitMinutes <= target) {
            return Boolean.FALSE;
        }
        LocalDateTime since = entity.getLastReevalTime() == null ? entity.getAdmissionTime() : entity.getLastReevalTime();
        long sinceMinutes = Math.max(0, Duration.between(since, LocalDateTime.now()).toMinutes());
        return sinceMinutes >= target;
    }

    private boolean waitingNow(Emergency entity) {
        return Objects.equals(entity.getEmergencyStatus(), EmergencyStatusEnum.WAITING.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(BizEmergencyUpsertDTO upsertDTO) {
        PatientMasterVO bizPatient;
        // 新患者模式：patientId为空时，先创建患者记录（取号/身份证补全/写入口校验归患者域）
        if (upsertDTO.getPatientId() == null) {
            EmergencyPatientCreateDTO createDTO = new EmergencyPatientCreateDTO();
            createDTO.setPatientName(upsertDTO.getPatientName());
            createDTO.setGender(upsertDTO.getGender());
            createDTO.setAge(upsertDTO.getAge());
            createDTO.setPhone(upsertDTO.getPhone());
            createDTO.setIdCard(upsertDTO.getIdCard());
            bizPatient = patientFacadeService.createEmergencyPatient(createDTO);
        } else {
            bizPatient = patientFacadeService.getPatient(upsertDTO.getPatientId());
            if (bizPatient == null) {
                throw new BusinessException("未能找到对应患者信息");
            }
        }

        Emergency emergency = BeanUtil.copyProperties(upsertDTO, Emergency.class);
        emergency.setEmergencyNo(redisSequenceService.generateEmergencyNo());
        emergency.setEmergencyStatus(EmergencyStatusEnum.WAITING.getCode()); // 候诊
        emergency.setAdmissionTime(LocalDateTime.now());
        // 新患者模式下，使用新创建的患者信息
        emergency.setPatientId(bizPatient.getId());
        emergency.setPatientNo(bizPatient.getPatientNo());
        // 姓名/性别/年龄一律以患者主档为准：前端漏传 patient_name 时原先直接 500
        // （该列 NOT NULL 无默认值），且不应信任客户端自报的人口学信息
        emergency.setPatientName(bizPatient.getPatientName());
        emergency.setGender(bizPatient.getGender());
        emergency.setAge(bizPatient.getAge());
        if (emergency.getZone() == null) {
            // 根据分诊级别自动分配区域
            emergency.setZone(getZoneByLevel(emergency.getTriageLevel()));
        }
        // MEWS 登记即算（WS/T 810—2022 五项版）。前端评分卡只是预览，
        // 入库存的一律是服务端按同一份规则算出来的分——两端各算一套必然对不上账
        emergency.setMewsScore(mewsScoreOf(emergency.getVitalSigns(), emergency.getConsciousness()));
        emergency.setReevalCount(0);
        // dept_name 是 NOT NULL 无默认值的快照列：只传 deptId 的调用原先直接 500
        // （报「系统内部错误」，连原因都不透出）。科室名属于参照数据，服务端按 ID 补齐比信任客户端更稳。
        if (!TextUtil.hasText(emergency.getDeptName())) {
            DeptBriefVO dept = systemFacadeService.findDepartment(emergency.getDeptId());
            if (dept == null) {
                throw new BusinessException("接诊科室不存在：" + emergency.getDeptId());
            }
            emergency.setDeptName(dept.getDeptName());
        }
        deptScopeService.assertDeptAccessible(emergency.getDeptId());
        applyDispatch(emergency);
        this.save(emergency);

        // 创建挂号记录（急诊号），保持数据一致性
        AppointInfo appointInfo = new AppointInfo();
        appointInfo.setRegistNo(redisSequenceService.generateAppointNo());
        // 新患者模式下 patientId 为 null，需要先创建患者记录
        appointInfo.setPatientId(emergency.getPatientId());

        appointInfo.setGender(bizPatient.getGender());
        appointInfo.setPatientNo(bizPatient.getPatientNo());
        appointInfo.setPatientName(bizPatient.getPatientName());
        appointInfo.setGender(bizPatient.getGender());
        appointInfo.setAge(bizPatient.getAge());
        appointInfo.setPhone(bizPatient.getPhone());
        appointInfo.setVisitType(VisitTypeEnum.FIRST_VISIT.getCode());
        appointInfo.setDeptId(emergency.getDeptId());
        appointInfo.setDeptName(emergency.getDeptName());
        appointInfo.setDoctorId(emergency.getDoctorId());
        appointInfo.setDoctorName(emergency.getDoctorName());
        appointInfo.setRegistType(RegistTypeEnum.EMERGENCY.getCode()); // 急诊号
        appointInfo.setRegistSource(1); // 窗口挂号
        appointInfo.setRegistTime(LocalDateTime.now());
        appointInfo.setArriveTime(LocalDateTime.now());
        appointInfo.setVisitDate(LocalDate.now());
        appointInfo.setRegistStatus(AppointStatusEnum.REGISTERED.getCode()); // 已挂号（未签到，急诊直录不签）
        appointInfoMapper.insert(appointInfo);

        // 同步创建就诊队列，让医生工作站可以看到急诊患者。
        // 叫号号码用短号「急+当日5位流水」（如急00035）：原先直接塞整张 JZ2026092600035 单号，
        // 诊室大屏 38px 大字下 15 个字符把患者姓名挤出可视区 —— 真实 HIS 的口径就是
        // 「叫号屏只放短号，全量急诊号留在病历/登记表上」。全量号仍留在急诊记录的急诊编号字段上，不丢。
        Queue queue = new Queue();
        String erNo = emergency.getEmergencyNo();
        queue.setQueueNo("急" + erNo.substring(erNo.length() - 5));
        queue.setRegistId(appointInfo.getId()); // 关联挂号记录
        queue.setPatientId(emergency.getPatientId());
        queue.setPatientNo(emergency.getPatientNo());
        queue.setPatientName(emergency.getPatientName());
        queue.setDeptId(emergency.getDeptId());
        queue.setArriveTime(LocalDateTime.now());
        queue.setDeptName(emergency.getDeptName());
        queue.setDoctorId(emergency.getDoctorId());
        queue.setDoctorName(emergency.getDoctorName());
        queue.setQueueType(QueueTypeEnum.PRIORITY.getCode());
        queue.setQueueStatus(QueueStatusEnum.WAITING.getCode()); // 候诊中
        queue.setSequenceNo(0);
        queue.setRegistType(RegistTypeEnum.EMERGENCY.getCode()); // 急诊挂号
        // 叫号可见性三件套，缺一个急诊患者就永远进不了医生站：
        // callNext/getTodayQueueList 都按 visit_date=今天取号，triage_level 决定
        // 「I 级心梗排在 IV 级扭伤前面」是否真的成立（原先这三列全 NULL，队列行是死账）。
        // 急诊分级在登记表单就已定，所以 triage_status 直接记 1-已核验，不占门诊分诊台的待核验数。
        queue.setVisitDate(LocalDate.now());
        queue.setTriageLevel(emergency.getTriageLevel());
        queue.setTriageStatus(emergency.getTriageLevel() != null ? 1 : 0);
        queue.setIsOverdue(YesOrNoEnum.NO.getCode());
        queue.setCallCount(0);
        queueMapper.insert(queue);

        return true;
    }

    // 登记即派单

    /**
     * 登记时把「该谁看这个急诊」定下来，并快照当时的应接诊时限。
     * <p>
     * 三条出路必须是三种不同的码值，因为事后完全看得出区别：
     * <ul>
     *   <li>填了医生 → 1-登记指定，不动前端选的人；</li>
     *   <li>没填、当日该科室此刻有排班在岗 → 2-系统派单，按排班落到具体的人；</li>
     *   <li>没填、也确实查不到在岗医生 → 3-入池待派单，<b>要求填原因</b>并留痕。</li>
     * </ul>
     * 第三条是全系统里「急诊池」唯一的入口口径：以前它和「护士忘了选医生」
     * 长得一模一样（doctor_id 都是 NULL），所以没人对池子负责。
     * <p>
     * 只覆盖候诊态：已定医生不因为后面排班变化被改派（改派走急诊台手工操作）。
     */
    private void applyDispatch(Emergency emergency) {
        emergency.setTargetSeeMinutes(waitPolicy.deadlineMinutes(emergency.getTriageLevel()));
        if (emergency.getDoctorId() != null) {
            // 医生名同样是快照列：只传 doctorId 的调用（API 直连、别的前端）会留下
            // 「有医生 ID、没有医生名」的行，列表按医生名判定 → 明明是派好了单，
            // 界面却显示成「待派单」。参照数据一律服务端按 ID 补齐。
            if (!TextUtil.hasText(emergency.getDoctorName())) {
                EmployeeBriefVO doctor = systemFacadeService.findEmployee(emergency.getDoctorId());
                if (doctor == null) {
                    throw new BusinessException("接诊医生不存在：" + emergency.getDoctorId());
                }
                emergency.setDoctorName(doctor.getEmpName());
            }
            emergency.setAssignType(EmergencyAssignTypeEnum.MANUAL.getCode());
            return;
        }
        ErDuty onDuty = pickOnDutySchedule(emergency.getDeptId());
        if (onDuty != null) {
            emergency.setAssignType(EmergencyAssignTypeEnum.AUTO_BY_SCHEDULE.getCode());
            emergency.setDoctorId(onDuty.getDoctorId());
            emergency.setDoctorName(onDuty.getDoctorName());
            return;
        }
        emergency.setAssignType(EmergencyAssignTypeEnum.POOL.getCode());
        String reason = TextUtil.hasText(emergency.getUnassignedReason())
                ? emergency.getUnassignedReason().trim()
                : "当日该科室无在岗排班医生，入待派单池";
        // 入参层不加 @Size：超长由服务端截断，不能让"粘贴了一长段说明"变成 400
        emergency.setUnassignedReason(TextUtil.cut(reason, 200));
    }

    /**
     * 当前时刻在岗的急诊医生排班。交接班时段两班重叠，取「最近开班的那一班」= 接班医生，
     * 否则凌晨入院的急诊会被派给已经下班的白班医生。
     */
    private ErDuty pickOnDutySchedule(Long deptId) {
        List<ErDuty> onDuty = onDutyErSchedules(deptId);
        return onDuty.stream()
                .max(Comparator.comparing(s -> ShiftCoverUtil.parseShiftTime(s.getStartTime())))
                .orElse(null);
    }

    /**
     * 急诊当班医生（此刻在岗）。
     *
     * <p>经值班表端口取该科室此刻在岗的医生（计划命中 + 已签到，端口内过滤听班 ON_CALL），
     * 映射为 ErDuty；无在岗排班时返回空列表，派单回退「入待派单池」，不编造当班人。
     * 消费点只读 ErDuty 的 doctorId/doctorName/shiftId/startTime/endTime。
     */
    private List<ErDuty> onDutyErSchedules(Long deptId) {
        return onDutyStaffSchedules(deptId, StaffTypeEnum.DOCTOR.getCode());
    }

    /**
     * 班次时段判定收口在 {@link ShiftCoverUtil}（跨零点夜班 20:00-08:00 要拆两段）。
     * 急诊派单、交班班次名、今日在岗、分诊当班护士四处共用同一份实现，
     * 免得同一个时刻在四个地方算出四份不同的在岗名单。
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(EmergencyStatusUpsertDTO statusDTO) {
        Emergency emergency = this.getById(statusDTO.getId());
        if (emergency == null) {
            throw new BusinessException("急诊记录不存在");
        }
        deptScopeService.assertDeptAccessible(emergency.getDeptId());
        Integer status = statusDTO.getStatus();
        int current = emergency.getEmergencyStatus() == null ? 1 : emergency.getEmergencyStatus();
        if (current >= 4) {
            throw new BusinessException("该急诊记录已结束（转住院/离院/死亡），不能再变更状态");
        }
        if (status == 4) {
            // 以前点一下就翻成「转住院」，账面上转了、入院记录里根本没有这个人。
            // 转住院必须走 /emergency/admit：真实入院登记（选病区床位）完成后才落终态。
            throw new BusinessException("转住院请走入院登记（需选择入院病区与床位）");
        }

        if (status == 3) {
            if (statusDTO.getObservationWardId() == null || statusDTO.getObservationBedId() == null) {
                throw new BusinessException("转入留观必须先分配留观床位");
            }
            BedVO bed = bedSelectList(statusDTO.getObservationWardId(), null).stream()
                    .filter(b -> b.getBedId().equals(statusDTO.getObservationBedId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("所选床位不属于该病区"));
            if (bed.getBedStatus() == null || bed.getBedStatus() != 1) {
                throw new BusinessException("留观床位不可用：" + bed.getBedStatusText());
            }
            // 换床（留观中再次提交）：先把旧床还回去，再占新床
            endObservation(emergency);
            inpatientFacadeService.occupyBedForObservation(bed.getBedId(), emergency.getPatientId());
            emergency.setObservationWardId(bed.getWardId());
            emergency.setObservationBedId(bed.getBedId());
            emergency.setObservationBed(bed.getBedNo());
            emergency.setObservationStartTime(LocalDateTime.now());
            emergency.setObservationEndTime(null);
        }

        if (status >= 5) {
            // 离院/死亡前先在观占床交还——必须在 emergencyStatus 改写之前
            // （endObservation 靠"当前还在留观"这个事实判断要不要放床）
            endObservation(emergency);
            emergency.setFinishTime(LocalDateTime.now());
        }
        emergency.setEmergencyStatus(status);
        if (status == 2) {
            claimDoctor(emergency);
            emergency.setDiagnosisTime(LocalDateTime.now());
        }
        // 守卫下沉到 WHERE：状态流转仅限「未结束（<4）」记录，并发终态流转时后到者 0 行失败
        boolean changed = this.update(emergency, new LambdaUpdateWrapper<Emergency>()
                .eq(Emergency::getId, emergency.getId())
                .lt(Emergency::getEmergencyStatus, EmergencyStatusEnum.ADMITTED.getCode()));
        if (!changed) {
            throw new BusinessException("该急诊记录已结束（转住院/离院/死亡），不能再变更状态");
        }
        syncAppointAndQueue(emergency, status);
        return true;
    }

    /**
     * 谁接诊谁负责：急诊共享池（登记时未派医生）在「接诊」一刻把医生写进急诊记录，
     * 由 {@link #syncAppointAndQueue} 带到挂号单与队列。
     * <p>不写队列医生，候诊队列的医师ID 恒为 NULL，医生站 callNext
     * （按 doctor_id=我收口）永远看不见这个患者——共享池就变成无人认领的死角。
     */
    private void claimDoctor(Emergency emergency) {
        Long meEmp = UserUtils.getCurrentUser().getEmployeeId();
        if (emergency.getDoctorId() != null && meEmp != null && !emergency.getDoctorId().equals(meEmp)) {
            String who = TextUtil.hasText(emergency.getDoctorName()) ? emergency.getDoctorName() : "当班医生";
            throw new BusinessException("该患者已由 " + who + " 接诊，请勿重复接诊");
        }
        if (emergency.getDoctorId() == null && meEmp != null) {
            emergency.setDoctorId(meEmp);
            emergency.setDoctorName(UserUtils.getCurrentUser().getRealName());
            // 认领要留痕：这一行原来是「入池待派单」，现在是"谁把它从池子里捞走的"。
            // 没有这一笔，超时升级永远分不清"没人管"和"有人认领了还没看"。
            emergency.setAssignType(EmergencyAssignTypeEnum.CLAIMED.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long admit(EmergencyAdmitDTO admitDTO) {
        Emergency emergency = this.getById(admitDTO.getId());
        if (emergency == null) {
            throw new BusinessException("急诊记录不存在");
        }
        deptScopeService.assertDeptAccessible(emergency.getDeptId());
        int current = emergency.getEmergencyStatus() == null ? 1 : emergency.getEmergencyStatus();
        if (current >= 4) {
            throw new BusinessException("该急诊记录已结束，不能重复转住院");
        }

        // 留观转住院常发生在本病区原床上：先释放留观床，住院分床才看得见这张空闲床。
        // 与入院登记同事务——登记失败一起回滚，不会出现「床放了、人没收」
        endObservation(emergency);

        AppointInfo appoint = findEmergencyAppoint(emergency.getPatientId());
        Long admitDoctorId = admitDTO.getAdmitDoctorId() != null ? admitDTO.getAdmitDoctorId() : emergency.getDoctorId();
        if (admitDoctorId == null) {
            admitDoctorId = UserUtils.getCurrentUser().getEmployeeId();
        }

        InpatientAdmitDTO inpatient = new InpatientAdmitDTO();
        inpatient.setPatientId(emergency.getPatientId());
        inpatient.setWardId(admitDTO.getWardId());
        inpatient.setBedId(admitDTO.getBedId());
        inpatient.setAdmitDoctorId(admitDoctorId);
        inpatient.setAdmitWay(2); // 入院途径：2-急诊（病案首页口径）
        inpatient.setRegistId(appoint == null ? null : appoint.getId());
        inpatient.setRegistNo(appoint == null ? null : appoint.getRegistNo());
        inpatient.setDiagnosis(TextUtil.hasText(admitDTO.getDiagnosis()) ? admitDTO.getDiagnosis() : emergency.getDiagnosis());
        Long admissionId = inpatientFacadeService.admit(inpatient);

        emergency.setEmergencyStatus(EmergencyStatusEnum.ADMITTED.getCode());
        emergency.setAdmissionId(admissionId);
        emergency.setFinishTime(LocalDateTime.now());
        // 守卫下沉到 WHERE：转住院仅限「未结束」记录，并发重复转住院/并发离院时后到者 0 行失败
        boolean admitted = this.update(emergency, new LambdaUpdateWrapper<Emergency>()
                .eq(Emergency::getId, emergency.getId())
                .lt(Emergency::getEmergencyStatus, EmergencyStatusEnum.ADMITTED.getCode()));
        if (!admitted) {
            throw new BusinessException("该急诊记录已结束，不能重复转住院");
        }
        syncAppointAndQueue(emergency, EmergencyStatusEnum.ADMITTED.getCode());
        return admissionId;
    }

    @Override
    public List<WardVO> wardSelectList() {
        return inpatientFacadeService.listWards();
    }

    @Override
    public List<BedVO> bedSelectList(Long wardId, Integer bedStatus) {
        return inpatientFacadeService.listBeds(wardId, null, bedStatus);
    }

    @Override
    public List<EmergencyDutyVO> dutySelectList(Long deptId) {
        deptScopeService.assertDeptAccessible(deptId);
        return onDutyErSchedules(deptId).stream().map(s -> {
            EmergencyDutyVO vo = new EmergencyDutyVO();
            vo.setDoctorId(s.getDoctorId());
            vo.setDoctorName(s.getDoctorName());
            vo.setStartTime(s.getStartTime());
            vo.setEndTime(s.getEndTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 结束留观：写结束时间并把占着的床位还给病区（必须在 emergencyStatus 被改写之前调用）
     */
    private void endObservation(Emergency emergency) {
        Integer status = emergency.getEmergencyStatus();
        if (status != null && status == 3 && emergency.getObservationBedId() != null) {
            inpatientFacadeService.releaseBedForObservation(emergency.getObservationBedId(), emergency.getPatientId());
            emergency.setObservationEndTime(LocalDateTime.now());
        }
    }

    /**
     * 状态流转回写挂号单与叫号队列。
     * <p>挂号状态必须跟着走：急诊直录的挂号单建单时是「已挂号」，
     * 如果就诊结束不回写，它永远是「未签到」——日终结转会把「已经看过的急诊患者」
     * 判成「爽约」，患者下次来会看到自己「上次爽约」。
     */
    private void syncAppointAndQueue(Emergency emergency, Integer status) {
        AppointInfo appointInfo = findEmergencyAppoint(emergency.getPatientId());
        if (appointInfo == null) {
            return;
        }
        LambdaQueryWrapper<Queue> queueWrapper = new LambdaQueryWrapper<>();
        queueWrapper.eq(Queue::getRegistId, appointInfo.getId());
        Queue queue = queueMapper.selectOne(queueWrapper);

        if (queue != null) {
            if (status == 2) {
                // 接诊 -> 就诊中；共享池认领的医生同步到队列（医生站按 doctor_id=我取号）
                if (queue.getDoctorId() == null && emergency.getDoctorId() != null) {
                    queue.setDoctorId(emergency.getDoctorId());
                    queue.setDoctorName(emergency.getDoctorName());
                }
                queue.setQueueStatus(QueueStatusEnum.CONSULTING.getCode());
                queue.setStartTime(LocalDateTime.now());
            } else if (status >= 4) {
                // 转住院/离院/死亡 -> 已就诊
                // 注意：这里曾经写成 5（=已退号），而注释写的是「已就诊」——
                // 急诊离院的患者会显示成「已退号」，退号率被凭空抬高。已改用枚举。
                queue.setQueueStatus(QueueStatusEnum.COMPLETED.getCode());
                queue.setEndTime(LocalDateTime.now());
            }
            // 守卫下沉到 WHERE：队列仍须为流转前状态（接诊<-候诊中；终态<-候诊/就诊中），
            // 并发流转时 0 行失败，避免用陈旧快照覆盖（如已离院又被并发写回就诊中）
            LambdaUpdateWrapper<Queue> queueGuard = new LambdaUpdateWrapper<Queue>()
                    .eq(Queue::getId, queue.getId());
            if (status == 2) {
                queueGuard.eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode());
            } else {
                queueGuard.in(Queue::getQueueStatus,
                        QueueStatusEnum.WAITING.getCode(), QueueStatusEnum.CONSULTING.getCode());
            }
            if (queueMapper.update(queue, queueGuard) == 0) {
                throw new BusinessException("该队列状态已变更，请刷新后重试");
            }
        }
        if (status == 2) {
            appointInfo.setRegistStatus(AppointStatusEnum.ACCEPTED.getCode());
            if (appointInfo.getDoctorId() == null && emergency.getDoctorId() != null) {
                appointInfo.setDoctorId(emergency.getDoctorId());
                appointInfo.setDoctorName(emergency.getDoctorName());
            }
        } else if (status >= 4) {
            appointInfo.setRegistStatus(AppointStatusEnum.COMPLETED.getCode());
        }
        if (status == 2 || status >= 4) {
            // 守卫下沉到 WHERE：挂号仍须为流转前状态（接诊<-已挂号；终态<-已挂号/已接诊），并发流转时 0 行失败
            LambdaUpdateWrapper<AppointInfo> appointGuard = new LambdaUpdateWrapper<AppointInfo>()
                    .eq(AppointInfo::getId, appointInfo.getId());
            if (status == 2) {
                appointGuard.eq(AppointInfo::getRegistStatus, AppointStatusEnum.REGISTERED.getCode());
            } else {
                appointGuard.in(AppointInfo::getRegistStatus,
                        AppointStatusEnum.REGISTERED.getCode(), AppointStatusEnum.ACCEPTED.getCode());
            }
            if (appointInfoMapper.update(appointInfo, appointGuard) == 0) {
                throw new BusinessException("该挂号状态已变更，请刷新后重试");
            }
        }
    }

    /**
     * 该患者最近一条急诊号挂号单（急诊直录产生，可能不存在——铺底数据没有挂号线索）
     */
    private AppointInfo findEmergencyAppoint(Long patientId) {
        LambdaQueryWrapper<AppointInfo> appointWrapper = new LambdaQueryWrapper<>();
        appointWrapper.eq(AppointInfo::getPatientId, patientId)
                .eq(AppointInfo::getRegistType, 3) // 急诊号
                .orderByDesc(AppointInfo::getCreateTime)
                .last("LIMIT 1");
        return appointInfoMapper.selectOne(appointWrapper);
    }

    @Override
    public EmergencyStatsVO getStats() {
        EmergencyStatsVO stats = new EmergencyStatsVO();
        List<Long> scoped = deptScopeService.scopedDeptIds(null);
        // 总急诊量（今日）
        LambdaQueryWrapper<Emergency> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .ge(Emergency::getCreateTime, TimeUtil.dayStart(LocalDateTime.now().toLocalDate()));
        stats.setTodayTotal(this.count(todayWrapper));

        // 候诊中
        LambdaQueryWrapper<Emergency> waitingWrapper = new LambdaQueryWrapper<>();
        waitingWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .eq(Emergency::getEmergencyStatus, 1);
        stats.setWaiting(this.count(waitingWrapper));

        // 诊治中
        LambdaQueryWrapper<Emergency> treatingWrapper = new LambdaQueryWrapper<>();
        treatingWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .eq(Emergency::getEmergencyStatus, 2);
        stats.setTreating(this.count(treatingWrapper));

        // 留观
        LambdaQueryWrapper<Emergency> obsWrapper = new LambdaQueryWrapper<>();
        obsWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .eq(Emergency::getEmergencyStatus, 3);
        stats.setObservation(this.count(obsWrapper));

        // 红区（I级+II级）
        LambdaQueryWrapper<Emergency> redWrapper = new LambdaQueryWrapper<>();
        redWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .in(Emergency::getTriageLevel, 1, 2)
                .notIn(Emergency::getEmergencyStatus, 4, 5, 6);
        stats.setRedZone(this.count(redWrapper));

        // 绿色通道
        LambdaQueryWrapper<Emergency> greenWrapper = new LambdaQueryWrapper<>();
        greenWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .isNotNull(Emergency::getGreenChannel)
                .ne(Emergency::getGreenChannel, "")
                .notIn(Emergency::getEmergencyStatus, 4, 5, 6);
        stats.setGreenChannel(this.count(greenWrapper));

        // 超时未接诊（兜底看板：这个数字不为 0 就说明有人该被追问）
        LambdaQueryWrapper<Emergency> overdueWrapper = waitingBase();
        overdueWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .apply(OVERDUE_SQL, waitPolicy.deadlineMinutes(null));
        stats.setOverdueWaiting(this.count(overdueWrapper));

        // 待派单池：候诊中且没有接诊医生
        LambdaQueryWrapper<Emergency> poolWrapper = waitingBase();
        poolWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .isNull(Emergency::getDoctorId);
        stats.setUnassignedWaiting(this.count(poolWrapper));

        // 待复评（分诊台催命榜）：候诊超时应接诊时限且又超一个时限未复评
        LambdaQueryWrapper<Emergency> reevalWrapper = waitingBase();
        reevalWrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .apply(REEVAL_DUE_SQL, waitPolicy.deadlineMinutes(null));
        stats.setReevalDue(this.count(reevalWrapper));

        // 留观两档：48 小时是"该开始张罗去向"，72 小时是"必须定下来"。
        // 分开数是为了让值班的人一眼看出差多少 —— 只有一个"超时限"时，刚过 48 的人以为不着急。
        stats.setObsOverWarn(countObservationOver(obsPolicy.warnHours(), scoped));
        stats.setObsOverMax(countObservationOver(obsThresholdOfMax(), scoped));
        // 阈值随统计一起出参：看板的「留观超 48 小时」文案由后端带出，前端不写死数字，
        // 否则系统参数一改，卡片标题和实际过滤口径就成了两套。
        stats.setObsWarnHours(obsPolicy.warnHours());
        stats.setObsMaxHours(obsThresholdOfMax());

        return stats;
    }

    /**
     * 留观中且已超该小时数（与列表的「留观榜」过滤同一式子，卡片数字点开必须就是这些人）
     */
    private long countObservationOver(int hours, List<Long> scoped) {
        LambdaQueryWrapper<Emergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(scoped != null, Emergency::getDeptId, scoped)
                .eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.OBSERVATION.getCode())
                .isNotNull(Emergency::getObservationStartTime)
                .apply(OBS_OVER_SQL, hours);
        return this.count(wrapper);
    }

    /**
     * 候诊中（status=1）的基础条件：超时数与池子数都只数"还在等的"
     */
    private LambdaQueryWrapper<Emergency> waitingBase() {
        LambdaQueryWrapper<Emergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode());
        return wrapper;
    }

    // 超时候诊升级（兜底：让"没人管"变成有人被追问）

    /**
     * 扫描「候诊中且已超过应接诊时限」的急诊，给该负责的人写站内信待办。
     * <p>
     * 收件人阶梯：
     * <ol>
     *   <li>已派单/已认领 → 那位医生；</li>
     *   <li>还在池子里（无医生）→ 该科室<b>此刻</b>在岗的排班医生（登记时没人，现在可能换班有人了），
     *       一个都没有 → 该科室职称带「主任」的在职员工；</li>
     *   <li>到「严重超时」追加同科室主任（医生催过一次还不行，就得让上级知道）；</li>
     *   <li>以上全落空 → 系统参数兜底接收人。仍然找不到人则这条本轮跳过，下一轮再试。</li>
     * </ol>
     * <p>
     * <b>可重入</b>：同一条急诊对同一收件人只催一次（{@code receiverIdsOfBiz} 判重），
     * 所以定时任务每 5 分钟跑一轮不会把收件箱刷满。真正的闭环动作是「接诊」，
     * 不是把待办点成已读 —— 接诊后 status 离开候诊，这条自然不再被扫到。
     */
    @Override
    public int escalateOverdue() {
        LambdaQueryWrapper<Emergency> wrapper = waitingBase();
        wrapper.apply(OVERDUE_SQL, waitPolicy.deadlineMinutes(null))
                .orderByAsc(Emergency::getAdmissionTime);
        List<Emergency> overdue = this.list(wrapper);
        int sent = 0;
        for (Emergency emergency : overdue) {
            try {
                sent += escalateOne(emergency);
            } catch (Exception ex) {
                // 单条失败不中断整批：升级是催办手段，一条炸了不能拖累其他条
                log.error("[急诊候诊] {} 超时升级失败：{}", emergency.getEmergencyNo(), ex.getMessage(), ex);
            }
        }
        if (sent > 0) {
            log.warn("[急诊候诊] 超时升级完成：本次发出 {} 条待办", sent);
        }
        return sent;
    }

    private int escalateOne(Emergency emergency) {
        long waitMinutes = emergency.getAdmissionTime() == null ? 0
                : Duration.between(emergency.getAdmissionTime(), LocalDateTime.now()).toMinutes();
        int target = emergency.getTargetSeeMinutes() == null
                ? waitPolicy.deadlineMinutes(emergency.getTriageLevel()) : emergency.getTargetSeeMinutes();
        int overdueLevel = EmergencyWaitPolicy.overdueLevelOf(waitMinutes, target);
        if (overdueLevel == 0) {
            return 0;
        }

        Set<Long> receivers = new LinkedHashSet<>();
        if (emergency.getDoctorId() != null) {
            receivers.add(emergency.getDoctorId());
        } else {
            onDutyErSchedules(emergency.getDeptId()).forEach(s -> receivers.add(s.getDoctorId()));
            if (receivers.isEmpty()) {
                receivers.addAll(deptLeaderIds(emergency.getDeptId()));
            }
        }
        // 严重超时：医生催过还不行，同科室主任一并收到（去重靠 LinkedHashSet，主任本人就是医生时不重发）
        if (overdueLevel >= 2) {
            receivers.addAll(deptLeaderIds(emergency.getDeptId()));
        }

        List<Long> alreadySent = sysMessageService.receiverIdsOfBiz(BizTypeEnum.EMERGENCY_WAIT.getType(), emergency.getId());
        receivers.removeAll(alreadySent);

        if (receivers.isEmpty()) {
            // 阶梯的第三级：本科室该催的人（派单医生 → 当班医生 → 科主任）都催过了还没人动手，
            // 交给**当日总值班**做全院协调 —— 在此之前这里直接落到系统参数的静态兜底人，
            // 那个口径的本质是"科室之外没人"，于是下班后/换班时这条链就断了。
            // 总值班是排班表里今天真在岗的那个人（含临时换班），换班不影响链路。
            DutyOfficerVO officer = dutyRosterService.current();
            DutyOfficerBriefVO duty = null;
            if (officer != null) {
                duty = new DutyOfficerBriefVO();
                duty.setEmployeeId(officer.getEmployeeId());
                duty.setEmployeeName(officer.getEmployeeName());
            }
            if (duty != null && duty.getEmployeeId() != null && !alreadySent.contains(duty.getEmployeeId())) {
                receivers.add(duty.getEmployeeId());
            }
        }
        if (receivers.isEmpty()) {
            // 阶梯的末级：总值班也催过了（或今天压根没排总值班），才落到系统参数兜底人，
            // 而不是继续沉默。每轮只对「还没通知过的人」发信，所以这条链是有界的：
            // 医生 → 主任 → 总值班 → 急诊台，四级都催过之后该轮返回 0，不会把收件箱刷爆。
            Receiver fallback = fallbackReceiver();
            if (fallback != null && !alreadySent.contains(fallback.employeeId())) {
                receivers.add(fallback.employeeId());
            }
        }
        if (receivers.isEmpty()) {
            return 0;
        }

        String levelText = EmergencyTriageLevelEnum.getLabel(emergency.getTriageLevel());
        String title = "急诊候诊超时：" + emergency.getPatientName() + "（" + levelText + "）";
        String content = String.format(
                "急诊 %s 已候诊 %s，应接诊时限 %s 分钟（分诊 %s，就诊科室 %s）。请立即接诊；"
                        + "本人无法处理时请在急诊台改派，不要让它留在候诊队列里。"
                        + "本科室无人响应时，本待办会自动升级到当日总值班做全院协调。",
                emergency.getEmergencyNo(), humanWait(waitMinutes), target, levelText,
                TextUtil.hasText(emergency.getDeptName()) ? emergency.getDeptName() : "-");

        int sent = 0;
        for (Long receiverId : receivers) {
            if (sendWaitTodo(receiverId, title, content, emergency, waitMinutes, target, levelText)) {
                sent++;
            }
        }
        return sent;
    }

    /**
     * 催办文本里的时长用「3小时12分钟」而不是 192 分钟 —— 收消息的人要在两秒内感到严重性
     */
    private String humanWait(long minutes) {
        if (minutes < 60) {
            return minutes + " 分钟";
        }
        return minutes / 60 + " 小时 " + minutes % 60 + " 分钟";
    }

    /**
     * 发一条待办。收件人名取自员工档案，查不到档案也要发（收件人 ID 才是事实，名字只是显示）
     */
    private boolean sendWaitTodo(Long receiverId, String title, String content, Emergency emergency,
                                 long waitMinutes, int targetMinutes, String levelText) {
        String receiverName = "站内用户";
        try {
            EmployeeBriefVO emp = systemFacadeService.findEmployee(receiverId);
            if (emp != null && TextUtil.hasText(emp.getEmpName())) {
                receiverName = emp.getEmpName();
            }
        } catch (Exception ignored) {
            // 名字查不到不影响投递
        }
        EmergencyWaitTodoPayloadVO payload = new EmergencyWaitTodoPayloadVO();
        payload.setPatientName(emergency.getPatientName());
        payload.setEmergencyNo(emergency.getEmergencyNo());
        payload.setTriageLevelText(levelText);
        payload.setDeptName(emergency.getDeptName());
        payload.setWaitMinutes(waitMinutes);
        payload.setTargetSeeMinutes(targetMinutes);
        return sysMessageService.sendSystemMessage(
                receiverId, receiverName, title, content,
                BizTypeEnum.EMERGENCY_WAIT.getType(), emergency.getId(),
                // warning 不是 urgent：全站 urgent 只留给危急值，催办把它挤占掉就等于把危急值埋了
                "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
    }

    /**
     * 同科室的副高及以上职称在职员工（301~304 副主任、401~404 主任，
     * 口径单点在 {@link EmpTitleCode#SENIOR}）。
     *
     * <p>⚠ 2026-09-28（sql/174）：职称已从中文自由文本改为职称字典码，
     * 这里原来是 likeRight(title, "主任")，改完之后会**静默永远查不到人**
     * —— 候诊超时催办从此不再通知科里任何上级，且不报错。
     */
    private List<Long> deptLeaderIds(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        return systemFacadeService.listActiveEmployeesByTitles(deptId, EmpTitleCode.SENIOR)
                .stream().map(EmployeeBriefVO::getId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 兜底接收人，配置值可以是用户名，也可以直接是员工ID。
     * 读不到/解析不出来一律返回 null —— 兜底逻辑本身不能再有静默失败，
     * 返回 null 由调用方按「这条没发出去」处理（下一轮再试），不发假的成功。
     */
    private Receiver fallbackReceiver() {
        try {
            String raw = systemFacadeService.findConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_FALLBACK_RECEIVER);
            if (!TextUtil.hasText(raw)) {
                return null;
            }
            raw = raw.trim();
            UserBriefVO user = systemFacadeService.findUserByName(raw);
            if (user != null) {
                if (user.getEmpId() == null) {
                    log.warn("[急诊候诊] 兜底接收人 {} 没有关联员工档案，无法接收站内信", raw);
                    return null;
                }
                String name = TextUtil.hasText(user.getRealName()) ? user.getRealName() : user.getUserName();
                return new Receiver(user.getEmpId(), name);
            }
            return new Receiver(Long.parseLong(raw), "兜底接收人");
        } catch (Exception ex) {
            log.warn("[急诊候诊] 读取兜底接收人配置 {} 失败：{}", SystemConfigKeyConst.EMERGENCY_WAIT_FALLBACK_RECEIVER, ex.getMessage());
            return null;
        }
    }

    private String getZoneByLevel(Integer level) {
        return EmergencyZoneEnum.zoneText(level);
    }

    /**
     * 交班弹框里的「待交班清单」口径：本科室未闭环（候诊/诊治中/留观）里
     * <b>无人指派的</b> + <b>挂在我名下的</b>。
     * <p>
     * 为什么不把"别人名下还在看的"也拉进来：那是他的交班内容，两张单混一张，
     * 事后就说不清"这个人到底是谁交出去的"。但<b>无主的行必须进清单</b> ——
     * 它谁都不交，正是 sql/145 之前那条急诊在池子里躺 288 小时的原因。
     */
    private LambdaQueryWrapper<Emergency> handoverScope(Long deptId, Long fromEmpId) {
        LambdaQueryWrapper<Emergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Emergency::getDeptId, deptId)
                .in(Emergency::getEmergencyStatus,
                        EmergencyStatusEnum.WAITING.getCode(),
                        EmergencyStatusEnum.TREATING.getCode(),
                        EmergencyStatusEnum.OBSERVATION.getCode())
                .and(w -> w.isNull(Emergency::getDoctorId).or().eq(Emergency::getDoctorId, fromEmpId))
                // 无主的排最前：它是交班要解决的第一件事；同类里等得最久的排前
                .last("ORDER BY (doctor_id IS NULL) DESC, admission_time ASC");
        return wrapper;
    }

    // 留观时限（sql/153）：读时算两档，不落列

    @Override
    public List<EmergencyHandoverPendingVO> handoverPendingList(Long deptId) {
        Long fromEmpId = requireCurrentEmployee();
        Long scopeDeptId = resolveHandoverDeptId(deptId);
        deptScopeService.assertDeptAccessible(scopeDeptId);
        return this.list(handoverScope(scopeDeptId, fromEmpId)).stream()
                .map(this::toPendingVO)
                .collect(Collectors.toList());
    }

    /**
     * 清单行出参。候诊时长/留观时长/两个档位<b>一律复用 {@link #toVO} 的算法</b>：
     * 同一件事在弹框和列表里算出两个数，值日的人第一个反应是"哪个是假的"。
     */
    private EmergencyHandoverPendingVO toPendingVO(Emergency entity) {
        BizEmergencyVO computed = toVO(entity);
        EmergencyHandoverPendingVO vo = new EmergencyHandoverPendingVO();
        vo.setEmergencyId(entity.getId());
        vo.setEmergencyNo(entity.getEmergencyNo());
        vo.setPatientId(entity.getPatientId());
        vo.setPatientName(entity.getPatientName());
        vo.setGender(entity.getGender());
        vo.setAge(entity.getAge());
        vo.setTriageLevel(entity.getTriageLevel());
        vo.setTriageLevelText(EmergencyTriageLevelEnum.getLabel(entity.getTriageLevel()));
        vo.setMewsScore(entity.getMewsScore());
        vo.setReevalDue(computed.getReevalDue());
        vo.setEmergencyStatus(entity.getEmergencyStatus());
        vo.setEmergencyStatusText(EmergencyStatusEnum.getLabel(entity.getEmergencyStatus()));
        vo.setDeptName(entity.getDeptName());
        vo.setChiefComplaint(entity.getChiefComplaint());
        vo.setDiagnosis(entity.getDiagnosis());
        vo.setDoctorId(entity.getDoctorId());
        vo.setDoctorName(entity.getDoctorName());
        vo.setPoolFlag(entity.getDoctorId() == null);
        vo.setAdmissionTime(entity.getAdmissionTime());
        vo.setTargetSeeMinutes(entity.getTargetSeeMinutes());
        vo.setWaitMinutes(computed.getWaitMinutes());
        vo.setOverdueLevel(computed.getOverdueLevel());
        vo.setOverdueText(computed.getOverdueText());
        vo.setObservationBed(entity.getObservationBed());
        vo.setObservationStartTime(entity.getObservationStartTime());
        vo.setObsHours(computed.getObsHours());
        vo.setObsLevel(computed.getObsLevel());
        vo.setObsLevelText(computed.getObsLevelText());
        return vo;
    }

    @Override
    public List<EmergencyTakeCandidateVO> handoverTakeList(Long deptId) {
        Long fromEmpId = requireCurrentEmployee();
        Long scopeDeptId = resolveHandoverDeptId(deptId);
        deptScopeService.assertDeptAccessible(scopeDeptId);
        // 先取本科室在职员工，再把"此刻在岗"的标记出来并排前面
        LinkedHashMap<Long, EmployeeBriefVO> employees = new LinkedHashMap<>();
        systemFacadeService.listActiveEmployeesByDept(scopeDeptId)
                .forEach(emp -> employees.putIfAbsent(emp.getId(), emp));
        Set<Long> onDutyIds = new LinkedHashSet<>();
        for (ErDuty schedule : onDutyErSchedules(scopeDeptId)) {
            Long doctorId = schedule.getDoctorId();
            // 排班医生未必挂本科室（跨科支援），下拉里要有他，否则"在岗"是假信息
            employees.computeIfAbsent(doctorId, id -> systemFacadeService.findEmployee(id));
            onDutyIds.add(doctorId);
        }
        return employees.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .filter(e -> !e.getKey().equals(fromEmpId))
                .sorted(Comparator
                        .comparing((Map.Entry<Long, EmployeeBriefVO> e) -> !onDutyIds.contains(e.getKey()))
                        .thenComparing(e -> e.getValue().getEmpName() == null ? "" : e.getValue().getEmpName()))
                .map(e -> toCandidate(e.getValue(), onDutyIds.contains(e.getKey()) ? "当前在岗" : "本科室"))
                .collect(Collectors.toList());
    }

    private EmergencyTakeCandidateVO toCandidate(EmployeeBriefVO emp, String sourceText) {
        EmergencyTakeCandidateVO vo = new EmergencyTakeCandidateVO();
        vo.setEmpId(emp.getId());
        vo.setEmpName(emp.getEmpName());
        vo.setDeptName(emp.getDeptName());
        vo.setSourceText(sourceText);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitHandover(EmergencyHandoverUpsertDTO submitDTO) {
        Long fromEmpId = requireCurrentEmployee();
        String fromEmpName = UserUtils.getCurrentUser().getRealName();
        if (!TextUtil.hasText(fromEmpName)) {
            EmployeeBriefVO me = systemFacadeService.findEmployee(fromEmpId);
            fromEmpName = me == null ? String.valueOf(fromEmpId) : me.getEmpName();
        }
        Long deptId = resolveHandoverDeptId(submitDTO.getDeptId());
        deptScopeService.assertDeptAccessible(deptId);
        DeptBriefVO dept = systemFacadeService.findDepartment(deptId);
        if (dept == null) {
            throw new BusinessException("交班科室不存在：" + deptId);
        }
        // 先判"是不是交给自己"再查员工状态：对着自己报"该员工已停用"是把人往错的方向查
        if (submitDTO.getTakeEmpId() != null && submitDTO.getTakeEmpId().equals(fromEmpId)) {
            throw new BusinessException("接班人不能是交出人本人：交给自己等于没有交接");
        }
        EmployeeBriefVO takeEmp = requireActiveEmployee(submitDTO.getTakeEmpId(), "接班人");

        LinkedHashMap<Long, Emergency> scope = this.list(handoverScope(deptId, fromEmpId)).stream()
                .collect(Collectors.toMap(Emergency::getId, e -> e, (a, b) -> a, LinkedHashMap::new));
        LinkedHashMap<Long, EmergencyHandoverItemDTO> submitted = matchHandoverItems(submitDTO.getItems(), scope);

        // 接续责任人：明细没单独指定就归整单接班人
        LinkedHashMap<Long, EmployeeBriefVO> takers = new LinkedHashMap<>();
        takers.put(takeEmp.getId(), takeEmp);
        LinkedHashMap<Long, Long> takeOf = new LinkedHashMap<>();
        for (EmergencyHandoverItemDTO item : submitDTO.getItems()) {
            Long takeId = item.getTakeDoctorId() == null ? takeEmp.getId() : item.getTakeDoctorId();
            if (takeId.equals(fromEmpId)) {
                throw new BusinessException("接续责任人不能是交出人本人：" + scope.get(item.getEmergencyId()).getPatientName());
            }
            takers.computeIfAbsent(takeId, id -> requireActiveEmployee(id, "接续医生"));
            takeOf.put(item.getEmergencyId(), takeId);
        }

        LocalDateTime now = LocalDateTime.now();
        EmergencyHandover handover = new EmergencyHandover();
        handover.setHandoverNo(generateHandoverNo());
        handover.setDeptId(deptId);
        handover.setDeptName(dept.getDeptName());
        handover.setFromEmpId(fromEmpId);
        handover.setFromEmpName(fromEmpName);
        handover.setTakeEmpId(takeEmp.getId());
        handover.setTakeEmpName(takeEmp.getEmpName());
        handover.setShiftName(currentShiftName(deptId, fromEmpId));
        // 滚动区间：从我上一次交班那一刻到现在（首次取当日 00:00），与班结单同一口径
        LocalDateTime lastEnd = emergencyHandoverMapper.selectLastPeriodEnd(fromEmpId, deptId);
        handover.setPeriodBegin(lastEnd == null ? TimeUtil.dayStart(now.toLocalDate()) : lastEnd);
        handover.setPeriodEnd(now);
        handover.setRemark(cutText(submitDTO.getRemark(), 500));
        handover.setPendingCount(scope.size());
        handover.setPoolCount((int) scope.values().stream().filter(e -> e.getDoctorId() == null).count());
        handover.setOverdueCount((int) scope.values().stream()
                .filter(e -> overdueOf(e) > 0).count());
        handover.setObservationCount((int) scope.values().stream()
                .filter(e -> Objects.equals(e.getEmergencyStatus(), EmergencyStatusEnum.OBSERVATION.getCode())).count());
        handover.setObsOverLimitCount((int) scope.values().stream()
                .filter(e -> obsLevelOf(e) >= 2).count());
        emergencyHandoverMapper.insert(handover);

        for (Emergency emergency : scope.values()) {
            EmergencyHandoverItemDTO item = submitted.get(emergency.getId());
            EmployeeBriefVO taker = takers.get(takeOf.get(emergency.getId()));
            emergencyHandoverItemMapper.insert(toHandoverItem(handover, emergency, item, taker, fromEmpId));
            transferResponsibility(emergency, taker, fromEmpId);
        }
        notifyTakers(handover, scope, submitted, takeOf, takers);
        log.info("[急诊交班] {} 向 {} 移交 {} 科室 {} 人（其中无主 {} 人），单号 {}",
                fromEmpName, takeEmp.getEmpName(), dept.getDeptName(), scope.size(),
                handover.getPoolCount(), handover.getHandoverNo());
        return handover.getId();
    }

    /**
     * 明细完整性核对：多交、重交、少交都在此拦住。
     * <p>
     * <b>"少交就拒绝"是本功能的支点</b>。原先急诊的池子之所以永远清不掉，
     * 就是因为交班是口头动作 —— 没人需要为"这条我漏了"负责。现在漏一条就提交不了，
     * 并且台账上留下了每一条的接续责任人。
     */
    private LinkedHashMap<Long, EmergencyHandoverItemDTO> matchHandoverItems(
            List<EmergencyHandoverItemDTO> items, LinkedHashMap<Long, Emergency> scope) {
        LinkedHashMap<Long, EmergencyHandoverItemDTO> submitted = new LinkedHashMap<>();
        for (EmergencyHandoverItemDTO item : items) {
            Emergency emergency = scope.get(item.getEmergencyId());
            if (emergency == null) {
                throw new BusinessException("记录 " + item.getEmergencyId()
                        + " 不在待交班清单（已闭环、已移交，或不属于本科室），请刷新清单后重试");
            }
            if (submitted.put(item.getEmergencyId(), item) != null) {
                throw new BusinessException("同一条急诊重复提交：" + emergency.getPatientName());
            }
        }
        List<String> missing = scope.values().stream()
                .filter(e -> !submitted.containsKey(e.getId()))
                .map(e -> e.getPatientName() + "(" + e.getEmergencyNo() + ")")
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new BusinessException("还有 " + missing.size() + " 条未点名移交："
                    + String.join("、", missing.stream().limit(10).collect(Collectors.toList()))
                    + (missing.size() > 10 ? " 等" : ""));
        }
        return submitted;
    }

    private EmergencyHandoverItem toHandoverItem(EmergencyHandover handover, Emergency emergency,
                                                 EmergencyHandoverItemDTO item, EmployeeBriefVO taker, Long fromEmpId) {
        EmergencyHandoverItem row = new EmergencyHandoverItem();
        row.setHandoverId(handover.getId());
        row.setEmergencyId(emergency.getId());
        row.setEmergencyNo(emergency.getEmergencyNo());
        row.setPatientId(emergency.getPatientId());
        row.setPatientName(emergency.getPatientName());
        row.setTriageLevel(emergency.getTriageLevel());
        row.setMewsScore(emergency.getMewsScore());
        // 交班时待复评的行要显式定格：接班的人第一件事是去复评，不是接着等
        row.setReevalDue(reevalDueOf(emergency, waitMinutesOf(emergency)));
        row.setEmergencyStatus(emergency.getEmergencyStatus());
        row.setFromDoctorId(emergency.getDoctorId());
        row.setFromDoctorName(emergency.getDoctorName());
        row.setTakeDoctorId(taker.getId());
        row.setTakeDoctorName(taker.getEmpName());
        // 文本先截后存：入参层不设 @Size，超长写库会撞 Data too long → 交班失败还看不出原因
        row.setDisposition(cutText(item.getDisposition().trim(), 100));
        row.setHandoverNote(TextUtil.hasText(item.getHandoverNote()) ? cutText(item.getHandoverNote().trim(), 300) : null);
        row.setWaitMinutes(waitMinutesOf(emergency));
        row.setObsHours(obsHoursOf(emergency));
        row.setOverdueLevel(overdueOf(emergency));
        return row;
    }

    /**
     * 责任转移：把这条急诊的负责医生换成接续责任人，并带上 assign_type=5-交班承接。
     * <p>
     * 只动「无主的」和「交出人名下的」两行（清单口径本身就只能查出这两类，这里是二次收口），
     * 别人的行不碰。挂号单与叫号队列同步换人 —— 医生站叫号按候诊队列的医师ID=我收口，
     * 只改急诊记录的话接班人在工作站上看不见接手的人，"交到了他手上"就是空话。
     */
    private void transferResponsibility(Emergency emergency, EmployeeBriefVO taker, Long fromEmpId) {
        if (emergency.getDoctorId() != null && !emergency.getDoctorId().equals(fromEmpId)) {
            return;
        }
        emergency.setDoctorId(taker.getId());
        emergency.setDoctorName(taker.getEmpName());
        emergency.setAssignType(EmergencyAssignTypeEnum.HANDOVER_TAKE.getCode());
        // 「未派单原因」是入池那一刻的事实，但这一列的界面语义是"现在为什么还没人负责"；
        // 人已经落定还留着它，列表上就会出现「有医生 + 写着无主原因」的自相矛盾行。留痕在台账的明细里。
        // 必须用 UpdateWrapper 显式 set null：updateById 默认跳过实体里的 null 字段，置空根本不落库。
        this.update(new LambdaUpdateWrapper<Emergency>()
                .eq(Emergency::getId, emergency.getId())
                .set(Emergency::getDoctorId, taker.getId())
                .set(Emergency::getDoctorName, taker.getEmpName())
                .set(Emergency::getAssignType, EmergencyAssignTypeEnum.HANDOVER_TAKE.getCode())
                .set(Emergency::getUnassignedReason, null));
        emergency.setUnassignedReason(null);

        // 挂号单与叫号队列同步换人。这里<b>不能</b>复用 findEmergencyAppoint(patientId)：
        // 它取的是「该患者最近一张急诊号」，同一个人第二次进急诊就会错抓到上一次那趟就诊，
        // 交班会把不该动的历史记录改掉医生。登记时队列短号固定是「急 + 急诊号后 5 位」，
        // 按它加患者定位出来的才是本次就诊。
        Queue queue = findEmergencyQueueOf(emergency);
        if (queue == null) {
            return;
        }
        if (queue.getDoctorId() == null || queue.getDoctorId().equals(fromEmpId)) {
            queue.setDoctorId(taker.getId());
            queue.setDoctorName(taker.getEmpName());
            queueMapper.updateById(queue);
        }
        AppointInfo appointInfo = appointInfoMapper.selectById(queue.getRegistId());
        if (appointInfo != null && (appointInfo.getDoctorId() == null || appointInfo.getDoctorId().equals(fromEmpId))) {
            appointInfo.setDoctorId(taker.getId());
            appointInfo.setDoctorName(taker.getEmpName());
            appointInfoMapper.updateById(appointInfo);
        }
    }

    /**
     * 本次急诊对应的叫号队列行（按「急+急诊号后 5 位」+ 患者定位，不是"该患者最近一张号"）
     */
    private Queue findEmergencyQueueOf(Emergency emergency) {
        String erNo = emergency.getEmergencyNo();
        if (!TextUtil.hasText(erNo) || erNo.length() < 5) {
            return null;
        }
        return queueMapper.selectOne(new LambdaQueryWrapper<Queue>()
                .eq(Queue::getQueueNo, "急" + erNo.substring(erNo.length() - 5))
                .eq(Queue::getPatientId, emergency.getPatientId())
                .orderByDesc(Queue::getId)
                .last("LIMIT 1"));
    }

    /**
     * 每位接续医生一条待办（同一人接手多条不刷屏）：交班的闭环点是"他知道有人被交给他了"
     */
    private void notifyTakers(EmergencyHandover handover, LinkedHashMap<Long, Emergency> scope,
                              LinkedHashMap<Long, EmergencyHandoverItemDTO> submitted,
                              LinkedHashMap<Long, Long> takeOf, LinkedHashMap<Long, EmployeeBriefVO> takers) {
        LinkedHashMap<Long, List<Emergency>> byTaker = new LinkedHashMap<>();
        scope.values().forEach(e -> byTaker
                .computeIfAbsent(takeOf.get(e.getId()), id -> new ArrayList<>()).add(e));
        byTaker.forEach((takerId, list) -> {
            EmployeeBriefVO taker = takers.get(takerId);
            String detail = list.stream()
                    .map(e -> {
                        BizEmergencyVO computed = toVO(e);
                        String stay = computed.getWaitMinutes() != null ? "候诊 " + computed.getWaitMinutes() + " 分钟" : "";
                        if (computed.getObsHours() != null) {
                            stay = (TextUtil.hasText(stay) ? stay + "，" : "") + "留观 " + computed.getObsHours() + " 小时";
                        }
                        return e.getPatientName() + "(" + e.getEmergencyNo() + ")："
                                + submitted.get(e.getId()).getDisposition()
                                + (TextUtil.hasText(stay) ? "；" + stay : "");
                    })
                    .collect(Collectors.joining("\n"));
            String content = handover.getFromEmpName() + " 在 " + handover.getDeptName()
                    + " 交班，共移交 " + list.size() + " 人，请立即查看病历确认接手：\n" + detail;
            EmergencyHandoverPayloadVO payload = new EmergencyHandoverPayloadVO();
            payload.setHandoverNo(handover.getHandoverNo());
            payload.setDeptName(handover.getDeptName());
            payload.setFromEmpName(handover.getFromEmpName());
            payload.setCount(list.size());
            sysMessageService.sendSystemMessage(takerId,
                    taker == null ? "站内用户" : taker.getEmpName(),
                    "急诊交班接收：" + handover.getFromEmpName() + " → "
                            + (taker == null ? "接续医生" : taker.getEmpName()) + "（" + list.size() + " 人）",
                    content, BizTypeEnum.EMERGENCY_HANDOVER.getType(), handover.getId(),
                    // warning：交班接收要当天处理，但不至于和危急值抢 urgent 这一档
                    "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
        });
    }

    @Override
    public PageResult<EmergencyHandoverVO> handoverListPage(EmergencyHandoverPageQueryDTO queryDTO) {
        LambdaQueryWrapper<EmergencyHandover> wrapper = new LambdaQueryWrapper<>();
        List<Long> scoped = deptScopeService.scopedDeptIds(queryDTO.getDeptId());
        wrapper.in(scoped != null, EmergencyHandover::getDeptId, scoped)
                .and(TextUtil.hasText(queryDTO.getKeyword()), w -> w
                        .like(EmergencyHandover::getHandoverNo, queryDTO.getKeyword())
                        .or().like(EmergencyHandover::getFromEmpName, queryDTO.getKeyword())
                        .or().like(EmergencyHandover::getTakeEmpName, queryDTO.getKeyword())
                        // 交过的患者也要能搜到：台账最常见的用法是"这个人上次是谁交的"
                        .or().apply("EXISTS (SELECT 1 FROM biz_emergency_handover_item i "
                                        + "WHERE i.handover_id = biz_emergency_handover.id AND i.del_flag = 0 "
                                        + "AND (i.patient_name LIKE CONCAT('%', {0}, '%') OR i.emergency_no LIKE CONCAT('%', {0}, '%')))",
                                queryDTO.getKeyword()))
                .orderByDesc(EmergencyHandover::getPeriodEnd);
        Page<EmergencyHandover> page = emergencyHandoverMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<EmergencyHandoverVO> records = page.getRecords() == null ? List.of()
                : page.getRecords().stream().map(h -> BeanUtil.copyProperties(h, EmergencyHandoverVO.class))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public EmergencyHandoverDetailVO handoverDetailById(Long id) {
        EmergencyHandover handover = emergencyHandoverMapper.selectById(id);
        if (handover == null) {
            throw new BusinessException("交班单不存在");
        }
        deptScopeService.assertDeptAccessible(handover.getDeptId());
        EmergencyHandoverDetailVO detail = new EmergencyHandoverDetailVO();
        detail.setHandover(BeanUtil.copyProperties(handover, EmergencyHandoverVO.class));
        detail.setItems(emergencyHandoverItemMapper.selectList(new LambdaQueryWrapper<EmergencyHandoverItem>()
                        .eq(EmergencyHandoverItem::getHandoverId, id)
                        .orderByAsc(EmergencyHandoverItem::getId))
                .stream().map(this::toItemVO).collect(Collectors.toList()));
        return detail;
    }

    private EmergencyHandoverItemVO toItemVO(EmergencyHandoverItem item) {
        EmergencyHandoverItemVO vo = BeanUtil.copyProperties(item, EmergencyHandoverItemVO.class);
        vo.setTriageLevelText(EmergencyTriageLevelEnum.getLabel(item.getTriageLevel()));
        vo.setEmergencyStatusText(EmergencyStatusEnum.getLabel(item.getEmergencyStatus()));
        vo.setOverdueText(EmergencyWaitPolicy.overdueText(item.getOverdueLevel() == null ? 0 : item.getOverdueLevel()));
        return vo;
    }

    /**
     * 扫描「留观中且已超过上限档」的急诊，向该负责的人写站内信待办。
     * <p>
     * 只催上限档（默认 72 小时），不催预警档（48 小时）：预警在榜单上看得见就够，
     * 提前 24 小时发信会让同一个人被催两次，收敛链白白拉长一倍。
     * 阶梯与判重完全沿用候诊升级那一套（医生 → 当班 → 主任 → 急诊台兜底，每人只催一次）。
     */
    @Override
    public int escalateObservation() {
        LambdaQueryWrapper<Emergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.OBSERVATION.getCode())
                .isNotNull(Emergency::getObservationStartTime)
                .apply(OBS_OVER_SQL, obsThresholdOfMax())
                .orderByAsc(Emergency::getObservationStartTime);
        List<Emergency> overdue = this.list(wrapper);
        int sent = 0;
        for (Emergency emergency : overdue) {
            try {
                sent += escalateObservationOne(emergency);
            } catch (Exception ex) {
                log.error("[急诊留观] {} 超时限催办失败：{}", emergency.getEmergencyNo(), ex.getMessage(), ex);
            }
        }
        if (sent > 0) {
            log.warn("[急诊留观] 超时限催办完成：本次发出 {} 条待办", sent);
        }
        return sent;
    }

    // 留观超时限升级（第 3 步的另一半：让长期占用留观床的人被追问去向）

    private int escalateObservationOne(Emergency emergency) {
        Long obsHours = obsHoursOf(emergency);
        if (obsHours == null || obsLevelOf(emergency) < 2) {
            return 0;
        }
        Set<Long> receivers = new LinkedHashSet<>();
        if (emergency.getDoctorId() != null) {
            receivers.add(emergency.getDoctorId());
        }
        // 留观患者往往由值班组共管：主管医生不在（换班/下班）时，当班医生与科室主任一并进阶梯
        onDutyErSchedules(emergency.getDeptId()).forEach(s -> receivers.add(s.getDoctorId()));
        receivers.addAll(deptLeaderIds(emergency.getDeptId()));

        List<Long> alreadySent = sysMessageService.receiverIdsOfBiz(BizTypeEnum.EMERGENCY_OBS.getType(), emergency.getId());
        receivers.removeAll(alreadySent);
        if (receivers.isEmpty()) {
            // 与候诊升级同一条阶梯的倒数第二级：本科室（主管医生/当班医生/科主任）都催过了，
            // 留给床位的争议（"该转哪个科""哪个科肯收"）只有总值班能跨科拍板。
            DutyOfficerVO officer = dutyRosterService.current();
            DutyOfficerBriefVO duty = null;
            if (officer != null) {
                duty = new DutyOfficerBriefVO();
                duty.setEmployeeId(officer.getEmployeeId());
                duty.setEmployeeName(officer.getEmployeeName());
            }
            if (duty != null && duty.getEmployeeId() != null && !alreadySent.contains(duty.getEmployeeId())) {
                receivers.add(duty.getEmployeeId());
            }
        }
        if (receivers.isEmpty()) {
            Receiver fallback = fallbackReceiver();
            if (fallback != null && !alreadySent.contains(fallback.employeeId())) {
                receivers.add(fallback.employeeId());
            }
        }
        if (receivers.isEmpty()) {
            return 0;
        }

        String content = String.format(
                "急诊 %s 患者 %s 已留观 %s 小时（本科上限 %d 小时），床位被长期占用。"
                        + "请立即评估去向：转住院、离院，或写明继续留观的理由。",
                emergency.getEmergencyNo(), emergency.getPatientName(), obsHours, obsPolicy.maxHours());
        int sent = 0;
        for (Long receiverId : receivers) {
            EmergencyObsTimeoutPayloadVO payload = new EmergencyObsTimeoutPayloadVO();
            payload.setPatientName(emergency.getPatientName());
            payload.setEmergencyNo(emergency.getEmergencyNo());
            payload.setDeptName(emergency.getDeptName());
            payload.setObsHours(obsHours);
            payload.setObservationBed(emergency.getObservationBed());
            if (sysMessageService.sendSystemMessage(receiverId, employeeNameOf(receiverId),
                    "急诊留观超时限：" + emergency.getPatientName() + "（已 " + obsHours + " 小时）",
                    content, BizTypeEnum.EMERGENCY_OBS.getType(), emergency.getId(),
                    "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0)) {
                sent++;
            }
        }
        return sent;
    }

    private String employeeNameOf(Long empId) {
        EmployeeBriefVO emp = systemFacadeService.findEmployee(empId);
        return emp != null && TextUtil.hasText(emp.getEmpName()) ? emp.getEmpName() : "站内用户";
    }

    // ==================== 分诊复评（MEWS + 候诊超时复评） ====================

    /**
     * MEWS 现算（登记与复评共用）：JSON/文本体征 + 意识码值 → 总分。
     * 解析失败/全缺项时按 0 分收口——评分是辅助，缺项不能把登记流程堵死，
     * 但界面必须提示「意识未采」，这件事由出参的 consciousnessMissing 负责。
     */
    private int mewsScoreOf(String vitalSigns, Integer consciousness) {
        return EmergencyMewsRules.score(new EmergencyMewsRules.MewsInput(
                EmergencyTriageRules.parse(vitalSigns), consciousness)).score();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reevaluate(EmergencyReevalDTO reevalDTO) {
        Emergency emergency = this.getById(reevalDTO.getId());
        if (emergency == null) {
            throw new BusinessException("急诊记录不存在");
        }
        deptScopeService.assertDeptAccessible(emergency.getDeptId());
        if (!waitingNow(emergency)) {
            // 只给候诊中的人复评：已接诊的患者归医生管（级别调整走医生站），
            // 已闭环的再改级别是在篡改历史定级
            throw new BusinessException("仅候诊中的急诊可以复评分级（已接诊或已闭环的记录不能改级）");
        }
        Integer before = emergency.getTriageLevel();
        Integer after = reevalDTO.getTriageLevel();
        // 只升不降：数字越小越危重，「升级」= 新级别数值 ≤ 原级别。
        // 前端禁用降级选项只是第一道，这里是无路可绕的第二道（API 直连、脚本调用都拦得住）
        if (after == null || before != null && after > before) {
            throw new BusinessException("复评只升不降：不能将分诊级别调轻（当前 "
                    + EmergencyTriageLevelEnum.getLabel(before) + "）");
        }

        // 体征：传了用新的（复测），没传沿用登记的——两种情况都写进流水，台账能看出有没有复测
        String vitalSigns = TextUtil.hasText(reevalDTO.getVitalSigns())
                ? reevalDTO.getVitalSigns() : emergency.getVitalSigns();
        int mews = mewsScoreOf(vitalSigns, reevalDTO.getConsciousness());

        boolean upgraded = before == null || after < before;
        emergency.setTriageLevel(after);
        emergency.setZone(getZoneByLevel(after));
        // 时限快照跟着新级别重算：III 级等成 I 级，应接诊时限从 30 分钟变 0（即刻），
        // 不重算的话升级了个寂寞——超时判定还在按老级别放水
        emergency.setTargetSeeMinutes(waitPolicy.deadlineMinutes(after));
        emergency.setConsciousness(reevalDTO.getConsciousness());
        emergency.setVitalSigns(vitalSigns);
        emergency.setMewsScore(mews);
        emergency.setLastReevalTime(LocalDateTime.now());
        emergency.setReevalCount((emergency.getReevalCount() == null ? 0 : emergency.getReevalCount()) + 1);
        // 守卫下沉到 WHERE：仅候诊中可复评，并发接诊时后到者 0 行失败
        boolean changed = this.update(emergency, new LambdaUpdateWrapper<Emergency>()
                .eq(Emergency::getId, emergency.getId())
                .eq(Emergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode()));
        if (!changed) {
            throw new BusinessException("该急诊记录状态已变更（可能已被接诊），请刷新后重试");
        }

        EmergencyReeval history = new EmergencyReeval();
        history.setEmergencyId(emergency.getId());
        history.setEmergencyNo(emergency.getEmergencyNo());
        history.setPatientName(emergency.getPatientName());
        history.setTriageLevelBefore(before);
        history.setTriageLevelAfter(after);
        history.setMewsScore(mews);
        history.setConsciousness(reevalDTO.getConsciousness());
        history.setVitalSigns(vitalSigns);
        // 原因先截后存：入参层不加 @Size，超长写库会撞 Data too long → 复评失败还看不出原因
        history.setReason(TextUtil.cut(reevalDTO.getReason().trim(), 500));
        emergencyReevalMapper.insert(history);

        // 队列行的分级同步：医生站叫号按 triage_level 排序，级别调了队列不调，
        // 「I 级心梗排在 IV 级扭伤前面」在升级这一刻就悄悄失效了
        syncQueueTriageLevel(emergency, after);

        if (upgraded) {
            notifyLevelUpgrade(emergency, before, after, history.getReason());
        }
        log.info("[急诊分诊] 复评：{} {}（{} → {}，MEWS {}），原因：{}",
                emergency.getEmergencyNo(), emergency.getPatientName(),
                EmergencyTriageLevelEnum.getLabel(before), EmergencyTriageLevelEnum.getLabel(after),
                mews, history.getReason());
    }

    /**
     * 复评后把叫号队列行的分级改掉（只改候诊中的队列行；已接诊的队列行不碰）
     */
    private void syncQueueTriageLevel(Emergency emergency, Integer after) {
        Queue queue = findEmergencyQueueOf(emergency);
        if (queue == null || !Objects.equals(queue.getQueueStatus(), QueueStatusEnum.WAITING.getCode())) {
            return;
        }
        queue.setTriageLevel(after);
        queue.setTriageStatus(after != null ? 1 : 0);
        queueMapper.updateById(queue);
    }

    @Override
    public List<EmergencyReevalVO> reevalList(Long emergencyId) {
        if (emergencyId == null) {
            return List.of();
        }
        return emergencyReevalMapper.selectList(new LambdaQueryWrapper<EmergencyReeval>()
                        .eq(EmergencyReeval::getEmergencyId, emergencyId)
                        .orderByAsc(EmergencyReeval::getId))
                .stream().map(this::toReevalVO).collect(Collectors.toList());
    }

    private EmergencyReevalVO toReevalVO(EmergencyReeval row) {
        EmergencyReevalVO vo = new EmergencyReevalVO();
        vo.setId(row.getId());
        vo.setEmergencyId(row.getEmergencyId());
        vo.setEmergencyNo(row.getEmergencyNo());
        vo.setPatientName(row.getPatientName());
        vo.setTriageLevelBefore(row.getTriageLevelBefore());
        vo.setTriageLevelBeforeText(EmergencyTriageLevelEnum.getLabel(row.getTriageLevelBefore()));
        vo.setTriageLevelAfter(row.getTriageLevelAfter());
        vo.setTriageLevelAfterText(EmergencyTriageLevelEnum.getLabel(row.getTriageLevelAfter()));
        vo.setUpgraded(row.getTriageLevelBefore() == null
                || row.getTriageLevelAfter() != null && row.getTriageLevelAfter() < row.getTriageLevelBefore());
        vo.setMewsScore(row.getMewsScore());
        vo.setConsciousness(row.getConsciousness());
        vo.setConsciousnessText(EmergencyConsciousnessEnum.getLabel(row.getConsciousness()));
        vo.setVitalSigns(row.getVitalSigns());
        vo.setReason(row.getReason());
        vo.setCreateBy(row.getCreateBy());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }

    /**
     * 复评到期待办：受众是**当班护理**（分诊台），不是医生。
     * <p>与 {@link #escalateOverdue} 的分工：那条催「有人来接诊」，这条催「该重新分级了」。
     * 分诊规范要求候诊超时须再次评估——没人接诊和没人复评是两条独立的失职，
     * 合成一条信只会让分诊台以为医生那边在动。
     * <p>可重入：同一条急诊对同一收件人只催一次（{@code receiverIdsOfBiz} 判重）。
     * 真正的闭环动作是「复评」——复评后 last_reeval_time 刷新，这条自然不再被扫到。
     */
    @Override
    public int escalateReeval() {
        LambdaQueryWrapper<Emergency> wrapper = waitingBase();
        wrapper.apply(REEVAL_DUE_SQL, waitPolicy.deadlineMinutes(null))
                .orderByAsc(Emergency::getAdmissionTime);
        List<Emergency> due = this.list(wrapper);
        int sent = 0;
        for (Emergency emergency : due) {
            try {
                sent += escalateReevalOne(emergency);
            } catch (Exception ex) {
                // 单条失败不中断整批：与候诊升级同一姿态，一条炸了不能拖累其他条
                log.error("[急诊分诊] {} 复评到期提醒失败：{}", emergency.getEmergencyNo(), ex.getMessage(), ex);
            }
        }
        if (sent > 0) {
            log.warn("[急诊分诊] 复评到期提醒完成：本次发出 {} 条待办", sent);
        }
        return sent;
    }

    private int escalateReevalOne(Emergency emergency) {
        long waitMinutes = emergency.getAdmissionTime() == null ? 0
                : Duration.between(emergency.getAdmissionTime(), LocalDateTime.now()).toMinutes();
        int target = emergency.getTargetSeeMinutes() == null
                ? waitPolicy.deadlineMinutes(emergency.getTriageLevel()) : emergency.getTargetSeeMinutes();

        // 当班护理（排班端口 staff_type=2）。没有护理排班的科室直接落空，
        // 由下面的兜底接收人接住——不编造「分诊台」这个人
        Set<Long> receivers = new LinkedHashSet<>();
        onDutyStaffSchedules(emergency.getDeptId(), StaffTypeEnum.NURSE.getCode())
                .forEach(s -> receivers.add(s.getDoctorId()));

        List<Long> alreadySent = sysMessageService.receiverIdsOfBiz(BizTypeEnum.EMERGENCY_REEVAL.getType(), emergency.getId());
        receivers.removeAll(alreadySent);
        if (receivers.isEmpty()) {
            Receiver fallback = fallbackReceiver();
            if (fallback != null && !alreadySent.contains(fallback.employeeId())) {
                receivers.add(fallback.employeeId());
            }
        }
        if (receivers.isEmpty()) {
            return 0;
        }

        String levelText = EmergencyTriageLevelEnum.getLabel(emergency.getTriageLevel());
        String title = "急诊待复评：" + emergency.getPatientName() + "（" + levelText + "）";
        String content = String.format(
                "急诊 %s 已候诊 %s，超过应接诊时限 %s 分钟仍未接诊，按分诊规范应再次评估分级。"
                        + "请到分诊台复评：重采意识与生命体征，级别只升不降；"
                        + "若已接诊请忽略本提醒。",
                emergency.getEmergencyNo(), humanWait(waitMinutes), target);

        EmergencyReevalDuePayloadVO payload = new EmergencyReevalDuePayloadVO();
        payload.setPatientName(emergency.getPatientName());
        payload.setEmergencyNo(emergency.getEmergencyNo());
        payload.setTriageLevelText(levelText);
        payload.setDeptName(emergency.getDeptName());
        payload.setWaitMinutes(waitMinutes);
        payload.setTargetSeeMinutes(target);
        payload.setMewsScore(emergency.getMewsScore());

        int sent = 0;
        for (Long receiverId : receivers) {
            if (sysMessageService.sendSystemMessage(
                    receiverId, employeeNameOf(receiverId), title, content,
                    BizTypeEnum.EMERGENCY_REEVAL.getType(), emergency.getId(),
                    // warning：当天必须处理，但不与危急值抢 urgent
                    "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0)) {
                sent++;
            }
        }
        return sent;
    }

    /**
     * 级别上调通知：受众是**医生**（负责医生，无主则当班医生）。
     * <p>III 级等成 I 级意味着处置方式从「排队」变成「立即抢救」——这条信等不得，
     * 所以它和给护士的「到期待办」分成两个 BizType，避免判重时互相吞掉。
     */
    private void notifyLevelUpgrade(Emergency emergency, Integer before, Integer after, String reason) {
        Set<Long> receivers = new LinkedHashSet<>();
        if (emergency.getDoctorId() != null) {
            receivers.add(emergency.getDoctorId());
        } else {
            onDutyErSchedules(emergency.getDeptId()).forEach(s -> receivers.add(s.getDoctorId()));
        }
        if (receivers.isEmpty()) {
            return;
        }
        List<Long> alreadySent = sysMessageService.receiverIdsOfBiz(
                BizTypeEnum.EMERGENCY_REEVAL_UPGRADE.getType(), emergency.getId());
        receivers.removeAll(alreadySent);
        if (receivers.isEmpty()) {
            return;
        }
        String content = String.format(
                "急诊 %s 患者 %s 的分诊级别由 %s 上调为 %s（MEWS %s 分）。原因：%s。"
                        + "请立即评估处置优先级。",
                emergency.getEmergencyNo(), emergency.getPatientName(),
                EmergencyTriageLevelEnum.getLabel(before), EmergencyTriageLevelEnum.getLabel(after),
                emergency.getMewsScore() == null ? "-" : emergency.getMewsScore(), reason);
        for (Long receiverId : receivers) {
            sysMessageService.sendSystemMessage(
                    receiverId, employeeNameOf(receiverId),
                    "急诊分诊升级：" + emergency.getPatientName() + "（"
                            + EmergencyTriageLevelEnum.getLabel(before) + " → "
                            + EmergencyTriageLevelEnum.getLabel(after) + "）",
                    content, BizTypeEnum.EMERGENCY_REEVAL_UPGRADE.getType(), emergency.getId(),
                    // 升级到 I/II 级是 urgent：这已经不是催办，是抢救令
                    EmergencyTriageLevelEnum.isRedZone(after) ? "urgent" : "warning",
                    null, 0);
        }
    }

    /**
     * 按岗位类别取此刻在岗排班（医生/护理共用）。急诊派单、复评提醒两处消费同一出口，
     * 免得「谁在岗」在同一个时刻算出两份名单。
     */
    private List<ErDuty> onDutyStaffSchedules(Long deptId, Integer staffType) {
        if (deptId == null) {
            return List.of();
        }
        List<com.his.spi.vo.StaffScheduleBriefVO> rows = dutyRosterFacadeService.listOnDutyStaff(
                LocalDateTime.now(), 1, deptId, staffType);
        List<ErDuty> result = new ArrayList<>(rows.size());
        for (com.his.spi.vo.StaffScheduleBriefVO row : rows) {
            if (row.getEmployeeId() == null) {
                continue;
            }
            result.add(new ErDuty(row.getEmployeeId(), row.getEmployeeName(),
                    row.getShiftId(), row.getStartTime(), row.getEndTime()));
        }
        return result;
    }

    /**
     * 已留观小时数（向下取整）；不在留观态且没有结束时间时返回 null（不编造时长）
     */
    private Long obsHoursOf(Emergency entity) {
        if (entity.getObservationStartTime() == null) {
            return null;
        }
        boolean observing = Objects.equals(entity.getEmergencyStatus(), EmergencyStatusEnum.OBSERVATION.getCode());
        LocalDateTime endAt = observing ? LocalDateTime.now() : entity.getObservationEndTime();
        if (endAt == null) {
            return null;
        }
        return Math.max(0, Duration.between(entity.getObservationStartTime(), endAt).toHours());
    }

    // 留观/候诊时长的现算入口（列表、清单、台账共用）

    /**
     * 留观档位：只有"还在观"的行才报警，已经离院的人挂在榜上是骗人
     */
    private int obsLevelOf(Emergency entity) {
        if (!Objects.equals(entity.getEmergencyStatus(), EmergencyStatusEnum.OBSERVATION.getCode())) {
            return 0;
        }
        Long hours = obsHoursOf(entity);
        return hours == null ? 0 : obsPolicy.levelOf(hours);
    }

    private Long waitMinutesOf(Emergency entity) {
        BizEmergencyVO computed = toVO(entity);
        return computed.getWaitMinutes();
    }

    private int overdueOf(Emergency entity) {
        Integer level = toVO(entity).getOverdueLevel();
        return level == null ? 0 : level;
    }

    /**
     * 上限档与预警档的较大者：配置被写反时判定仍然单调（同 {@link EmergencyObservationPolicy#levelOf}）
     */
    private int obsThresholdOfMax() {
        return Math.max(obsPolicy.maxHours(), obsPolicy.warnHours());
    }

    private Long requireCurrentEmployee() {
        Long empId = UserUtils.getCurrentUser().getEmployeeId();
        if (empId == null) {
            throw new BusinessException("未获取到登录员工身份，无法办理交班");
        }
        return empId;
    }

    // 交班小工具

    /**
     * 交班科室：不传则用当前登录岗位所在科室；两者都没有就拒，绝不"默认全院"
     */
    private Long resolveHandoverDeptId(Long deptId) {
        if (deptId != null) {
            return deptId;
        }
        var user = UserUtils.getCurrentUser();
        Long myDeptId = user == null ? null : user.getDeptId();
        if (myDeptId == null) {
            throw new BusinessException("当前账号未挂科室，请在交班时选择科室");
        }
        return myDeptId;
    }

    private EmployeeBriefVO requireActiveEmployee(Long empId, String label) {
        // C-非 web 入参：整单接班人与明细推导出的接续医生共用此工具，后者不是 HTTP 参数绑定，Bean Validation 不覆盖，保留
        if (empId == null) {
            throw new BusinessException(label + "不能为空");
        }
        EmployeeBriefVO emp = systemFacadeService.findEmployee(empId);
        if (emp == null) {
            throw new BusinessException(label + "不存在：" + empId);
        }
        if (emp.getStatus() != null && !Objects.equals(emp.getStatus(), EnableStatusEnum.ENABLED.getCode())) {
            throw new BusinessException(label + "已停用：" + emp.getEmpName());
        }
        return emp;
    }

    /**
     * 交出人此刻所在班次名（台账上"白班交的班"要能看出来）；查不到就留空，不编。
     *
     * <p>先在该科室此刻在岗名单里定位交出人所在班次，再按班次ID经号源端口取班次名；
     * 不在岗或查不到班次名时返回 null，交班台账班次名留空。
     */
    private String currentShiftName(Long deptId, Long empId) {
        if (empId == null) {
            return null;
        }
        List<com.his.spi.vo.StaffScheduleBriefVO> rows = dutyRosterFacadeService.listOnDutyStaff(
                LocalDateTime.now(), 1, deptId, null);
        for (com.his.spi.vo.StaffScheduleBriefVO row : rows) {
            if (Objects.equals(row.getEmployeeId(), empId) && row.getShiftId() != null) {
                return clinicSourceFacadeService.findShiftName(row.getShiftId());
            }
        }
        return null;
    }

    /**
     * 交班单号：EJ + yyyyMMdd + 4 位流水（Redis 递增）。
     * 序列 key 直接传字面量而不进 {@code Constants}：号段只被本模块使用，
     * 进公共常量表反而让别的模块误以为可以复用同一串号。
     */
    private String generateHandoverNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long seq = redisSequenceService.next("EMERGENCY_HANDOVER");
        return "EJ" + dateStr + String.format("%04d", seq);
    }

    private String cutText(String text, int max) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    /**
     * 急诊当班医生快照（由岗位排班在岗行映射而来）。
     *
     * <p>字段命名刻意对齐旧「门诊号源排班」消费点的 getter（getDoctorId/getDoctorName/
     * getShiftId/getStartTime/getEndTime），让急诊各消费点无需感知数据源切换。
     */
    private static class ErDuty {
        private final Long doctorId;
        private final String doctorName;
        private final Long shiftId;
        private final String startTime;
        private final String endTime;

        private ErDuty(Long doctorId, String doctorName, Long shiftId, String startTime, String endTime) {
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.shiftId = shiftId;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Long getDoctorId() {
            return doctorId;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public Long getShiftId() {
            return shiftId;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }
    }

    /**
     * 兜底接收人（员工ID + 姓名）
     */
    private record Receiver(Long employeeId, String name) {
    }
}
