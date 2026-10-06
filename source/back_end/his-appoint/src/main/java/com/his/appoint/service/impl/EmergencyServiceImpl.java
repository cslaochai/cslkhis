package com.his.appoint.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.*;
import com.his.appoint.entity.*;
import com.his.appoint.enums.*;
import com.his.appoint.mapper.*;
import com.his.appoint.service.EmergencyService;
import com.his.appoint.service.ScheduleService;
import com.his.appoint.support.EmergencyObservationPolicy;
import com.his.appoint.support.EmergencyTriageRules;
import com.his.appoint.support.EmergencyWaitPolicy;
import com.his.appoint.vo.*;
import com.his.common.base.PageResult;
import com.his.common.enums.EmergencyAssignTypeEnum;
import com.his.common.enums.EmergencyStatusEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.support.EmpTitleCode;
import com.his.common.util.ShiftCoverUtil;
import com.his.patient.dto.InpatientAdmitDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.InpatientService;
import com.his.patient.support.PatientProfileValidator;
import com.his.patient.vo.BedVO;
import com.his.patient.vo.WardVO;
import com.his.system.entity.*;
import com.his.system.enums.BizTypeEnum;
import com.his.system.mapper.*;
import com.his.system.service.DutyRosterService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.DutyOfficerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyServiceImpl extends ServiceImpl<BizEmergencyMapper, BizEmergency> implements EmergencyService {

    /**
     * 候诊是否超时的判定式（读时算，不落列）。target_see_minutes 为 NULL 的存量行按未定级时限收口。
     */
    private static final String OVERDUE_SQL =
            "admission_time IS NOT NULL AND TIMESTAMPDIFF(MINUTE, admission_time, NOW()) > IFNULL(target_see_minutes, {0})";

    /**
     * 留观已超该小时数（读时算，不落列）；状态=3 的收口写在各处 wrapper 里，本式只管时间
     */
    private static final String OBS_OVER_SQL =
            "TIMESTAMPDIFF(HOUR, observation_start_time, NOW()) >= {0}";

    /**
     * 无人可发待办时的兜底接收人（用户名或员工ID），口径同 lab.critical_value_fallback_receiver
     */
    private static final String FALLBACK_RECEIVER_CONFIG_KEY = "emergency.wait_fallback_receiver";

    private final BizQueueMapper queueMapper;
    private final BizAppointInfoMapper appointInfoMapper;
    private final BizPatientMapper bizPatientMapper;
    private final RedisSequenceService sequenceService;
    private final InpatientService inpatientService;
    private final ScheduleService scheduleService;
    private final EmergencyWaitPolicy waitPolicy;
    private final EmergencyObservationPolicy obsPolicy;
    private final SysMessageService sysMessageService;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final SysUserMapper sysUserMapper;
    private final SysConfigMapper sysConfigMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    /**
     * 全院当天谁负责（总值班）：科室阶梯走完后的兜底收口人，见 sql/169
     */
    private final DutyRosterService dutyRosterService;
    private final BizShiftMapper bizShiftMapper;
    private final BizEmergencyHandoverMapper handoverMapper;
    private final BizEmergencyHandoverItemMapper handoverItemMapper;

    @Override
    public PageResult<BizEmergencyVO> listPage(EmergencyQueryDTO queryDTO) {
        LambdaQueryWrapper<BizEmergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getTriageLevel() != null, BizEmergency::getTriageLevel, queryDTO.getTriageLevel())
                .eq(queryDTO.getEmergencyStatus() != null, BizEmergency::getEmergencyStatus, queryDTO.getEmergencyStatus())
                .and(StringUtils.hasText(queryDTO.getKeyword()), w -> w
                        .like(BizEmergency::getPatientName, queryDTO.getKeyword())
                        .or().like(BizEmergency::getEmergencyNo, queryDTO.getKeyword())
                        .or().like(BizEmergency::getChiefComplaint, queryDTO.getKeyword()));

        if (Boolean.TRUE.equals(queryDTO.getUnassignedOnly())) {
            // 待派单池 = 候诊中且没有接诊医生；这就是「急诊池」在本系统里的唯一口径
            wrapper.eq(BizEmergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode())
                    .isNull(BizEmergency::getDoctorId);
        }
        boolean overdueOnly = Boolean.TRUE.equals(queryDTO.getOverdueOnly());
        boolean observationBoard = queryDTO.getObservationMinHours() != null;
        if (overdueOnly && observationBoard) {
            // 两个开关各自收口一种状态，同时开等于查"既在候诊又在留观"的人：必空。
            // 与其返回一张空表让人怀疑后端挂了，不如直接说清是筛选态打架。
            throw new BusinessException("「只看超时候诊」与「留观榜」不能同时开启，请只保留一个");
        }
        if (overdueOnly) {
            wrapper.eq(BizEmergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode())
                    .apply(OVERDUE_SQL, waitPolicy.deadlineMinutes(null));
        }
        if (observationBoard) {
            // 留观榜：0 = 全部在观；48/72 由统计卡带入。只数"还在观"的人 ——
            // 已经离院/转住院的人挂在榜上，值班的人会以为床位还占着，白跑一趟。
            wrapper.eq(BizEmergency::getEmergencyStatus, EmergencyStatusEnum.OBSERVATION.getCode())
                    .apply(OBS_OVER_SQL, queryDTO.getObservationMinHours());
        }

        // 只看超时=救火队列、留观榜=占床最久优先，两者都是最老的排最前；其余保持新建在前
        if (overdueOnly) {
            wrapper.orderByAsc(BizEmergency::getAdmissionTime);
        } else if (observationBoard) {
            wrapper.orderByAsc(BizEmergency::getObservationStartTime);
        } else {
            wrapper.orderByDesc(BizEmergency::getCreateTime);
        }

        Page<BizEmergency> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<BizEmergencyVO> records = page.getRecords() == null ? List.of()
                : page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    /**
     * 实体 → 出参，候诊时长与超时档位在这里现算
     */
    private BizEmergencyVO toVO(BizEmergency entity) {
        BizEmergencyVO vo = new BizEmergencyVO();
        BeanUtils.copyProperties(entity, vo);
        EmergencyAssignTypeEnum assignType = EmergencyAssignTypeEnum.fromCode(entity.getAssignType());
        vo.setAssignTypeText(assignType == null ? "" : assignType.getLabel());
        LocalDateTime endAt = waitingNow(entity)
                ? LocalDateTime.now()
                : (entity.getDiagnosisTime() == null ? null : entity.getDiagnosisTime());
        // 停表口径：候诊中算到当下；已经接诊的算到「开始诊治」那一刻。
        // 既没在候诊、又查不到开始诊治时间（sql/145 之前的存量行），就不编造一个时长。
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
        return vo;
    }

    private boolean waitingNow(BizEmergency entity) {
        return Objects.equals(entity.getEmergencyStatus(), EmergencyStatusEnum.WAITING.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(BizEmergencyUpsertDTO upsertDTO) {
        BizPatient bizPatient;
        // 新患者模式：patientId为空时，先创建患者记录
        if (upsertDTO.getPatientId() == null) {
            bizPatient = new BizPatient();
            bizPatient.setPatientNo(sequenceService.generatePatientNo());
            bizPatient.setPatientName(upsertDTO.getPatientName());
            bizPatient.setGender(upsertDTO.getGender());
            bizPatient.setAge(upsertDTO.getAge());
            bizPatient.setPhone(upsertDTO.getPhone());
            // 身份证：急诊可以没有，但填了就要落库 —— 否则 EMPI 的同证去重拦不住急诊建档
            bizPatient.setIdCard(upsertDTO.getIdCard());
            // 有身份证就以它为准补出生日期/年龄（和主档建档同一口径，见 PatientController#fillBirthDateAndAge）
            LocalDate birthDate = PatientProfileValidator.birthDateOfIdCard(upsertDTO.getIdCard());
            if (birthDate != null) {
                bizPatient.setBirthDate(birthDate);
                bizPatient.setAge(Period.between(birthDate, LocalDate.now()).getYears());
            }
            bizPatient.setStatus(YesOrNoEnum.YES.getCode()); // 正常
            // 急诊建档是第二条写患者基本信息的路径，同样要过写入口校验：
            // 三无患者允许没有身份证，但姓名和性别不能省（下游性别判断全靠它）
            PatientProfileValidator.validateForEmergency(bizPatient);
            bizPatientMapper.insert(bizPatient);
        } else {
            bizPatient = bizPatientMapper.selectById(upsertDTO.getPatientId());
            if (bizPatient == null) {
                throw new BusinessException("未能找到对应患者信息");
            }
        }

        BizEmergency emergency = BeanUtil.copyProperties(upsertDTO, BizEmergency.class);
        emergency.setEmergencyNo(sequenceService.generateEmergencyNo());
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
        // dept_name 是 NOT NULL 无默认值的快照列：只传 deptId 的调用原先直接 500
        // （报「系统内部错误」，连原因都不透出）。科室名属于参照数据，服务端按 ID 补齐比信任客户端更稳。
        if (!StringUtils.hasText(emergency.getDeptName())) {
            SysDepartment dept = sysDepartmentMapper.selectById(emergency.getDeptId());
            if (dept == null) {
                throw new BusinessException("接诊科室不存在：" + emergency.getDeptId());
            }
            emergency.setDeptName(dept.getDeptName());
        }
        applyDispatch(emergency);
        this.save(emergency);

        // 创建挂号记录（急诊号），保持数据一致性
        BizAppointInfo appointInfo = new BizAppointInfo();
        appointInfo.setRegistNo(sequenceService.generateAppointNo());
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
        BizQueue queue = new BizQueue();
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
    private void applyDispatch(BizEmergency emergency) {
        emergency.setTargetSeeMinutes(waitPolicy.deadlineMinutes(emergency.getTriageLevel()));
        if (emergency.getDoctorId() != null) {
            // 医生名同样是快照列：只传 doctorId 的调用（API 直连、别的前端）会留下
            // 「有医生 ID、没有医生名」的行，列表按医生名判定 → 明明是派好了单，
            // 界面却显示成「待派单」。参照数据一律服务端按 ID 补齐。
            if (!StringUtils.hasText(emergency.getDoctorName())) {
                SysEmployee doctor = sysEmployeeMapper.selectById(emergency.getDoctorId());
                if (doctor == null) {
                    throw new BusinessException("接诊医生不存在：" + emergency.getDoctorId());
                }
                emergency.setDoctorName(doctor.getEmpName());
            }
            emergency.setAssignType(EmergencyAssignTypeEnum.MANUAL.getCode());
            return;
        }
        BizSchedule onDuty = pickOnDutySchedule(emergency.getDeptId());
        if (onDuty != null) {
            emergency.setAssignType(EmergencyAssignTypeEnum.AUTO_BY_SCHEDULE.getCode());
            emergency.setDoctorId(onDuty.getDoctorId());
            emergency.setDoctorName(onDuty.getDoctorName());
            return;
        }
        emergency.setAssignType(EmergencyAssignTypeEnum.POOL.getCode());
        String reason = StringUtils.hasText(emergency.getUnassignedReason())
                ? emergency.getUnassignedReason().trim()
                : "当日该科室无在岗排班医生，入待派单池";
        // 入参层不加 @Size：超长由服务端截断，不能让"粘贴了一长段说明"变成 400
        emergency.setUnassignedReason(cut(reason, 200));
    }

    /**
     * 当前时刻在岗的排班。交接班时段两班重叠，取「最近开班的那一班」= 接班医生，
     * 否则凌晨入院的急诊会被派给已经下班的白班医生。
     */
    private BizSchedule pickOnDutySchedule(Long deptId) {
        List<BizSchedule> onDuty = onDutySchedules(deptId);
        return onDuty.stream()
                .max(Comparator.comparing(s -> ShiftCoverUtil.parseShiftTime(s.getStartTime())))
                .orElse(null);
    }

    /**
     * 该科室此刻在岗（有医生）的当日排班；deptId 为空或时间不可解析一律排除
     */
    private List<BizSchedule> onDutySchedules(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        LocalTime now = LocalTime.now();
        List<BizSchedule> onDuty = new ArrayList<>();
        for (BizSchedule s : scheduleService.getTodaySchedule(deptId)) {
            if (s.getDoctorId() == null) {
                continue;
            }
            LocalTime start = ShiftCoverUtil.parseShiftTime(s.getStartTime());
            LocalTime end = ShiftCoverUtil.parseShiftTime(s.getEndTime());
            if (start == null || end == null) {
                continue;
            }
            if (ShiftCoverUtil.covers(now, start, end)) {
                onDuty.add(s);
            }
        }
        return onDuty;
    }

    /**
     * 班次时段判定收口在 {@link ShiftCoverUtil}（跨零点夜班 20:00-08:00 要拆两段）。
     * 急诊派单、交班班次名、今日在岗、分诊当班护士四处共用同一份实现，
     * 免得同一个时刻在四个地方算出四份不同的在岗名单。
     */

    private String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(EmergencyStatusUpsertDTO statusDTO) {
        BizEmergency emergency = this.getById(statusDTO.getId());
        if (emergency == null) {
            throw new BusinessException("急诊记录不存在");
        }
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
            inpatientService.occupyBedForObservation(bed.getBedId(), emergency.getPatientId());
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
        this.updateById(emergency);
        syncAppointAndQueue(emergency, status);
        return true;
    }

    /**
     * 谁接诊谁负责：急诊共享池（登记时未派医生）在「接诊」一刻把医生写进急诊记录，
     * 由 {@link #syncAppointAndQueue} 带到挂号单与队列。
     * <p>不写队列医生，候诊队列的医师ID 恒为 NULL，医生站 callNext
     * （按 doctor_id=我收口）永远看不见这个患者——共享池就变成无人认领的死角。
     */
    private void claimDoctor(BizEmergency emergency) {
        Long meEmp = UserUtils.getCurrentUser().getEmployeeId();
        if (emergency.getDoctorId() != null && meEmp != null && !emergency.getDoctorId().equals(meEmp)) {
            String who = StringUtils.hasText(emergency.getDoctorName()) ? emergency.getDoctorName() : "当班医生";
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
        BizEmergency emergency = this.getById(admitDTO.getId());
        if (emergency == null) {
            throw new BusinessException("急诊记录不存在");
        }
        int current = emergency.getEmergencyStatus() == null ? 1 : emergency.getEmergencyStatus();
        if (current >= 4) {
            throw new BusinessException("该急诊记录已结束，不能重复转住院");
        }

        // 留观转住院常发生在本病区原床上：先释放留观床，住院分床才看得见这张空闲床。
        // 与入院登记同事务——登记失败一起回滚，不会出现「床放了、人没收」
        endObservation(emergency);

        BizAppointInfo appoint = findEmergencyAppoint(emergency.getPatientId());
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
        inpatient.setDiagnosis(StringUtils.hasText(admitDTO.getDiagnosis()) ? admitDTO.getDiagnosis() : emergency.getDiagnosis());
        Long admissionId = inpatientService.admit(inpatient);

        emergency.setEmergencyStatus(EmergencyStatusEnum.ADMITTED.getCode());
        emergency.setAdmissionId(admissionId);
        emergency.setFinishTime(LocalDateTime.now());
        this.updateById(emergency);
        syncAppointAndQueue(emergency, EmergencyStatusEnum.ADMITTED.getCode());
        return admissionId;
    }

    @Override
    public List<WardVO> wardSelectList() {
        return inpatientService.listWards();
    }

    @Override
    public List<BedVO> bedSelectList(Long wardId, Integer bedStatus) {
        return inpatientService.listBeds(wardId, null, bedStatus);
    }

    @Override
    public List<EmergencyDutyVO> dutySelectList(Long deptId) {
        return onDutySchedules(deptId).stream().map(s -> {
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
    private void endObservation(BizEmergency emergency) {
        Integer status = emergency.getEmergencyStatus();
        if (status != null && status == 3 && emergency.getObservationBedId() != null) {
            inpatientService.releaseBedForObservation(emergency.getObservationBedId(), emergency.getPatientId());
            emergency.setObservationEndTime(LocalDateTime.now());
        }
    }

    /**
     * 状态流转回写挂号单与叫号队列。
     * <p>挂号状态必须跟着走：急诊直录的挂号单建单时是「已挂号」，
     * 如果就诊结束不回写，它永远是「未签到」——日终结转会把「已经看过的急诊患者」
     * 判成「爽约」，患者下次来会看到自己「上次爽约」。
     */
    private void syncAppointAndQueue(BizEmergency emergency, Integer status) {
        BizAppointInfo appointInfo = findEmergencyAppoint(emergency.getPatientId());
        if (appointInfo == null) {
            return;
        }
        LambdaQueryWrapper<BizQueue> queueWrapper = new LambdaQueryWrapper<>();
        queueWrapper.eq(BizQueue::getRegistId, appointInfo.getId());
        BizQueue queue = queueMapper.selectOne(queueWrapper);

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
            queueMapper.updateById(queue);
        }
        if (status == 2) {
            appointInfo.setRegistStatus(AppointStatusEnum.ACCEPTED.getCode());
            if (appointInfo.getDoctorId() == null && emergency.getDoctorId() != null) {
                appointInfo.setDoctorId(emergency.getDoctorId());
                appointInfo.setDoctorName(emergency.getDoctorName());
            }
            appointInfoMapper.updateById(appointInfo);
        } else if (status >= 4) {
            appointInfo.setRegistStatus(AppointStatusEnum.COMPLETED.getCode());
            appointInfoMapper.updateById(appointInfo);
        }
    }

    /**
     * 该患者最近一条急诊号挂号单（急诊直录产生，可能不存在——铺底数据没有挂号线索）
     */
    private BizAppointInfo findEmergencyAppoint(Long patientId) {
        LambdaQueryWrapper<BizAppointInfo> appointWrapper = new LambdaQueryWrapper<>();
        appointWrapper.eq(BizAppointInfo::getPatientId, patientId)
                .eq(BizAppointInfo::getRegistType, 3) // 急诊号
                .orderByDesc(BizAppointInfo::getCreateTime)
                .last("LIMIT 1");
        return appointInfoMapper.selectOne(appointWrapper);
    }

    @Override
    public EmergencyStatsVO getStats() {
        EmergencyStatsVO stats = new EmergencyStatsVO();
        // 总急诊量（今日）
        LambdaQueryWrapper<BizEmergency> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(BizEmergency::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        stats.setTodayTotal(this.count(todayWrapper));

        // 候诊中
        LambdaQueryWrapper<BizEmergency> waitingWrapper = new LambdaQueryWrapper<>();
        waitingWrapper.eq(BizEmergency::getEmergencyStatus, 1);
        stats.setWaiting(this.count(waitingWrapper));

        // 诊治中
        LambdaQueryWrapper<BizEmergency> treatingWrapper = new LambdaQueryWrapper<>();
        treatingWrapper.eq(BizEmergency::getEmergencyStatus, 2);
        stats.setTreating(this.count(treatingWrapper));

        // 留观
        LambdaQueryWrapper<BizEmergency> obsWrapper = new LambdaQueryWrapper<>();
        obsWrapper.eq(BizEmergency::getEmergencyStatus, 3);
        stats.setObservation(this.count(obsWrapper));

        // 红区（I级+II级）
        LambdaQueryWrapper<BizEmergency> redWrapper = new LambdaQueryWrapper<>();
        redWrapper.in(BizEmergency::getTriageLevel, 1, 2)
                .notIn(BizEmergency::getEmergencyStatus, 4, 5, 6);
        stats.setRedZone(this.count(redWrapper));

        // 绿色通道
        LambdaQueryWrapper<BizEmergency> greenWrapper = new LambdaQueryWrapper<>();
        greenWrapper.isNotNull(BizEmergency::getGreenChannel)
                .ne(BizEmergency::getGreenChannel, "")
                .notIn(BizEmergency::getEmergencyStatus, 4, 5, 6);
        stats.setGreenChannel(this.count(greenWrapper));

        // 超时未接诊（兜底看板：这个数字不为 0 就说明有人该被追问）
        LambdaQueryWrapper<BizEmergency> overdueWrapper = waitingBase();
        overdueWrapper.apply(OVERDUE_SQL, waitPolicy.deadlineMinutes(null));
        stats.setOverdueWaiting(this.count(overdueWrapper));

        // 待派单池：候诊中且没有接诊医生
        LambdaQueryWrapper<BizEmergency> poolWrapper = waitingBase();
        poolWrapper.isNull(BizEmergency::getDoctorId);
        stats.setUnassignedWaiting(this.count(poolWrapper));

        // 留观两档：48 小时是"该开始张罗去向"，72 小时是"必须定下来"。
        // 分开数是为了让值班的人一眼看出差多少 —— 只有一个"超时限"时，刚过 48 的人以为不着急。
        stats.setObsOverWarn(countObservationOver(obsPolicy.warnHours()));
        stats.setObsOverMax(countObservationOver(obsThresholdOfMax()));
        // 阈值随统计一起出参：看板的「留观超 48 小时」文案由后端带出，前端不写死数字，
        // 否则系统参数一改，卡片标题和实际过滤口径就成了两套。
        stats.setObsWarnHours(obsPolicy.warnHours());
        stats.setObsMaxHours(obsThresholdOfMax());

        return stats;
    }

    /**
     * 留观中且已超该小时数（与列表的「留观榜」过滤同一式子，卡片数字点开必须就是这些人）
     */
    private long countObservationOver(int hours) {
        LambdaQueryWrapper<BizEmergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizEmergency::getEmergencyStatus, EmergencyStatusEnum.OBSERVATION.getCode())
                .isNotNull(BizEmergency::getObservationStartTime)
                .apply(OBS_OVER_SQL, hours);
        return this.count(wrapper);
    }

    /**
     * 候诊中（status=1）的基础条件：超时数与池子数都只数"还在等的"
     */
    private LambdaQueryWrapper<BizEmergency> waitingBase() {
        LambdaQueryWrapper<BizEmergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizEmergency::getEmergencyStatus, EmergencyStatusEnum.WAITING.getCode());
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
        LambdaQueryWrapper<BizEmergency> wrapper = waitingBase();
        wrapper.apply(OVERDUE_SQL, waitPolicy.deadlineMinutes(null))
                .orderByAsc(BizEmergency::getAdmissionTime);
        List<BizEmergency> overdue = this.list(wrapper);
        int sent = 0;
        for (BizEmergency emergency : overdue) {
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

    private int escalateOne(BizEmergency emergency) {
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
            onDutySchedules(emergency.getDeptId()).forEach(s -> receivers.add(s.getDoctorId()));
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
            DutyOfficerVO duty = dutyRosterService.current();
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

        String levelText = EmergencyTriageRules.levelText(emergency.getTriageLevel());
        String title = "急诊候诊超时：" + emergency.getPatientName() + "（" + levelText + "）";
        String content = String.format(
                "急诊 %s 已候诊 %s，应接诊时限 %s 分钟（分诊 %s，就诊科室 %s）。请立即接诊；"
                        + "本人无法处理时请在急诊台改派，不要让它留在候诊队列里。"
                        + "本科室无人响应时，本待办会自动升级到当日总值班做全院协调。",
                emergency.getEmergencyNo(), humanWait(waitMinutes), target, levelText,
                StringUtils.hasText(emergency.getDeptName()) ? emergency.getDeptName() : "-");

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
    private boolean sendWaitTodo(Long receiverId, String title, String content, BizEmergency emergency,
                                 long waitMinutes, int targetMinutes, String levelText) {
        String receiverName = "站内用户";
        try {
            SysEmployee emp = sysEmployeeMapper.selectById(receiverId);
            if (emp != null && StringUtils.hasText(emp.getEmpName())) {
                receiverName = emp.getEmpName();
            }
        } catch (Exception ignored) {
            // 名字查不到不影响投递
        }
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("patientName", emergency.getPatientName());
        payload.put("emergencyNo", emergency.getEmergencyNo());
        payload.put("triageLevelText", levelText);
        payload.put("deptName", emergency.getDeptName());
        payload.put("waitMinutes", waitMinutes);
        payload.put("targetSeeMinutes", targetMinutes);
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
        return sysEmployeeMapper.selectList(new LambdaQueryWrapper<SysEmployee>()
                        .eq(SysEmployee::getDeptId, deptId)
                        .in(SysEmployee::getTitle, EmpTitleCode.SENIOR)
                        .eq(SysEmployee::getStatus, 1))
                .stream().map(SysEmployee::getId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 兜底接收人，配置值可以是用户名，也可以直接是员工ID。
     * 读不到/解析不出来一律返回 null —— 兜底逻辑本身不能再有静默失败，
     * 返回 null 由调用方按「这条没发出去」处理（下一轮再试），不发假的成功。
     */
    private Receiver fallbackReceiver() {
        try {
            SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, FALLBACK_RECEIVER_CONFIG_KEY)
                    .last("LIMIT 1"));
            if (config == null || !StringUtils.hasText(config.getConfigValue())) {
                return null;
            }
            String raw = config.getConfigValue().trim();
            SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserName, raw).last("LIMIT 1"));
            if (user != null) {
                if (user.getEmpId() == null) {
                    log.warn("[急诊候诊] 兜底接收人 {} 没有关联员工档案，无法接收站内信", raw);
                    return null;
                }
                String name = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUserName();
                return new Receiver(user.getEmpId(), name);
            }
            return new Receiver(Long.parseLong(raw), "兜底接收人");
        } catch (Exception ex) {
            log.warn("[急诊候诊] 读取兜底接收人配置 {} 失败：{}", FALLBACK_RECEIVER_CONFIG_KEY, ex.getMessage());
            return null;
        }
    }

    private String getZoneByLevel(Integer level) {
        if (level == null) return "绿区";
        return switch (level) {
            case 1, 2 -> "红区";
            case 3 -> "黄区";
            default -> "绿区";
        };
    }

    /**
     * 交班弹框里的「待交班清单」口径：本科室未闭环（候诊/诊治中/留观）里
     * <b>无人指派的</b> + <b>挂在我名下的</b>。
     * <p>
     * 为什么不把"别人名下还在看的"也拉进来：那是他的交班内容，两张单混一张，
     * 事后就说不清"这个人到底是谁交出去的"。但<b>无主的行必须进清单</b> ——
     * 它谁都不交，正是 sql/145 之前那条急诊在池子里躺 288 小时的原因。
     */
    private LambdaQueryWrapper<BizEmergency> handoverScope(Long deptId, Long fromEmpId) {
        LambdaQueryWrapper<BizEmergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizEmergency::getDeptId, deptId)
                .in(BizEmergency::getEmergencyStatus,
                        EmergencyStatusEnum.WAITING.getCode(),
                        EmergencyStatusEnum.TREATING.getCode(),
                        EmergencyStatusEnum.OBSERVATION.getCode())
                .and(w -> w.isNull(BizEmergency::getDoctorId).or().eq(BizEmergency::getDoctorId, fromEmpId))
                // 无主的排最前：它是交班要解决的第一件事；同类里等得最久的排前
                .last("ORDER BY (doctor_id IS NULL) DESC, admission_time ASC");
        return wrapper;
    }

    // 留观时限（sql/153）：读时算两档，不落列

    @Override
    public List<EmergencyHandoverPendingVO> handoverPendingList(Long deptId) {
        Long fromEmpId = requireCurrentEmployee();
        Long scopeDeptId = resolveHandoverDeptId(deptId);
        return this.list(handoverScope(scopeDeptId, fromEmpId)).stream()
                .map(this::toPendingVO)
                .collect(Collectors.toList());
    }

    /**
     * 清单行出参。候诊时长/留观时长/两个档位<b>一律复用 {@link #toVO} 的算法</b>：
     * 同一件事在弹框和列表里算出两个数，值日的人第一个反应是"哪个是假的"。
     */
    private EmergencyHandoverPendingVO toPendingVO(BizEmergency entity) {
        BizEmergencyVO computed = toVO(entity);
        EmergencyHandoverPendingVO vo = new EmergencyHandoverPendingVO();
        vo.setEmergencyId(entity.getId());
        vo.setEmergencyNo(entity.getEmergencyNo());
        vo.setPatientId(entity.getPatientId());
        vo.setPatientName(entity.getPatientName());
        vo.setGender(entity.getGender());
        vo.setAge(entity.getAge());
        vo.setTriageLevel(entity.getTriageLevel());
        vo.setTriageLevelText(EmergencyTriageRules.levelText(entity.getTriageLevel()));
        vo.setEmergencyStatus(entity.getEmergencyStatus());
        vo.setEmergencyStatusText(EmergencyStatusEnum.fromCode(
                entity.getEmergencyStatus() == null ? 0 : entity.getEmergencyStatus()).getLabel());
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
        // 先取本科室在职员工，再把"此刻在岗"的标记出来并排前面
        LinkedHashMap<Long, SysEmployee> employees = new LinkedHashMap<>();
        sysEmployeeMapper.selectList(new LambdaQueryWrapper<SysEmployee>()
                        .eq(SysEmployee::getDeptId, scopeDeptId)
                        .eq(SysEmployee::getStatus, 1))
                .forEach(emp -> employees.putIfAbsent(emp.getId(), emp));
        Set<Long> onDutyIds = new LinkedHashSet<>();
        for (BizSchedule schedule : onDutySchedules(scopeDeptId)) {
            Long doctorId = schedule.getDoctorId();
            // 排班医生未必挂本科室（跨科支援），下拉里要有他，否则"在岗"是假信息
            employees.computeIfAbsent(doctorId, id -> sysEmployeeMapper.selectById(id));
            onDutyIds.add(doctorId);
        }
        return employees.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .filter(e -> !e.getKey().equals(fromEmpId))
                .sorted(Comparator
                        .comparing((Map.Entry<Long, SysEmployee> e) -> !onDutyIds.contains(e.getKey()))
                        .thenComparing(e -> e.getValue().getEmpName() == null ? "" : e.getValue().getEmpName()))
                .map(e -> toCandidate(e.getValue(), onDutyIds.contains(e.getKey()) ? "当前在岗" : "本科室"))
                .collect(Collectors.toList());
    }

    private EmergencyTakeCandidateVO toCandidate(SysEmployee emp, String sourceText) {
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
        if (!StringUtils.hasText(fromEmpName)) {
            SysEmployee me = sysEmployeeMapper.selectById(fromEmpId);
            fromEmpName = me == null ? String.valueOf(fromEmpId) : me.getEmpName();
        }
        Long deptId = resolveHandoverDeptId(submitDTO.getDeptId());
        SysDepartment dept = sysDepartmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException("交班科室不存在：" + deptId);
        }
        // 先判"是不是交给自己"再查员工状态：对着自己报"该员工已停用"是把人往错的方向查
        if (submitDTO.getTakeEmpId() != null && submitDTO.getTakeEmpId().equals(fromEmpId)) {
            throw new BusinessException("接班人不能是交出人本人：交给自己等于没有交接");
        }
        SysEmployee takeEmp = requireActiveEmployee(submitDTO.getTakeEmpId(), "接班人");

        LinkedHashMap<Long, BizEmergency> scope = this.list(handoverScope(deptId, fromEmpId)).stream()
                .collect(Collectors.toMap(BizEmergency::getId, e -> e, (a, b) -> a, LinkedHashMap::new));
        LinkedHashMap<Long, EmergencyHandoverItemDTO> submitted = matchHandoverItems(submitDTO.getItems(), scope);

        // 接续责任人：明细没单独指定就归整单接班人
        LinkedHashMap<Long, SysEmployee> takers = new LinkedHashMap<>();
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
        BizEmergencyHandover handover = new BizEmergencyHandover();
        handover.setHandoverNo(generateHandoverNo());
        handover.setDeptId(deptId);
        handover.setDeptName(dept.getDeptName());
        handover.setFromEmpId(fromEmpId);
        handover.setFromEmpName(fromEmpName);
        handover.setTakeEmpId(takeEmp.getId());
        handover.setTakeEmpName(takeEmp.getEmpName());
        handover.setShiftName(currentShiftName(deptId, fromEmpId));
        // 滚动区间：从我上一次交班那一刻到现在（首次取当日 00:00），与班结单同一口径
        LocalDateTime lastEnd = handoverMapper.selectLastPeriodEnd(fromEmpId, deptId);
        handover.setPeriodBegin(lastEnd == null ? now.toLocalDate().atStartOfDay() : lastEnd);
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
        handoverMapper.insert(handover);

        for (BizEmergency emergency : scope.values()) {
            EmergencyHandoverItemDTO item = submitted.get(emergency.getId());
            SysEmployee taker = takers.get(takeOf.get(emergency.getId()));
            handoverItemMapper.insert(toHandoverItem(handover, emergency, item, taker, fromEmpId));
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
            List<EmergencyHandoverItemDTO> items, LinkedHashMap<Long, BizEmergency> scope) {
        LinkedHashMap<Long, EmergencyHandoverItemDTO> submitted = new LinkedHashMap<>();
        for (EmergencyHandoverItemDTO item : items) {
            BizEmergency emergency = scope.get(item.getEmergencyId());
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

    private BizEmergencyHandoverItem toHandoverItem(BizEmergencyHandover handover, BizEmergency emergency,
                                                    EmergencyHandoverItemDTO item, SysEmployee taker, Long fromEmpId) {
        BizEmergencyHandoverItem row = new BizEmergencyHandoverItem();
        row.setHandoverId(handover.getId());
        row.setEmergencyId(emergency.getId());
        row.setEmergencyNo(emergency.getEmergencyNo());
        row.setPatientId(emergency.getPatientId());
        row.setPatientName(emergency.getPatientName());
        row.setTriageLevel(emergency.getTriageLevel());
        row.setEmergencyStatus(emergency.getEmergencyStatus());
        row.setFromDoctorId(emergency.getDoctorId());
        row.setFromDoctorName(emergency.getDoctorName());
        row.setTakeDoctorId(taker.getId());
        row.setTakeDoctorName(taker.getEmpName());
        // 文本先截后存：入参层不设 @Size，超长写库会撞 Data too long → 交班失败还看不出原因
        row.setDisposition(cutText(item.getDisposition().trim(), 100));
        row.setHandoverNote(StringUtils.hasText(item.getHandoverNote()) ? cutText(item.getHandoverNote().trim(), 300) : null);
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
    private void transferResponsibility(BizEmergency emergency, SysEmployee taker, Long fromEmpId) {
        if (emergency.getDoctorId() != null && !emergency.getDoctorId().equals(fromEmpId)) {
            return;
        }
        emergency.setDoctorId(taker.getId());
        emergency.setDoctorName(taker.getEmpName());
        emergency.setAssignType(EmergencyAssignTypeEnum.HANDOVER_TAKE.getCode());
        // 「未派单原因」是入池那一刻的事实，但这一列的界面语义是"现在为什么还没人负责"；
        // 人已经落定还留着它，列表上就会出现「有医生 + 写着无主原因」的自相矛盾行。留痕在台账的明细里。
        // 必须用 UpdateWrapper 显式 set null：updateById 默认跳过实体里的 null 字段，置空根本不落库。
        this.update(new LambdaUpdateWrapper<BizEmergency>()
                .eq(BizEmergency::getId, emergency.getId())
                .set(BizEmergency::getDoctorId, taker.getId())
                .set(BizEmergency::getDoctorName, taker.getEmpName())
                .set(BizEmergency::getAssignType, EmergencyAssignTypeEnum.HANDOVER_TAKE.getCode())
                .set(BizEmergency::getUnassignedReason, null));
        emergency.setUnassignedReason(null);

        // 挂号单与叫号队列同步换人。这里<b>不能</b>复用 findEmergencyAppoint(patientId)：
        // 它取的是「该患者最近一张急诊号」，同一个人第二次进急诊就会错抓到上一次那趟就诊，
        // 交班会把不该动的历史记录改掉医生。登记时队列短号固定是「急 + 急诊号后 5 位」，
        // 按它加患者定位出来的才是本次就诊。
        BizQueue queue = findEmergencyQueueOf(emergency);
        if (queue == null) {
            return;
        }
        if (queue.getDoctorId() == null || queue.getDoctorId().equals(fromEmpId)) {
            queue.setDoctorId(taker.getId());
            queue.setDoctorName(taker.getEmpName());
            queueMapper.updateById(queue);
        }
        BizAppointInfo appointInfo = appointInfoMapper.selectById(queue.getRegistId());
        if (appointInfo != null && (appointInfo.getDoctorId() == null || appointInfo.getDoctorId().equals(fromEmpId))) {
            appointInfo.setDoctorId(taker.getId());
            appointInfo.setDoctorName(taker.getEmpName());
            appointInfoMapper.updateById(appointInfo);
        }
    }

    /**
     * 本次急诊对应的叫号队列行（按「急+急诊号后 5 位」+ 患者定位，不是"该患者最近一张号"）
     */
    private BizQueue findEmergencyQueueOf(BizEmergency emergency) {
        String erNo = emergency.getEmergencyNo();
        if (!StringUtils.hasText(erNo) || erNo.length() < 5) {
            return null;
        }
        return queueMapper.selectOne(new LambdaQueryWrapper<BizQueue>()
                .eq(BizQueue::getQueueNo, "急" + erNo.substring(erNo.length() - 5))
                .eq(BizQueue::getPatientId, emergency.getPatientId())
                .orderByDesc(BizQueue::getId)
                .last("LIMIT 1"));
    }

    /**
     * 每位接续医生一条待办（同一人接手多条不刷屏）：交班的闭环点是"他知道有人被交给他了"
     */
    private void notifyTakers(BizEmergencyHandover handover, LinkedHashMap<Long, BizEmergency> scope,
                              LinkedHashMap<Long, EmergencyHandoverItemDTO> submitted,
                              LinkedHashMap<Long, Long> takeOf, LinkedHashMap<Long, SysEmployee> takers) {
        LinkedHashMap<Long, List<BizEmergency>> byTaker = new LinkedHashMap<>();
        scope.values().forEach(e -> byTaker
                .computeIfAbsent(takeOf.get(e.getId()), id -> new ArrayList<>()).add(e));
        byTaker.forEach((takerId, list) -> {
            SysEmployee taker = takers.get(takerId);
            String detail = list.stream()
                    .map(e -> {
                        BizEmergencyVO computed = toVO(e);
                        String stay = computed.getWaitMinutes() != null ? "候诊 " + computed.getWaitMinutes() + " 分钟" : "";
                        if (computed.getObsHours() != null) {
                            stay = (StringUtils.hasText(stay) ? stay + "，" : "") + "留观 " + computed.getObsHours() + " 小时";
                        }
                        return e.getPatientName() + "(" + e.getEmergencyNo() + ")："
                                + submitted.get(e.getId()).getDisposition()
                                + (StringUtils.hasText(stay) ? "；" + stay : "");
                    })
                    .collect(Collectors.joining("\n"));
            String content = handover.getFromEmpName() + " 在 " + handover.getDeptName()
                    + " 交班，共移交 " + list.size() + " 人，请立即查看病历确认接手：\n" + detail;
            LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
            payload.put("handoverNo", handover.getHandoverNo());
            payload.put("deptName", handover.getDeptName());
            payload.put("fromEmpName", handover.getFromEmpName());
            payload.put("count", list.size());
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
    public PageResult<EmergencyHandoverVO> handoverListPage(EmergencyHandoverQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizEmergencyHandover> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getDeptId() != null, BizEmergencyHandover::getDeptId, queryDTO.getDeptId())
                .and(StringUtils.hasText(queryDTO.getKeyword()), w -> w
                        .like(BizEmergencyHandover::getHandoverNo, queryDTO.getKeyword())
                        .or().like(BizEmergencyHandover::getFromEmpName, queryDTO.getKeyword())
                        .or().like(BizEmergencyHandover::getTakeEmpName, queryDTO.getKeyword())
                        // 交过的患者也要能搜到：台账最常见的用法是"这个人上次是谁交的"
                        .or().apply("EXISTS (SELECT 1 FROM biz_emergency_handover_item i "
                                        + "WHERE i.handover_id = biz_emergency_handover.id AND i.del_flag = 0 "
                                        + "AND (i.patient_name LIKE CONCAT('%', {0}, '%') OR i.emergency_no LIKE CONCAT('%', {0}, '%')))",
                                queryDTO.getKeyword()))
                .orderByDesc(BizEmergencyHandover::getPeriodEnd);
        Page<BizEmergencyHandover> page = handoverMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<EmergencyHandoverVO> records = page.getRecords() == null ? List.of()
                : page.getRecords().stream().map(h -> BeanUtil.copyProperties(h, EmergencyHandoverVO.class))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public EmergencyHandoverDetailVO handoverDetailById(Long id) {
        BizEmergencyHandover handover = handoverMapper.selectById(id);
        if (handover == null) {
            throw new BusinessException("交班单不存在");
        }
        EmergencyHandoverDetailVO detail = new EmergencyHandoverDetailVO();
        detail.setHandover(BeanUtil.copyProperties(handover, EmergencyHandoverVO.class));
        detail.setItems(handoverItemMapper.selectList(new LambdaQueryWrapper<BizEmergencyHandoverItem>()
                        .eq(BizEmergencyHandoverItem::getHandoverId, id)
                        .orderByAsc(BizEmergencyHandoverItem::getId))
                .stream().map(this::toItemVO).collect(Collectors.toList()));
        return detail;
    }

    private EmergencyHandoverItemVO toItemVO(BizEmergencyHandoverItem item) {
        EmergencyHandoverItemVO vo = BeanUtil.copyProperties(item, EmergencyHandoverItemVO.class);
        vo.setTriageLevelText(EmergencyTriageRules.levelText(item.getTriageLevel()));
        vo.setEmergencyStatusText(EmergencyStatusEnum.fromCode(
                item.getEmergencyStatus() == null ? 0 : item.getEmergencyStatus()).getLabel());
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
        LambdaQueryWrapper<BizEmergency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizEmergency::getEmergencyStatus, EmergencyStatusEnum.OBSERVATION.getCode())
                .isNotNull(BizEmergency::getObservationStartTime)
                .apply(OBS_OVER_SQL, obsThresholdOfMax())
                .orderByAsc(BizEmergency::getObservationStartTime);
        List<BizEmergency> overdue = this.list(wrapper);
        int sent = 0;
        for (BizEmergency emergency : overdue) {
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

    private int escalateObservationOne(BizEmergency emergency) {
        Long obsHours = obsHoursOf(emergency);
        if (obsHours == null || obsLevelOf(emergency) < 2) {
            return 0;
        }
        Set<Long> receivers = new LinkedHashSet<>();
        if (emergency.getDoctorId() != null) {
            receivers.add(emergency.getDoctorId());
        }
        // 留观患者往往由值班组共管：主管医生不在（换班/下班）时，当班医生与科室主任一并进阶梯
        onDutySchedules(emergency.getDeptId()).forEach(s -> receivers.add(s.getDoctorId()));
        receivers.addAll(deptLeaderIds(emergency.getDeptId()));

        List<Long> alreadySent = sysMessageService.receiverIdsOfBiz(BizTypeEnum.EMERGENCY_OBS.getType(), emergency.getId());
        receivers.removeAll(alreadySent);
        if (receivers.isEmpty()) {
            // 与候诊升级同一条阶梯的倒数第二级：本科室（主管医生/当班医生/科主任）都催过了，
            // 留给床位的争议（"该转哪个科""哪个科肯收"）只有总值班能跨科拍板。
            DutyOfficerVO duty = dutyRosterService.current();
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
            LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
            payload.put("patientName", emergency.getPatientName());
            payload.put("emergencyNo", emergency.getEmergencyNo());
            payload.put("deptName", emergency.getDeptName());
            payload.put("obsHours", obsHours);
            payload.put("observationBed", emergency.getObservationBed());
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
        SysEmployee emp = sysEmployeeMapper.selectById(empId);
        return emp != null && StringUtils.hasText(emp.getEmpName()) ? emp.getEmpName() : "站内用户";
    }

    /**
     * 已留观小时数（向下取整）；不在留观态且没有结束时间时返回 null（不编造时长）
     */
    private Long obsHoursOf(BizEmergency entity) {
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
    private int obsLevelOf(BizEmergency entity) {
        if (!Objects.equals(entity.getEmergencyStatus(), EmergencyStatusEnum.OBSERVATION.getCode())) {
            return 0;
        }
        Long hours = obsHoursOf(entity);
        return hours == null ? 0 : obsPolicy.levelOf(hours);
    }

    private Long waitMinutesOf(BizEmergency entity) {
        BizEmergencyVO computed = toVO(entity);
        return computed.getWaitMinutes();
    }

    private int overdueOf(BizEmergency entity) {
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

    private SysEmployee requireActiveEmployee(Long empId, String label) {
        // ② 非 web 入口的入参：整单接班人与明细推导出的接续医生共用此工具，后者不是 HTTP 参数绑定，Bean Validation 跑不到
        if (empId == null) {
            throw new BusinessException(label + "不能为空");
        }
        SysEmployee emp = sysEmployeeMapper.selectById(empId);
        if (emp == null) {
            throw new BusinessException(label + "不存在：" + empId);
        }
        if (emp.getStatus() != null && emp.getStatus() != 1) {
            throw new BusinessException(label + "已停用：" + emp.getEmpName());
        }
        return emp;
    }

    /**
     * 交出人此刻所在班次名（台账上"白班交的班"要能看出来）；查不到就留空，不编
     */
    private String currentShiftName(Long deptId, Long empId) {
        LocalTime now = LocalTime.now();
        for (BizSchedule schedule : scheduleService.getTodaySchedule(deptId)) {
            if (!Objects.equals(schedule.getDoctorId(), empId) || schedule.getShiftId() == null) {
                continue;
            }
            LocalTime start = ShiftCoverUtil.parseShiftTime(schedule.getStartTime());
            LocalTime end = ShiftCoverUtil.parseShiftTime(schedule.getEndTime());
            if (start == null || end == null) {
                continue;
            }
            if (ShiftCoverUtil.covers(now, start, end)) {
                BizShift shift = bizShiftMapper.selectById(schedule.getShiftId());
                return shift == null ? null : shift.getShiftName();
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
        long seq = sequenceService.next("EMERGENCY_HANDOVER");
        return "EJ" + dateStr + String.format("%04d", seq);
    }

    private String cutText(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    /**
     * 兜底接收人（员工ID + 姓名）
     */
    private record Receiver(Long employeeId, String name) {
    }
}
