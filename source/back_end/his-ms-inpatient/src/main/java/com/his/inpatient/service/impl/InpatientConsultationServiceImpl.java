package com.his.inpatient.service.impl;

import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.PatientMasterVO;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.spi.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.inpatient.dto.*;
import com.his.inpatient.entity.*;
import com.his.inpatient.mapper.*;
import com.his.inpatient.service.InpatientConsultationService;
import com.his.inpatient.vo.ConsultationVO;
import com.his.inpatient.vo.InpatientConsultInvitePayloadVO;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.EmployeeBriefVO;
import com.his.spi.vo.WardVO;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.BizTypeEnum;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.SysMessageService;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 住院会诊服务实现（P4.1）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientConsultationServiceImpl extends ServiceImpl<BizConsultationMapper, BizConsultation> implements InpatientConsultationService {

    private final PatientFacadeService patientFacadeService;
    private final RedisSequenceService redisSequenceService;
    /**
     * 申请时未指定会诊医生：既有列 doctor_id 是 NOT NULL，用 0 表示"未指定"
     */
    private static final long DOCTOR_UNSPECIFIED = 0L;
    /**
     * 急会诊响应时限（分钟）。
     * <p>只作为**查询时判定**超时的依据，不落状态列 —— 与"危急值超时是查询时算的"同一口径。
     */
    private static final int URGENT_RESPONSE_MINUTES = 10;

    /**
     * 普通会诊响应时限（分钟）= 24 小时。
     * <p>与急会诊同样**只在查询时判定**超时，不落状态列；与营养看板的
     * 「普通超 24 小时」口径一致（两处都对不上时，临床会同时看到两种答案）。
     */
    private static final int NORMAL_RESPONSE_MINUTES = 24 * 60;

    private final SysMessageService sysMessageService;


    private final BizConsultationMapper bizConsultationMapper;

    private final BizAdmissionMapper bizAdmissionMapper;


    private final BizInpatientRecordMapper bizInpatientRecordMapper;

    private final SysBedMapper sysBedMapper;

    private final SystemFacadeService systemFacadeService;

    private final DeptScopeService deptScopeService;

    // 申请 / 修改

    /**
     * 会诊是否按时应答：急会诊 ≤10 分钟、普通 ≤24 小时；未应答按超时计。
     */
    private static boolean onTime(Integer urgent, LocalDateTime applyTime, LocalDateTime acceptTime) {
        if (applyTime == null || acceptTime == null) {
            return false;
        }
        long minutes = Duration.between(applyTime, acceptTime).toMinutes();
        if (minutes < 0) {
            return false;
        }
        return urgent != null && urgent == YesOrNoEnum.YES.getCode()
                ? minutes <= URGENT_RESPONSE_MINUTES
                : minutes <= NORMAL_RESPONSE_MINUTES;
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(ConsultationUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Integer category = dto.getConsultCategory() == null
                ? ConsultCategoryEnum.NORMAL.getCode() : dto.getConsultCategory();

        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException(400, "入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException(400, "该患者已出院，不能再申请会诊");
        }
        Long fromDeptId = resolveAdmissionDeptId(admission);
        if (fromDeptId == null) {
            throw new BusinessException(400, "入院记录缺少科室信息，无法确定申请科室");
        }
        // 数据权限：只能为自己科室在院的患者申请会诊（会诊科室是业务目标，不受限）
        deptScopeService.assertDeptAccessible(fromDeptId);

        // 范围与科室的一致性：科内必须同科室；科间/全院必须跨科室。
        // 提示只说「选错了」，不回报内部科室 ID —— 医生看的是科室名，ID 只会让人以为是系统故障。
        boolean sameDept = Objects.equals(fromDeptId, dto.getToDeptId());
        if (Objects.equals(ConsultScopeEnum.IN_DEPT.getCode(), dto.getConsultType()) && !sameDept) {
            throw new BusinessException(400, "「科内会诊」的会诊科室必须是本科室；请到别的科室会诊请改选「科间会诊」");
        }
        if (!Objects.equals(ConsultScopeEnum.IN_DEPT.getCode(), dto.getConsultType()) && sameDept) {
            throw new BusinessException(400, "「" + ConsultScopeEnum.getLabelOrUnknown(dto.getConsultType())
                    + "」必须请到别的科室；本科室内部的请会诊请选「科内会诊」");
        }

        LocalDateTime now = TimeUtil.nowSeconds();
        int isUrgent = Objects.equals(YesOrNoEnum.YES.getCode(), dto.getIsUrgent())
                ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode();

        if (dto.getId() != null) {
            return updateOne(dto, admission, isUrgent);
        }

        // 新增：同一住院 + 同一会诊科室不允许并存两条未完成会诊（四核对的"重复"）
        long unfinished = bizConsultationMapper.selectCount(new LambdaQueryWrapper<BizConsultation>()
                .eq(BizConsultation::getAdmissionId, admission.getAdmissionId())
                .eq(BizConsultation::getToDeptId, dto.getToDeptId())
                .in(BizConsultation::getConsultStatus, ConsultationStatusEnum.PENDING.getCode(), ConsultationStatusEnum.ACCEPTED.getCode()));
        if (unfinished > 0) {
            throw new BusinessException(400, "该住院已存在向「" + deptNameOf(dto.getToDeptId())
                    + "」发起且未完成的会诊（" + unfinished + " 条），不能重复申请；请先完成或取消原会诊");
        }

        BizConsultation entity = new BizConsultation();
        entity.setConsultationNo(nextConsultationNo());
        entity.setPatientId(admission.getPatientId());
        entity.setVisitId(admission.getVisitId());
        entity.setAdmissionId(admission.getAdmissionId());
        entity.setFromDeptId(fromDeptId);
        entity.setApplyDoctorId(operatorUser.getEmployeeId());
        entity.setApplyDoctorName(operatorUser.getRealName());
        entity.setToDeptId(dto.getToDeptId());
        entity.setConsultType(dto.getConsultType());
        // 类别申请时定死，修改申请不允许改类别（否则能把会诊从营养工作台藏到普通工作台）
        entity.setConsultCategory(category);
        entity.setIsUrgent(isUrgent);
        entity.setReason(dto.getReason());
        entity.setDoctorId(dto.getDoctorId() != null ? dto.getDoctorId() : DOCTOR_UNSPECIFIED);
        entity.setApplyTime(now);
        entity.setConsultStatus(ConsultationStatusEnum.PENDING.getCode());
        entity.setRemark(dto.getRemark());
        bizConsultationMapper.insert(entity);

        notifyNewConsultation(entity, admission);
        log.info("申请会诊 consultationNo={} admissionId={} 申请科室={} 会诊科室={} 范围={} 急={} 申请医生={}",
                entity.getConsultationNo(), admission.getAdmissionId(), fromDeptId, dto.getToDeptId(),
                ConsultScopeEnum.getLabel(dto.getConsultType()), isUrgent, entity.getApplyDoctorName());
        return entity.getConsultationNo();
    }

    // 应答

    /**
     * consult 发送方：新会诊申请 → 站内信通知会诊方（待办型，接诊/取消时闭环）。
     *
     * <p>收件人两路：申请时指定了会诊医生（doctorId != 0）→ 只发给该医生；
     * 未指定 → 发给会诊科室全部在职员工（会诊本来就是请到科室，由科室自行安排人接诊）。
     *
     * <p>急会诊 urgent（红顶置顶），普通会诊 warning。发送失败只记日志：
     * 会诊单已落库，一条消息不该把临床申请回滚掉。
     */
    private void notifyNewConsultation(BizConsultation entity, BizAdmission admission) {
        try {
            List<EmployeeBriefVO> receivers = new java.util.ArrayList<>();
            if (entity.getDoctorId() != null && entity.getDoctorId() != DOCTOR_UNSPECIFIED) {
                EmployeeBriefVO specified = systemFacadeService.findEmployee(entity.getDoctorId());
                if (specified != null && Integer.valueOf(1).equals(specified.getStatus())) {
                    receivers.add(specified);
                }
            } else {
                receivers = systemFacadeService.listActiveEmployeesByDept(entity.getToDeptId());
            }
            if (receivers.isEmpty()) {
                log.warn("[会诊] 未找到会诊方收件人，站内信跳过 consultationNo={} toDeptId={} doctorId={}",
                        entity.getConsultationNo(), entity.getToDeptId(), entity.getDoctorId());
                return;
            }
            PatientMasterVO patient = patientFacadeService.getPatient(admission.getPatientId());
            String patientName = patient == null || patient.getPatientName() == null ? "未知" : patient.getPatientName();
            boolean urgent = Objects.equals(YesOrNoEnum.YES.getCode(), entity.getIsUrgent());
            String title = (urgent ? "急会诊邀请：" : "会诊邀请：") + patientName
                    + "（" + entity.getConsultationNo() + "）";
            String content = String.format(
                    "%s %s 医生为患者 %s 申请%s会诊（%s）：\n会诊理由：%s\n请尽快在会诊列表中「接诊」。",
                    deptNameOf(entity.getFromDeptId()),
                    entity.getApplyDoctorName() == null ? "未知" : entity.getApplyDoctorName(),
                    patientName,
                    urgent ? "急" : "",
                    ConsultScopeEnum.getLabel(entity.getConsultType()),
                    entity.getReason());
            InpatientConsultInvitePayloadVO payload = new InpatientConsultInvitePayloadVO();
            payload.setPatientName(patientName);
            payload.setConsultationNo(entity.getConsultationNo());
            payload.setToDeptName(deptNameOf(entity.getToDeptId()));
            payload.setReason(entity.getReason());
            payload.setUrgent(urgent);
            String payloadJson = cn.hutool.json.JSONUtil.toJsonStr(payload);
            for (EmployeeBriefVO receiver : receivers) {
                sysMessageService.sendSystemMessage(receiver.getId(),
                        receiver.getEmpName() == null ? "站内用户" : receiver.getEmpName(),
                        title, content,
                        BizTypeEnum.CONSULT.getType(), entity.getConsultationId(),
                        urgent ? "urgent" : "warning", payloadJson, 0);
            }
        } catch (Exception ex) {
            log.warn("[会诊] 邀请通知发送失败 consultationNo={} toDeptId={}",
                    entity.getConsultationNo(), entity.getToDeptId(), ex);
        }
    }


    /**
     * 修改「待应答」的申请。
     */
    private String updateOne(ConsultationUpsertDTO dto, BizAdmission admission, int isUrgent) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizConsultation entity = bizConsultationMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException(400, "会诊记录不存在");
        }
        if (!Objects.equals(entity.getAdmissionId(), admission.getAdmissionId())) {
            throw new BusinessException(400, "该会诊不属于本次住院，不能修改");
        }
        if (!Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException(400, "会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.getLabelOrUnknown(entity.getConsultStatus())
                    + "」，只有「待应答」的会诊申请可以修改；已应答的请直接完成会诊");
        }
        long unfinished = bizConsultationMapper.selectCount(new LambdaQueryWrapper<BizConsultation>()
                .eq(BizConsultation::getAdmissionId, admission.getAdmissionId())
                .eq(BizConsultation::getToDeptId, dto.getToDeptId())
                .in(BizConsultation::getConsultStatus, ConsultationStatusEnum.PENDING.getCode(), ConsultationStatusEnum.ACCEPTED.getCode())
                .ne(BizConsultation::getConsultationId, entity.getConsultationId()));
        if (unfinished > 0) {
            throw new BusinessException(400, "该住院已存在向「" + deptNameOf(dto.getToDeptId())
                    + "」发起且未完成的会诊，不能改成同一个会诊科室");
        }

        entity.setToDeptId(dto.getToDeptId());
        entity.setConsultType(dto.getConsultType());
        entity.setIsUrgent(isUrgent);
        entity.setReason(dto.getReason());
        entity.setDoctorId(dto.getDoctorId() != null ? dto.getDoctorId() : DOCTOR_UNSPECIFIED);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        bizConsultationMapper.updateById(entity);
        log.info("修改会诊申请 consultationNo={} 会诊科室={} 范围={} 急={} 操作人={}",
                entity.getConsultationNo(), dto.getToDeptId(),
                ConsultScopeEnum.getLabel(dto.getConsultType()), isUrgent, operatorUser.getRealName());
        return entity.getConsultationNo();
    }

    // 取消

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(ConsultationAcceptDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizConsultation entity = mustGet(dto.getConsultationId());
        // 接诊只能由会诊科室的人做：申请科室能看见这单，但不能替会诊科室接诊
        assertReceivingAccessible(entity);
        if (!Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException(400, "会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.getLabelOrUnknown(entity.getConsultStatus()) + "」，不能应答");
        }
        Long doctorId = operatorUser.getEmployeeId();
        if (doctorId == null) {
            // 没登录上下文就没有"谁接诊"这件事 —— 宁可报错，也不留一条没有接诊人的会诊
            throw new BusinessException("未能识别当前登录用户，无法记录接诊医生");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        String doctorName = operatorUser.getRealName();

        entity.setConsultStatus(ConsultationStatusEnum.ACCEPTED.getCode());
        entity.setAcceptTime(now);
        entity.setAcceptDoctorId(doctorId);
        entity.setAcceptDoctorName(doctorName);
        // 接诊即确定实际会诊医生：申请时指定的（或 0=未指定）在这里被真实的人覆盖
        entity.setDoctorId(doctorId);
        if (TextUtil.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        bizConsultationMapper.updateById(entity);

        // 消息侧待办联动：会诊已接诊 → 该会诊的站内信 handle_status 0→1。
        closeConsultTodo(entity.getConsultationId(), 1);

        // 急会诊超时只提醒、不阻断：临床已经接诊了，再拒绝反而是把病人放下
        Long waitMinutes = TimeUtil.minutesBetween(entity.getApplyTime(), now);
        boolean overdue = Objects.equals(YesOrNoEnum.YES.getCode(), entity.getIsUrgent()) && waitMinutes != null
                && waitMinutes > URGENT_RESPONSE_MINUTES;
        if (overdue) {
            log.warn("急会诊 {} 应答超时：申请时间={} 接诊时间={} 等待 {} 分钟（时限 {} 分钟），接诊医生={}",
                    entity.getConsultationNo(), entity.getApplyTime(), now, waitMinutes,
                    URGENT_RESPONSE_MINUTES, doctorName);
        }
        log.info("会诊应答 consultationNo={} 接诊医生={} 等待分钟={}", entity.getConsultationNo(), doctorName, waitMinutes);

        notifyApplicant(entity, "会诊已接诊", deptNameOf(entity.getToDeptId()) + " " + doctorName
                + " 已接诊（会诊号 " + entity.getConsultationNo() + "），等待会诊结论。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String finish(ConsultationFinishDTO dto) {
        BizConsultation entity = mustGet(dto.getConsultationId());
        // 结论是会诊科室写的：申请科室不能替别人下结论
        assertReceivingAccessible(entity);

        // ★ 铁律：未应答不可完成（= 医嘱未校对不可执行）
        if (Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException(400, "会诊 " + entity.getConsultationNo()
                    + " 尚未被会诊科室应答，不能填写结论；请先由会诊科室「接诊」");
        }
        if (!Objects.equals(ConsultationStatusEnum.ACCEPTED.getCode(), entity.getConsultStatus())) {
            throw new BusinessException(400, "会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.getLabelOrUnknown(entity.getConsultStatus()) + "」，不能重复完成");
        }

        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime consultTime = TimeUtil.toSeconds(dto.getConsultTime() != null ? dto.getConsultTime() : now);
        if (entity.getAcceptTime() != null && consultTime.isBefore(entity.getAcceptTime())) {
            throw new BusinessException(400, "会诊时间不能早于接诊时间（接诊时间 " + entity.getAcceptTime() + "）");
        }

        // 回写住院病历：先写病历，再改会诊 —— 病历写不进去就不许把会诊标成完成
        BizInpatientRecord record = writeBackRecord(entity, dto.getConclusion(), consultTime, now);
        Long recordId = record.getId();

        entity.setConsultStatus(ConsultationStatusEnum.FINISHED.getCode());
        entity.setConclusion(dto.getConclusion());
        entity.setConsultTime(consultTime);
        entity.setFinishTime(now);
        entity.setRecordId(recordId);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        bizConsultationMapper.updateById(entity);

        log.info("会诊完成 consultationNo={} 会诊科室={} 结论字数={} 回写病历ID={} recordNo={}",
                entity.getConsultationNo(), entity.getToDeptId(),
                dto.getConclusion().length(), recordId, record.getRecordNo());

        notifyApplicant(entity, "会诊已完成", deptNameOf(entity.getToDeptId()) + " "
                + (entity.getAcceptDoctorName() == null ? "" : entity.getAcceptDoctorName())
                + " 已完成会诊（会诊号 " + entity.getConsultationNo() + "），结论已回写病历 "
                + record.getRecordNo() + "。");
        // 返回病历号而不是雪花主键：界面上「病历 BL2026...」才是医生能拿去翻病历的东西
        return record.getRecordNo();
    }

    /**
     * 把会诊结论回写成一份住院病历文书（record_type=9 会诊记录，状态直接「已提交」）。
     */
    private BizInpatientRecord writeBackRecord(BizConsultation entity, String conclusion,
                                               LocalDateTime consultTime, LocalDateTime now) {
        BizAdmission admission = bizAdmissionMapper.selectById(entity.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在，无法回写会诊病历（admissionId=" + entity.getAdmissionId() + "）");
        }
        PatientMasterVO patient = patientFacadeService.getPatient(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在，无法回写会诊病历");
        }

        String deptName = null;
        String wardName = null;
        if (admission.getWardId() != null) {
            WardVO ward = sysBedMapper.selectWardById(admission.getWardId());
            if (ward != null) {
                wardName = ward.getWardName();
                deptName = ward.getDeptName();
            }
        }
        String bedNo = null;
        if (admission.getBedId() != null) {
            SysBed bed = sysBedMapper.selectById(admission.getBedId());
            if (bed != null) {
                bedNo = bed.getBedNo();
            }
        }

        BizInpatientRecord record = new BizInpatientRecord();
        record.setRecordNo(nextRecordNo());
        record.setAdmissionId(admission.getAdmissionId());
        record.setPatientId(admission.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());
        record.setGender(patient.getGender());
        record.setAge(patient.getAge());
        record.setAgeUnit(AgeUnitEnum.YEAR.getCode());
        record.setDeptId(admission.getDeptId() != null ? admission.getDeptId() : entity.getFromDeptId());
        record.setDeptName(deptName);
        record.setWardId(admission.getWardId());
        record.setWardName(wardName);
        record.setBedNo(bedNo);
        record.setRecordType(InpatientRecordTypeEnum.CONSULTATION.getCode());
        record.setRecordTitle(deptNameOf(entity.getToDeptId()) + "会诊记录");
        record.setRecordTime(consultTime);
        // 结论进正文；会诊理由与申请医生也留痕在正文里，病历自洽（不必翻会诊表才能读懂）
        record.setCourseNote(conclusion);
        record.setRemark("系统回写：会诊号 " + entity.getConsultationNo()
                + "，会诊科室 " + deptNameOf(entity.getToDeptId())
                + "，会诊理由：" + entity.getReason());
        record.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
        // 签名的医生是**实际会诊医生**（接诊人），不是发起人
        record.setDoctorId(entity.getAcceptDoctorId() != null ? entity.getAcceptDoctorId() : entity.getDoctorId());
        record.setDoctorName(entity.getAcceptDoctorName());
        record.setSubmitTime(now);
        bizInpatientRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(ConsultationCancelDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizConsultation entity = mustGet(dto.getConsultationId());
        assertConsultAccessible(entity.getFromDeptId(), entity.getToDeptId());
        if (Objects.equals(ConsultationStatusEnum.CANCELLED.getCode(), entity.getConsultStatus())) {
            throw new BusinessException(400, "会诊 " + entity.getConsultationNo() + " 已取消，不能重复取消");
        }
        if (!Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException(400, "会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.getLabelOrUnknown(entity.getConsultStatus())
                    + "」，不能取消；会诊科室已接诊的会诊必须走「完成」");
        }
        entity.setConsultStatus(ConsultationStatusEnum.CANCELLED.getCode());
        entity.setCancelReason(dto.getCancelReason());
        bizConsultationMapper.updateById(entity);

        // 消息侧待办联动：会诊已取消 → 待办消息置「已关闭」（2），收件人不用再惦记
        closeConsultTodo(entity.getConsultationId(), 2);

        log.info("取消会诊 consultationNo={} 原因={} 操作人={}",
                entity.getConsultationNo(), dto.getCancelReason(), operatorUser.getRealName());
    }

    /**
     * 会诊待办消息闭环：handle_status 0 → {@code targetStatus}（1 已处理 / 2 已关闭）
     */
    private void closeConsultTodo(Long consultationId, int targetStatus) {
        try {
            sysMessageService.closeTodoByBiz(BizTypeEnum.CONSULT.getType(), consultationId, targetStatus);
        } catch (Exception ex) {
            // 闭环联动失败不回滚业务：会诊状态是对的，只是收件箱滞后
            log.warn("[会诊] 待办消息闭环失败 consultationId={} targetStatus={}", consultationId, targetStatus, ex);
        }
    }

    @Override
    public IPage<ConsultationVO> listPage(ConsultationPageQueryDTO query) {
        Page<ConsultationVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Long reqDeptId = query.getFromDeptId() != null ? query.getFromDeptId() : query.getToDeptId();
        List<Long> deptIds = deptScopeService.scopedDeptIds(reqDeptId);
        IPage<ConsultationVO> result = bizConsultationMapper.selectConsultationPage(page, query, deptIds);
        result.getRecords().forEach(this::decorate);
        return result;
    }

    @Override
    public ConsultationVO getDetailById(Long consultationId) {
        ConsultationVO vo = bizConsultationMapper.selectConsultationById(consultationId);
        if (vo == null) {
            throw new BusinessException(400, "会诊记录不存在");
        }
        assertConsultAccessible(vo.getFromDeptId(), vo.getToDeptId());
        decorate(vo);
        return vo;
    }

    @Override
    public long countUnfinished(Long toDeptId, Long admissionId) {
        return bizConsultationMapper.countUnfinished(deptScopeService.scopedDeptIds(toDeptId), admissionId);
    }

    private void decorate(ConsultationVO vo) {
        vo.setConsultTypeText(ConsultScopeEnum.getLabel(vo.getConsultType()));
        vo.setConsultStatusText(ConsultationStatusEnum.getLabel(vo.getConsultStatus()));
        vo.setIsUrgentText(YesOrNoEnum.getLabel(vo.getIsUrgent()));
        // 类别 null（存量行未填）→ 一律按"普通科间会诊"显示；非 null 脏数据 → 空串，由数据治理修复，不伪装
        vo.setConsultCategoryText(vo.getConsultCategory() == null
                ? ConsultCategoryEnum.NORMAL.getLabel() : ConsultCategoryEnum.getLabel(vo.getConsultCategory()));
        // 是否按时应答：营养会诊及时应答率的行级依据；未应答一律按超时计
        vo.setOnTime(onTime(vo.getIsUrgent(), vo.getApplyTime(), vo.getAcceptTime()));

        boolean pending = Objects.equals(ConsultationStatusEnum.PENDING.getCode(), vo.getConsultStatus());
        boolean accepted = Objects.equals(ConsultationStatusEnum.ACCEPTED.getCode(), vo.getConsultStatus());
        // 按钮只按「谁真能点」给：接诊/完成限会诊科室，否则申请方会看到一个点了就 403 的按钮；
        // 营养工作台按类别并集会把发到别科的会诊也列出来，这里不同步收口就必然出现死按钮
        boolean receiving = deptScopeService.canAccessDept(vo.getToDeptId());
        boolean applying = deptScopeService.canAccessDept(vo.getFromDeptId());
        vo.setCanAccept(pending && receiving);
        vo.setCanFinish(accepted && receiving);
        vo.setCanEdit(pending && applying);
        vo.setCanCancel(pending && (applying || receiving));

        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime from = vo.getApplyTime();
        LocalDateTime to = vo.getAcceptTime() != null ? vo.getAcceptTime() : now;
        vo.setResponseMinutes(from == null ? null : TimeUtil.minutesBetween(from, to));

        // 超时**查询时算**：只看"还没应答的"，急会诊 10 分钟、普通会诊 24 小时
        boolean urgent = Objects.equals(YesOrNoEnum.YES.getCode(), vo.getIsUrgent());
        int limitMinutes = urgent ? URGENT_RESPONSE_MINUTES : NORMAL_RESPONSE_MINUTES;
        boolean overdue = pending && vo.getApplyTime() != null
                && now.isAfter(vo.getApplyTime().plusMinutes(limitMinutes));
        vo.setOverdue(overdue);
        if (overdue) {
            Long waited = TimeUtil.minutesBetween(vo.getApplyTime(), now);
            vo.setOverdueText(urgent
                    ? "急会诊已等待 " + waited + " 分钟未应答（时限 " + URGENT_RESPONSE_MINUTES + " 分钟）"
                    : "普通会诊已等待 " + (waited == null ? "—" : waited / 60) + " 小时未应答（时限 24 小时）");
        }
    }

    private BizConsultation mustGet(Long consultationId) {
        BizConsultation entity = bizConsultationMapper.selectById(consultationId);
        if (entity == null) {
            throw new BusinessException(400, "会诊记录不存在");
        }
        return entity;
    }

    /**
     * 申请科室取入院科室；入院科室为空时从病区推导（病区必有科室）
     */
    private Long resolveAdmissionDeptId(BizAdmission admission) {
        if (admission.getDeptId() != null) {
            return admission.getDeptId();
        }
        if (admission.getWardId() != null) {
            WardVO ward = sysBedMapper.selectWardById(admission.getWardId());
            if (ward != null) {
                return ward.getDeptId();
            }
        }
        return null;
    }

    /**
     * 科室名（取不到就返回原文案"ID=xx"，绝不编一个科室名）
     */
    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return "未知科室";
        }
        String name = bizConsultationMapper.selectDeptName(deptId);
        return TextUtil.hasText(name) ? name : "未知科室(ID=" + deptId + ")";
    }

    private String nextConsultationNo() {
        return redisSequenceService.generateConsultationNo();
    }

    private String nextRecordNo() {
        return redisSequenceService.generateInpatientRecordNo();
    }

    /**
     * 会诊单双方（申请/会诊科室）任一在授权范围内即可**查看**（详情、取消）。
     */
    private void assertConsultAccessible(Long fromDeptId, Long toDeptId) {
        if (!deptScopeService.canAccessDept(fromDeptId)
                && !deptScopeService.canAccessDept(toDeptId)) {
            throw new BusinessException(403, "该数据所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * 应答/完成只认会诊科室：申请方看得见这单，但不能替会诊科室接诊，更不能替别人写结论。
     * <p>只按「申请方或受邀方」放行会让申请医生接诊自己发出去的会诊 —— 病历上会留一个
     * 自己请自己会诊的结论，责任链断在这里。
     */
    private void assertReceivingAccessible(BizConsultation entity) {
        if (!deptScopeService.canAccessDept(entity.getToDeptId())) {
            throw new BusinessException(403, "只有会诊科室「" + deptNameOf(entity.getToDeptId())
                    + "」的医师可以接诊与完成会诊");
        }
    }

    /**
     * 会诊进展回推申请人：接诊、完成各发一条**通知型**消息（handleStatus=null，已读即闭环，
     * 不占待办、也不会被 closeConsultTodo 误关）。
     * <p>发给自己就跳过；发送失败只记日志 —— 会诊状态已经落库，一条消息不该把它回滚。
     */
    private void notifyApplicant(BizConsultation entity, String titlePrefix, String content) {
        Long applicantId = entity.getApplyDoctorId();
        CurrentUser operator = UserUtils.getCurrentUser();
        if (applicantId == null || (operator != null && Objects.equals(applicantId, operator.getEmployeeId()))) {
            return;
        }
        try {
            EmployeeBriefVO applicant = systemFacadeService.findEmployee(applicantId);
            if (applicant == null) {
                log.warn("[会诊] 找不到申请医生，进展通知跳过 consultationNo={} applyDoctorId={}",
                        entity.getConsultationNo(), applicantId);
                return;
            }
            String title = titlePrefix + "：" + entity.getConsultationNo();
            sysMessageService.sendSystemMessage(applicant.getId(),
                    applicant.getEmpName() == null ? "站内用户" : applicant.getEmpName(),
                    title, content, BizTypeEnum.CONSULT.getType(), entity.getConsultationId(),
                    "info", null, null);
        } catch (Exception ex) {
            log.warn("[会诊] 进展通知发送失败 consultationNo={} applicantId={}",
                    entity.getConsultationNo(), applicantId, ex);
        }
    }
}
