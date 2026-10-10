package com.his.patient.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.api.InpatientSettlementGateway;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.BedStatusEnum;
import com.his.patient.enums.DischargeWayEnum;
import com.his.patient.enums.SummaryStatusEnum;
import com.his.patient.enums.VisitStatusEnum;
import com.his.patient.mapper.*;
import com.his.patient.service.AdmissionOrderService;
import com.his.patient.service.BedCenterService;
import com.his.patient.service.DeathCertificateService;
import com.his.patient.service.InpatientService;
import com.his.patient.support.SettlementGate;
import com.his.patient.support.SummaryOperationSeq;
import com.his.patient.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.enums.BizTypeEnum;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 住院管理实现（第 1 期）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientServiceImpl extends ServiceImpl<BizInpatientSummaryMapper, BizInpatientSummary> implements InpatientService {

    private final RedisSequenceService redisSequenceService;
    private final DeptScopeService deptScopeService;

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizDischargeMapper bizDischargeMapper;

    private final SysBedMapper sysBedMapper;

    private final BedMapMapper bedMapMapper;

    private final BizPatientMapper bizPatientMapper;

    private final BizInpatientSummaryMapper bizInpatientSummaryMapper;

    private final BizInpatientDiagnosisMapper bizInpatientDiagnosisMapper;

    private final BizInpatientOperationMapper bizInpatientOperationMapper;

    private final BizVisitMapper bizVisitMapper;

    private final AdmissionOrderService admissionOrderService;

    private final SettlementGate settlementGate;

    private final SysMessageService sysMessageService;

    private final DeathCertificateService deathCertificateService;

    private final ObjectProvider<BedCenterService> bedCenterProvider;

    private final DictCacheService dictCacheService;

    /**
     * 时间精度统一到「秒」。
     *
     * <p>库表的时间列是 {@code DATETIME(0)}，MySQL 存进去时会<b>四舍五入</b>（不是截断）：
     * 写 {@code 21:52:44.824}，读出来是 {@code 21:52:45}。后果有两个，都是真踩过的：
     * <ol>
     *   <li>「出院时间不能早于入院时间」会误报——内存里 now() 还停在 44.8 秒，库里的入院时间却已经是 45 秒；</li>
     *   <li>住院天数在跨日边界上会多算一天（23:59:59.7 的入院被存成次日 00:00:00）。</li>
     * </ol>
     * 所以凡是会落库、又会被拿来比较的时间，一律先截到秒，保证「写进去的 = 读回来的」。
     */
    // 查询

    /**
     * 把系统侧的校验结论并进备注（原有备注在前，系统留痕在后，用「 | 」分隔）
     */
    private static String mergeRemark(String original, String note) {
        if (!TextUtil.hasText(note)) {
            return original;
        }
        if (!TextUtil.hasText(original)) {
            return note;
        }
        return original + " | " + note;
    }

    @Override
    public IPage<InpatientVO> listPage(InpatientQueryPageDTO query) {
        // 科室数据权限收口（M6）：scopeDeptIds 是服务端专用字段，先清掉前端可能伪造的值再按登录态填充。
        // 传了 deptId → 越权直接拒绝；没传且受限 → 收敛到授权科室集合（不再是"不传=看全院"）。
        query.setScopeDeptIds(null);
        Long scopedDeptId = deptScopeService.resolveDeptId(query.getDeptId());
        if (scopedDeptId != null) {
            query.setDeptId(scopedDeptId);
        } else if (deptScopeService.isScoped()) {
            query.setScopeDeptIds(List.copyOf(deptScopeService.allowedDeptIds()));
        }
        Page<InpatientVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<InpatientVO> result = bizAdmissionMapper.selectInpatientPage(page, query);

        LocalDateTime now = LocalDateTime.now();
        for (InpatientVO vo : result.getRecords()) {
            if (Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), vo.getAdmitStatus())) {
                // 在院：天数按「至今」实时算，不能用首页里那个出院时才算出来的值
                vo.setInpatientDays(calcInpatientDays(vo.getAdmitTime(), now));
            } else if (vo.getInpatientDays() == null) {
                vo.setInpatientDays(calcInpatientDays(vo.getAdmitTime(), vo.getDischargeTime()));
            }
        }
        return result;
    }

    @Override
    public IPage<InpatientVO> listMyDeptPage(InpatientQueryPageDTO query) {
        CurrentUser user = UserUtils.getCurrentUser();
        Long deptId = user == null ? null : user.getDeptId();
        if (deptId == null) {
            // fail-closed：没有科室就无法划定"本科室"，宁可报错也不能退化成全院列表
            throw new BusinessException("当前账号未绑定科室，无法查看本科室在院患者，请在系统管理中维护所属科室");
        }
        query.setDeptId(deptId);
        query.setWardId(null);
        return listPage(query);
    }

    // 入院登记

    @Override
    public InpatientDetailVO detail(Long admissionId) {
        InpatientDetailVO.AdmissionInfo info = bizAdmissionMapper.selectAdmissionInfo(admissionId);
        if (info == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), info.getAdmitStatus())) {
            info.setInpatientDays(calcInpatientDays(info.getAdmitTime(), LocalDateTime.now()));
        }

        InpatientDetailVO vo = new InpatientDetailVO();
        vo.setAdmission(info);

        BizInpatientSummary summary = bizInpatientSummaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, admissionId));
        if (summary != null) {
            InpatientDetailVO.SummaryInfo si = new InpatientDetailVO.SummaryInfo();
            BeanUtils.copyProperties(summary, si);
            vo.setSummary(si);
            vo.setSummaryStatusText(SummaryStatusEnum.getText(summary.getSummaryStatus()));
            if (info.getInpatientDays() == null) {
                info.setInpatientDays(summary.getInpatientDays());
            }
        } else {
            // 未生成首页不是「正常」，要如实说
            vo.setSummaryStatusText(SummaryStatusEnum.getText(null));
        }

        List<InpatientDetailVO.DiagnosisInfo> diagnoses = new ArrayList<>();
        for (BizInpatientDiagnosis d : bizInpatientDiagnosisMapper.selectList(new LambdaQueryWrapper<BizInpatientDiagnosis>()
                .eq(BizInpatientDiagnosis::getAdmissionId, admissionId)
                .orderByAsc(BizInpatientDiagnosis::getDiagType)
                .orderByAsc(BizInpatientDiagnosis::getSeqNo))) {
            InpatientDetailVO.DiagnosisInfo di = new InpatientDetailVO.DiagnosisInfo();
            BeanUtils.copyProperties(d, di);
            diagnoses.add(di);
        }
        vo.setDiagnoses(diagnoses);

        List<InpatientDetailVO.OperationInfo> operations = new ArrayList<>();
        for (BizInpatientOperation o : bizInpatientOperationMapper.selectList(new LambdaQueryWrapper<BizInpatientOperation>()
                .eq(BizInpatientOperation::getAdmissionId, admissionId)
                .orderByAsc(BizInpatientOperation::getSeqNo))) {
            InpatientDetailVO.OperationInfo oi = new InpatientDetailVO.OperationInfo();
            BeanUtils.copyProperties(o, oi);
            operations.add(oi);
        }
        vo.setOperations(operations);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long admit(InpatientAdmitDTO dto) {
        // C-非 web 入参：除 InpatientController 外还被 BedCenterServiceImpl:592、his-appoint BizEmergencyServiceImpl:495 以内部构造的 InpatientAdmitDTO 直调，Bean Validation 不覆盖，保留
        if (dto.getPatientId() == null) {
            throw new BusinessException("患者不能为空");
        }
        // C-非 web 入参：同上（病区）——非 web 直调路径无 @Valid，保留
        if (dto.getWardId() == null) {
            throw new BusinessException("病区不能为空");
        }
        // C-非 web 入参：同上（床位）——非 web 直调路径无 @Valid，保留
        if (dto.getBedId() == null) {
            throw new BusinessException("床位不能为空");
        }
        // C-非 web 入参：同上（入院医生）——非 web 直调路径无 @Valid，保留
        if (dto.getAdmitDoctorId() == null) {
            throw new BusinessException("入院医生不能为空");
        }

        // 住院证（门诊转住院）
        // 有证 = 门诊转住院：入院途径由证的性质决定（必为门诊），不允许入院处手填；
        // 无证 = 直接入院（急诊 / 转院 / 其他），入院途径必须由调用方显式给出。
        BizAdmissionOrder order = null;
        if (dto.getAdmissionOrderId() != null) {
            order = admissionOrderService.requireAdmittable(dto.getAdmissionOrderId());
            if (!Objects.equals(order.getPatientId(), dto.getPatientId())) {
                throw new BusinessException("住院证与所选患者不一致（证上患者：" + order.getPatientName() + "）");
            }
            if (dto.getAdmitWay() != null && !Objects.equals(1, dto.getAdmitWay())) {
                log.warn("有住院证（门诊转住院）时忽略传入的入院途径 {}，强制为「门诊」", dto.getAdmitWay());
            }
        } else {
            // B-条件必填：仅无住院证（直接入院）分支才要求入院途径；有证时途径由性质决定，DTO 上的 @NotNull 会把合法的有证收治挡成 400，保留
            if (dto.getAdmitWay() == null) {
                throw new BusinessException("入院途径不能为空（病案首页必填项）");
            }
        }
        int admitWay = order != null ? 1 : dto.getAdmitWay();

        BizPatient patient = bizPatientMapper.selectById(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        if (bizAdmissionMapper.countInHospitalByPatient(dto.getPatientId()) > 0) {
            throw new BusinessException("该患者已有在院记录，不能重复办理入院");
        }

        SysBed bed = sysBedMapper.selectById(dto.getBedId());
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }
        if (!Objects.equals(bed.getWardId(), dto.getWardId())) {
            throw new BusinessException("所选床位不属于该病区");
        }
        // 床位可用性 = 「空闲」或「床位中心给本患者预留的锁定床」。
        // 第二个分支是床位服务中心能成立的前提：跨科调配会先把床锁给患者（3-锁定 + 挂 patient_id），
        // 这里若只认空闲，所有从床位中心走的收治都会被自己的预留挡在门外。
        // 锁定床必须挂着同一个患者才放行 —— 给别人预留的床依然进不来。
        boolean reservedForSelf = Objects.equals(BedStatusEnum.LOCKED.getCode(), bed.getBedStatus())
                && Objects.equals(dto.getPatientId(), bed.getPatientId());
        if (!Objects.equals(BedStatusEnum.FREE.getCode(), bed.getBedStatus()) && !reservedForSelf) {
            throw new BusinessException("床位当前不可用（" + BedStatusEnum.labelOrUnknown(bed.getBedStatus()) + "），请选择空闲床位");
        }

        WardVO ward = sysBedMapper.selectWardById(dto.getWardId());
        if (ward == null) {
            throw new BusinessException("病区不存在");
        }
        Long deptId = dto.getDeptId() != null ? dto.getDeptId() : ward.getDeptId();
        if (deptId == null) {
            throw new BusinessException("病区未绑定科室，无法确定入院科室");
        }
        if (!Objects.equals(deptId, bed.getDeptId())) {
            throw new BusinessException("入院科室与床位所属科室不一致");
        }

        LocalDateTime admitTime = TimeUtil.toSeconds(dto.getAdmitTime() != null ? dto.getAdmitTime() : LocalDateTime.now());

        // 门诊线索：有证以证为准（证是那次门诊留下的凭据）
        Long registId = order != null ? order.getRegistId() : dto.getRegistId();
        String registNo = order != null ? order.getRegistNo() : dto.getRegistNo();

        BizAdmission admission = new BizAdmission();
        admission.setAdmissionNo(nextAdmissionNo());
        admission.setPatientId(dto.getPatientId());
        // 就诊次：第 1 期漏写这个字段，导致 visit_id 长期是悬空引用。本期补齐。
        admission.setVisitId(ensureVisit(dto.getPatientId(), registId));
        admission.setRegistId(registId);
        admission.setRegistNo(registNo);
        admission.setAdmissionOrderId(order != null ? order.getId() : null);
        admission.setDeptId(deptId);
        admission.setWardId(dto.getWardId());
        admission.setBedId(dto.getBedId());
        admission.setAdmitDoctorId(dto.getAdmitDoctorId());
        admission.setAdmitTime(admitTime);
        admission.setAdmitWay(admitWay);
        admission.setDiagnosis(dto.getDiagnosis());
        admission.setAdmitDiagnosisCode(dto.getAdmitDiagnosisCode());
        admission.setAdmitDiagnosisName(dto.getAdmitDiagnosisName());
        admission.setAdmitStatus(AdmitStatusEnum.IN_HOSPITAL.getCode());
        admission.setRemark(dto.getRemark());
        bizAdmissionMapper.insert(admission);

        occupyBed(bed, dto.getPatientId(), dto.getWardId());
        createSummaryDraft(admission, patient, ward, bed);

        // 收治回填必须放在最后：入院记录真的落库了，才把证置为已收治。
        // 顺序反了会出现"证说已收治、但入院记录不存在"的孤儿状态。
        if (order != null) {
            admissionOrderService.markAdmitted(order, admission.getAdmissionId(), deptId, admitTime);
            // 收治完成 → 站内信通知开证医生（admit，通知型）：医生开完证患者在办理，
            // 收进哪个病区哪张床医生必须知道。通知失败不回滚收治（try/catch，事务边界在业务动作）。
            notifyAdmitted(order, patient, ward, bed, admission.getAdmissionNo());
        }

        // 收治 → 等床队列收口（同一事务）。不做这一步，从入出院管理页直接收治的患者
        // 会在床位中心留下一条"还在等床"的幽灵记录，而他的床位已经是占用状态了。
        try {
            BedCenterService bedCenter = bedCenterProvider.getIfAvailable();
            if (bedCenter != null) {
                bedCenter.markAdmittedByPatient(dto.getPatientId(), admission.getAdmissionId(), admitTime);
            }
        } catch (Exception e) {
            // 队列记账不是入院的条件：人已经住进来了，不能因为记账失败把整笔入院回滚掉
            log.warn("[入院登记] 回填床位排队队列失败 admissionNo={} patientId={}",
                    admission.getAdmissionNo(), dto.getPatientId(), e);
        }

        log.info("入院登记成功 admissionNo={} admissionId={} patientId={} bedId={} 住院证={} 挂号={} 就诊次={}",
                admission.getAdmissionNo(), admission.getAdmissionId(), dto.getPatientId(), dto.getBedId(),
                order == null ? "-" : order.getOrderNo(), registNo == null ? "-" : registNo, admission.getVisitId());
        return admission.getAdmissionId();
    }

    // 换床

    /**
     * 收治完成 → 站内信通知开证医生（bizType=admit，通知型：read_status 即闭环）。
     * <p>
     * 收件人 = 住院证 sourceDoctorId（开证医生本人，不是病区全员——病区护士站已有
     * 「本科室在院患者」一手列表，全员 fan-out 只会制造噪音）。通知失败只留 warn 日志，
     * 不回滚收治事务。
     */
    private void notifyAdmitted(BizAdmissionOrder order, BizPatient patient, WardVO ward, SysBed bed, String admissionNo) {
        if (order.getSourceDoctorId() == null) {
            return;
        }
        try {
            String title = "患者已收治入院：" + patient.getPatientName();
            String content = String.format(
                    "您开具住院证（%s）的患者 %s 已办理入院，入住 %s%s床，住院号 %s。",
                    order.getOrderNo(),
                    patient.getPatientName(),
                    ward.getWardName() == null ? "" : ward.getWardName(),
                    bed.getBedNo() == null ? String.valueOf(bed.getBedId()) : bed.getBedNo(),
                    admissionNo);
            InpatientAdmitNotifyPayloadVO payload = new InpatientAdmitNotifyPayloadVO();
            payload.setPatientName(patient.getPatientName());
            payload.setOrderNo(order.getOrderNo());
            payload.setWardName(ward.getWardName());
            payload.setBedNo(bed.getBedNo());
            payload.setAdmissionNo(admissionNo);
            String payloadJson = JSONUtil.toJsonStr(payload);
            sysMessageService.sendSystemMessage(order.getSourceDoctorId(), order.getSourceDoctorName(),
                    title, content, BizTypeEnum.ADMIT.getType(), order.getId(), "info", payloadJson, null);
        } catch (Exception ex) {
            log.warn("[入院登记] 收治通知发送失败 admissionNo={} sourceDoctorId={}",
                    admissionNo, order.getSourceDoctorId(), ex);
        }
    }

    // 出院办理

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(InpatientTransferDTO dto) {
        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能换床");
        }
        if (Objects.equals(admission.getBedId(), dto.getNewBedId())) {
            throw new BusinessException("目标床位与原床位相同");
        }
        SysBed newBed = sysBedMapper.selectById(dto.getNewBedId());
        if (newBed == null) {
            throw new BusinessException("目标床位不存在");
        }
        if (!Objects.equals(BedStatusEnum.FREE.getCode(), newBed.getBedStatus())) {
            throw new BusinessException("目标床位当前不可用（" + BedStatusEnum.labelOrUnknown(newBed.getBedStatus()) + "）");
        }
        if (!Objects.equals(newBed.getDeptId(), admission.getDeptId())) {
            throw new BusinessException("只能在本病区内换床；跨科室请走转科流程");
        }

        Long oldBedId = admission.getBedId();
        Long oldWardId = admission.getWardId();

        releaseBed(oldBedId);
        occupyBed(newBed, admission.getPatientId(), newBed.getWardId());

        admission.setBedId(newBed.getBedId());
        admission.setWardId(newBed.getWardId());
        if (TextUtil.hasText(dto.getReason())) {
            admission.setRemark(dto.getReason());
        }
        bizAdmissionMapper.updateById(admission);

        if (!Objects.equals(oldWardId, newBed.getWardId())) {
            sysBedMapper.syncWardOccupied(oldWardId);
        }
        log.info("换床成功 admissionId={} bedId {} -> {}", dto.getAdmissionId(), oldBedId, dto.getNewBedId());
    }

    // 病案首页

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discharge(InpatientDischargeDTO dto) {
        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已办理出院，不能重复办理");
        }
        if (bizDischargeMapper.countByAdmission(dto.getAdmissionId()) > 0) {
            throw new BusinessException("该入院已存在出院记录");
        }

        LocalDateTime dischargeTime = TimeUtil.toSeconds(dto.getDischargeTime() != null ? dto.getDischargeTime() : LocalDateTime.now());
        // 两边都截到秒再比：库里存的是秒，拿毫秒级的 now() 去比会误报「早于入院时间」
        if (admission.getAdmitTime() != null
                && dischargeTime.isBefore(TimeUtil.toSeconds(admission.getAdmitTime()))) {
            throw new BusinessException("出院时间不能早于入院时间");
        }

        int deathFlag = Objects.equals(1, dto.getDeathFlag()) ? 1 : 0;
        if (deathFlag == 1 && !Objects.equals(DischargeWayEnum.DEATH.getCode(), dto.getDischargeWay())) {
            throw new BusinessException("死亡病例的离院方式必须为「死亡」");
        }
        if (deathFlag == 0 && Objects.equals(DischargeWayEnum.DEATH.getCode(), dto.getDischargeWay())) {
            throw new BusinessException("离院方式为「死亡」时，死亡标志必须为「是」");
        }
        // 死亡证明若已填（家属来办证常常在出院前后），出院时间必须与证明的死亡时间是同一时点
        if (deathFlag == 1) {
            deathCertificateService.assertDischargeConsistent(dto.getAdmissionId(), dischargeTime);
        }

        // 出院前必须已结算：一次住院没有结算单就出院，财务侧永远追不到这笔账。
        // 欠费结算也算已结算 —— 拦的是"完全没结算"，不是"没交钱"。
        String settleNote = null;
        if (settlementGate.available()) {
            InpatientSettlementGateway.SettlementState state = settlementGate.state(dto.getAdmissionId());
            if (state == null) {
                throw new BusinessException("未能确认该住院的结算状态，暂不允许出院（请稍后重试或联系收费处）");
            }
            if (!state.isSettled()) {
                throw new BusinessException("该住院尚未办理住院结算，请先结算（欠费可办理欠费结算）再出院");
            }
            settleNote = "出院结算校验：" + state.getText();
        } else {
            // 收费模块缺席：放行但必须留痕，绝不能假装校验过
            settleNote = "出院时未校验住院结算：收费模块未接入（InpatientSettlementGateway 无实现）";
            log.warn(settleNote + "，admissionId={}", dto.getAdmissionId());
        }

        // 再入院判定必须在把本人置为出院之前做（查询里已排除本人，顺序不影响，但先算更直观）
        boolean readmit31d = bizAdmissionMapper.countReadmitWithin31d(
                admission.getPatientId(), admission.getAdmissionId(), dischargeTime) > 0;

        int days = calcInpatientDays(admission.getAdmitTime(), dischargeTime);

        BizDischarge discharge = new BizDischarge();
        discharge.setDischargeNo(nextDischargeNo());
        discharge.setAdmissionId(admission.getAdmissionId());
        discharge.setPatientId(admission.getPatientId());
        discharge.setDischargeTime(dischargeTime);
        discharge.setDischargeDoctorId(dto.getDischargeDoctorId());
        discharge.setDischargeDiagnosis(dto.getDischargeDiagnosis());
        discharge.setDischargeDiagnosisCode(dto.getDischargeDiagnosisCode());
        discharge.setDischargeWay(dto.getDischargeWay());
        discharge.setDeathFlag(deathFlag);
        discharge.setDischargeSummary(dto.getDischargeSummary());
        discharge.setDischargeStatus(1);
        discharge.setRemark(mergeRemark(dto.getRemark(), settleNote));
        bizDischargeMapper.insert(discharge);

        admission.setAdmitStatus(AdmitStatusEnum.DISCHARGED.getCode());
        admission.setDischargeTime(dischargeTime);
        bizAdmissionMapper.updateById(admission);

        releaseBed(admission.getBedId());

        BizInpatientSummary summary = getOrCreateSummary(admission);
        summary.setDischargeTime(dischargeTime);
        summary.setInpatientDays(days);
        summary.setDischargeWay(dto.getDischargeWay());
        summary.setDeathFlag(deathFlag);
        summary.setReadmit31d(readmit31d ? 1 : 0);
        if (summary.getSummaryStatus() == null || summary.getSummaryStatus() == SummaryStatusEnum.DRAFT.getCode()) {
            summary.setSummaryStatus(SummaryStatusEnum.SUBMITTED.getCode());
        }
        bizInpatientSummaryMapper.updateById(summary);

        log.info("出院办理成功 admissionId={} dischargeNo={} days={} way={} readmit31d={}",
                admission.getAdmissionId(), discharge.getDischargeNo(), days, dto.getDischargeWay(), readmit31d);
    }

    // 统计 / 床位

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InpatientDetailVO saveSummary(InpatientSummaryUpsertDTO dto) {
        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        BizInpatientSummary summary = getOrCreateSummary(admission);
        if (Objects.equals(SummaryStatusEnum.ARCHIVED.getCode(), summary.getSummaryStatus())) {
            throw new BusinessException("病案首页已归档，不允许修改");
        }

        List<InpatientSummaryUpsertDTO.DiagnosisItem> diagnoses = dto.getDiagnoses();
        if (diagnoses != null) {
            if (diagnoses.isEmpty()) {
                // 允许清空（比如录错整份重来），但要显式提示这是有意为之
                log.info("病案首页诊断明细被清空 admissionId={}", dto.getAdmissionId());
            } else {
                long mainCount = diagnoses.stream().filter(d -> Objects.equals(1, d.getDiagType())).count();
                if (mainCount != 1) {
                    throw new BusinessException("主要诊断必须且只能有 1 条（当前 " + mainCount + " 条）");
                }
            }
        }

        List<InpatientSummaryUpsertDTO.OperationItem> operations = dto.getOperations();
        if (operations != null) {
            if (!operations.isEmpty()) {
                long mainOpCount = operations.stream().filter(o -> Objects.equals(1, o.getIsMain())).count();
                if (mainOpCount > 1) {
                    throw new BusinessException("主要手术最多只能有 1 条（当前 " + mainOpCount + " 条）");
                }
            }
            summary.setIsSurgery(operations.isEmpty() ? 0 : 1);
        }

        // 只更新非空字段（MyBatis-Plus 默认 NOT_NULL 策略），清空字段请走整表替换的明细
        copySummaryFields(dto, summary);

        if (diagnoses != null && !diagnoses.isEmpty()) {
            InpatientSummaryUpsertDTO.DiagnosisItem main = diagnoses.stream()
                    .filter(d -> Objects.equals(1, d.getDiagType())).findFirst().orElse(null);
            if (main != null) {
                summary.setMainDiagnosisCode(main.getIcdCode());
                summary.setMainDiagnosisName(main.getIcdName());
            }
        }
        bizInpatientSummaryMapper.updateById(summary);

        if (diagnoses != null) {
            bizInpatientDiagnosisMapper.delete(new LambdaQueryWrapper<BizInpatientDiagnosis>()
                    .eq(BizInpatientDiagnosis::getAdmissionId, dto.getAdmissionId()));
            List<InpatientSummaryUpsertDTO.DiagnosisItem> sorted = new ArrayList<>(diagnoses);
            // 主要诊断恒为第 1 条
            sorted.sort(Comparator.comparing(d -> Objects.equals(1, d.getDiagType()) ? 0 : 1));
            int seq = 1;
            for (InpatientSummaryUpsertDTO.DiagnosisItem item : sorted) {
                BizInpatientDiagnosis entity = new BizInpatientDiagnosis();
                entity.setAdmissionId(dto.getAdmissionId());
                entity.setSeqNo(seq++);
                entity.setDiagType(item.getDiagType());
                entity.setIcdCode(item.getIcdCode());
                entity.setIcdName(item.getIcdName());
                entity.setAdmitCondition(item.getAdmitCondition());
                entity.setCcLevel(item.getCcLevel());
                entity.setDiagnosisBasis(item.getDiagnosisBasis());
                bizInpatientDiagnosisMapper.insert(entity);
            }
        }

        if (operations != null) {
            // 保护手术闭环回写的行（P4.3）
            // 病案首页手术明细现在有两个写入方：手术闭环完成时回写 + 首页表单。
            // 表单整表 delete+insert 会把闭环回写的行一起删掉，表现就是
            // "手术做了、状态也已完成，但首页查不到这条手术明细" —— 四核对当场失败。
            // 区分靠 apply_id：非空 = 闭环回写（表单不得删除/覆盖），为空 = 表单手工行。
            // 刻意不靠 remark 文本前缀判断 —— 文本会被改、会被人抄，列不会。
            List<BizInpatientOperation> systemRows = bizInpatientOperationMapper.selectList(
                    new LambdaQueryWrapper<BizInpatientOperation>()
                            .eq(BizInpatientOperation::getAdmissionId, dto.getAdmissionId())
                            .isNotNull(BizInpatientOperation::getApplyId));

            bizInpatientOperationMapper.delete(new LambdaQueryWrapper<BizInpatientOperation>()
                    .eq(BizInpatientOperation::getAdmissionId, dto.getAdmissionId())
                    .isNull(BizInpatientOperation::getApplyId));

            if (!systemRows.isEmpty()) {
                Set<String> systemNames = systemRows.stream()
                        .map(BizInpatientOperation::getOperationName)
                        .filter(TextUtil::hasText)
                        .map(String::trim)
                        .collect(Collectors.toSet());
                for (InpatientSummaryUpsertDTO.OperationItem o : operations) {
                    if (TextUtil.hasText(o.getOperationName())
                            && systemNames.contains(o.getOperationName().trim())) {
                        throw new BusinessException("手术「" + o.getOperationName().trim()
                                + "」已由手术闭环回写（见 biz_inpatient_operation.apply_id），"
                                + "首页不能再手工录一条同名手术，否则首页会出现两条重复明细；"
                                + "要改这台手术的信息，请回到「手术排期」里改手术单");
                    }
                }
                boolean manualHasMain = operations.stream()
                        .anyMatch(o -> Objects.equals(1, o.getIsMain()));
                if (manualHasMain && systemRows.stream().anyMatch(r -> Objects.equals(1, r.getIsMain()))) {
                    throw new BusinessException("该住院已有手术闭环回写的「主要手术」，"
                            + "首页不能再手工录入一条主要手术（首页主要手术只能有 1 条）");
                }
            }

            List<InpatientSummaryUpsertDTO.OperationItem> sorted = new ArrayList<>(operations);
            sorted.sort(Comparator.comparing(o -> Objects.equals(1, o.getIsMain()) ? 0 : 1));
            // 序号从"闭环回写行的条数"之后开始：前面那些行的序号归它们，不能被手工行覆盖
            int seq = systemRows.size() + 1;
            for (InpatientSummaryUpsertDTO.OperationItem item : sorted) {
                BizInpatientOperation entity = new BizInpatientOperation();
                entity.setAdmissionId(dto.getAdmissionId());
                entity.setSeqNo(seq++);
                entity.setIsMain(item.getIsMain());
                entity.setOperationCode(item.getOperationCode());
                entity.setOperationName(item.getOperationName());
                entity.setOperationDate(item.getOperationDate());
                entity.setOperationLevel(item.getOperationLevel());
                entity.setIncisionLevel(item.getIncisionLevel());
                entity.setAnesthesiaType(item.getAnesthesiaType());
                entity.setSurgeonId(item.getSurgeonId());
                entity.setSurgeonName(item.getSurgeonName());
                entity.setAssistantName(item.getAssistantName());
                entity.setOperationBasis(item.getOperationBasis());
                // 手工行：apply_id 必须保持 null，它是"可被表单删除"的唯一标记
                entity.setApplyId(null);
                bizInpatientOperationMapper.insert(entity);
            }
            // 两个写入方共用同一套序号规则（主要手术恒为第 1 条），必须一处实现
            SummaryOperationSeq.reseq(bizInpatientOperationMapper, dto.getAdmissionId());
        }

        log.info("病案首页保存成功 admissionId={} 诊断={} 手术={}",
                dto.getAdmissionId(), diagnoses == null ? "-" : diagnoses.size(),
                operations == null ? "-" : operations.size());
        return detail(dto.getAdmissionId());
    }

    @Override
    public InpatientStatsVO stats() {
        InpatientStatsVO vo = new InpatientStatsVO();
        // 与 listPage 同一收口口径（M6）：受限角色看到的"在院/今日入出院"必须和列表条数对得上，
        // 否则出现「列表 3 条、卡片全院总数」。床位是全院物理资源，不随科室收口。
        List<Long> scopeDeptIds = deptScopeService.isScoped()
                ? List.copyOf(deptScopeService.allowedDeptIds()) : null;
        LambdaQueryWrapper<BizAdmission> inHospitalWrapper = new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getAdmitStatus, AdmitStatusEnum.IN_HOSPITAL.getCode());
        if (scopeDeptIds != null) {
            inHospitalWrapper.in(BizAdmission::getDeptId, scopeDeptIds);
        }
        vo.setInHospitalCount(bizAdmissionMapper.selectCount(inHospitalWrapper));
        vo.setTodayAdmitted(bizAdmissionMapper.countTodayAdmitted(scopeDeptIds));
        vo.setTodayDischarged(bizAdmissionMapper.countTodayDischarged(scopeDeptIds));
        vo.setPendingAdmissionOrderCount(admissionOrderService.countPending());

        long total = sysBedMapper.selectCount(null);
        long free = sysBedMapper.countByStatus(BedStatusEnum.FREE.getCode());
        long occupied = sysBedMapper.countByStatus(BedStatusEnum.OCCUPIED.getCode());
        vo.setTotalBeds(total);
        vo.setFreeBeds(free);
        vo.setOccupiedBeds(occupied);
        vo.setBedUsageRate(percent(occupied, total));
        return vo;
    }

    @Override
    public List<WardVO> listWards() {
        List<WardVO> wards = sysBedMapper.selectWardList();
        for (WardVO w : wards) {
            int total = w.getTotalBeds() == null ? 0 : w.getTotalBeds();
            int occupied = w.getOccupiedBeds() == null ? 0 : w.getOccupiedBeds();
            w.setUsageRate(percent(occupied, total));
        }
        return wards;
    }

    @Override
    public List<BedVO> listBeds(Long wardId, Long deptId, Integer bedStatus) {
        List<BedVO> beds = sysBedMapper.selectBedList(wardId, deptId, bedStatus);
        for (BedVO b : beds) {
            b.setBedStatusText(BedStatusEnum.getText(b.getBedStatus()));
        }
        return beds;
    }

    @Override
    public BedMapVO bedMap(BedMapQueryDTO query) {
        BedMapVO result = new BedMapVO();

        // 1) 科室收口：显式传的 deptId 先过授权校验（越权直接拒，不静默改写）
        Long deptId = deptScopeService.resolveDeptId(query.getDeptId());
        if (deptId == null) {
            // 不受限（data_scope=1）也没指定科室 → 落主岗位科室。
            // 不退化成"全院 1000+ 张卡"：那样既画不开，也不是任何人关心的视图。
            CurrentUser user = UserUtils.getCurrentUser();
            deptId = user == null ? null : user.getDeptId();
        }
        if (deptId == null) {
            throw new BusinessException("当前账号未绑定科室，请在系统管理中维护所属科室或直接指定科室后查看床位图");
        }
        result.setDeptId(deptId);

        // 2) 病区收口：病区必须属于已定科室，否则视为越界（跨科窥探）
        Long wardId = query.getWardId();
        if (wardId != null) {
            BedMapVO.WardOption owner = bedMapMapper.selectWardOwner(wardId);
            if (owner == null) {
                throw new BusinessException("病区不存在");
            }
            if (!deptId.equals(owner.getDeptId())) {
                throw new BusinessException("该病区不属于当前科室，无法查看");
            }
            result.setWardId(wardId);
            result.setWardName(owner.getWardName());
        }

        List<BedMapVO.BedCard> beds = bedMapMapper.selectBedCards(deptId, wardId);
        for (BedMapVO.BedCard bed : beds) {
            bed.setBedStatusText(BedStatusEnum.getText(bed.getBedStatus()));
            bed.setNursingLevelText(bed.getNursingLevel() == null
                    ? "未评估" : dictCacheService.getDicDataLabel(DictTypeConst.NURSING_LEVEL, bed.getNursingLevel()));
        }
        result.setBeds(beds);
        if (!beds.isEmpty()) {
            result.setDeptName(beds.get(0).getDeptName());
        }
        result.setSummary(summarize(beds));
        result.setDeptOptions(resolveDeptOptions(deptId, result.getDeptName()));
        result.setWardOptions(bedMapMapper.selectWardOptions(deptId));
        return result;
    }

    /**
     * 顶部统计：全部由本次返回的床位现算，保证"卡片数 = 统计数"自洽
     */
    private BedMapVO.Summary summarize(List<BedMapVO.BedCard> beds) {
        BedMapVO.Summary s = new BedMapVO.Summary();
        for (BedMapVO.BedCard bed : beds) {
            s.setTotalBeds(s.getTotalBeds() + 1);
            Integer status = bed.getBedStatus();
            if (status == null) {
                continue;
            }
            BedStatusEnum bedStatus = BedStatusEnum.fromCode(status);
            if (bedStatus == null) {
                continue;
            }
            switch (bedStatus) {
                case FREE -> s.setFree(s.getFree() + 1);
                case OCCUPIED -> s.setOccupied(s.getOccupied() + 1);
                case REPAIR -> s.setRepair(s.getRepair() + 1);
                case LOCKED -> s.setLocked(s.getLocked() + 1);
            }
            if (bedStatus != BedStatusEnum.OCCUPIED) {
                continue;
            }
            if (Boolean.TRUE.equals(bed.getNewToday())) {
                s.setNewToday(s.getNewToday() + 1);
            }
            if (TextUtil.hasText(bed.getAllergyHistory())) {
                s.setAllergyCount(s.getAllergyCount() + 1);
            }
            if (Boolean.TRUE.equals(bed.getCritical())) {
                s.setCriticalCount(s.getCriticalCount() + 1);
            }
            Integer postOpDays = bed.getPostOpDays();
            if (postOpDays != null && postOpDays >= 0 && postOpDays <= 30) {
                s.setPostOpCount(s.getPostOpCount() + 1);
            }
            Integer level = bed.getNursingLevel();
            if (level == null) {
                s.setLevelUnknown(s.getLevelUnknown() + 1);
            } else if (level == 1) {
                s.setLevelSpecial(s.getLevelSpecial() + 1);
            } else if (level == 2) {
                s.setLevelOne(s.getLevelOne() + 1);
            } else if (level == 3) {
                s.setLevelTwo(s.getLevelTwo() + 1);
            } else if (level == 4) {
                s.setLevelThree(s.getLevelThree() + 1);
            }
        }
        // 维修/锁定的床不能住人，把它们算进分母会把使用率压得比事实好看
        s.setUsageRate(percent(s.getOccupied(), s.getTotalBeds() - s.getRepair() - s.getLocked()));
        return s;
    }

    // 内部方法

    /**
     * 科室下拉：按授权范围收口，且只保留真的有床位的科室
     */
    private List<BedMapVO.DeptOption> resolveDeptOptions(Long currentDeptId, String currentDeptName) {
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        List<BedMapVO.DeptOption> options = new ArrayList<>();
        for (BedMapVO.DeptOption option : bedMapMapper.selectDeptOptions()) {
            if (allowed == null || allowed.contains(option.getDeptId())) {
                options.add(option);
            }
        }
        if (options.stream().noneMatch(o -> Objects.equals(o.getDeptId(), currentDeptId))) {
            // 当前科室一个床位都没有时也要能显示出来，否则下拉里没有它、卡片区却是空的，看着像坏了
            BedMapVO.DeptOption self = new BedMapVO.DeptOption();
            self.setDeptId(currentDeptId);
            self.setDeptName(TextUtil.hasText(currentDeptName) ? currentDeptName : "当前科室");
            options.add(0, self);
        }
        return options;
    }

    /**
     * 床位占用：状态置占用 + 记患者 + 同步病区冗余计数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void occupyBedForObservation(Long bedId, Long patientId) {
        if (bedId == null || patientId == null) {
            throw new BusinessException("留观占床需要床位与患者");
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }
        if (!Objects.equals(BedStatusEnum.FREE.getCode(), bed.getBedStatus())) {
            throw new BusinessException("床位当前不可用（" + BedStatusEnum.labelOrUnknown(bed.getBedStatus()) + "），请选择空闲床位");
        }
        occupyBed(bed, patientId, bed.getWardId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBedForObservation(Long bedId, Long patientId) {
        if (bedId == null) {
            return;
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        if (bed == null || !Objects.equals(bed.getPatientId(), patientId)) {
            return;
        }
        releaseBed(bedId);
    }

    private void occupyBed(SysBed bed, Long patientId, Long wardId) {
        sysBedMapper.update(null, new LambdaUpdateWrapper<SysBed>()
                .eq(SysBed::getBedId, bed.getBedId())
                .set(SysBed::getBedStatus, BedStatusEnum.OCCUPIED.getCode())
                .set(SysBed::getPatientId, patientId)
                .set(SysBed::getUpdateTime, LocalDateTime.now()));
        sysBedMapper.syncWardOccupied(wardId);
    }

    /**
     * 床位释放：状态置空闲 + 清患者
     * <p>注意必须用 UpdateWrapper 显式 {@code set(patientId, null)}——{@code updateById} 的
     * NOT_NULL 策略会跳过 null 字段，导致患者一直挂在床位上（脏占用）。
     */
    private void releaseBed(Long bedId) {
        if (bedId == null) {
            return;
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        if (bed == null) {
            return;
        }
        sysBedMapper.update(null, new LambdaUpdateWrapper<SysBed>()
                .eq(SysBed::getBedId, bedId)
                .set(SysBed::getBedStatus, BedStatusEnum.FREE.getCode())
                .set(SysBed::getPatientId, null)
                .set(SysBed::getUpdateTime, LocalDateTime.now()));
        sysBedMapper.syncWardOccupied(bed.getWardId());
    }

    /**
     * 住院天数：出院日期 - 入院日期，不足 1 天按 1 天（真实首页口径）
     */
    private int calcInpatientDays(LocalDateTime admitTime, LocalDateTime endTime) {
        if (admitTime == null || endTime == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(admitTime.toLocalDate(), endTime.toLocalDate());
        return days < 1 ? 1 : (int) days;
    }

    private String nextAdmissionNo() {
        return redisSequenceService.generateAdmissionNo();
    }

    private String nextDischargeNo() {
        return redisSequenceService.generateDischargeNo();
    }

    /**
     * 保证本次入院挂得上一个「就诊次」（就诊次）。
     *
     * <p><b>为什么需要它：</b>就诊次这张表一直存在，却<b>没有任何 Java 代码引用过它</b>，
     * 所以入院记录的就诊ID 长期是悬空引用——8 条入院里 4 条有值，但没人说得清
     * 那些值是谁写的。三甲的「统一数据管理」和后续 CDR 时间轴都要靠就诊次把一次来院串起来，
     * 所以这里把它真正落地：同患者当天有进行中的就诊次就复用（顺带把挂号 ID 并进 regist_ids），
     * 没有就新建一条。
     *
     * <p>注意：不复用已结束（visit_status=2）的就诊次。复用会让新入院的费用/病历挂到上次就诊上，
     * 时间轴直接错乱。
     */
    private Long ensureVisit(Long patientId, Long registId) {
        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime dayStart = TimeUtil.dayStart(now.toLocalDate());
        LocalDateTime dayEnd = TimeUtil.dayStart(now.toLocalDate().plusDays(1));

        BizVisit existing = bizVisitMapper.selectOne(new LambdaQueryWrapper<BizVisit>()
                .eq(BizVisit::getPatientId, patientId)
                .eq(BizVisit::getVisitStatus, VisitStatusEnum.OPEN.getCode())
                .ge(BizVisit::getStartTime, dayStart)
                .lt(BizVisit::getStartTime, dayEnd)
                .orderByDesc(BizVisit::getVisitId)
                .last("LIMIT 1"));
        if (existing != null) {
            if (registId != null && !containsRegistId(existing.getRegistIds(), registId)) {
                existing.setRegistIds(TextUtil.hasText(existing.getRegistIds())
                        ? existing.getRegistIds() + "," + registId
                        : String.valueOf(registId));
                bizVisitMapper.updateById(existing);
            }
            return existing.getVisitId();
        }

        BizVisit visit = new BizVisit();
        visit.setVisitNo(nextVisitNo());
        visit.setPatientId(patientId);
        visit.setStartTime(now);
        visit.setTotalAmount(BigDecimal.ZERO);
        visit.setVisitStatus(VisitStatusEnum.OPEN.getCode());
        visit.setRegistIds(registId == null ? null : String.valueOf(registId));
        visit.setRemark("住院收治时建立的就诊次");
        bizVisitMapper.insert(visit);
        log.info("新建就诊次 visitNo={} visitId={} patientId={} registId={}",
                visit.getVisitNo(), visit.getVisitId(), patientId, registId);
        return visit.getVisitId();
    }

    private String nextVisitNo() {
        return redisSequenceService.generateVisitNo();
    }

    /**
     * regist_ids 是逗号分隔的字符串列；按整段比对，避免 "1" 命中 "11" 这种子串误判
     */
    private boolean containsRegistId(String registIds, Long registId) {
        if (!TextUtil.hasText(registIds) || registId == null) {
            return false;
        }
        String target = String.valueOf(registId);
        for (String part : registIds.split(",")) {
            if (target.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取病案首页，不存在则补建（兼容第 1 期上线前就已入院的既有数据）
     */
    private BizInpatientSummary getOrCreateSummary(BizAdmission admission) {
        BizInpatientSummary summary = bizInpatientSummaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, admission.getAdmissionId()));
        if (summary == null) {
            summary = new BizInpatientSummary();
            summary.setAdmissionId(admission.getAdmissionId());
            summary.setPatientId(admission.getPatientId());
            summary.setAdmitTime(admission.getAdmitTime());
            summary.setAdmitWay(admission.getAdmitWay());
            summary.setMainDiagnosisName(admission.getDiagnosis());
            summary.setSummaryStatus(SummaryStatusEnum.DRAFT.getCode());
            bizInpatientSummaryMapper.insert(summary);
        }
        return summary;
    }

    /**
     * 入院时生成病案首页草稿，把患者与床位信息快照进去。
     * <p>快照的意义：患者姓名、床位在住院期间可能变，首页要留「当时是什么」。
     */
    private void createSummaryDraft(BizAdmission admission, BizPatient patient, WardVO ward, SysBed bed) {
        BizInpatientSummary summary = new BizInpatientSummary();
        summary.setAdmissionId(admission.getAdmissionId());
        summary.setPatientId(admission.getPatientId());
        summary.setPatientName(patient.getPatientName());
        summary.setGender(patient.getGender());
        summary.setAge(patient.getAge());
        // 年龄单位：只有明确年龄段时才写；不足 1 岁的月/天口径需录入者确认，不猜
        summary.setAgeUnit(patient.getAge() == null || patient.getAge() > 0 ? 1 : null);
        summary.setIdCard(patient.getIdCard());
        summary.setMedicalInsuranceNo(patient.getMedicalInsuranceNo());
        summary.setDeptId(admission.getDeptId());
        summary.setDeptName(ward.getDeptName());
        summary.setWardId(admission.getWardId());
        summary.setWardName(ward.getWardName());
        summary.setBedNo(bed.getBedNo());
        summary.setAdmitTime(admission.getAdmitTime());
        summary.setAdmitWay(admission.getAdmitWay());
        summary.setDeathFlag(0);
        summary.setReadmit31d(0);
        summary.setIsSurgery(0);
        summary.setIsRescue(0);
        summary.setIsCritical(0);
        summary.setMainDiagnosisCode(admission.getAdmitDiagnosisCode());
        summary.setMainDiagnosisName(TextUtil.hasText(admission.getAdmitDiagnosisName())
                ? admission.getAdmitDiagnosisName() : admission.getDiagnosis());
        summary.setSummaryStatus(SummaryStatusEnum.DRAFT.getCode());
        bizInpatientSummaryMapper.insert(summary);
    }

    /**
     * 首页字段拷贝（只覆盖 DTO 里非空的字段）
     */
    private void copySummaryFields(InpatientSummaryUpsertDTO dto, BizInpatientSummary summary) {
        if (dto.getAgeUnit() != null) {
            summary.setAgeUnit(dto.getAgeUnit());
        }
        if (dto.getAdmitWay() != null) {
            summary.setAdmitWay(dto.getAdmitWay());
        }
        if (dto.getDischargeWay() != null) {
            summary.setDischargeWay(dto.getDischargeWay());
        }
        if (dto.getIsRescue() != null) {
            summary.setIsRescue(dto.getIsRescue());
        }
        if (dto.getIsCritical() != null) {
            summary.setIsCritical(dto.getIsCritical());
        }
        if (dto.getTotalAmount() != null) {
            summary.setTotalAmount(dto.getTotalAmount());
        }
        if (dto.getWesternDrugAmount() != null) {
            summary.setWesternDrugAmount(dto.getWesternDrugAmount());
        }
        if (dto.getChineseDrugAmount() != null) {
            summary.setChineseDrugAmount(dto.getChineseDrugAmount());
        }
        if (dto.getHerbalAmount() != null) {
            summary.setHerbalAmount(dto.getHerbalAmount());
        }
        if (dto.getExamAmount() != null) {
            summary.setExamAmount(dto.getExamAmount());
        }
        if (dto.getLabAmount() != null) {
            summary.setLabAmount(dto.getLabAmount());
        }
        if (dto.getTreatmentAmount() != null) {
            summary.setTreatmentAmount(dto.getTreatmentAmount());
        }
        if (dto.getOperationAmount() != null) {
            summary.setOperationAmount(dto.getOperationAmount());
        }
        if (dto.getMaterialAmount() != null) {
            summary.setMaterialAmount(dto.getMaterialAmount());
        }
        if (dto.getBedAmount() != null) {
            summary.setBedAmount(dto.getBedAmount());
        }
        if (dto.getNursingAmount() != null) {
            summary.setNursingAmount(dto.getNursingAmount());
        }
        if (dto.getOtherAmount() != null) {
            summary.setOtherAmount(dto.getOtherAmount());
        }
        if (dto.getRemark() != null) {
            summary.setRemark(dto.getRemark());
        }
    }

    private BigDecimal percent(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    @Override
    public BizAdmission getAdmissionById(Long admissionId) {
        return admissionId == null ? null : bizAdmissionMapper.selectById(admissionId);
    }

    @Override
    public WardVO getWardById(Long wardId) {
        return wardId == null ? null : sysBedMapper.selectWardById(wardId);
    }

    @Override
    public String getBedNoById(Long bedId) {
        if (bedId == null) {
            return null;
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        return bed == null ? null : bed.getBedNo();
    }

    @Override
    public boolean isSummaryArchived(Long admissionId) {
        BizInpatientSummary summary = bizInpatientSummaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, admissionId)
                .last("LIMIT 1"));
        return summary != null && Objects.equals(SummaryStatusEnum.ARCHIVED.getCode(), summary.getSummaryStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInpatientOperation appendSurgeryOperation(BizInpatientOperation operation) {
        List<BizInpatientOperation> existing = bizInpatientOperationMapper.selectList(
                new LambdaQueryWrapper<BizInpatientOperation>()
                        .eq(BizInpatientOperation::getAdmissionId, operation.getAdmissionId()));

        if (Objects.equals(1, operation.getIsMain()) && existing.stream()
                .anyMatch(o -> Objects.equals(1, o.getIsMain()))) {
            throw new BusinessException("该住院的病案首页已有一条主要手术，本单也是「主要手术」，"
                    + "回写会让首页出现两条主手术。请先在首页把原主手术改次要，或把本单改次要");
        }
        // 首页手工录入过同名手术 → 先让病案室删掉，避免出现两条重复明细
        String actualName = operation.getOperationName().trim();
        boolean manualDuplicate = existing.stream()
                .anyMatch(o -> o.getApplyId() == null
                        && TextUtil.hasText(o.getOperationName())
                        && o.getOperationName().trim().equals(actualName));
        if (manualDuplicate) {
            throw new BusinessException("病案首页已手工录入同名手术「" + actualName
                    + "」，回写会变成两条重复明细。请先在首页删除该行，再登记本次手术完成");
        }

        operation.setSeqNo(existing.size() + 1);          // 先占位，插完统一重排
        bizInpatientOperationMapper.insert(operation);
        SummaryOperationSeq.reseq(bizInpatientOperationMapper, operation.getAdmissionId());

        BizInpatientSummary summary = bizInpatientSummaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, operation.getAdmissionId())
                .last("LIMIT 1"));
        // 只标记手术事实，不碰费用列：手术费用要走收费明细，不在这里猜价格
        if (summary != null && !Objects.equals(1, summary.getIsSurgery())) {
            BizInpatientSummary upd = new BizInpatientSummary();
            upd.setId(summary.getId());
            upd.setIsSurgery(1);
            bizInpatientSummaryMapper.updateById(upd);
        }
        return operation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSummaryTransfused(Long admissionId) {
        BizInpatientSummary summary = bizInpatientSummaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, admissionId)
                .last("LIMIT 1"));
        // 只标记输血事实，不碰费用列：输血费用要走收费明细，不在这里猜价格
        if (summary != null && !Objects.equals(1, summary.getIsTransfusion())) {
            BizInpatientSummary upd = new BizInpatientSummary();
            upd.setId(summary.getId());
            upd.setIsTransfusion(1);
            bizInpatientSummaryMapper.updateById(upd);
        }
    }
}