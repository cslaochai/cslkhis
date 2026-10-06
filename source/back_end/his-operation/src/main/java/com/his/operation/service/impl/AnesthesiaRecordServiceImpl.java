package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.util.TimeUtil;
import com.his.common.exception.BusinessException;
import com.his.operation.dto.*;
import com.his.operation.entity.*;
import com.his.operation.enums.AirwayDeviceEnum;
import com.his.operation.enums.AnesthesiaChargeStatusEnum;
import com.his.operation.enums.AnesthesiaEffectEnum;
import com.his.operation.enums.AnesthesiaRecordStatusEnum;
import com.his.operation.enums.AsaGradeEnum;
import com.his.operation.enums.ChargeSourceEnum;
import com.his.operation.enums.MedPhaseEnum;
import com.his.operation.enums.MedRouteEnum;
import com.his.operation.enums.OperationAnesthesiaMethodEnum;
import com.his.operation.enums.OperationApplyStatusEnum;
import com.his.operation.enums.OperationEmergencyEnum;
import com.his.operation.enums.PostopDispositionEnum;
import com.his.operation.enums.VentilationModeEnum;
import com.his.operation.enums.VisitConclusionEnum;
import com.his.operation.mapper.*;
import com.his.operation.service.AnesthesiaRecordService;
import com.his.operation.service.AnesthesiaVisitService;
import com.his.operation.support.AnesthesiaCalcs;
import com.his.operation.support.OperationChargeBiller;
import com.his.operation.vo.*;
import com.his.patient.entity.BizPatient;
import com.his.patient.service.PatientService;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 麻醉记录单服务实现（G15 核心）。
 *
 * <p>本类固化了这些<b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>术前访视闸门</b>：没有"可施行麻醉"结论的访视单，不许开立麻醉记录。
 *       急诊手术允许抢先麻醉但同时记 {@code visitPending=true} —— 事后补不出访视的那一台
 *       会在列表里一直标红，而不是随时间安静消失。</li>
 *   <li><b>已提交后锁死体征与用药</b>：术后补一条 8:15 的血压是伪造，
 *       与"术后补一条术前核对记录是伪造"同一条原则。</li>
 *   <li><b>提交门槛</b>：麻醉方式、麻醉起止时间、至少一条生命体征，缺一不可 ——
 *       一张没有体征的麻醉单等于"这台手术期间没有人在看着"。</li>
 *   <li><b>提交即联动计费</b>：麻醉费 / 监护费 / 插管费一次性落到住院费用单；
 *       计费失败不回滚业务（钱没计上 ≠ 麻醉没做），但状态会标记"计费异常"并写明原因。</li>
 *   <li><b>时间一律截到秒</b>：库表 DATETIME(0) 会四舍五入，不截就"写进去的 ≠ 读回来的"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnesthesiaRecordServiceImpl implements AnesthesiaRecordService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizAnesthesiaRecordMapper recordMapper;
    private final BizAnesthesiaVitalMapper vitalMapper;
    private final BizAnesthesiaMedMapper medMapper;
    private final BizOperationApplyMapper applyMapper;
    private final BizOperationChargeItemMapper chargeItemMapper;
    private final PatientService patientService;
    private final AnesthesiaVisitService visitService;
    private final OperationChargeBiller biller;

    // 查询

    private static <T> T pick(T existing, T incoming) {
        return incoming != null ? incoming : existing;
    }

    private static Long minutesBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        long m = Duration.between(from, to).toMinutes();
        return m < 0 ? null : m;
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // 开立 / 更新

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    // 生命体征 / 用药（只增不改，且仅"记录中"可增）

    @Override
    public IPage<AnesthesiaRecordVO> listPage(AnesthesiaRecordQueryPageDTO query) {
        if (query == null) {
            query = new AnesthesiaRecordQueryPageDTO();
        }
        IPage<AnesthesiaRecordVO> page = recordMapper.selectRecordPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        page.getRecords().forEach(this::decorate);
        return page;
    }

    @Override
    public AnesthesiaRecordVO getDetailById(Long recordId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (recordId == null) {
            throw new BusinessException("麻醉记录单ID不能为空");
        }
        AnesthesiaRecordVO vo = recordMapper.selectVOById(recordId);
        if (vo == null) {
            throw new BusinessException("麻醉记录单不存在");
        }
        decorateDetail(vo);
        return vo;
    }

    @Override
    public AnesthesiaRecordVO getByApply(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        AnesthesiaRecordVO vo = recordMapper.selectVOByApply(applyId);
        if (vo != null) {
            decorateDetail(vo);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(AnesthesiaRecordUpsertDTO dto) {
        BizOperationApply apply = applyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new BusinessException("手术申请单不存在");
        }
        if (Integer.valueOf(OperationApplyStatusEnum.CANCELLED.getCode()).equals(apply.getOperationStatus())) {
            throw new BusinessException("手术单 " + apply.getApplyNo() + " 已取消，不能开立麻醉记录");
        }
        if (recordMapper.selectVOByApply(apply.getId()) != null) {
            throw new BusinessException("该手术已有麻醉记录单 " + recordMapper.selectVOByApply(apply.getId()).getRecordNo()
                    + "，不能重复开立（一台手术一份麻醉记录）");
        }

        // ★ 术前访视闸门
        Long visitId = null;
        boolean emergency = Integer.valueOf(1).equals(apply.getIsEmergency());
        AnesthesiaVisitVO existingVisit;
        try {
            existingVisit = visitService.getByApply(apply.getId());
        } catch (BusinessException e) {
            // 没有访视单是正常的（getByApply 对"不存在"返回 null，抛错说明别的问题，往上抛）
            throw e;
        }
        boolean visitPending = false;
        if (existingVisit == null) {
            if (!emergency) {
                throw new BusinessException("该手术尚无麻醉术前访视记录，不能开立麻醉记录 —— "
                        + "麻醉科没看过这个病人就上台，等于把风险评估跳过了（若为抢救想先麻醉，"
                        + "请先把手术单标记为急诊手术，系统会在本单上留「待补访视」的标记）");
            }
            visitPending = true;
        } else if (!Integer.valueOf(VisitConclusionEnum.OK.getCode()).equals(existingVisit.getConclusion())
                || !Integer.valueOf(1).equals(existingVisit.getVisitStatus())) {
            // 已经明确下了"暂缓/需会诊"结论的，连急诊也不允许越过 ——
            // 让急诊绕过一条明写着的禁忌，比让急诊绕过"还没写"，危险得多。
            throw new BusinessException("该手术的麻醉术前访视结论为「"
                    + VisitConclusionEnum.labelOrUnknown(existingVisit.getConclusion())
                    + "」，不能开立麻醉记录");
        } else {
            visitId = existingVisit.getId();
        }

        BizAnesthesiaRecord entity = new BizAnesthesiaRecord();
        entity.setRecordNo(nextRecordNo());
        entity.setApplyId(apply.getId());
        entity.setApplyNo(apply.getApplyNo());
        entity.setAdmissionId(apply.getAdmissionId());
        entity.setPatientId(apply.getPatientId());
        entity.setPatientName(apply.getPatientName());
        entity.setGender(apply.getGender());
        entity.setAge(apply.getAge());
        entity.setVisitId(visitId);
        // 麻醉方式：优先取本次传入（允许临时改方式），再用申请单登记值
        entity.setAnesthesiaType(dto.getAnesthesiaType() != null
                ? dto.getAnesthesiaType() : apply.getAnesthesiaType());
        entity.setAsaGrade(dto.getAsaGrade() != null
                ? dto.getAsaGrade() : (existingVisit == null ? null : existingVisit.getAsaGrade()));
        entity.setAnesthetistId(dto.getAnesthetistId() != null ? dto.getAnesthetistId() : currentEmpId());
        entity.setAnesthetistName(employeeNameOf(entity.getAnesthetistId()));
        entity.setAssistantAnesthetistName(dto.getAssistantAnesthetistName());
        entity.setEnterRoomTime(TimeUtil.toSeconds(dto.getEnterRoomTime() == null ? now() : dto.getEnterRoomTime()));
        entity.setRecordStatus(AnesthesiaRecordStatusEnum.DRAFT.getCode());
        entity.setChargeStatus(AnesthesiaChargeStatusEnum.PENDING.getCode());
        // 「急诊超前麻醉」的状态靠 visit_id 为空来表达，不额外加一列：
        // 加列就会出现"visit_id 有值但 pending=1"这种自相矛盾的行
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        recordMapper.insert(entity);

        log.info("开立麻醉记录 recordNo={} applyNo={} 麻醉方式={} 急诊={} 访视ID={} 待补访视={} 开立人={}",
                entity.getRecordNo(), apply.getApplyNo(),
                OperationAnesthesiaMethodEnum.labelOrUnknown(entity.getAnesthesiaType()),
                emergency, visitId, visitPending, currentName());
        return entity.getRecordNo();
    }

    // 提交 / 审核 / 计费

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AnesthesiaRecordUpdateUpsertDTO dto) {
        BizAnesthesiaRecord entity = mustEditable(dto == null ? null : dto.getRecordId());
        // B-条件必填：标记发生麻醉不良事件时才要求经过与处理，跨字段条件，DTO 注解无法表达，保留
        if (Integer.valueOf(1).equals(dto.getAdverseEventFlag()) && !StringUtils.hasText(dto.getAdverseEventNote())) {
            throw new BusinessException("已标记发生麻醉不良事件，必须填写经过与处理");
        }
        copyNotNullIgnoring(dto, entity);
        recordMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addVital(AnesthesiaVitalUpsertDTO dto) {
        BizAnesthesiaRecord record = mustEditable(dto == null ? null : dto.getRecordId());
        LocalDateTime sampleTime = TimeUtil.toSeconds(dto.getSampleTime());
        if (vitalMapper.countSameTime(record.getId(), sampleTime) > 0) {
            throw new BusinessException("采样时刻 " + dto.getSampleTime()
                    + " 已有生命体征记录；同一时刻一个点只能有一组真值（要改请先看是不是采样时刻填错了）");
        }
        BizAnesthesiaVital vital = new BizAnesthesiaVital();
        vital.setRecordId(record.getId());
        vital.setSampleTime(sampleTime);
        vital.setSystolic(dto.getSystolic());
        vital.setDiastolic(dto.getDiastolic());
        vital.setHeartRate(dto.getHeartRate());
        vital.setRespiration(dto.getRespiration());
        vital.setTemperature(dto.getTemperature());
        vital.setSpo2(dto.getSpo2());
        vital.setEtco2(dto.getEtco2());
        vital.setRemark(dto.getRemark());
        vitalMapper.insert(vital);
    }

    @Override
    public List<AnesthesiaVitalVO> listVitals(Long recordId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (recordId == null) {
            throw new BusinessException("麻醉记录单ID不能为空");
        }
        List<BizAnesthesiaVital> list = vitalMapper.selectList(
                new LambdaQueryWrapper<BizAnesthesiaVital>()
                        .eq(BizAnesthesiaVital::getRecordId, recordId)
                        .orderByAsc(BizAnesthesiaVital::getSampleTime)
                        .orderByAsc(BizAnesthesiaVital::getId));
        List<AnesthesiaVitalVO> vos = new ArrayList<>();
        for (BizAnesthesiaVital v : list) {
            AnesthesiaVitalVO vo = new AnesthesiaVitalVO();
            BeanUtils.copyProperties(v, vo);
            vo.setAbnormalText(abnormalText(v));
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMed(AnesthesiaMedUpsertDTO dto) {
        BizAnesthesiaRecord record = mustEditable(dto == null ? null : dto.getRecordId());
        BizAnesthesiaMed med = new BizAnesthesiaMed();
        med.setRecordId(record.getId());
        med.setMedTime(TimeUtil.toSeconds(dto.getMedTime()));
        med.setMedPhase(dto.getMedPhase());
        med.setDrugCode(dto.getDrugCode());
        med.setDrugName(dto.getDrugName().trim());
        med.setDose(dto.getDose());
        med.setUnit(dto.getUnit());
        med.setRoute(dto.getRoute());
        med.setRemark(dto.getRemark());
        medMapper.insert(med);
    }

    @Override
    public List<AnesthesiaMedVO> listMeds(Long recordId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (recordId == null) {
            throw new BusinessException("麻醉记录单ID不能为空");
        }
        List<BizAnesthesiaMed> list = medMapper.selectList(
                new LambdaQueryWrapper<BizAnesthesiaMed>()
                        .eq(BizAnesthesiaMed::getRecordId, recordId)
                        .orderByAsc(BizAnesthesiaMed::getMedTime)
                        .orderByAsc(BizAnesthesiaMed::getId));
        List<AnesthesiaMedVO> vos = new ArrayList<>();
        for (BizAnesthesiaMed m : list) {
            AnesthesiaMedVO vo = new AnesthesiaMedVO();
            BeanUtils.copyProperties(m, vo);
            vo.setMedPhaseText(MedPhaseEnum.getText(m.getMedPhase()));
            vo.setRouteText(MedRouteEnum.getText(m.getRoute()));
            vo.setDoseText(m.getDose() == null ? null
                    : m.getDose().stripTrailingZeros().toPlainString()
                    + (StringUtils.hasText(m.getUnit()) ? " " + m.getUnit() : ""));
            vos.add(vo);
        }
        return vos;
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationChargeSummaryVO submit(AnesthesiaActionDTO dto) {
        BizAnesthesiaRecord entity = mustGet(dto == null ? null : dto.getId());
        if (!Integer.valueOf(AnesthesiaRecordStatusEnum.DRAFT.getCode()).equals(entity.getRecordStatus())) {
            throw new BusinessException("麻醉记录单 " + entity.getRecordNo() + " 当前状态为「"
                    + AnesthesiaRecordStatusEnum.labelOrUnknown(entity.getRecordStatus()) + "」，只有「记录中」可以提交");
        }
        if (entity.getAnesthesiaType() == null) {
            throw new BusinessException("未登记麻醉方式，不能提交（不知道做的是什么麻醉）");
        }
        if (entity.getAnesthesiaStartTime() == null || entity.getAnesthesiaEndTime() == null) {
            throw new BusinessException("未登记麻醉开始/结束时间，不能提交（算不出麻醉时长，也没法计监护费）");
        }
        long vitalCount = vitalMapper.selectCount(
                new LambdaQueryWrapper<BizAnesthesiaVital>().eq(BizAnesthesiaVital::getRecordId, entity.getId()));
        if (vitalCount == 0) {
            throw new BusinessException("一条生命体征都没有，不能提交 —— "
                    + "没有体征的麻醉记录单等于「这台手术期间没有人在看着」");
        }
        if (Integer.valueOf(1).equals(entity.getAdverseEventFlag())
                && !StringUtils.hasText(entity.getAdverseEventNote())) {
            throw new BusinessException("已标记发生麻醉不良事件，请先补写经过与处理再提交");
        }

        entity.setRecordStatus(AnesthesiaRecordStatusEnum.SUBMITTED.getCode());
        entity.setSubmitDoctorId(currentEmpId());
        entity.setSubmitDoctorName(currentName());
        entity.setSubmitTime(now());
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }

        // ★ 计费联动：业务照常推进，记账失败只记失败（money 没到账 ≠ 麻醉没做）
        OperationChargeSummaryVO summary = new OperationChargeSummaryVO();
        try {
            summary = biller.billRecord(entity, patientNoOf(entity.getPatientId()));
        } catch (Exception e) {
            log.error("麻醉记录 {} 计费异常：{}", entity.getRecordNo(), e.getMessage(), e);
            summary.getMessages().add("计费过程异常：" + e.getMessage());
            summary.setFailedItems(summary.getFailedItems() + 1);
        }
        applyChargeResult(entity, summary);
        recordMapper.updateById(entity);
        log.info("提交麻醉记录 recordNo={} 状态=已提交 计费=成功{}项/失败{}项 金额={} 提交人={}",
                entity.getRecordNo(), summary.getSuccessItems(), summary.getFailedItems(),
                summary.getAmount(), currentName());
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AnesthesiaActionDTO dto) {
        BizAnesthesiaRecord entity = mustGet(dto == null ? null : dto.getId());
        if (!Integer.valueOf(AnesthesiaRecordStatusEnum.SUBMITTED.getCode()).equals(entity.getRecordStatus())) {
            throw new BusinessException("麻醉记录单 " + entity.getRecordNo() + " 当前状态为「"
                    + AnesthesiaRecordStatusEnum.labelOrUnknown(entity.getRecordStatus()) + "」，只有「已提交」可以审核");
        }
        entity.setRecordStatus(AnesthesiaRecordStatusEnum.AUDITED.getCode());
        entity.setAuditDoctorId(currentEmpId());
        entity.setAuditDoctorName(currentName());
        entity.setAuditTime(now());
        if (dto != null && StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        recordMapper.updateById(entity);
        log.info("审核麻醉记录 recordNo={} 审核人={}", entity.getRecordNo(), currentName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationChargeSummaryVO charge(AnesthesiaActionDTO dto) {
        BizAnesthesiaRecord entity = mustGet(dto == null ? null : dto.getId());
        if (Integer.valueOf(AnesthesiaRecordStatusEnum.DRAFT.getCode()).equals(entity.getRecordStatus())) {
            throw new BusinessException("麻醉记录单 " + entity.getRecordNo()
                    + " 还在「记录中」，先在提交时统一计费（或改完内容再提交）");
        }
        OperationChargeSummaryVO summary;
        try {
            summary = biller.billRecord(entity, patientNoOf(entity.getPatientId()));
        } catch (Exception e) {
            log.error("麻醉记录 {} 重新计费异常：{}", entity.getRecordNo(), e.getMessage(), e);
            summary = new OperationChargeSummaryVO();
            summary.getMessages().add("计费过程异常：" + e.getMessage());
            summary.setFailedItems(1);
        }
        applyChargeResult(entity, summary);
        recordMapper.updateById(entity);
        return summary;
    }

    @Override
    public long countUncharged() {
        return recordMapper.selectCount(new LambdaQueryWrapper<BizAnesthesiaRecord>()
                .ne(BizAnesthesiaRecord::getChargeStatus, AnesthesiaChargeStatusEnum.DONE.getCode()));
    }

    @Override
    public List<OperationChargeItemVO> listChargeItems(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        List<OperationChargeItemVO> vos = new ArrayList<>();
        for (BizOperationChargeItem row : chargeItemMapper.selectByApply(applyId)) {
            OperationChargeItemVO vo = new OperationChargeItemVO();
            BeanUtils.copyProperties(row, vo);
            vo.setChargeStatusText(AnesthesiaChargeStatusEnum.getText(row.getChargeStatus()));
            vo.setSourceTypeText(ChargeSourceEnum.getText(row.getSourceType()));
            vos.add(vo);
        }
        return vos;
    }

    private void applyChargeResult(BizAnesthesiaRecord entity, OperationChargeSummaryVO summary) {
        if (summary == null || summary.getTotalItems() == 0) {
            entity.setChargeStatus(AnesthesiaChargeStatusEnum.PENDING.getCode());
            entity.setChargeFailReason("本次没有可计费项目");
            return;
        }
        entity.setChargedAmount(nz(entity.getChargedAmount()).add(summary.getAmount()));
        if (summary.hasFailure()) {
            entity.setChargeStatus(AnesthesiaChargeStatusEnum.FAILED.getCode());
            entity.setChargeFailReason(AnesthesiaCalcs.clipReason(String.join("；", summary.getMessages())));
        } else {
            entity.setChargeStatus(AnesthesiaChargeStatusEnum.DONE.getCode());
            entity.setFeeNo(summary.getFeeNo());
            entity.setChargeFailReason(null);
        }
    }

    private String patientNoOf(Long patientId) {
        if (patientId == null) {
            return null;
        }
        BizPatient patient = patientService.getById(patientId);
        return patient == null ? null : patient.getPatientNo();
    }

    private BizAnesthesiaRecord mustGet(Long recordId) {
        // C-非 DTO 入参：私有 helper 校验方法参数，被多入口复用，Bean Validation 不覆盖，保留
        if (recordId == null) {
            throw new BusinessException("麻醉记录单ID不能为空");
        }
        BizAnesthesiaRecord entity = recordMapper.selectById(recordId);
        if (entity == null) {
            throw new BusinessException("麻醉记录单不存在");
        }
        return entity;
    }

    /**
     * 取"仍可编辑"的记录：已提交/已审核一律拒绝（不允许术后改记载）
     */
    private BizAnesthesiaRecord mustEditable(Long recordId) {
        BizAnesthesiaRecord entity = mustGet(recordId);
        if (!Integer.valueOf(AnesthesiaRecordStatusEnum.DRAFT.getCode()).equals(entity.getRecordStatus())) {
            throw new BusinessException("麻醉记录单 " + entity.getRecordNo() + " 当前状态为「"
                    + AnesthesiaRecordStatusEnum.labelOrUnknown(entity.getRecordStatus())
                    + "」，已固化的麻醉记录不能再改内容（术后补一条记载属于伪造）");
        }
        return entity;
    }

    // 展示态

    /**
     * 把 DTO 里非空的值拷进实体；ID / 状态 / 时间留痕列一概不拷。
     *
     * <p>不用 {@code BeanUtils.copyProperties} 直接覆盖：DTO 里没给的一律保持原值，
     * 否则"只改出血量"会把别的字段擦成 null，而用户根本没意识到自己提交了那样的表单。
     */
    private void copyNotNullIgnoring(AnesthesiaRecordUpdateUpsertDTO dto, BizAnesthesiaRecord entity) {
        if (dto.getAnesthesiaType() != null) {
            entity.setAnesthesiaType(dto.getAnesthesiaType());
        }
        if (dto.getAnesthesiaMethodDetail() != null) {
            entity.setAnesthesiaMethodDetail(dto.getAnesthesiaMethodDetail());
        }
        if (dto.getAirwayDevice() != null) {
            entity.setAirwayDevice(dto.getAirwayDevice());
        }
        if (dto.getAirwayDeviceSpec() != null) {
            entity.setAirwayDeviceSpec(dto.getAirwayDeviceSpec());
        }
        if (dto.getVentilationMode() != null) {
            entity.setVentilationMode(dto.getVentilationMode());
        }
        if (dto.getAnesthetistId() != null) {
            entity.setAnesthetistId(dto.getAnesthetistId());
            entity.setAnesthetistName(employeeNameOf(dto.getAnesthetistId()));
        }
        if (dto.getAssistantAnesthetistName() != null) {
            entity.setAssistantAnesthetistName(dto.getAssistantAnesthetistName());
        }
        entity.setEnterRoomTime(TimeUtil.toSeconds(pick(entity.getEnterRoomTime(), dto.getEnterRoomTime())));
        entity.setAnesthesiaStartTime(TimeUtil.toSeconds(pick(entity.getAnesthesiaStartTime(), dto.getAnesthesiaStartTime())));
        entity.setOperationStartTime(TimeUtil.toSeconds(pick(entity.getOperationStartTime(), dto.getOperationStartTime())));
        entity.setOperationEndTime(TimeUtil.toSeconds(pick(entity.getOperationEndTime(), dto.getOperationEndTime())));
        entity.setAnesthesiaEndTime(TimeUtil.toSeconds(pick(entity.getAnesthesiaEndTime(), dto.getAnesthesiaEndTime())));
        entity.setLeaveRoomTime(TimeUtil.toSeconds(pick(entity.getLeaveRoomTime(), dto.getLeaveRoomTime())));
        entity.setCrystalloid(pick(entity.getCrystalloid(), dto.getCrystalloid()));
        entity.setColloid(pick(entity.getColloid(), dto.getColloid()));
        entity.setBloodTransfusion(pick(entity.getBloodTransfusion(), dto.getBloodTransfusion()));
        entity.setAutotransfusion(pick(entity.getAutotransfusion(), dto.getAutotransfusion()));
        entity.setUrineOutput(pick(entity.getUrineOutput(), dto.getUrineOutput()));
        entity.setBloodLoss(pick(entity.getBloodLoss(), dto.getBloodLoss()));
        entity.setAdverseEventFlag(pick(entity.getAdverseEventFlag(), dto.getAdverseEventFlag()));
        entity.setAdverseEventNote(pick(entity.getAdverseEventNote(), dto.getAdverseEventNote()));
        entity.setAnesthesiaEffect(pick(entity.getAnesthesiaEffect(), dto.getAnesthesiaEffect()));
        entity.setPostopDisposition(pick(entity.getPostopDisposition(), dto.getPostopDisposition()));
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
    }

    /**
     * 生命体征常识边界（成人）—— 只做标记，不下结论
     */
    private String abnormalText(BizAnesthesiaVital v) {
        StringBuilder sb = new StringBuilder();
        if (v.getSystolic() != null && (v.getSystolic() < 90 || v.getSystolic() >= 180)) {
            sb.append("血压异常 ");
        }
        if (v.getHeartRate() != null && (v.getHeartRate() < 50 || v.getHeartRate() > 120)) {
            sb.append("心率异常 ");
        }
        if (v.getSpo2() != null && v.getSpo2() < 92) {
            sb.append("氧合偏低 ");
        }
        if (v.getEtco2() != null && (v.getEtco2() < 30 || v.getEtco2() > 50)) {
            sb.append("EtCO2 异常 ");
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = applyMapper.selectEmployeeName(empId);
        return StringUtils.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    private String nextRecordNo() {
        String prefix = "MZ" + LocalDate.now().format(NO_DATE);
        long seq = recordMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private void decorate(AnesthesiaRecordVO vo) {
        vo.setRecordStatusText(AnesthesiaRecordStatusEnum.getText(vo.getRecordStatus()));
        vo.setAnesthesiaTypeText(OperationAnesthesiaMethodEnum.getText(vo.getAnesthesiaType()));
        vo.setApplyAnesthesiaTypeText(OperationAnesthesiaMethodEnum.getText(vo.getApplyAnesthesiaType()));
        vo.setAsaText(AsaGradeEnum.getText(vo.getAsaGrade()));
        vo.setAirwayDeviceText(AirwayDeviceEnum.getText(vo.getAirwayDevice()));
        vo.setVentilationText(VentilationModeEnum.getText(vo.getVentilationMode()));
        vo.setEffectText(AnesthesiaEffectEnum.getText(vo.getAnesthesiaEffect()));
        vo.setDispositionText(PostopDispositionEnum.getText(vo.getPostopDisposition()));
        vo.setChargeStatusText(AnesthesiaChargeStatusEnum.getText(vo.getChargeStatus()));
        vo.setVisitConclusionText(VisitConclusionEnum.getText(vo.getVisitConclusion()));
        vo.setEmergencyText(OperationEmergencyEnum.getText(vo.getIsEmergency()));
        vo.setOperationStatusText(OperationApplyStatusEnum.getText(vo.getOperationStatus()));

        Long anesthesiaMinutes = minutesBetween(vo.getAnesthesiaStartTime(), vo.getAnesthesiaEndTime());
        vo.setAnesthesiaMinutes(anesthesiaMinutes);
        vo.setAnesthesiaDurationText(AnesthesiaCalcs.durationText(anesthesiaMinutes));
        Long operationMinutes = minutesBetween(vo.getOperationStartTime(), vo.getOperationEndTime());
        vo.setOperationMinutes(operationMinutes);
        vo.setOperationDurationText(AnesthesiaCalcs.durationText(operationMinutes));
        Long billBase = anesthesiaMinutes != null ? anesthesiaMinutes : operationMinutes;
        vo.setBillHours(AnesthesiaCalcs.billHours(billBase));

        boolean draft = Integer.valueOf(AnesthesiaRecordStatusEnum.DRAFT.getCode()).equals(vo.getRecordStatus());
        boolean submitted = Integer.valueOf(AnesthesiaRecordStatusEnum.SUBMITTED.getCode()).equals(vo.getRecordStatus());
        boolean audited = Integer.valueOf(AnesthesiaRecordStatusEnum.AUDITED.getCode()).equals(vo.getRecordStatus());

        vo.setCanEditVitals(draft);
        vo.setCanSubmit(draft);
        vo.setCanAudit(submitted);
        vo.setCanOpenPacu(submitted || audited);
        vo.setCanCharge(!draft);
        vo.setVisitPending(vo.getVisitId() == null);
        vo.setWarningText(warningOf(vo, anesthesiaMinutes));
    }

    private void decorateDetail(AnesthesiaRecordVO vo) {
        decorate(vo);
        vo.setVitals(listVitals(vo.getId()));
        vo.setMeds(listMeds(vo.getId()));
        vo.setVitalCount(vo.getVitals() == null ? 0 : vo.getVitals().size());
        vo.setMedCount(vo.getMeds() == null ? 0 : vo.getMeds().size());
    }

    private String warningOf(AnesthesiaRecordVO vo, Long minutes) {
        if (vo.getVisitId() == null) {
            return "急诊超前麻醉：尚未登记术前访视（必须补录）";
        }
        if (Integer.valueOf(AnesthesiaChargeStatusEnum.FAILED.getCode()).equals(vo.getChargeStatus())) {
            return "计费异常：" + vo.getChargeFailReason();
        }
        if (Integer.valueOf(AnesthesiaChargeStatusEnum.PENDING.getCode()).equals(vo.getChargeStatus())
                && !Integer.valueOf(AnesthesiaRecordStatusEnum.DRAFT.getCode()).equals(vo.getRecordStatus())) {
            return "已固化但尚未计费";
        }
        if (minutes == null) {
            return "缺少麻醉起止时间，无法计算麻醉时长";
        }
        return null;
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径
     */
    private Long currentEmpId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentName() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            if (StringUtils.hasText(user.getEmployeeName())) {
                return user.getEmployeeName();
            }
            if (StringUtils.hasText(user.getRealName())) {
                return user.getRealName();
            }
            return user.getUsername();
        } catch (Exception e) {
            return null;
        }
    }
}
