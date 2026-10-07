package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.*;
import com.his.patient.mapper.*;
import com.his.patient.service.InpatientConsultationService;
import com.his.patient.vo.ConsultationVO;
import com.his.patient.vo.WardVO;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysMessage;
import com.his.system.enums.BizTypeEnum;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 住院会诊服务实现（P4.1）。
 *
 * <p>除接口注释里那条状态机（申请 → 应答 → 完成 → 回写病历）之外，本类固化了这些
 * <b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>未应答不可完成</b>：与"医嘱未校对不可执行"同源。会诊方没接诊就出结论，
 *       等于替会诊科室编造了一次它没参与的会诊。</li>
 *   <li><b>取消只允许「待应答」</b>：一旦接诊，这条记录就必须走完成 ——
 *       取消它是在销毁"已经发生过的临床行为"。</li>
 *   <li><b>申请字段服务端推导</b>：申请科室取入院科室、申请医生取当前登录用户（员工ID）、
 *       患者取入院记录的患者。前端只给"给谁、请哪个科、为什么"。</li>
 *   <li><b>同一住院 + 同一会诊科室不允许并存两条未完成会诊</b>：这就是四核对里
 *       "重复"那一类（两次=重复）。要再请，先把上一条完成或取消。</li>
 *   <li><b>完成即回写病历</b>（record_type=9 会诊记录，直接"已提交"），并把病历ID回填
 *       记录ID：四核对里"病历有医嘱没记"这一侧必须能查到这次会诊。
 *       回写失败整笔事务回滚 —— 不允许出现"会诊说完成了、病历里查不到"。</li>
 *   <li><b>急会诊超时是查询时算的</b>：不落状态列、不起定时任务，
 *       与"危急值超时是查询时算的"同一口径。</li>
 *   <li><b>时间一律截到秒</b>：库表 DATETIME(0) 会四舍五入，不截就"写进去的 ≠ 读回来的"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientConsultationServiceImpl implements InpatientConsultationService {
    /**
     * 申请时未指定会诊医生：既有列 doctor_id 是 NOT NULL，用 0 表示"未指定"
     */
    private static final long DOCTOR_UNSPECIFIED = 0L;
    /**
     * 急会诊响应时限（分钟）。
     * <p>只作为**查询时判定**超时的依据，不落状态列 —— 与"危急值超时是查询时算的"同一口径。
     */
    private static final int URGENT_RESPONSE_MINUTES = 10;
    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SysMessageService sysMessageService;

    private final DictCacheService dictCacheService;

    private final BizConsultationMapper bizConsultationMapper;

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizPatientMapper bizPatientMapper;

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

    private final SysBedMapper sysBedMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    // 申请 / 修改
    private static Long minutesBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        return Duration.between(from, to).toMinutes();
    }

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
                : minutes <= 24 * 60L;
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(ConsultationUpsertDTO dto) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了
        if (dto == null) {
            throw new BusinessException("入院ID不能为空（会诊必须挂在一次住院上）");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Integer category = dto.getConsultCategory() == null
                ? ConsultCategoryEnum.NORMAL.getCode() : dto.getConsultCategory();

        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能再申请会诊");
        }
        Long fromDeptId = resolveAdmissionDeptId(admission);
        if (fromDeptId == null) {
            throw new BusinessException("入院记录缺少科室信息，无法确定申请科室");
        }

        // 范围与科室的一致性：科内必须同科室；科间/全院必须跨科室。
        boolean sameDept = Objects.equals(fromDeptId, dto.getToDeptId());
        if (Objects.equals(ConsultScopeEnum.IN_DEPT.getCode(), dto.getConsultType()) && !sameDept) {
            throw new BusinessException("「科内会诊」的会诊科室必须与申请科室一致（当前申请科室 ID=" + fromDeptId + "）");
        }
        if (!Objects.equals(ConsultScopeEnum.IN_DEPT.getCode(), dto.getConsultType()) && sameDept) {
            throw new BusinessException("「" + ConsultScopeEnum.labelOrUnknown(dto.getConsultType())
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
            throw new BusinessException("该住院已存在向「" + deptNameOf(dto.getToDeptId())
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
                ConsultScopeEnum.getText(dto.getConsultType()), isUrgent, entity.getApplyDoctorName());
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
            List<SysEmployee> receivers = new java.util.ArrayList<>();
            if (entity.getDoctorId() != null && entity.getDoctorId() != DOCTOR_UNSPECIFIED) {
                SysEmployee specified = sysEmployeeMapper.selectById(entity.getDoctorId());
                if (specified != null && Integer.valueOf(1).equals(specified.getStatus())) {
                    receivers.add(specified);
                }
            } else {
                receivers = sysEmployeeMapper.selectList(new LambdaQueryWrapper<SysEmployee>()
                        .eq(SysEmployee::getDeptId, entity.getToDeptId())
                        .eq(SysEmployee::getStatus, 1));
            }
            if (receivers.isEmpty()) {
                log.warn("[会诊] 未找到会诊方收件人，站内信跳过 consultationNo={} toDeptId={} doctorId={}",
                        entity.getConsultationNo(), entity.getToDeptId(), entity.getDoctorId());
                return;
            }
            BizPatient patient = bizPatientMapper.selectById(admission.getPatientId());
            String patientName = patient == null || patient.getPatientName() == null ? "未知" : patient.getPatientName();
            boolean urgent = Objects.equals(YesOrNoEnum.YES.getCode(), entity.getIsUrgent());
            String title = (urgent ? "急会诊邀请：" : "会诊邀请：") + patientName
                    + "（" + entity.getConsultationNo() + "）";
            String content = String.format(
                    "%s科 %s 医生为患者 %s 申请%s会诊（%s）：\n会诊理由：%s\n请尽快在会诊列表中「接诊」。",
                    deptNameOf(entity.getFromDeptId()),
                    entity.getApplyDoctorName() == null ? "未知" : entity.getApplyDoctorName(),
                    patientName,
                    urgent ? "急" : "",
                    ConsultScopeEnum.getText(entity.getConsultType()),
                    entity.getReason());
            String payload = cn.hutool.json.JSONUtil.toJsonStr(new java.util.LinkedHashMap<String, Object>() {{
                put("patientName", patientName);
                put("consultationNo", entity.getConsultationNo());
                put("toDeptName", deptNameOf(entity.getToDeptId()));
                put("reason", entity.getReason());
                put("isUrgent", urgent);
            }});
            for (SysEmployee receiver : receivers) {
                sysMessageService.sendSystemMessage(receiver.getId(),
                        receiver.getEmpName() == null ? "站内用户" : receiver.getEmpName(),
                        title, content,
                        BizTypeEnum.CONSULT.getType(), entity.getConsultationId(),
                        urgent ? "urgent" : "warning", payload, 0);
            }
        } catch (Exception ex) {
            log.warn("[会诊] 邀请通知发送失败 consultationNo={} toDeptId={}",
                    entity.getConsultationNo(), entity.getToDeptId(), ex);
        }
    }

    // 完成（回写病历）

    /**
     * 修改「待应答」的申请。
     *
     * <p>只允许改"请哪个科、什么范围、急不急、为什么"这四项 + 备注：
     * 患者、入院、申请科室、申请医生、申请时间都是**已经发生的事实**，改它们等于改病史。
     */
    private String updateOne(ConsultationUpsertDTO dto, BizAdmission admission, int isUrgent) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizConsultation entity = bizConsultationMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("会诊记录不存在");
        }
        if (!Objects.equals(entity.getAdmissionId(), admission.getAdmissionId())) {
            throw new BusinessException("该会诊不属于本次住院，不能修改");
        }
        if (!Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException("会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.labelOrUnknown(entity.getConsultStatus())
                    + "」，只有「待应答」的会诊申请可以修改；已应答的请直接完成会诊");
        }
        long unfinished = bizConsultationMapper.selectCount(new LambdaQueryWrapper<BizConsultation>()
                .eq(BizConsultation::getAdmissionId, admission.getAdmissionId())
                .eq(BizConsultation::getToDeptId, dto.getToDeptId())
                .in(BizConsultation::getConsultStatus, ConsultationStatusEnum.PENDING.getCode(), ConsultationStatusEnum.ACCEPTED.getCode())
                .ne(BizConsultation::getConsultationId, entity.getConsultationId()));
        if (unfinished > 0) {
            throw new BusinessException("该住院已存在向「" + deptNameOf(dto.getToDeptId())
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
                ConsultScopeEnum.getText(dto.getConsultType()), isUrgent, operatorUser.getRealName());
        return entity.getConsultationNo();
    }

    // 取消

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(ConsultationAcceptDTO dto) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了
        if (dto == null) {
            throw new BusinessException("会诊ID不能为空");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizConsultation entity = mustGet(dto.getConsultationId());
        if (!Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException("会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.labelOrUnknown(entity.getConsultStatus()) + "」，不能应答");
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
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        bizConsultationMapper.updateById(entity);

        // 消息侧待办联动：会诊已接诊 → 该会诊的站内信 handle_status 0→1。
        // 缺了这一步，会诊科室接诊了，收件箱里还挂着「待处理」（与危急值闭环同一手法）。
        closeConsultTodo(entity.getConsultationId(), 1);

        // 急会诊超时只提醒、不阻断：临床已经接诊了，再拒绝反而是把病人放下
        Long waitMinutes = minutesBetween(entity.getApplyTime(), now);
        boolean overdue = Objects.equals(YesOrNoEnum.YES.getCode(), entity.getIsUrgent()) && waitMinutes != null
                && waitMinutes > URGENT_RESPONSE_MINUTES;
        if (overdue) {
            log.warn("急会诊 {} 应答超时：申请时间={} 接诊时间={} 等待 {} 分钟（时限 {} 分钟），接诊医生={}",
                    entity.getConsultationNo(), entity.getApplyTime(), now, waitMinutes,
                    URGENT_RESPONSE_MINUTES, doctorName);
        }
        log.info("会诊应答 consultationNo={} 接诊医生={} 等待分钟={}", entity.getConsultationNo(), doctorName, waitMinutes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String finish(ConsultationFinishDTO dto) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了
        if (dto == null) {
            throw new BusinessException("会诊ID不能为空");
        }
        BizConsultation entity = mustGet(dto.getConsultationId());

        // ★ 铁律：未应答不可完成（= 医嘱未校对不可执行）
        if (Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException("会诊 " + entity.getConsultationNo()
                    + " 尚未被会诊科室应答，不能填写结论；请先由会诊科室「接诊」");
        }
        if (!Objects.equals(ConsultationStatusEnum.ACCEPTED.getCode(), entity.getConsultStatus())) {
            throw new BusinessException("会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.labelOrUnknown(entity.getConsultStatus()) + "」，不能重复完成");
        }

        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime consultTime = TimeUtil.toSeconds(dto.getConsultTime() != null ? dto.getConsultTime() : now);
        if (entity.getAcceptTime() != null && consultTime.isBefore(entity.getAcceptTime())) {
            throw new BusinessException("会诊时间不能早于接诊时间（接诊时间 " + entity.getAcceptTime() + "）");
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
        return recordId == null ? null : String.valueOf(recordId);
    }

    // 查询

    /**
     * 把会诊结论回写成一份住院病历文书（record_type=9 会诊记录，状态直接「已提交」）。
     *
     * <p>为什么不回写就"顺手 mark 一下"：四核对里"病历有医嘱没记"这一类缺陷，
     * 追的就是"做了却没写"。会诊做了、病历没有，等于把这次会诊变成飞检时的举证失败。
     */
    private BizInpatientRecord writeBackRecord(BizConsultation entity, String conclusion,
                                               LocalDateTime consultTime, LocalDateTime now) {
        BizAdmission admission = bizAdmissionMapper.selectById(entity.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在，无法回写会诊病历（admissionId=" + entity.getAdmissionId() + "）");
        }
        BizPatient patient = bizPatientMapper.selectById(admission.getPatientId());
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
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了
        if (dto == null) {
            throw new BusinessException("会诊ID不能为空");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizConsultation entity = mustGet(dto.getConsultationId());
        if (Objects.equals(ConsultationStatusEnum.CANCELLED.getCode(), entity.getConsultStatus())) {
            throw new BusinessException("会诊 " + entity.getConsultationNo() + " 已取消，不能重复取消");
        }
        if (!Objects.equals(ConsultationStatusEnum.PENDING.getCode(), entity.getConsultStatus())) {
            throw new BusinessException("会诊 " + entity.getConsultationNo() + " 当前状态为「"
                    + ConsultationStatusEnum.labelOrUnknown(entity.getConsultStatus())
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
            sysMessageService.update(new LambdaUpdateWrapper<SysMessage>()
                    .eq(SysMessage::getBizType, BizTypeEnum.CONSULT.getType())
                    .eq(SysMessage::getBizId, consultationId)
                    .eq(SysMessage::getHandleStatus, 0)
                    .set(SysMessage::getHandleStatus, targetStatus));
        } catch (Exception ex) {
            // 闭环联动失败不回滚业务：会诊状态是对的，只是收件箱滞后
            log.warn("[会诊] 待办消息闭环失败 consultationId={} targetStatus={}", consultationId, targetStatus, ex);
        }
    }

    // 展示态

    @Override
    public IPage<ConsultationVO> listPage(ConsultationQueryPageDTO query) {
        Page<ConsultationVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ConsultationVO> result = bizConsultationMapper.selectConsultationPage(page, query);
        result.getRecords().forEach(this::decorate);
        return result;
    }

    // 工具

    @Override
    public ConsultationVO getDetailById(Long consultationId) {
        // ②非web入口：service 方法参数判空，没有 DTO 字段可挂注解（HTTP 侧 @RequestParam 已必填）
        if (consultationId == null) {
            throw new BusinessException("会诊ID不能为空");
        }
        ConsultationVO vo = bizConsultationMapper.selectConsultationById(consultationId);
        if (vo == null) {
            throw new BusinessException("会诊记录不存在");
        }
        decorate(vo);
        return vo;
    }

    @Override
    public long countUnfinished(Long toDeptId, Long admissionId) {
        return bizConsultationMapper.countUnfinished(toDeptId, admissionId);
    }

    private void decorate(ConsultationVO vo) {
        vo.setConsultTypeText(ConsultScopeEnum.getText(vo.getConsultType()));
        vo.setConsultStatusText(ConsultationStatusEnum.getText(vo.getConsultStatus()));
        vo.setIsUrgentText(dictCacheService.getDicDataLabel("biz_patient_consultUrgentEnum", vo.getIsUrgent()));
        // 类别 null（存量行未填）→ 一律按"普通科间会诊"显示；非 null 脏数据 → 空串，由数据治理修复，不伪装
        vo.setConsultCategoryText(vo.getConsultCategory() == null
                ? ConsultCategoryEnum.NORMAL.getLabel() : ConsultCategoryEnum.getText(vo.getConsultCategory()));
        // 是否按时应答：营养会诊及时应答率的行级依据；未应答一律按超时计
        vo.setOnTime(onTime(vo.getIsUrgent(), vo.getApplyTime(), vo.getAcceptTime()));

        boolean pending = Objects.equals(ConsultationStatusEnum.PENDING.getCode(), vo.getConsultStatus());
        boolean accepted = Objects.equals(ConsultationStatusEnum.ACCEPTED.getCode(), vo.getConsultStatus());
        vo.setCanAccept(pending);
        vo.setCanEdit(pending);
        vo.setCanCancel(pending);
        vo.setCanFinish(accepted);

        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime from = vo.getApplyTime();
        LocalDateTime to = vo.getAcceptTime() != null ? vo.getAcceptTime() : now;
        vo.setResponseMinutes(from == null ? null : minutesBetween(from, to));

        // 急会诊超时**查询时算**：只看"还没应答的急会诊"
        boolean overdue = Objects.equals(YesOrNoEnum.YES.getCode(), vo.getIsUrgent()) && pending
                && vo.getApplyTime() != null
                && now.isAfter(vo.getApplyTime().plusMinutes(URGENT_RESPONSE_MINUTES));
        vo.setOverdue(overdue);
        if (overdue) {
            vo.setOverdueText("急会诊已等待 " + minutesBetween(vo.getApplyTime(), now) + " 分钟未应答（时限 "
                    + URGENT_RESPONSE_MINUTES + " 分钟）");
        }
    }

    private BizConsultation mustGet(Long consultationId) {
        BizConsultation entity = bizConsultationMapper.selectById(consultationId);
        if (entity == null) {
            throw new BusinessException("会诊记录不存在");
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
        return StringUtils.hasText(name) ? name : "未知科室(ID=" + deptId + ")";
    }

    private String nextConsultationNo() {
        String prefix = "HZ" + LocalDate.now().format(NO_DATE);
        long seq = bizConsultationMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private String nextRecordNo() {
        String prefix = "BL" + LocalDate.now().format(NO_DATE);
        long seq = bizInpatientRecordMapper.countByRecordNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径
     */
}