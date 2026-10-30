package com.his.clinical.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.clinical.constant.EmrSubmitConst;
import com.his.clinical.dto.*;
import com.his.clinical.entity.*;
import com.his.clinical.event.ClinicSubmittedEvent;
import com.his.clinical.mapper.*;
import com.his.clinical.service.AiDraftDiffService;
import com.his.clinical.service.EmrService;
import com.his.clinical.service.EmrSignatureService;
import com.his.clinical.service.QualityControlService;
import com.his.clinical.support.QcOperator;
import com.his.clinical.support.QcSnapshot;
import com.his.clinical.vo.*;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.support.SystemOperatorContext;
import com.his.common.support.TcmGramUnits;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.spi.dto.FeeBookDTO;
import com.his.spi.dto.SignCommandDTO;
import com.his.spi.service.*;
import com.his.spi.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生工作站服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrServiceImpl extends ServiceImpl<BizMedicalRecordMapper, BizMedicalRecord> implements EmrService {
    static final String[] HUIFANG_TYPE = {"糖尿病", "高血压", "冠心病"};
    /**
     * 手写笔迹 PNG dataURL 长度上限（字符数），与病危告知/请假签收同一口径
     */
    private static final int RX_SIGNATURE_MAX = 512 * 1024;
    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final BizPrescriptionAuditLogMapper bizPrescriptionAuditLogMapper;
    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;
    private final BizMedicalRecordLogMapper bizMedicalRecordLogMapper;
    private final DeptScopeService deptScopeService;
    private final ChargeFacadeService chargeFacadeService;
    private final com.his.spi.service.QrPayFacadeService qrPayFacadeService;
    private final PatientFacadeService patientFacadeService;
    private final RegistStateFacadeService registStateFacadeService;
    private final DrugMasterFacadeService drugMasterFacadeService;
    private final EmrSignatureService emrSignatureService;
    private final RedisSequenceService redisSequenceService;
    private final QueueFacadeService queueFacadeService;
    private final BizMedicalRecordArchiveMapper bizMedicalRecordArchiveMapper;
    private final QualityControlService qualityControlService;
    private final BizFollowupTaskMapper bizFollowupTaskMapper;
    private final BizChronicRecordMapper bizChronicRecordMapper;
    private final MedicalTechFacadeService medicalTechFacadeService;
    private final AiDraftDiffService aiDraftDiffService;
    private final com.his.spi.service.StockFacadeService stockFacadeService;
    private final com.his.spi.service.AntibioticFacadeService antibioticFacadeService;
    private final SignCertFacadeService signCertFacadeService;
    private final SystemFacadeService systemFacadeService;
    private final SysMessageService sysMessageService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 引导单「取药流程」的判据：本次应收里有没有药品类项目（2-西药 3-中成药 4-中药饮片）。
     */
    private static boolean isDrugItemType(Integer itemType) {
        return Objects.equals(PaymentItemTypeEnum.WESTERN_MEDICINE.getCode(), itemType)
                || Objects.equals(PaymentItemTypeEnum.CHINESE_PATENT_MEDICINE.getCode(), itemType)
                || Objects.equals(PaymentItemTypeEnum.CHINESE_HERBAL_MEDICINE.getCode(), itemType);
    }

    @Override
    public List<BizMedicalRecordVO> getByPatientId(MedicalRecordQueryDTO queryDTO) {
        // 工作站接口，患者 token 同样调得到：不校验就是「改个 patientId 读别人病历」
        if (patientFacadeService.patientScopeViolated(queryDTO.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的病历");
        }
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMedicalRecord::getPatientId, queryDTO.getPatientId())
                .eq(Objects.nonNull(queryDTO.getRecordStatus()), BizMedicalRecord::getRecordStatus, queryDTO.getRecordStatus())
                .orderByDesc(BizMedicalRecord::getCreateTime);
        return toVoList(this.list(wrapper));
    }

    @Override
    public BizMedicalRecordVO getByRegistId(Long registId) {
        if (registId == null) {
            return new BizMedicalRecordVO();
        }
        // 历史数据里同一个 registId 可能有多份病历（早期「保存不传 recordId 就再插一份」造成的）。
        // getOne(wrapper) 在命中多行时会抛 TooManyResultsException → 整个 500，
        // 医生站一选中患者就崩、既往/处方/检查全不加载，而且拿不到 recordId 还会继续堆新病历。
        // 这里取最新一份，不抛异常；真正的收敛在 saveMedicalRecord 的按 registId 续写。
        BizMedicalRecord one = latestByRegistId(registId);
        BizMedicalRecordVO vo;
        if (Objects.isNull(one)) {
            vo = new BizMedicalRecordVO();
        } else {
            vo = BeanUtil.copyProperties(one, BizMedicalRecordVO.class);
            if (patientFacadeService.patientScopeViolated(vo.getPatientId())) {
                throw new BusinessException("无权查看该病历");
            }
        }
        return vo;
    }

    /**
     * 取某次就诊最新一份病历；同 registId 有多份时取 id 最大（最新）的。
     */
    private BizMedicalRecord latestByRegistId(Long registId) {
        if (registId == null) {
            return null;
        }
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMedicalRecord::getRegistId, registId)
                .orderByDesc(BizMedicalRecord::getId)
                .last("LIMIT 1");
        return this.getOne(wrapper, false);
    }

    private List<BizMedicalRecordVO> toVoList(List<BizMedicalRecord> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    private BizMedicalRecordVO toVo(BizMedicalRecord record) {
        BizMedicalRecordVO vo = new BizMedicalRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    @Override
    public PageResult<BizMedicalRecordVO> listPage(MedicalRecordPageQueryDTO queryDTO) {
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        // 科室数据权限收口（M6）：先越权校验显式 deptId，再按授权科室集合收敛（不传时不再等于看全院）。
        Long scopedDeptId = deptScopeService.resolveDeptId(queryDTO.getDeptId());
        if (scopedDeptId != null) {
            queryDTO.setDeptId(scopedDeptId);
        }
        wrapper.eq(queryDTO.getPatientId() != null, BizMedicalRecord::getPatientId, queryDTO.getPatientId())
                .eq(queryDTO.getDoctorId() != null, BizMedicalRecord::getDoctorId, queryDTO.getDoctorId())
                .eq(queryDTO.getDeptId() != null, BizMedicalRecord::getDeptId, queryDTO.getDeptId())
                .in(deptScopeService.isScoped() && scopedDeptId == null,
                        BizMedicalRecord::getDeptId, deptScopeService.allowedDeptIds())
                .eq(queryDTO.getVisitDate() != null, BizMedicalRecord::getVisitDate, queryDTO.getVisitDate())
                .ge(queryDTO.getVisitDateStart() != null, BizMedicalRecord::getVisitDate, queryDTO.getVisitDateStart())
                .le(queryDTO.getVisitDateEnd() != null, BizMedicalRecord::getVisitDate, queryDTO.getVisitDateEnd())
                .eq(queryDTO.getRecordStatus() != null, BizMedicalRecord::getRecordStatus, queryDTO.getRecordStatus())
                .eq(queryDTO.getReviewStatus() != null, BizMedicalRecord::getReviewStatus, queryDTO.getReviewStatus())
                .and(TextUtil.hasText(queryDTO.getKeyword()), w -> w
                        .like(BizMedicalRecord::getPatientName, queryDTO.getKeyword())
                        .or().like(BizMedicalRecord::getRecordNo, queryDTO.getKeyword())
                        .or().like(BizMedicalRecord::getRegistNo, queryDTO.getKeyword()))
                .orderByDesc(BizMedicalRecord::getSubmitTime)
                .orderByDesc(BizMedicalRecord::getCreateTime)
                // 必须补唯一二级键：submit_time / create_time 都是 DATETIME(0)（秒精度），
                // 同秒写入的多行在 MySQL 里顺序不保证稳定 → 翻页会重复行 + 丢行，且**不报错**
                .orderByDesc(BizMedicalRecord::getId);

        Page<BizMedicalRecord> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), toVoList(page.getRecords()));
    }

    @Override
    public EmrRecordDetailVO getRecordDetail(Long recordId) {
        BizMedicalRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        if (patientFacadeService.patientScopeViolated(record.getPatientId())) {
            throw new BusinessException("无权查看该病历");
        }

        // 查询处方列表
        LambdaQueryWrapper<BizPrescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.eq(BizPrescription::getRecordId, recordId);
        List<BizPrescription> prescriptions = bizPrescriptionMapper.selectList(prescriptionWrapper);

        // 查询检查申请列表
        LambdaQueryWrapper<BizInspectionApply> inspectionWrapper = new LambdaQueryWrapper<>();
        inspectionWrapper.eq(BizInspectionApply::getRecordId, recordId);
        List<BizInspectionApply> inspectionApplies = bizInspectionApplyMapper.selectList(inspectionWrapper);

        // 查询检验申请列表
        LambdaQueryWrapper<BizLaboratoryApply> laboratoryWrapper = new LambdaQueryWrapper<>();
        laboratoryWrapper.eq(BizLaboratoryApply::getRecordId, recordId);
        List<BizLaboratoryApply> laboratoryApplies = bizLaboratoryApplyMapper.selectList(laboratoryWrapper);

        EmrRecordDetailVO vo = new EmrRecordDetailVO();
        vo.setRecord(toRecordVO(record));
        vo.setPrescriptions(toPrescriptionVOList(prescriptions));
        vo.setInspectionApplies(toInspectionApplyVOList(inspectionApplies));
        vo.setLaboratoryApplies(toLaboratoryApplyVOList(laboratoryApplies));
        return vo;
    }

    /**
     * 病历实体转患者端 VO：字段整体搬运，但院内质控列（审核人/审核意见/签名锚点/引导单路径）
     * 在 MyMedicalRecordVO 里根本不存在，所以不可能被顺手带出去。
     */
    private MyMedicalRecordVO toMyRecordVO(BizMedicalRecord record) {
        MyMedicalRecordVO vo = new MyMedicalRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setRecordStatusText(RecordStatusEnum.getLabel(record.getRecordStatus()));
        return vo;
    }

    @Override
    public List<MyMedicalRecordVO> myRecords(Long patientId) {
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMedicalRecord::getPatientId, patientId)
                .in(BizMedicalRecord::getRecordStatus,
                        RecordStatusEnum.SUBMITTED.getCode(), RecordStatusEnum.ARCHIVED.getCode())
                .orderByDesc(BizMedicalRecord::getVisitDate);
        return this.list(wrapper).stream().map(this::toMyRecordVO).toList();
    }

    @Override
    public MyMedicalRecordVO myRecordDetail(Long patientId, Long recordId) {
        BizMedicalRecord record = this.getById(recordId);
        // 不区分「不存在」与「不是你的」—— 否则这个接口变成「病历号是否存在」的探针
        if (record == null || !patientId.equals(record.getPatientId())
                || record.getRecordStatus() == null
                || record.getRecordStatus() == RecordStatusEnum.DRAFT.getCode()
                || record.getRecordStatus() == RecordStatusEnum.VOIDED.getCode()) {
            throw new BusinessException("病历不存在");
        }
        return toMyRecordVO(record);
    }

    /**
     * 病历实体转 VO
     */
    private BizMedicalRecordVO toRecordVO(BizMedicalRecord record) {
        if (record == null) {
            return null;
        }
        BizMedicalRecordVO vo = new BizMedicalRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    /**
     * 处方实体列表转 VO 列表（含处方明细）
     */
    private List<BizPrescriptionVO> toPrescriptionVOList(List<BizPrescription> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizPrescriptionVO vo = new BizPrescriptionVO();
            BeanUtils.copyProperties(entity, vo);
            if (entity.getDetails() != null) {
                vo.setDetails(entity.getDetails().stream().map(detail -> {
                    BizPrescriptionDetailVO detailVO = new BizPrescriptionDetailVO();
                    BeanUtils.copyProperties(detail, detailVO);
                    return detailVO;
                }).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 检查申请实体列表转 VO 列表
     */
    private List<BizInspectionApplyVO> toInspectionApplyVOList(List<BizInspectionApply> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizInspectionApplyVO vo = new BizInspectionApplyVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 检验申请实体列表转 VO 列表
     */
    private List<BizLaboratoryApplyVO> toLaboratoryApplyVOList(List<BizLaboratoryApply> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizLaboratoryApplyVO vo = new BizLaboratoryApplyVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRecord(Long recordId, boolean approved, String remark, String reviewerName) {
        BizMedicalRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        if (record.getReviewStatus() != null && record.getReviewStatus() != 1) {
            throw new BusinessException("当前状态不允许审核");
        }
        record.setReviewStatus(approved ? 2 : 3); // 2-通过 3-驳回
        record.setReviewBy(reviewerName);
        record.setReviewTime(LocalDateTime.now());
        record.setReviewRemark(remark);
        String op = approved ? "审核通过" : "审核驳回";
        addRecordLog(recordId, record.getRecordNo(), op, null, null, remark);
        return this.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPrescription(BizPrescription prescription, String pin) {
        // 抗菌药物处方权闸：必须在落库之前 —— 开出去再拦，处方号已经生成、库存已经锁定。
        // 目录外药品由端口内部放过，临床侧只把本单全部药品ID上交。
        List<Long> drugIds = prescription.getDetails() == null ? Collections.emptyList()
                : prescription.getDetails().stream()
                .map(BizPrescriptionDetail::getDrugId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        antibioticFacadeService.assertCanPrescribe(prescription.getDoctorId(), drugIds);

        prescription.setPrescriptionNo(redisSequenceService.generatePrescriptionNo());
        prescription.setPrescriptionStatus(PrescriptionStatusEnum.DRAFT.getCode());

        // 保存处方主表
        this.bizPrescriptionMapper.insert(prescription);

        // 保存处方明细并锁定库存
        if (prescription.getDetails() != null && !prescription.getDetails().isEmpty()) {
            for (BizPrescriptionDetail detail : prescription.getDetails()) {
                detail.setPrescriptionId(prescription.getId());
                detail.setPrescriptionNo(prescription.getPrescriptionNo());
                detail.setDetailStatus(PrescriptionDetailStatusEnum.NORMAL.getCode());
                this.bizPrescriptionDetailMapper.insert(detail);
                // 开方锁库：跨批次把可用量转锁定量；该药品无批次时端口内部放过，硬闸门在发药扣库
                stockFacadeService.lockStockByDrug(detail.getDrugId(), detail.getQuantity());
            }
        }

        // 开方签名：必须在明细落库之后 —— 被签内容包括处方明细，先签会签出一份"没有药"的处方
        signPrescription(prescription, pin);

        return true;
    }

    /**
     * 开方医师签名（场景 RX_CREATE）。
     *
     * <p>签名失败即抛出让整个开方事务回滚：**一张"开出去了但没有医师签名"的处方
     * 比一张开不出去的处方更危险** —— 它会安静地流到发药窗口。
     *
     * @param pin 签名人本次输入的 PIN（软签二次确认）
     */
    private void signPrescription(BizPrescription p, String pin) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizTypeEnum.PRESCRIPTION.getCode());
        cmd.setBizId(p.getId());
        cmd.setSignScene(SignSceneEnum.RX_CREATE.getCode());
        cmd.setSignerId(p.getDoctorId());
        cmd.setSignerName(p.getDoctorName());
        cmd.setSignerDeptId(p.getDeptId());
        cmd.setSignerDeptName(p.getDeptName());
        cmd.setPin(pin);
        try {
            SignatureVO sig = emrSignatureService.sign(cmd);
            // 同步回内存实体：本方法之后若还有 updateById(prescription)，旧值会把锚点覆盖掉
            p.setDoctorSignId(sig.getId());
            p.setDoctorSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("处方 " + p.getPrescriptionNo() + " 开方签名失败：" + e.getMessage());
        }
    }

    /**
     * 校验开方医师手写笔迹（PNG dataURL）。空值放行（老前端/未手写）；
     * 非空必须是 PNG dataURL 且不超上限，否则整单回滚，不静默落库脏图。
     */
    private String validateDoctorSignature(String signature) {
        if (!TextUtil.hasText(signature)) {
            return null;
        }
        String trimmed = signature.trim();
        if (!trimmed.startsWith("data:image/png;base64,")) {
            throw new BusinessException("医师手写签名格式不合法（需 PNG 手写板数据）");
        }
        if (trimmed.length() > RX_SIGNATURE_MAX) {
            throw new BusinessException("医师手写签名图片过大，请清空画板后重新签名");
        }
        return trimmed;
    }

    /**
     * 结诊提交时，签本次就诊全部处方的**开方医师名**（P5.6）。
     *
     * <p><b>为什么不放在处方保存的那一刻</b>：病历每次保存都是"先删处方、再逐条重建"，
     * 草稿阶段医生反复改方，处方会被反复删除重建 —— 在那里签名只会签出一堆
     * 立刻被删掉的证据（签名记录还在、对象已经没了，验签全是"对象不存在"）。
     * 只有结诊提交这一刻，处方内容才真正定稿。
     *
     * <p>只签医师签名ID 为空的行：签名服务自己也会拒绝重复签名，
     * 但在这里先滤掉可以让"已签的处方"不参与，避免整批回滚。
     *
     * @param pin 签名人本次输入的 PIN（软签二次确认）
     */
    private void signPrescriptionsOfRecord(BizMedicalRecord r, String pin) {
        List<BizPrescription> list = bizPrescriptionMapper.selectList(
                new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getRecordId, r.getId())
                        .isNull(BizPrescription::getDoctorSignId)
                        .orderByAsc(BizPrescription::getId));
        for (BizPrescription p : list) {
            signPrescription(p, pin);
        }
    }

    @Override
    public BizPrescription getPrescriptionDetail(Long prescriptionId) {
        BizPrescription prescription = bizPrescriptionMapper.selectById(prescriptionId);
        if (prescription != null) {
            LambdaQueryWrapper<BizPrescriptionDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId);
            List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(wrapper);
            prescription.setDetails(details);
        }
        return prescription;
    }

    @Override
    public List<BizPrescription> selectPrescriptionList(Long patientId, Long doctorId, Integer prescriptionStatus) {
        LambdaQueryWrapper<BizPrescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizPrescription::getPatientId, patientId)
                .eq(doctorId != null, BizPrescription::getDoctorId, doctorId)
                .eq(prescriptionStatus != null, BizPrescription::getPrescriptionStatus, prescriptionStatus)
                .orderByDesc(BizPrescription::getCreateTime);
        return bizPrescriptionMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addInspectionApply(BizInspectionApply apply) {
        apply.setApplyNo(redisSequenceService.generateInspectionApplyNo());
        apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
        apply.setSubmitTime(LocalDateTime.now());
        return bizInspectionApplyMapper.insert(apply) > 0;
        // 注意：检查记录在患者缴费后才创建（由 ChargeController.processPayment 处理）
    }

    @Override
    public List<BizInspectionApply> selectInspectionApplyList(Long patientId, Long doctorId) {
        LambdaQueryWrapper<BizInspectionApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizInspectionApply::getPatientId, patientId)
                .eq(doctorId != null, BizInspectionApply::getDoctorId, doctorId)
                .orderByDesc(BizInspectionApply::getCreateTime);
        return bizInspectionApplyMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addLaboratoryApply(BizLaboratoryApply apply) {
        apply.setApplyNo(redisSequenceService.generateLaboratoryApplyNo());
        apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode()); // 1-已提交（未缴费）
        apply.setSubmitTime(LocalDateTime.now());
        return bizLaboratoryApplyMapper.insert(apply) > 0;
        // 注意：检验记录在患者缴费后才创建（由 ChargeController.processPayment 处理）
    }

    @Override
    public List<BizLaboratoryApply> selectLaboratoryApplyList(Long patientId, Long doctorId) {
        LambdaQueryWrapper<BizLaboratoryApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizLaboratoryApply::getPatientId, patientId)
                .eq(doctorId != null, BizLaboratoryApply::getDoctorId, doctorId)
                .orderByDesc(BizLaboratoryApply::getCreateTime);
        return bizLaboratoryApplyMapper.selectList(wrapper);
    }

    /**
     * 记录病历修改日志
     */
    private void logRecordChanges(BizMedicalRecord oldRecord, BizMedicalRecord newRecord) {
        String[] fields = {"chiefComplaint", "presentIllness", "pastHistory", "personalHistory",
                "familyHistory", "allergyHistory", "temperature", "pulse", "respiration",
                "systolicPressure", "diastolicPressure", "generalCondition", "specialistExam",
                "auxiliaryExam", "diagnosis", "diagnosisCode", "diagnosisName", "treatmentPlan"};
        String[] labels = {"主诉", "现病史", "既往史", "个人史", "家族史", "过敏史",
                "体温", "脉搏", "呼吸", "收缩压", "舒张压", "一般情况",
                "专科检查", "辅助检查", "诊断", "诊断编码", "诊断名称", "治疗方案"};

        for (int i = 0; i < fields.length; i++) {
            try {
                var oldVal = getFieldValue(oldRecord, fields[i]);
                var newVal = getFieldValue(newRecord, fields[i]);
                String oldStr = oldVal != null ? String.valueOf(oldVal) : null;
                String newStr = newVal != null ? String.valueOf(newVal) : null;
                if (!Objects.equals(oldStr, newStr)) {
                    BizMedicalRecordLog log = new BizMedicalRecordLog();
                    log.setRecordId(newRecord.getId());
                    log.setRecordNo(newRecord.getRecordNo());
                    log.setOperation("修改病历");
                    log.setFieldName(labels[i]);
                    log.setOldValue(oldStr);
                    log.setNewValue(newStr);
                    bizMedicalRecordLogMapper.insert(log);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Object getFieldValue(BizMedicalRecord record, String fieldName) {
        try {
            var field = BizMedicalRecord.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(record);
        } catch (Exception e) {
            return null;
        }
    }

    private void addRecordLog(Long recordId, String recordNo, String operation,
                              String fieldName, String oldValue, String newValue) {
        BizMedicalRecordLog log = new BizMedicalRecordLog();
        log.setRecordId(recordId);
        log.setRecordNo(recordNo);
        log.setOperation(operation);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        bizMedicalRecordLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePrescriptionsByRecordId(Long recordId) {
        if (recordId == null) return false;
        // 删除处方明细并解锁库存
        LambdaQueryWrapper<BizPrescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.eq(BizPrescription::getRecordId, recordId);
        List<BizPrescription> prescriptions = bizPrescriptionMapper.selectList(prescriptionWrapper);
        for (BizPrescription prescription : prescriptions) {
            // 查询处方明细
            LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId());
            List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(detailWrapper);

            // 删方前解锁：按明细把锁定量退回可用量，只退实际锁着的部分
            for (BizPrescriptionDetail detail : details) {
                stockFacadeService.unlockStockByDrug(detail.getDrugId(), detail.getQuantity());
            }

            // 删除处方明细
            bizPrescriptionDetailMapper.delete(detailWrapper);
        }
        // 删除处方主表
        return bizPrescriptionMapper.delete(prescriptionWrapper) >= 0;
    }

    // 检查/检验申请单（批次E：开单即落库 + 删除保护 + 结果回显）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInspectionAppliesByRecordId(Long recordId) {
        if (recordId == null) return false;
        // 批次E/E2：按 recordId 全删是个危险动作（会把已缴费、已出报告的申请单一起抹掉），
        // 现在逐条走带保护的 deleteInspectionApply —— 任何一个删不掉就整批失败并说明原因。
        // 注意：saveMedicalRecord 已经不再调用本方法（申请单改为「开单即落库 + 结诊回填」）。
        List<BizInspectionApply> list = bizInspectionApplyMapper.selectList(new LambdaQueryWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getRecordId, recordId));
        for (BizInspectionApply apply : list) {
            deleteInspectionApply(apply.getId());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLaboratoryAppliesByRecordId(Long recordId) {
        if (recordId == null) return false;
        List<BizLaboratoryApply> list = bizLaboratoryApplyMapper.selectList(new LambdaQueryWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getRecordId, recordId));
        for (BizLaboratoryApply apply : list) {
            deleteLaboratoryApply(apply.getId());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInspectionApplyVO upsertInspectionApply(InspectionApplyUpsertDTO dto, String pin) {
        RegistStateRowVO appoint = requireAppoint(dto.getRegistId());
        PatientBriefVO patient = requirePatient(dto.getPatientId());
        LabItemVO item = medicalTechFacadeService.findInspectionItem(dto.getInspectionItemId());
        if (item == null) {
            throw new BusinessException("检查项目不存在: " + dto.getInspectionItemId());
        }

        BizInspectionApply apply;
        if (dto.getId() != null) {
            apply = bizInspectionApplyMapper.selectById(dto.getId());
            if (apply == null) {
                throw new BusinessException("检查申请单不存在");
            }
            requireApplyEditable(apply.getApplyNo(), apply.getApplyStatus());
            requireApplyUnsigned(apply.getApplyNo(), apply.getSignStatus());
        } else {
            apply = new BizInspectionApply();
            apply.setApplyNo(redisSequenceService.generateInspectionApplyNo());
            apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
            apply.setSubmitTime(LocalDateTime.now());
        }

        fillInspectionApplySnapshot(apply, appoint, patient);
        apply.setInspectionItemId(item.getId());
        apply.setInspectionItemCode(item.getItemCode());
        apply.setInspectionItemName(item.getItemName());
        apply.setInspectionDeptId(item.getDeptId());
        apply.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
        apply.setBodyPart(dto.getBodyPart());
        apply.setInspectionPurpose(dto.getInspectionPurpose());
        apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
        apply.setSpecialRequirements(dto.getSpecialRequirements());
        apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
        bindRecord(apply, dto.getRecordId(), appoint.getId());

        if (apply.getId() != null) {
            bizInspectionApplyMapper.updateById(apply);
        } else {
            bizInspectionApplyMapper.insert(apply);
        }
        // 开单即签：签名失败整单回滚（与门诊病历"结诊即签"同一口径，不留"开了单没签名"的半成品）
        signInspectionApply(apply, pin);
        return toInspectionApplyVO(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizLaboratoryApplyVO upsertLaboratoryApply(LaboratoryApplyUpsertDTO dto, String pin) {
        RegistStateRowVO appoint = requireAppoint(dto.getRegistId());
        PatientBriefVO patient = requirePatient(dto.getPatientId());
        LabItemVO item = medicalTechFacadeService.findLabItem(dto.getLaboratoryItemId());
        if (item == null) {
            throw new BusinessException("检验项目不存在: " + dto.getLaboratoryItemId());
        }

        BizLaboratoryApply apply;
        if (dto.getId() != null) {
            apply = bizLaboratoryApplyMapper.selectById(dto.getId());
            if (apply == null) {
                throw new BusinessException("检验申请单不存在");
            }
            requireApplyEditable(apply.getApplyNo(), apply.getApplyStatus());
            requireApplyUnsigned(apply.getApplyNo(), apply.getSignStatus());
        } else {
            apply = new BizLaboratoryApply();
            apply.setApplyNo(redisSequenceService.generateLaboratoryApplyNo());
            apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
            apply.setSubmitTime(LocalDateTime.now());
        }

        fillLaboratoryApplySnapshot(apply, appoint, patient);
        apply.setLaboratoryItemId(item.getId());
        apply.setLaboratoryItemCode(item.getItemCode());
        apply.setLaboratoryItemName(item.getItemName());
        apply.setLaboratoryDeptId(item.getDeptId());
        apply.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
        // 标本类型：字典优先（同一个项目固定同一种标本），医生传了才用医生的，都没有给「血液」
        apply.setSpecimenType(TextUtil.hasText(item.getSpecimenType()) ? item.getSpecimenType()
                : (TextUtil.hasText(dto.getSpecimenType()) ? dto.getSpecimenType() : "血液"));
        apply.setLaboratoryPurpose(dto.getLaboratoryPurpose());
        apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
        apply.setIsFasting(dto.getIsFasting() == null ? 0 : dto.getIsFasting());
        apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
        bindRecord(apply, dto.getRecordId(), appoint.getId());

        if (apply.getId() != null) {
            bizLaboratoryApplyMapper.updateById(apply);
        } else {
            bizLaboratoryApplyMapper.insert(apply);
        }
        signLaboratoryApply(apply, pin);
        return toLaboratoryApplyVO(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInspectionApply(Long id) {
        if (id == null) {
            return false;
        }
        BizInspectionApply apply = bizInspectionApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("检查申请单不存在或已删除");
        }
        String block = inspectionDeleteBlock(apply.getId(), apply.getApplyStatus(),
                inspectionExecMap(List.of(id)).get(id), apply.getSignStatus());
        if (block != null) {
            throw new BusinessException("检查申请单 " + apply.getApplyNo() + " 不允许删除：" + block);
        }
        return bizInspectionApplyMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLaboratoryApply(Long id) {
        if (id == null) {
            return false;
        }
        BizLaboratoryApply apply = bizLaboratoryApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("检验申请单不存在或已删除");
        }
        String block = laboratoryDeleteBlock(apply.getId(), apply.getApplyStatus(),
                laboratoryExecMap(List.of(id)).get(id), apply.getSignStatus());
        if (block != null) {
            throw new BusinessException("检验申请单 " + apply.getApplyNo() + " 不允许删除：" + block);
        }
        return bizLaboratoryApplyMapper.deleteById(id) > 0;
    }

    // 申请单：内部辅助

    @Override
    public List<BizInspectionApplyVO> listInspectionApplies(Long registId) {
        if (registId == null) {
            return new ArrayList<>();
        }
        // 按**挂号**过滤而不是按患者：同一个患者这次开的检查和上次的检查必须分开，
        // 原实现只按 patientId 查，切到新患者身上还会显示上一次就诊的检查单。
        List<BizInspectionApply> list = bizInspectionApplyMapper.selectList(new LambdaQueryWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getRegistId, registId)
                .orderByDesc(BizInspectionApply::getCreateTime)
                .orderByDesc(BizInspectionApply::getId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        Map<Long, ExecStatusVO> execMap = inspectionExecMap(
                list.stream().map(BizInspectionApply::getId).collect(Collectors.toList()));
        List<BizInspectionApplyVO> vos = new ArrayList<>(list.size());
        for (BizInspectionApply apply : list) {
            vos.add(toInspectionApplyVO(apply, execMap.get(apply.getId())));
        }
        return vos;
    }

    @Override
    public List<BizLaboratoryApplyVO> listLaboratoryApplies(Long registId) {
        if (registId == null) {
            return new ArrayList<>();
        }
        List<BizLaboratoryApply> list = bizLaboratoryApplyMapper.selectList(new LambdaQueryWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getRegistId, registId)
                .orderByDesc(BizLaboratoryApply::getCreateTime)
                .orderByDesc(BizLaboratoryApply::getId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        Map<Long, ExecStatusVO> execMap = laboratoryExecMap(
                list.stream().map(BizLaboratoryApply::getId).collect(Collectors.toList()));
        List<BizLaboratoryApplyVO> vos = new ArrayList<>(list.size());
        for (BizLaboratoryApply apply : list) {
            vos.add(toLaboratoryApplyVO(apply, execMap.get(apply.getId())));
        }
        return vos;
    }

    private RegistStateRowVO requireAppoint(Long registId) {
        RegistStateRowVO appoint = getAppointBrief(registId);
        if (appoint == null) {
            throw new BusinessException("挂号记录不存在");
        }
        return appoint;
    }

    private PatientBriefVO requirePatient(Long patientId) {
        PatientBriefVO patient = patientFacadeService.findPatient(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        return patient;
    }

    private RegistStateRowVO getAppointBrief(Long registId) {
        return registId == null ? null : registStateFacadeService.getRegistRow(registId);
    }

    private Long completeVisit(Long registId, Long queueId) {
        // 结诊联动（经 SPI 委派 his-appoint）：挂号置「已完成」、队列置「已完成」并回写结束时刻
        if (registId != null && registStateFacadeService.getRegistRow(registId) != null) {
            boolean ok = registStateFacadeService.updateRegistStatus(registId,
                    AppointStatusEnum.COMPLETED.getCode(),
                    List.of(AppointStatusEnum.ACCEPTED.getCode(), AppointStatusEnum.COMPLETED.getCode()));
            if (!ok) {
                throw new BusinessException("该挂号状态已变更，请刷新后重试");
            }
        }

        if (queueId == null) {
            return null;
        }
        QueueStateRowVO queue = registStateFacadeService.getQueueRow(queueId);
        if (queue == null) {
            return null;
        }
        boolean ok = registStateFacadeService.finishQueue(queueId,
                QueueStatusEnum.COMPLETED.getCode(), LocalDateTime.now(),
                List.of(QueueStatusEnum.CONSULTING.getCode(), QueueStatusEnum.COMPLETED.getCode()));
        if (!ok) {
            throw new BusinessException("该队列状态已变更，请刷新后重试");
        }
        return queue.getDoctorId();
    }

    /**
     * 申请单可编辑闸门：只有「已提交未缴费」能改。
     * 已缴费的申请单不许改项目/价格 —— 那是已经算进收费单里的东西。
     */
    private void requireApplyEditable(String applyNo, Integer applyStatus) {
        if (!Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), applyStatus)) {
            throw new BusinessException("申请单 " + applyNo + " 当前状态为「"
                    + ApplyStatusEnum.getLabelOrUnknown(applyStatus) + "」，不允许修改；已缴费请先走退费流程");
        }
    }

    /**
     * 签名即锁定：已签名的申请单不许改内容，要改先去签名验签作废签名（作废留痕）。
     */
    private void requireApplyUnsigned(String applyNo, Integer signStatus) {
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), signStatus)) {
            throw new BusinessException("申请单 " + applyNo + " 已电子签名（签名即锁定），不允许直接修改；"
                    + "如需修改，请先在「签名验签」作废该签名，作废后再次保存会自动完成补签");
        }
    }

    /**
     * 检查申请单电子签名（业务类型=7，场景=申请开立）：模式A下既用于「结诊统一签名」的新行开单，
     * 也用于已落库行的重签（作废-补签闭环）；签名失败随本事务回滚。
     *
     * @param pin 签名人本次输入的 PIN（软签二次确认）
     */
    private void signInspectionApply(BizInspectionApply apply, String pin) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizTypeEnum.INSPECTION_APPLY.getCode());
        cmd.setBizId(apply.getId());
        cmd.setSignScene(SignSceneEnum.APPLY_CREATE.getCode());
        cmd.setSignerId(apply.getDoctorId());
        cmd.setSignerName(apply.getDoctorName());
        cmd.setSignerDeptId(apply.getDeptId());
        cmd.setSignerDeptName(apply.getDeptName());
        cmd.setPin(pin);
        try {
            SignatureVO sig = emrSignatureService.sign(cmd);
            apply.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
            apply.setSignId(sig.getId());
            apply.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("检查申请开单失败（" + apply.getApplyNo() + "）：" + e.getMessage());
        }
    }

    /**
     * 检验申请单电子签名（业务类型=8，场景=申请开立），规则同 {@link #signInspectionApply}。
     *
     * @param pin 签名人本次输入的 PIN（软签二次确认）
     */
    private void signLaboratoryApply(BizLaboratoryApply apply, String pin) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizTypeEnum.LAB_APPLY.getCode());
        cmd.setBizId(apply.getId());
        cmd.setSignScene(SignSceneEnum.APPLY_CREATE.getCode());
        cmd.setSignerId(apply.getDoctorId());
        cmd.setSignerName(apply.getDoctorName());
        cmd.setSignerDeptId(apply.getDeptId());
        cmd.setSignerDeptName(apply.getDeptName());
        cmd.setPin(pin);
        try {
            SignatureVO sig = emrSignatureService.sign(cmd);
            apply.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
            apply.setSignId(sig.getId());
            apply.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("检验申请开单失败（" + apply.getApplyNo() + "）：" + e.getMessage());
        }
    }

    /**
     * 患者 / 科室 / 医生快照 —— 申请单是独立单据，必须自带这些信息，不能靠关联查。
     */
    private void fillInspectionApplySnapshot(BizInspectionApply apply, RegistStateRowVO appoint, PatientBriefVO patient) {
        apply.setRegistId(appoint.getId());
        apply.setPatientId(patient.getId());
        apply.setPatientNo(patient.getPatientNo());
        apply.setPatientName(patient.getPatientName());
        apply.setGender(patient.getGender());
        apply.setAge(patient.getAge());
        apply.setVisitDate(visitDateOf(appoint));
        apply.setDeptId(appoint.getDeptId());
        apply.setDeptName(appoint.getDeptName());
        apply.setDoctorId(doctorIdOf(appoint));
        apply.setDoctorName(doctorNameOf(appoint));
    }

    private void fillLaboratoryApplySnapshot(BizLaboratoryApply apply, RegistStateRowVO appoint, PatientBriefVO patient) {
        apply.setRegistId(appoint.getId());
        apply.setPatientId(patient.getId());
        apply.setPatientNo(patient.getPatientNo());
        apply.setPatientName(patient.getPatientName());
        apply.setGender(patient.getGender());
        apply.setAge(patient.getAge());
        apply.setVisitDate(visitDateOf(appoint));
        apply.setDeptId(appoint.getDeptId());
        apply.setDeptName(appoint.getDeptName());
        apply.setDoctorId(doctorIdOf(appoint));
        apply.setDoctorName(doctorNameOf(appoint));
    }

    /**
     * 就诊日期：以挂号记录上的就诊日期为准，取不到才退回今天。
     * <p>不要无脑写 {@code LocalDate.now()} —— 补开单时（今天给 9 月 16 日的挂号补一张检查）
     * 会把申请单的就诊日期写成今天，跟挂号/病历对不上。
     */
    private LocalDate visitDateOf(RegistStateRowVO appoint) {
        return appoint.getVisitDate() != null ? appoint.getVisitDate() : LocalDate.now();
    }

    /**
     * 开单医生：优先取当前登录医生（开单这件事是"我"做的），
     * 取不到再退回挂号记录的医生；两者都空则取 0 触发 NOT NULL 约束暴露问题，
     * 而不是静默写个 0 看起来像正常数据。
     */
    private Long doctorIdOf(RegistStateRowVO appoint) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user != null && user.getEmployeeId() != null) {
            return user.getEmployeeId();
        }
        return appoint.getDoctorId();
    }

    private String doctorNameOf(RegistStateRowVO appoint) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user != null && user.getRealName() != null) {
            return user.getRealName();
        }
        return appoint.getDoctorName();
    }

    /**
     * 绑定病历：医生传了 recordId 就用它，否则用本次就诊已有病历（刚挂号就开单时病历还不存在）。
     * 结诊时 {@code saveMedicalRecord} 会把剩下的空 recordId 统一回填。
     */
    private void bindRecord(BizInspectionApply apply, Long dtoRecordId, Long registId) {
        Long recordId = dtoRecordId != null ? dtoRecordId : latestRecordId(registId);
        if (recordId != null) {
            BizMedicalRecord record = this.getById(recordId);
            if (record != null) {
                apply.setRecordId(record.getId());
                apply.setRecordNo(record.getRecordNo());
            }
        }
    }

    private void bindRecord(BizLaboratoryApply apply, Long dtoRecordId, Long registId) {
        Long recordId = dtoRecordId != null ? dtoRecordId : latestRecordId(registId);
        if (recordId != null) {
            BizMedicalRecord record = this.getById(recordId);
            if (record != null) {
                apply.setRecordId(record.getId());
                apply.setRecordNo(record.getRecordNo());
            }
        }
    }

    private Long latestRecordId(Long registId) {
        BizMedicalRecord record = latestByRegistId(registId);
        return record == null ? null : record.getId();
    }

    private BizInspectionApplyVO toInspectionApplyVO(BizInspectionApply apply) {
        return toInspectionApplyVO(apply,
                inspectionExecMap(Collections.singletonList(apply.getId())).get(apply.getId()));
    }

    private BizLaboratoryApplyVO toLaboratoryApplyVO(BizLaboratoryApply apply) {
        return toLaboratoryApplyVO(apply,
                laboratoryExecMap(Collections.singletonList(apply.getId())).get(apply.getId()));
    }

    private BizInspectionApplyVO toInspectionApplyVO(BizInspectionApply apply,
                                                     ExecStatusVO exec) {
        BizInspectionApplyVO vo = new BizInspectionApplyVO();
        BeanUtils.copyProperties(apply, vo);
        String text;
        if (Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), apply.getApplyStatus())) {
            text = "待缴费";
        } else if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), apply.getApplyStatus())) {
            text = "已取消";
        } else if (exec != null && TextUtil.hasText(exec.getExecStatusText())) {
            text = exec.getExecStatusText();
        } else {
            text = "已缴费待执行";
        }
        vo.setExecStatusText(text);
        if (exec != null) {
            vo.setExecRecordId(exec.getExecRecordId());
            vo.setExecStatus(exec.getExecStatus());
            vo.setCritical(Boolean.TRUE.equals(exec.getCritical()));
        } else {
            vo.setCritical(false);
        }
        String block = inspectionDeleteBlock(apply.getId(), apply.getApplyStatus(), exec, apply.getSignStatus());
        vo.setCanDelete(block == null);
        vo.setDeleteBlockReason(block);
        return vo;
    }

    private BizLaboratoryApplyVO toLaboratoryApplyVO(BizLaboratoryApply apply,
                                                     ExecStatusVO exec) {
        BizLaboratoryApplyVO vo = new BizLaboratoryApplyVO();
        BeanUtils.copyProperties(apply, vo);
        String text;
        if (Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), apply.getApplyStatus())) {
            text = "待缴费";
        } else if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), apply.getApplyStatus())) {
            text = "已取消";
        } else if (exec != null && TextUtil.hasText(exec.getExecStatusText())) {
            text = exec.getExecStatusText();
        } else {
            text = "已缴费待执行";
        }
        vo.setExecStatusText(text);
        if (exec != null) {
            vo.setExecRecordId(exec.getExecRecordId());
            vo.setExecStatus(exec.getExecStatus());
            vo.setCritical(Boolean.TRUE.equals(exec.getCritical()));
        } else {
            vo.setCritical(false);
        }
        String block = laboratoryDeleteBlock(apply.getId(), apply.getApplyStatus(), exec, apply.getSignStatus());
        vo.setCanDelete(block == null);
        vo.setDeleteBlockReason(block);
        return vo;
    }

    /**
     * 申请单删除闸门（检查）。返回 null = 可删；否则返回不允许删除的原因。
     *
     * <p>三道闸：① 只有「已提交未缴费」能删（已缴费走退费）；② 已有<b>在跑</b>的执行记录不能删
     * （检查科手上那条工作项会指不到申请单）；③ 已被收费单明细引用不能删
     * （哪怕还没缴费，收费单上那条明细的 source_id 也会悬空）。
     *
     * <p>注意 ② 的口径是「在跑」：退费会在执行记录上留一条**已取消**的墓碑
     * （再次缴费会复活它），如果把墓碑也算作占用，医生退费后就再也删不掉这张单了 —— 死角。
     * 墓碑不占用，检查科那边本来也不该再处理它。
     */
    private String inspectionDeleteBlock(Long applyId, Integer applyStatus,
                                         ExecStatusVO exec, Integer signStatus) {
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), signStatus)) {
            return "该申请单已电子签名（签名即锁定），请先在「签名验签」作废签名";
        }
        if (!Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), applyStatus)) {
            return Objects.equals(ApplyStatusEnum.PAID.getCode(), applyStatus)
                    ? "该申请单已缴费，请先走退费流程"
                    : "该申请单已取消，不允许删除";
        }
        if (exec != null && exec.getExecRecordId() != null && !isCancelledInspection(exec.getExecStatus())) {
            return "该申请单已生成检查记录（" + exec.getExecStatusText() + "），不允许删除";
        }
        if (feeBookedExists(FeeSourceTypeEnum.EXAM_APPLY.getCode(), applyId)) {
            return "该申请单已产生费用记账，请先红冲该笔费用";
        }
        return null;
    }

    private String laboratoryDeleteBlock(Long applyId, Integer applyStatus,
                                         ExecStatusVO exec, Integer signStatus) {
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), signStatus)) {
            return "该申请单已电子签名（签名即锁定），请先在「签名验签」作废签名";
        }
        if (!Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), applyStatus)) {
            return Objects.equals(ApplyStatusEnum.PAID.getCode(), applyStatus)
                    ? "该申请单已缴费，请先走退费流程"
                    : "该申请单已取消，不允许删除";
        }
        if (exec != null && exec.getExecRecordId() != null && !isCancelledLaboratory(exec.getExecStatus())) {
            return "该申请单已生成检验记录（" + exec.getExecStatusText() + "），不允许删除";
        }
        if (feeBookedExists(FeeSourceTypeEnum.LAB_APPLY.getCode(), applyId)) {
            return "该申请单已产生费用记账，请先红冲该笔费用";
        }
        return null;
    }

    /**
     * 检查记录「已取消(7)」——退费留下的墓碑，不占用申请单。
     */
    private boolean isCancelledInspection(Integer recordStatus) {
        return recordStatus != null && recordStatus == 7;
    }

    /**
     * 检验记录「已取消(8)」——与检查不是同一套码表，别合并判断。
     */
    private boolean isCancelledLaboratory(Integer recordStatus) {
        return recordStatus != null && recordStatus == 8;
    }

    /**
     * 是否已产生费用记账行。查询失败按"已记账"处理 —— 宁可拒删。
     */
    private boolean feeBookedExists(Integer sourceType, Long sourceId) {
        try {
            return chargeFacadeService.findBookedBySource(sourceType, sourceId, null) != null;
        } catch (Exception e) {
            log.warn("[申请单] 记账行引用校验失败，按「已记账」保守处理，sourceType={} sourceId={}", sourceType, sourceId, e);
            return true;
        }
    }

    /**
     * 项目类型码 → 引导单上的费用名（字典 his_charge_item_type，权威在 PaymentItemTypeEnum）。
     */
    private String feeItemTypeName(Integer itemType) {
        PaymentItemTypeEnum typeEnum = PaymentItemTypeEnum.fromCode(itemType);
        return (typeEnum == null ? "其他" : typeEnum.getDesc()) + "费";
    }

    private Map<Long, ExecStatusVO> inspectionExecMap(List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyMap();
        }
        try {
            List<ExecStatusVO> list = medicalTechFacadeService.listInspectionExecStatus(applyIds);
            return indexExecStatus(list);
        } catch (Exception e) {
            log.warn("[申请单] 查询检查执行进度失败，降级为不显示进度", e);
            return Collections.emptyMap();
        }
    }

    private Map<Long, ExecStatusVO> laboratoryExecMap(List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyMap();
        }
        try {
            List<ExecStatusVO> list = medicalTechFacadeService.listLaboratoryExecStatus(applyIds);
            return indexExecStatus(list);
        } catch (Exception e) {
            log.warn("[申请单] 查询检验执行进度失败，降级为不显示进度", e);
            return Collections.emptyMap();
        }
    }

    private Map<Long, ExecStatusVO> indexExecStatus(
            List<ExecStatusVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<Long, ExecStatusVO> map = new HashMap<>();
        for (ExecStatusVO s : list) {
            if (s != null && s.getApplyId() != null) {
                map.put(s.getApplyId(), s);
            }
        }
        return map;
    }

    /**
     * 结诊回填：把本次就诊下所有还没挂病历的申请单补上 recordId / recordNo。
     * 加 `isNull(recordId)` 条件是为了不动历史就诊的申请单。
     */
    private void backfillApplyRecord(Long registId, Long recordId, String recordNo) {
        if (registId == null || recordId == null) {
            return;
        }
        bizInspectionApplyMapper.update(null, new LambdaUpdateWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getRegistId, registId)
                .isNull(BizInspectionApply::getRecordId)
                .set(BizInspectionApply::getRecordId, recordId)
                .set(BizInspectionApply::getRecordNo, recordNo));
        bizLaboratoryApplyMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getRegistId, registId)
                .isNull(BizLaboratoryApply::getRecordId)
                .set(BizLaboratoryApply::getRecordId, recordId)
                .set(BizLaboratoryApply::getRecordNo, recordNo));
    }

    /**
     * 同步检查申请（模式A：申请单随病历保存落库，编辑与落库解耦）。
     *
     * <p>医生工作台内的编辑（选项目/改部位/改目的/套模板）只停留在本地数组，
     * 「保存病历 / 结诊」时申请单数组才随病历一起提交到这里：
     * <ul>
     *   <li>无 id 的新行：按 (挂号 + 项目) 幂等判重后插入（防止重复开单）；</li>
     *   <li>有 id 的已落库行：仅当「已提交(1) 且 未签名」时更新内容——已缴费/已取消/已签名行
     *       跳过并记日志（这些行前端已禁编辑，内容以库为准；单行状态不能阻断整个病历保存）；</li>
     *   <li>结诊提交（isSubmit=true）：对同步后的申请单统一电子签名（内容最终确认，签名即锁定）。</li>
     * </ul>
     *
     * <p>刻意不做「先删后增」：申请单不是病历的附属物，重建会把已缴费的单子删掉、
     * 让执行记录的 apply_id 悬空。
     */
    private void ensureInspectionApplies(Long registId, RegistStateRowVO appoint, PatientBriefVO patient,
                                         List<MedicalRecordSaveDTO.InspectionApplyDTO> dtos, boolean isSubmit,
                                         String pin) {
        if (registId == null || CollectionUtils.isEmpty(dtos)) {
            return;
        }
        for (MedicalRecordSaveDTO.InspectionApplyDTO dto : dtos) {
            if (dto.getItemId() == null) {
                continue;
            }
            BizInspectionApply apply;
            if (dto.getId() != null) {
                apply = bizInspectionApplyMapper.selectById(dto.getId());
                if (apply == null) {
                    throw new BusinessException("检查申请单不存在或已删除（id=" + dto.getId() + "）");
                }
                if (!Objects.equals(registId, apply.getRegistId())) {
                    throw new BusinessException("检查申请单不属于本次就诊（id=" + dto.getId() + "）");
                }
                if (!Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), apply.getApplyStatus())) {
                    log.warn("[病历保存] 检查申请单 {} 状态为「{}」，跳过更新（内容以库为准）",
                            apply.getApplyNo(), ApplyStatusEnum.getLabelOrUnknown(apply.getApplyStatus()));
                    continue;
                }
                if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), apply.getSignStatus())) {
                    log.warn("[病历保存] 检查申请单 {} 已电子签名（签名即锁定），跳过更新；如需修改请先在「签名验签」作废该签名",
                            apply.getApplyNo());
                    continue;
                }
                applyInspectionContent(apply, appoint, patient, dto);
                bizInspectionApplyMapper.updateById(apply);
            } else {
                // 幂等判重：同一就诊已开过同项目申请单，不再重复开单
                Long exists = bizInspectionApplyMapper.selectCount(new LambdaQueryWrapper<BizInspectionApply>()
                        .eq(BizInspectionApply::getRegistId, registId)
                        .eq(BizInspectionApply::getInspectionItemId, dto.getItemId()));
                if (exists != null && exists > 0) {
                    continue;
                }
                apply = new BizInspectionApply();
                apply.setApplyNo(redisSequenceService.generateInspectionApplyNo());
                apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
                apply.setSubmitTime(LocalDateTime.now());
                applyInspectionContent(apply, appoint, patient, dto);
                bizInspectionApplyMapper.insert(apply);
            }
            // 结诊提交：内容最终确认，统一签名锁定（已签名行在上面已跳过）
            if (isSubmit && apply.getId() != null) {
                signInspectionApply(apply, pin);
            }
        }
    }

    /**
     * 检查申请内容装配（快照 + 项目档案 + 医生填写项），insert / update 共用同一口径
     * （与 {@link #upsertInspectionApply} 保持一致）。
     */
    private void applyInspectionContent(BizInspectionApply apply, RegistStateRowVO appoint, PatientBriefVO patient,
                                        MedicalRecordSaveDTO.InspectionApplyDTO dto) {
        LabItemVO item = medicalTechFacadeService.findInspectionItem(dto.getItemId());
        if (item == null) {
            throw new BusinessException("检查项目不存在: " + dto.getItemId());
        }
        fillInspectionApplySnapshot(apply, appoint, patient);
        apply.setInspectionItemId(item.getId());
        apply.setInspectionItemCode(item.getItemCode());
        apply.setInspectionItemName(item.getItemName());
        apply.setInspectionDeptId(item.getDeptId());
        apply.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
        apply.setBodyPart(dto.getBodyPart());
        apply.setInspectionPurpose(dto.getPurpose());
        apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
        apply.setSpecialRequirements(dto.getSpecialRequirements());
        apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
    }

    private void ensureLaboratoryApplies(Long registId, RegistStateRowVO appoint, PatientBriefVO patient,
                                         List<MedicalRecordSaveDTO.LaboratoryApplyDTO> dtos, boolean isSubmit,
                                         String pin) {
        if (registId == null || CollectionUtils.isEmpty(dtos)) {
            return;
        }
        for (MedicalRecordSaveDTO.LaboratoryApplyDTO dto : dtos) {
            if (dto.getItemId() == null) {
                continue;
            }
            BizLaboratoryApply apply;
            if (dto.getId() != null) {
                apply = bizLaboratoryApplyMapper.selectById(dto.getId());
                if (apply == null) {
                    throw new BusinessException("检验申请单不存在或已删除（id=" + dto.getId() + "）");
                }
                if (!Objects.equals(registId, apply.getRegistId())) {
                    throw new BusinessException("检验申请单不属于本次就诊（id=" + dto.getId() + "）");
                }
                if (!Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), apply.getApplyStatus())) {
                    log.warn("[病历保存] 检验申请单 {} 状态为「{}」，跳过更新（内容以库为准）",
                            apply.getApplyNo(), ApplyStatusEnum.getLabelOrUnknown(apply.getApplyStatus()));
                    continue;
                }
                if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), apply.getSignStatus())) {
                    log.warn("[病历保存] 检验申请单 {} 已电子签名（签名即锁定），跳过更新；如需修改请先在「签名验签」作废该签名",
                            apply.getApplyNo());
                    continue;
                }
                applyLaboratoryContent(apply, appoint, patient, dto);
                bizLaboratoryApplyMapper.updateById(apply);
            } else {
                // 幂等判重：同一就诊已开过同项目申请单，不再重复开单
                Long exists = bizLaboratoryApplyMapper.selectCount(new LambdaQueryWrapper<BizLaboratoryApply>()
                        .eq(BizLaboratoryApply::getRegistId, registId)
                        .eq(BizLaboratoryApply::getLaboratoryItemId, dto.getItemId()));
                if (exists != null && exists > 0) {
                    continue;
                }
                apply = new BizLaboratoryApply();
                apply.setApplyNo(redisSequenceService.generateLaboratoryApplyNo());
                apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
                apply.setSubmitTime(LocalDateTime.now());
                applyLaboratoryContent(apply, appoint, patient, dto);
                bizLaboratoryApplyMapper.insert(apply);
            }
            // 结诊提交：内容最终确认，统一签名锁定（已签名行在上面已跳过）
            if (isSubmit && apply.getId() != null) {
                signLaboratoryApply(apply, pin);
            }
        }
    }

    /**
     * 检验申请内容装配（快照 + 项目档案 + 医生填写项），insert / update 共用同一口径
     * （与 {@link #upsertLaboratoryApply} 保持一致）。
     */
    private void applyLaboratoryContent(BizLaboratoryApply apply, RegistStateRowVO appoint, PatientBriefVO patient,
                                        MedicalRecordSaveDTO.LaboratoryApplyDTO dto) {
        LabItemVO item = medicalTechFacadeService.findLabItem(dto.getItemId());
        if (item == null) {
            throw new BusinessException("检验项目不存在: " + dto.getItemId());
        }
        fillLaboratoryApplySnapshot(apply, appoint, patient);
        apply.setLaboratoryItemId(item.getId());
        apply.setLaboratoryItemCode(item.getItemCode());
        apply.setLaboratoryItemName(item.getItemName());
        apply.setLaboratoryDeptId(item.getDeptId());
        apply.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
        apply.setSpecimenType(TextUtil.hasText(dto.getSpecimenType()) ? dto.getSpecimenType()
                : (TextUtil.hasText(item.getSpecimenType()) ? item.getSpecimenType() : "血液"));
        apply.setLaboratoryPurpose(dto.getPurpose());
        apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
        apply.setIsFasting(dto.getIsFasting() == null ? 0 : dto.getIsFasting());
        apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
    }

    /**
     * 结诊必填门槛（业务规则，AGENTS.md §4.3 豁免③）：临时保存（recordSave）允许草稿部分落库，
     * 结诊提交（recordSubmitDirect）才强校验临床必填。口径与前端 handleComplete requiredFields
     * 逐字一致（主诉/现病史/过敏史/既往史/个人史/家族史/体温/收缩压/诊断）；改前端必同步本方法。
     */
    private void requireSubmitFields(MedicalRecordSaveDTO dto) {
        if (!TextUtil.hasText(dto.getChiefComplaint())) {
            throw new BusinessException("主诉不能为空");
        }
        if (!TextUtil.hasText(dto.getPresentIllness())) {
            throw new BusinessException("现病史不能为空");
        }
        if (!TextUtil.hasText(dto.getAllergyHistory())) {
            throw new BusinessException("过敏史不能为空");
        }
        if (!TextUtil.hasText(dto.getPastHistory())) {
            throw new BusinessException("既往史不能为空");
        }
        if (!TextUtil.hasText(dto.getPersonalHistory())) {
            throw new BusinessException("个人史不能为空");
        }
        if (!TextUtil.hasText(dto.getFamilyHistory())) {
            throw new BusinessException("家族史不能为空");
        }
        if (!TextUtil.hasText(dto.getTemperature())) {
            throw new BusinessException("体温不能为空");
        }
        if (!TextUtil.hasText(dto.getSystolicPressure())) {
            throw new BusinessException("收缩压不能为空");
        }
        if (!TextUtil.hasText(dto.getDiagnosis())) {
            throw new BusinessException("诊断不能为空");
        }
    }

    /**
     * 结诊定稿的否决项闸：把「即将定稿」的病历内容交给质控引擎挑一遍否决项，
     * 命中就按「需要二次确认」的响应码抛出缺陷清单，由前端弹框让医生选「去修改」还是「仍要结诊」。
     *
     * <p>报错文案就是给医生看的那份清单（逐条问题描述），不许缩成一句「质控不通过」——
     * 医生得知道改哪一条。放行后结诊收尾照旧出综合质控单，这些否决项仍然留痕在单上。
     */
    private void assertNoSubmitVeto(BizMedicalRecord bizMedicalRecord) {
        List<QcIssueVO> vetoIssues = qualityControlService.inspectSubmitVeto(QcSnapshot.ofOutpatient(bizMedicalRecord));
        if (vetoIssues.isEmpty()) {
            return;
        }
        String details = vetoIssues.stream()
                .map(QcIssueVO::getErrorDetail)
                .collect(Collectors.joining("；"));
        throw new BusinessException(EmrSubmitConst.QC_CONFIRM_CODE,
                "病历有 " + vetoIssues.size() + " 项否决项缺陷：" + details + "。修改后可重新结诊。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveMedicalRecord(MedicalRecordSaveDTO recordSaveDTO, boolean isSubmit, String pin) {
        // 结诊门槛：草稿可部分保存，提交时临床必填必须齐全（见 requireSubmitFields）
        if (isSubmit) {
            requireSubmitFields(recordSaveDTO);
        }
        CurrentUser currentUser = UserUtils.getCurrentUser();
        BizMedicalRecord bizMedicalRecord;
        // 一次就诊 = 一份病历。前端如果因为历史脏数据（同 registId 多份，getByRegistId 曾直接 500）
        // 没拿到 recordId，再走「新增」就会又堆一份，越用越乱。这里按 registId 兜底续写最新那份。
        Long effectiveRecordId = recordSaveDTO.getRecordId();
        if (effectiveRecordId == null && recordSaveDTO.getRegistId() != null) {
            BizMedicalRecord existing = latestByRegistId(recordSaveDTO.getRegistId());
            if (existing != null) {
                effectiveRecordId = existing.getId();
            }
        }
        if (effectiveRecordId != null) {
            bizMedicalRecord = this.getById(effectiveRecordId);
            if (bizMedicalRecord == null) {
                throw new BusinessException("病历不存在");
            }
            if (bizMedicalRecord.getRecordStatus() == 2) {
                throw new BusinessException("病历已提交，不可修改");
            }
            // 签名即锁定（P5.5）：锁挂在内容上。recordStatus 只拦得住"已提交"，
            // 拦不住"已签名但状态被别的入口改回去"这类情况，两道都要有。
            if (java.util.Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), bizMedicalRecord.getSignStatus())) {
                throw new BusinessException("病历 " + bizMedicalRecord.getRecordNo()
                        + " 已由 " + bizMedicalRecord.getDoctorName() + " 签名（"
                        + bizMedicalRecord.getSignedTime() + "），签名即锁定，不允许修改；"
                        + "如确需更正，请先在「签名验签」作废该签名（作废会留痕并解除锁定）");
            }
        } else {
            // 新增病历
            bizMedicalRecord = new BizMedicalRecord();
            bizMedicalRecord.setRecordNo(redisSequenceService.generateMedicalRecordNo());
            bizMedicalRecord.setRecordStatus(RecordStatusEnum.DRAFT.getCode());
        }

        // 从患者表查询患者信息（经 SPI 委派 his-appoint 读取 patient 域）
        PatientBriefVO patientInfo = patientFacadeService.findPatient(recordSaveDTO.getPatientId());
        if (patientInfo == null) {
            throw new BusinessException("患者不存在");
        }

        // 从挂号表查询挂号信息（科室、医生）（经 SPI 委派 his-appoint）
        RegistStateRowVO appointInfo = getAppointBrief(recordSaveDTO.getRegistId());
        if (appointInfo == null) {
            throw new BusinessException("挂号记录不存在");
        }

        // 设置病历字段
        bizMedicalRecord.setPatientId(patientInfo.getId());
        bizMedicalRecord.setPatientNo(patientInfo.getPatientNo());
        bizMedicalRecord.setPatientName(patientInfo.getPatientName());
        bizMedicalRecord.setGender(patientInfo.getGender());
        bizMedicalRecord.setAge(patientInfo.getAge());
        bizMedicalRecord.setRegistId(recordSaveDTO.getRegistId());
        bizMedicalRecord.setRegistNo(appointInfo.getRegistNo());
        bizMedicalRecord.setDeptId(appointInfo.getDeptId());
        bizMedicalRecord.setDeptName(appointInfo.getDeptName());
        bizMedicalRecord.setDoctorId(currentUser.getEmployeeId());
        bizMedicalRecord.setDoctorName(currentUser.getRealName());
        bizMedicalRecord.setVisitDate(LocalDate.now());
        bizMedicalRecord.setChiefComplaint(recordSaveDTO.getChiefComplaint());
        bizMedicalRecord.setPresentIllness(recordSaveDTO.getPresentIllness());
        bizMedicalRecord.setAllergyHistory(recordSaveDTO.getAllergyHistory());
        bizMedicalRecord.setPastHistory(recordSaveDTO.getPastHistory());
        bizMedicalRecord.setPersonalHistory(recordSaveDTO.getPersonalHistory());
        bizMedicalRecord.setFamilyHistory(recordSaveDTO.getFamilyHistory());
        bizMedicalRecord.setMarriageHistory(recordSaveDTO.getMarriageHistory());
        bizMedicalRecord.setTemperature(recordSaveDTO.getTemperature());
        bizMedicalRecord.setPulse(recordSaveDTO.getPulse());
        bizMedicalRecord.setRespiration(recordSaveDTO.getRespiration());
        bizMedicalRecord.setSystolicPressure(recordSaveDTO.getSystolicPressure());
        bizMedicalRecord.setDiastolicPressure(recordSaveDTO.getDiastolicPressure());
        bizMedicalRecord.setGeneralCondition(recordSaveDTO.getGeneralCondition());
        bizMedicalRecord.setSkinMucosa(recordSaveDTO.getSkinMucosa());
        bizMedicalRecord.setHeadNeck(recordSaveDTO.getHeadNeck());
        bizMedicalRecord.setChestLung(recordSaveDTO.getChestLung());
        bizMedicalRecord.setHeart(recordSaveDTO.getHeart());
        bizMedicalRecord.setAbdomen(recordSaveDTO.getAbdomen());
        bizMedicalRecord.setSpineLimbs(recordSaveDTO.getSpineLimbs());
        bizMedicalRecord.setNervousSystem(recordSaveDTO.getNervousSystem());
        bizMedicalRecord.setSpecialistExam(recordSaveDTO.getSpecialistExam());
        bizMedicalRecord.setAuxiliaryExam(recordSaveDTO.getAuxiliaryExam());
        bizMedicalRecord.setDiagnosis(recordSaveDTO.getDiagnosis());
        bizMedicalRecord.setDiagnosisCode(recordSaveDTO.getDiagnosisCode());
        bizMedicalRecord.setDiagnosisName(recordSaveDTO.getDiagnosisName());
        bizMedicalRecord.setTreatmentPlan(recordSaveDTO.getTreatmentPlan());

        // 结诊定稿预检（事中的那一道）：否决项命中就先不把这份病历定稿。
        // 放在任何写入之前 —— 命中即抛，整个事务回滚，本次什么都没落库，医生改完原样重发即可。
        if (isSubmit && !Boolean.TRUE.equals(recordSaveDTO.getForceSubmit())) {
            assertNoSubmitVeto(bizMedicalRecord);
        }

        // 用实体自身是否有 id 判断新增/更新，不再看 DTO：按 registId 兜底命中的既有病历也要走更新
        if (bizMedicalRecord.getId() != null) {
            this.updateById(bizMedicalRecord);
        } else {
            this.save(bizMedicalRecord);
        }
        Long recordId = bizMedicalRecord.getId();

        // AI 草稿留痕（G-10）：前端只在「本次填过草稿」时携带原文，保存成功后即清空，
        // 所以正常不会重复落行；医生一字未改也落（changed=0，「模型零修改率」同样是训练信号）。
        // 留痕失败只记日志不回滚 —— 病历保存是主业务，不能让审计旁路把它拖垮。
        try {
            aiDraftDiffService.record(recordId, recordSaveDTO.getRegistId(),
                    patientInfo.getId(), patientInfo.getPatientNo(), patientInfo.getPatientName(),
                    appointInfo.getDeptId(), appointInfo.getDeptName(),
                    currentUser.getEmployeeId(), currentUser.getRealName(),
                    recordSaveDTO.getAiDraftPresentIllness(), recordSaveDTO.getPresentIllness());
        } catch (Exception e) {
            log.warn("AI 草稿留痕失败 recordId={}：{}", recordId, e.getMessage());
        }

        // 2. 保存处方（先删后增）。L7 审方退回重开闭环：删除前先看该病历下被退回（7）的旧单 ——
        // 医生改方重新保存 = 重提，新处方继承历史退回次数，并落一条「退回后重提」流水（record_id 串轮次）
        List<BizPrescription> returnedOld = bizPrescriptionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getRecordId, recordId)
                        .eq(BizPrescription::getPrescriptionStatus,
                                com.his.common.enums.PrescriptionStatusEnum.RETURNED_AUDIT.getCode())
                        // 实体无 delFlag 字段、MP 不会自动过滤软删行（处方主表有 del_flag 列），
                        // 已被软删的历史退回单不得再参与继承，否则退回次数会越滚越大
                        .last("AND del_flag = 0"));
        int inheritedReturnCount = returnedOld.stream()
                .map(BizPrescription::getReturnCount)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        deletePrescriptionsByRecordId(recordId);
        if (recordSaveDTO.getPrescriptions() != null && !recordSaveDTO.getPrescriptions().isEmpty()) {
            for (MedicalRecordSaveDTO.PrescriptionDTO prescriptionDTO : recordSaveDTO.getPrescriptions()) {
                BizPrescription prescription = new BizPrescription();
                prescription.setRecordId(recordId);
                prescription.setPatientId(patientInfo.getId());
                prescription.setPatientNo(patientInfo.getPatientNo());
                prescription.setPatientName(patientInfo.getPatientName());
                prescription.setDoctorId(appointInfo.getDoctorId());
                prescription.setDoctorName(appointInfo.getDoctorName());
                prescription.setRegistId(recordSaveDTO.getRegistId());
                prescription.setDeptId(appointInfo.getDeptId());
                prescription.setDeptName(appointInfo.getDeptName());
                prescription.setVisitDate(LocalDate.now());
                prescription.setPrescriptionType(prescriptionDTO.getPrescriptionType());
                boolean tcmDecoctionRx = Integer.valueOf(3).equals(prescriptionDTO.getPrescriptionType());
                if (tcmDecoctionRx) {
                    Integer doseCount = prescriptionDTO.getDoseCount();
                    if (doseCount == null || doseCount < 1 || doseCount > 30) {
                        throw new BusinessException("中药饮片处方必须填剂数（1~30 剂）");
                    }
                    Integer decoctFlag = prescriptionDTO.getDecoctFlag();
                    if (decoctFlag == null || (decoctFlag != 1 && decoctFlag != 2)) {
                        throw new BusinessException("中药饮片处方必须选择煎服方式（1-代煎 2-自煎）");
                    }
                    prescription.setDoseCount(doseCount);
                    prescription.setDecoctFlag(decoctFlag);
                }
                boolean longRx = Boolean.TRUE.equals(prescriptionDTO.getIsLongPrescription());
                if (longRx) {
                    Integer days = prescriptionDTO.getLongPrescriptionDays();
                    if (days == null || days < 1 || days > 90) {
                        throw new BusinessException("长处方用药天数必须在 1~90 天之间");
                    }
                    Long chronicCount = bizChronicRecordMapper.selectCount(
                            new LambdaQueryWrapper<BizChronicRecord>()
                                    .eq(BizChronicRecord::getPatientId, patientInfo.getId())
                                    .eq(BizChronicRecord::getConfirmStatus, 1));
                    if (chronicCount == null || chronicCount == 0) {
                        throw new BusinessException("该患者无已认定的慢病档案，不能开慢病长处方");
                    }
                    prescription.setIsLongPrescription(YesOrNoEnum.YES.getCode());
                    prescription.setLongPrescriptionDays(days);
                } else {
                    prescription.setIsLongPrescription(YesOrNoEnum.NO.getCode());
                }
                prescription.setPrescriptionStatus(PrescriptionStatusEnum.DRAFT.getCode());
                if (!returnedOld.isEmpty()) {
                    prescription.setReturnCount(inheritedReturnCount);
                }
                prescription.setPrescriptionNo(redisSequenceService.generatePrescriptionNo());
                prescription.setDoctorSignature(validateDoctorSignature(prescriptionDTO.getDoctorSignature()));
                this.bizPrescriptionMapper.insert(prescription);

                // L7 重提流水（只增）：仅当旧单里存在被退回的处方，本次保存才视为「退回后重提」
                if (!returnedOld.isEmpty()) {
                    BizPrescriptionAuditLog resubmit = new BizPrescriptionAuditLog();
                    resubmit.setPrescriptionId(prescription.getId());
                    resubmit.setPrescriptionNo(prescription.getPrescriptionNo());
                    resubmit.setRecordId(recordId);
                    resubmit.setRegistId(prescription.getRegistId());
                    resubmit.setRoundNo((int) (bizPrescriptionAuditLogMapper.selectCount(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizPrescriptionAuditLog>()
                                    .eq(BizPrescriptionAuditLog::getRecordId, recordId)) + 1));
                    resubmit.setAction(RxAuditActionEnum.RESUBMIT.getCode());
                    resubmit.setAuditorId(prescription.getDoctorId());
                    resubmit.setAuditorName(prescription.getDoctorName());
                    resubmit.setOpinion("医生修改处方后重新提交");
                    bizPrescriptionAuditLogMapper.insert(resubmit);
                }

                // 保存处方明细
                if (prescriptionDTO.getDetails() != null && !prescriptionDTO.getDetails().isEmpty()) {
                    for (MedicalRecordSaveDTO.PrescriptionDetailDTO detailDTO : prescriptionDTO.getDetails()) {
                        BizPrescriptionDetail detail = new BizPrescriptionDetail();
                        detail.setPrescriptionId(prescription.getId());
                        detail.setPrescriptionNo(prescription.getPrescriptionNo());
                        detail.setDrugId(detailDTO.getDrugId());
                        detail.setDrugName(detailDTO.getDrugName());
                        detail.setSpecification(detailDTO.getSpecification());
                        detail.setUnit(detailDTO.getUnit());
                        detail.setSingleDosage(detailDTO.getSingleDosage());
                        detail.setUsageDosage(detailDTO.getSingleDosage());
                        detail.setFrequency(detailDTO.getFrequency());
                        detail.setRoute(detailDTO.getRoute());
                        detail.setDuration(detailDTO.getDuration());
                        detail.setDetailStatus(PrescriptionDetailStatusEnum.NORMAL.getCode());
                        DrugBriefVO drug = drugMasterFacadeService.findDrug(detail.getDrugId());
                        boolean gramDosed = false;
                        BigDecimal grams = null;
                        if (tcmDecoctionRx) {
                            if (drug == null) {
                                throw new BusinessException("中药饮片明细缺少药品档案（药品ID：" + detailDTO.getDrugId() + "）");
                            }
                            if (!TcmGramUnits.gramDosed(drug.getGramPerUnit())) {
                                throw new BusinessException("饮片「" + drug.getDrugName()
                                        + "」档案未维护克换算率(gram_per_unit)，不能按克开方");
                            }
                            gramDosed = true;
                            grams = tcmTotalGrams(detailDTO.getSingleDosage(), prescription.getDoseCount(), drug.getDrugName());
                            detail.setUnit("g");
                            detail.setQuantity(grams);
                            detail.setTotalDosage(grams);
                        } else {
                            detail.setQuantity(detailDTO.getQuantity());
                        }
                        if (drug != null) {
                            detail.setDrugCode(drug.getDrugCode());
                            BigDecimal price = gramDosed
                                    ? TcmGramUnits.perGramPrice(drug.getRetailPrice(), drug.getGramPerUnit())
                                    : drug.getRetailPrice();
                            detail.setPrice(price);
                            detail.setAmount(TcmGramUnits.amountOf(price, detail.getQuantity()));
                        }

                        this.bizPrescriptionDetailMapper.insert(detail);

                        // 开方锁库：跨批次把可用量转锁定量；无批次时端口内部放过，硬闸门在发药扣库
                        stockFacadeService.lockStockByDrug(detail.getDrugId(), detail.getQuantity());
                    }

                    // 更新处方主表的总金额和药品数量
                    LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
                    detailWrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId());
                    List<BizPrescriptionDetail> savedDetails = bizPrescriptionDetailMapper.selectList(detailWrapper);
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    int drugCount = 0;
                    for (BizPrescriptionDetail savedDetail : savedDetails) {
                        if (savedDetail.getAmount() != null) {
                            totalAmount = totalAmount.add(savedDetail.getAmount());
                        }
                        drugCount++;
                    }
                    prescription.setTotalAmount(totalAmount);
                    prescription.setDrugCount(drugCount);
                    this.bizPrescriptionMapper.updateById(prescription);
                }
            }
        }

        // 3. 检查申请（模式A）——随病历保存落库，**不做"先删后增"**。
        //    申请单不是病历的附属物：每次保存病历都重建申请单，会把已缴费的单子删掉、
        //    把执行记录的 apply_id 删成悬空。这里同步前端提交的申请单数组（无 id 新增、
        //    有 id 更新），结诊提交（isSubmit=true）时统一电子签名（内容最终确认、签名即锁定）。
        //    无签结诊预检：医生没有有效签名证书时不再整体回滚（旧行为是签名失败→结诊失败），
        //    改为「业务照常、签名留白」——申请单/病历/处方保持未签名锚点进入「待补签」半拦截态，
        //    归档与发药按未签名锚点拦截，预警在事务提交后发给持吊销处置权的角色。
        final boolean signSkipped = isSubmit
                && signCertFacadeService.getActiveCertByEmp(bizMedicalRecord.getDoctorId()) == null;
        ensureInspectionApplies(recordSaveDTO.getRegistId(), appointInfo, patientInfo,
                recordSaveDTO.getInspectionApplies(), isSubmit && !signSkipped, pin);
        backfillApplyRecord(recordSaveDTO.getRegistId(), recordId, bizMedicalRecord.getRecordNo());

        // 4. 检验申请（同 3）
        ensureLaboratoryApplies(recordSaveDTO.getRegistId(), appointInfo, patientInfo,
                recordSaveDTO.getLaboratoryApplies(), isSubmit && !signSkipped, pin);

        // 5. 如果是结诊，还需要提交病历并生成收费单
        if (isSubmit) {
            // 结诊联动（经 SPI 委派 his-appoint）：挂号置「已完成」、队列置「已完成」并回写结束时间
            Long doctorId = completeVisit(recordSaveDTO.getRegistId(), recordSaveDTO.getQueueId());
            // 清除医生接诊状态缓存
            if (doctorId != null) {
                queueFacadeService.finishConsulting(doctorId);
            }

            //提交病历
            bizMedicalRecord.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
            bizMedicalRecord.setReviewStatus(AuditStatusEnum.PENDING_AUDIT.getCode());
            bizMedicalRecord.setSubmitTime(LocalDateTime.now());
            this.updateById(bizMedicalRecord);
            addRecordLog(recordId, bizMedicalRecord.getRecordNo(), "提交病历", null, null, "结诊提交");

            // 3. 按类型生成收费单（L1 记账行）
            generateChargeRecords(bizMedicalRecord, recordSaveDTO);

            // L2 待缴账单不在结诊事务里同步出，挪到 asyncAfterSubmit 异步做（AFTER_COMMIT），
            // 避免 settle 聚合+写账单又堵结诊。患者点打印时如果 L2 还没出，renderPayQrCode
            // 会按 L1 未结算金额兜底显示"待缴费 ¥xx 请到收费窗口"。

            // 6. 生成病案首页
            generateArchive(bizMedicalRecord);

            // 10. 生成随访任务（慢病或需要复诊的患者）
            String diagnosis = recordSaveDTO.getDiagnosis();
            if (diagnosis != null && Arrays.stream(HUIFANG_TYPE).anyMatch(diagnosis::contains)) {
                BizFollowupTask task = new BizFollowupTask();
                task.setTaskNo(redisSequenceService.generateFollowupTaskNo());
                task.setPatientId(patientInfo.getId());
                task.setPatientNo(patientInfo.getPatientNo());
                task.setPatientName(patientInfo.getPatientName());
                task.setDiagnosis(diagnosis);
                task.setFollowupType(FollowupTypeEnum.CHRONIC.getCode());
                task.setFollowupContent("慢病随访：关注病情变化，指导用药");
                task.setFollowupTime(LocalDateTime.now().plusDays(30)); // 30天后随访
                task.setFollowupStatus(FollowupTaskStatusEnum.PENDING.getCode());
                task.setCreateBy(currentUser.getRealName());
                task.setCreateTime(LocalDateTime.now());
                bizFollowupTaskMapper.insert(task);
            }

            // 11. 生成病历归档记录
            BizMedicalRecordArchive archive = new BizMedicalRecordArchive();
            archive.setArchiveNo(redisSequenceService.generateMedicalRecordArchiveNo());
            archive.setRecordId(bizMedicalRecord.getId());
            archive.setRecordNo(bizMedicalRecord.getRecordNo());
            archive.setPatientId(patientInfo.getId());
            archive.setPatientNo(patientInfo.getPatientNo());
            archive.setPatientName(patientInfo.getPatientName());
            archive.setRegistId(appointInfo.getId());
            archive.setVisitDate(LocalDate.now());
            archive.setDeptId(bizMedicalRecord.getDeptId());
            archive.setDeptName(bizMedicalRecord.getDeptName());
            archive.setDoctorId(bizMedicalRecord.getDoctorId());
            archive.setDoctorName(bizMedicalRecord.getDoctorName());
            archive.setDiagnosis(diagnosis);
            archive.setArchiveStatus(ArchiveStatusEnum.PENDING.getCode());
            archive.setCreateBy(bizMedicalRecord.getDoctorName());
            archive.setCreateTime(LocalDateTime.now());
            bizMedicalRecordArchiveMapper.insert(archive);

            // 13. 结诊提交即签（P5.5）—— 放在最后，因为它必须排**在所有内容写入之后**：
            //     签名绑的是内容摘要，先把病历内容全部写定再签，才能保证"签完之后内容不再变"。
            //     同时也必须在"已提交状态落库之后"：签名服务是从库里读对象状态做放行判断的，
            //     只改内存对象没用（住院病历那边实测被这条拦过）。
            //     无签结诊（signSkipped）时不签：病历/处方/申请单留未签名锚点，
            //     后续由归档（archive）与发药（dispense）按锚点拦截，等补签后放行。
            if (signSkipped) {
                bizMedicalRecord.setSignStatus(ObjectSignStatusEnum.UNSIGNED.getCode());
                this.updateById(bizMedicalRecord);
                addRecordLog(recordId, bizMedicalRecord.getRecordNo(),
                        "结诊提交（无有效电子签名证书，签名待补签）", null, null, "已提交，待补签");
            } else {
                signOutpatientRecord(bizMedicalRecord, pin);

                // 13b. 同一时刻签本次就诊的处方（开方医师签名，P5.6）。
                //      处方在本次保存里是"先删后增"重建出来的，到这里必然都是未签名的；
                //      草稿阶段的临时保存不会进这个分支（本块整体在「结诊提交」里）。
                signPrescriptionsOfRecord(bizMedicalRecord, pin);
            }

            // 14. 事务提交后异步收尾：规则质控 + 引导单 PDF + 处方签 PDF。
            //     这三件事原来同步堵在结诊事务里（iText 加载 simsun 字体 + 规则引擎全量跑 + 两次写盘），
            //     医生点结诊要等好几秒。它们都是事后产物，不该在关键路径上。
            //     用 afterCommit 确保主事务提交成功后才跑；失败只记日志，不影响结诊结果。
            final Long submittedRecordId = bizMedicalRecord.getId();
            // 身份快照必须在这里取：收尾跑在事务提交后的独立线程里，那里没有登录态，
            // 而质控单上的质控人必须是点结诊的这位医生。
            CurrentUser submitUser = UserUtils.getCurrentUser();
            if (submitUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            final QcOperator submittedOperator = new QcOperator(submitUser.getEmployeeId(), submitUser.getRealName());
            // 无签结诊预警的文案要素在这里取：预警同样跑在提交后，不带登录态
            final String alertRecordNo = bizMedicalRecord.getRecordNo();
            final String alertPatientName = bizMedicalRecord.getPatientName();
            final String alertDeptName = bizMedicalRecord.getDeptName();
            final String alertDoctorName = bizMedicalRecord.getDoctorName();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            eventPublisher.publishEvent(new ClinicSubmittedEvent(submittedRecordId, submittedOperator));
                        } catch (Exception ex) {
                            log.error("[结诊收尾] 发布事件失败 recordId={}", submittedRecordId, ex);
                        }
                        if (signSkipped) {
                            sendUnsignedSignAlert(submittedRecordId, alertRecordNo, alertPatientName,
                                    alertDeptName, alertDoctorName);
                        }
                    }
                });
            } else {
                // 极端情况：没有事务同步（被非事务上下文调用），直接同步跑保底
                asyncAfterSubmit(submittedRecordId, submittedOperator);
                if (signSkipped) {
                    sendUnsignedSignAlert(submittedRecordId, alertRecordNo, alertPatientName,
                            alertDeptName, alertDoctorName);
                }
            }
        }

        return recordId;
    }

    /**
     * 无签结诊预警：发给持有签名证书吊销处置权（sign:cert:revoke）的人员（医务科）。
     *
     * <p>每天每份病历最多一条（countSentSince 按天判重），重复结诊/补跑不刷屏；
     * 接收人按权限圈定而非写死角色名，权限授给谁谁就收。
     */
    private void sendUnsignedSignAlert(Long recordId, String recordNo, String patientName,
                                       String deptName, String doctorName) {
        try {
            LocalDateTime sinceToday = LocalDate.now().atStartOfDay();
            if (sysMessageService.countSentSince(BizTypeEnum.SIGN.getType(), recordId, sinceToday) > 0) {
                return;
            }
            List<EmployeeBriefVO> receivers = systemFacadeService.listEmployeesByPermission("sign:cert:revoke");
            if (receivers.isEmpty()) {
                log.warn("[无签结诊预警] 无接收人（无人持有 sign:cert:revoke 权限），预警未发出 recordId={}", recordId);
                return;
            }
            String title = "病历待补签提醒";
            String content = String.format(
                    "医生 %s 于 %s 在 %s 提交的病历（病历号 %s，患者 %s）结诊时无有效电子签名证书，" +
                            "病历与处方未完成电子签名，已锁定「待补签」，归档与发药已被拦截，" +
                            "请在 2 个工作日内跟进补签或人工核验。",
                    doctorName == null ? "未知" : doctorName,
                    LocalDate.now(),
                    deptName == null ? "未知" : deptName,
                    recordNo,
                    patientName == null ? "未知" : patientName);
            for (EmployeeBriefVO receiver : receivers) {
                sysMessageService.sendSystemMessage(receiver.getId(), receiver.getEmpName(),
                        title, content, BizTypeEnum.SIGN.getType(), recordId);
            }
        } catch (Exception ex) {
            log.error("[无签结诊预警] 发送失败 recordId={}", recordId, ex);
        }
    }

    /**
     * 门诊病历结诊提交后的签名（P5.5）。
     *
     * <p>签名失败**不吞**：调用方（saveMedicalRecord）的整个事务回滚，提交动作整体失败。
     * 理由是签名是这套能力的唯一产出，"界面显示已提交、库里没有签名证据"这种半成品
     * 事后无法分辨是漏签还是被篡改，代价远大于让医生重试一次。
     *
     * <p>签名落库后把新签名值同步回内存实体：本方法之后若还有 updateById(bizMedicalRecord)
     * 之类的写操作，内存里的旧 signStatus（null/0）会把刚落库的锚点覆盖掉。
     *
     * @param pin 签名人本次输入的 PIN（软签二次确认）
     */
    private void signOutpatientRecord(BizMedicalRecord r, String pin) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizTypeEnum.OUTPATIENT_RECORD.getCode());
        cmd.setBizId(r.getId());
        cmd.setSignScene(SignSceneEnum.SUBMIT.getCode());
        cmd.setSignerId(r.getDoctorId());
        cmd.setSignerName(r.getDoctorName());
        cmd.setSignerDeptId(r.getDeptId());
        cmd.setSignerDeptName(r.getDeptName());
        cmd.setPin(pin);
        try {
            SignatureVO sig = emrSignatureService.sign(cmd);
            r.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
            r.setSignId(sig.getId());
            r.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("门诊病历提交失败（病历 " + r.getRecordNo() + "）：" + e.getMessage());
        }
    }

    /**
     * 中药饮片：本味实发总克数 = 每剂克数 × 剂数
     */
    private BigDecimal tcmTotalGrams(String singleDosage, Integer doseCount, String drugName) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+(\\.\\d+)?")
                .matcher(singleDosage == null ? "" : singleDosage.trim());
        if (!matcher.find()) {
            throw new BusinessException("饮片「" + drugName + "」的每剂克数必须是数字（如 15 或 15g）");
        }
        BigDecimal perDoseGrams = new BigDecimal(matcher.group());
        if (perDoseGrams.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("饮片「" + drugName + "」的每剂克数必须大于 0");
        }
        return perDoseGrams.multiply(new BigDecimal(doseCount)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 病历提交：把本次开立的检查/检验/药品逐条写成 L1 记账行（不再"生成一条综合收费单"）。
     */
    private void generateChargeRecords(BizMedicalRecord record, MedicalRecordSaveDTO recordSaveDTO) {
        // 查询挂号记录获取 registNo（经 SPI 委派 his-appoint）
        RegistStateRowVO appointInfo = getAppointBrief(record.getRegistId());
        if (Objects.isNull(appointInfo)) {
            throw new BusinessException("未能找到挂号记录");
        }
        String registNo = appointInfo.getRegistNo();

        List<FeeBookDTO> books = new ArrayList<>();

        // 1. 检查明细
        LambdaQueryWrapper<BizInspectionApply> inspectionWrapper = new LambdaQueryWrapper<>();
        inspectionWrapper.eq(BizInspectionApply::getRecordId, record.getId());
        List<BizInspectionApply> inspections = bizInspectionApplyMapper.selectList(inspectionWrapper);
        for (BizInspectionApply apply : inspections) {
            FeeBookDTO fee = newVisitFee(record, registNo, PaymentItemTypeEnum.EXAMINATION.getCode());
            fee.setSourceType(FeeSourceTypeEnum.EXAM_APPLY.getCode());
            fee.setItemCode(apply.getInspectionItemCode());
            fee.setItemName(apply.getInspectionItemName());
            fee.setQuantity(BigDecimal.ONE);
            fee.setPrice(NumUtil.orZero(apply.getPrice()));
            fee.setSourceId(apply.getId());
            fee.setSourceNo(apply.getApplyNo());
            books.add(fee);
        }

        // 2. 检验明细
        LambdaQueryWrapper<BizLaboratoryApply> laboratoryWrapper = new LambdaQueryWrapper<>();
        laboratoryWrapper.eq(BizLaboratoryApply::getRecordId, record.getId());
        List<BizLaboratoryApply> laboratories = bizLaboratoryApplyMapper.selectList(laboratoryWrapper);
        for (BizLaboratoryApply apply : laboratories) {
            FeeBookDTO fee = newVisitFee(record, registNo, PaymentItemTypeEnum.LABORATORY_TEST.getCode());
            fee.setSourceType(FeeSourceTypeEnum.LAB_APPLY.getCode());
            fee.setItemCode(apply.getLaboratoryItemCode());
            fee.setItemName(apply.getLaboratoryItemName());
            fee.setQuantity(BigDecimal.ONE);
            fee.setPrice(NumUtil.orZero(apply.getPrice()));
            fee.setSourceId(apply.getId());
            fee.setSourceNo(apply.getApplyNo());
            books.add(fee);
        }

        // 3. 药品明细（从所有处方获取，锚点仍是处方明细ID，与申请单口径一致）
        LambdaQueryWrapper<BizPrescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.eq(BizPrescription::getRecordId, record.getId());
        List<BizPrescription> prescriptions = bizPrescriptionMapper.selectList(prescriptionWrapper);
        for (BizPrescription prescription : prescriptions) {
            LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId());
            List<BizPrescriptionDetail> prescriptionDetails = bizPrescriptionDetailMapper.selectList(detailWrapper);
            for (BizPrescriptionDetail pd : prescriptionDetails) {
                FeeBookDTO fee = newVisitFee(record, registNo, PaymentItemTypeEnum.WESTERN_MEDICINE.getCode());
                fee.setSourceType(FeeSourceTypeEnum.PRESCRIPTION.getCode());
                fee.setItemCode(pd.getDrugCode());
                fee.setItemName(pd.getDrugName());
                fee.setSpecification(pd.getSpecification());
                fee.setUnit(pd.getUnit());
                fee.setQuantity(pd.getQuantity() == null ? BigDecimal.ONE : pd.getQuantity());
                fee.setPrice(NumUtil.orZero(pd.getPrice()));
                fee.setSourceId(pd.getId());
                fee.setSourceNo(prescription.getPrescriptionNo());
                books.add(fee);
            }
        }

        if (books.isEmpty()) {
            return;
        }
        List<FeeRecordVO> booked = chargeFacadeService.bookBatch(books);
        if (booked.isEmpty()) {
            // 记不上账不能装作记上了：病历照常提交，但把缺口写进日志，收费台按待结算行兜底
            log.warn("[病历提交] 病历 {} 本次就诊 {} 个项目未能记账，请到收费窗口手工计费",
                    record.getRecordNo(), books.size());
        }
    }

    /**
     * 本次就诊记账行的公共表头：患者与就诊快照 + 开单科室（收入归科依据）+ 目录类别，项目明细由调用处补。
     */
    private FeeBookDTO newVisitFee(BizMedicalRecord record, String registNo, Integer itemType) {
        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(record.getPatientId());
        dto.setPatientNo(record.getPatientNo());
        dto.setPatientName(record.getPatientName());
        dto.setEncounterType(EncounterTypeEnum.OUTPATIENT.getCode());
        dto.setEncounterId(record.getRegistId());
        dto.setEncounterNo(registNo);
        dto.setDeptId(record.getDeptId());
        dto.setDeptName(record.getDeptName());
        dto.setDoctorId(record.getDoctorId());
        dto.setDoctorName(record.getDoctorName());
        dto.setItemType(itemType);
        dto.setCatalogType(FeeCatalogTypeEnum.byItemType(itemType));
        return dto;
    }

    /**
     * 生成患者引导单PDF（包含实际费用、项目明细和付款二维码）
     */
    private String generateGuidePdf(BizMedicalRecord record) {
        String dateDir = LocalDate.now().format(DateFormats.COMPACT_DATE);
        String fileName = record.getRecordNo() + ".pdf";
        String relativePath = "uploads/guide/" + dateDir + "/" + fileName;

        // 获取上传目录路径
        String uploadBaseDir = System.getProperty("user.dir");
        String absolutePath = uploadBaseDir + "/" + relativePath;

        // 创建目录
        java.io.File dir = new java.io.File(uploadBaseDir + "/uploads/guide/" + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 费用来自 L1 记账行：按项目类型聚合的**未结算**净额（排除挂号时已收的诊查费）
        List<FeeTypeSumVO> receivables = chargeFacadeService.sumPendingGroupByItemType(
                EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (FeeTypeSumVO receivable : receivables) {
            totalAmount = totalAmount.add(receivable.getAmount() != null ? receivable.getAmount() : BigDecimal.ZERO);
        }

        // 查询检验检查项目
        LambdaQueryWrapper<BizLaboratoryApply> labWrapper = new LambdaQueryWrapper<>();
        labWrapper.eq(BizLaboratoryApply::getRecordId, record.getId());
        List<BizLaboratoryApply> labApplies = bizLaboratoryApplyMapper.selectList(labWrapper);

        LambdaQueryWrapper<BizInspectionApply> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(BizInspectionApply::getRecordId, record.getId());
        List<BizInspectionApply> insApplies = bizInspectionApplyMapper.selectList(insWrapper);

        try {
            // 创建PDF文档
            com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 36, 36, 36, 36);
            com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(absolutePath));
            document.open();

            // 中文字体 - 使用系统字体
            com.lowagie.text.pdf.BaseFont bfChinese = com.lowagie.text.pdf.BaseFont.createFont(
                    "C:/Windows/Fonts/simsun.ttc,0", "Identity-H", com.lowagie.text.pdf.BaseFont.EMBEDDED);
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(bfChinese, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(bfChinese, 12, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(bfChinese, 10, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font smallFont = new com.lowagie.text.Font(bfChinese, 8, com.lowagie.text.Font.NORMAL);

            // 头部表格：左列 = 付款二维码（真实聚合码，位于左上角），右列 = 标题 + 患者信息
            com.lowagie.text.pdf.PdfPTable headTable = new com.lowagie.text.pdf.PdfPTable(2);
            headTable.setWidthPercentage(100);
            headTable.setWidths(new float[]{1.1f, 2.6f});
            com.lowagie.text.pdf.PdfPCell qrCell = new com.lowagie.text.pdf.PdfPCell();
            qrCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            qrCell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
            qrCell.setPadding(4);
            renderPayQrCode(record, qrCell, smallFont);
            com.lowagie.text.pdf.PdfPCell infoCell = new com.lowagie.text.pdf.PdfPCell();
            infoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            infoCell.setPadding(4);
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("患者就诊引导单", titleFont);
            title.setSpacingAfter(10);
            infoCell.addElement(title);
            infoCell.addElement(createParagraph("患者姓名：" + record.getPatientName(), normalFont));
            infoCell.addElement(createParagraph("病历号：" + record.getRecordNo(), normalFont));
            infoCell.addElement(createParagraph("就诊科室：" + record.getDeptName(), normalFont));
            infoCell.addElement(createParagraph("接诊医生：" + record.getDoctorName(), normalFont));
            infoCell.addElement(createParagraph("就诊日期：" + record.getVisitDate(), normalFont));
            headTable.addCell(qrCell);
            headTable.addCell(infoCell);
            document.add(headTable);
            document.add(createParagraph(" ", normalFont));

            // 诊断信息
            document.add(createParagraph("【诊断信息】", headerFont));
            document.add(createParagraph("诊断：" + record.getDiagnosisName(), normalFont));
            document.add(createParagraph(" ", normalFont));

            // 检验项目
            if (!labApplies.isEmpty()) {
                document.add(createParagraph("【检验项目】", headerFont));
                for (BizLaboratoryApply lab : labApplies) {
                    StringBuilder labItem = new StringBuilder("  - " + lab.getLaboratoryItemName());
                    if (lab.getSpecimenType() != null) {
                        labItem.append("（标本：").append(lab.getSpecimenType()).append("）");
                    }
                    document.add(createParagraph(labItem.toString(), normalFont));
                }
                document.add(createParagraph("  采样地点：一楼检验科", smallFont));
                boolean isFasting = labApplies.get(0).getIsFasting() != null && labApplies.get(0).getIsFasting() == 1;
                document.add(createParagraph("  注意事项：" + (isFasting ? "需空腹" : "无特殊要求"), smallFont));
                document.add(createParagraph(" ", normalFont));
            }

            // 检查项目
            if (!insApplies.isEmpty()) {
                document.add(createParagraph("【检查项目】", headerFont));
                for (BizInspectionApply ins : insApplies) {
                    StringBuilder insItem = new StringBuilder("  - " + ins.getInspectionItemName());
                    if (ins.getBodyPart() != null) {
                        insItem.append("（部位：").append(ins.getBodyPart()).append("）");
                    }
                    document.add(createParagraph(insItem.toString(), normalFont));
                }
                document.add(createParagraph("  检查地点：二楼检查科", smallFont));
                document.add(createParagraph(" ", normalFont));
            }

            // 费用明细
            document.add(createParagraph("【费用明细】", headerFont));
            for (FeeTypeSumVO receivable : receivables) {
                BigDecimal amount = receivable.getAmount() != null ? receivable.getAmount() : BigDecimal.ZERO;
                document.add(createParagraph("  " + feeItemTypeName(receivable.getItemType()) + "：¥" + amount.toPlainString(), normalFont));
            }
            document.add(createParagraph("  合计：¥" + totalAmount.toPlainString(), headerFont));
            document.add(createParagraph(" ", normalFont));

            // 缴费指引（二维码已渲染在左上角头部）
            document.add(createParagraph("【缴费指引】", headerFont));
            document.add(createParagraph("请扫描左上角二维码完成在线缴费", normalFont));
            document.add(createParagraph("支持支付方式：微信、支付宝、医保电子凭证", normalFont));
            document.add(createParagraph(" ", normalFont));

            // 取药流程
            document.add(createParagraph("【取药流程】", headerFont));
            if (receivables.stream().anyMatch(r -> isDrugItemType(r.getItemType()))) {
                document.add(createParagraph("您有药品费用，请凭处方前往一楼药房取药", normalFont));
                document.add(createParagraph("取药时间：周一至周五 8:00-17:30", normalFont));
            } else {
                document.add(createParagraph("本次就诊无药品费用", normalFont));
            }
            document.add(createParagraph(" ", normalFont));

            // 注意事项
            document.add(createParagraph("【注意事项】", headerFont));
            document.add(createParagraph("1. 请妥善保管此引导单", normalFont));
            document.add(createParagraph("2. 如有不适请及时就医", normalFont));
            document.add(createParagraph("3. 复诊时间请遵医嘱", normalFont));
            document.add(createParagraph(" ", normalFont));

            // 医院信息
            document.add(createParagraph("医院地址：湖南省长沙市岳麓区金州大道520号", smallFont));
            document.add(createParagraph("咨询电话：010-88888888", smallFont));

            document.close();

            return relativePath;
        } catch (Exception e) {
            throw new BusinessException("引导单生成失败: " + e.getMessage());
        }
    }

    private com.lowagie.text.Paragraph createParagraph(String text, com.lowagie.text.Font font) {
        com.lowagie.text.Paragraph p = new com.lowagie.text.Paragraph(text, font);
        p.setSpacingAfter(4);
        return p;
    }

    /**
     * 结诊文书付款二维码（左上角）：取该就诊第一张待缴账单出<b>真实聚合支付码</b>（复用收费台聚合码，
     * 患者扫码进入真实缴费闭环）。
     *
     * <p>判断口径（2026-10-24 修）：
     * <ul>
     *   <li>有 L2 待缴账单 → 出聚合码；</li>
     *   <li>无 L2 账单但 L1 未结算金额 = 0 → 真·无应收，显示"本次费用已结清/无需缴费"；</li>
     *   <li>无 L2 账单但 L1 未结算金额 &gt; 0 → 异常（账单生成失败），显示"待缴费 ¥xx，请到收费窗口"，
     *       <b>绝不能谎称已结清</b>（旧 bug：患者没缴费却被告知已结清）。</li>
     * </ul>
     *
     * @param cell 二维码单元格（PDF 表格左列，即文书左上角）
     */
    private void renderPayQrCode(BizMedicalRecord record, com.lowagie.text.pdf.PdfPCell cell, com.lowagie.text.Font smallFont) {
        try {
            List<BillBriefVO> pendingBills = chargeFacadeService.pendingBillsOfEncounter(
                    EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());
            if (pendingBills.isEmpty()) {
                // 没有待缴账单：按 L1 未结算金额区分"真无应收"和"异常漏账"
                BigDecimal dueAmount = BigDecimal.ZERO;
                try {
                    List<FeeTypeSumVO> receivables = chargeFacadeService.sumPendingGroupByItemType(
                            EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());
                    for (FeeTypeSumVO r : receivables) {
                        dueAmount = dueAmount.add(r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
                    }
                } catch (Exception ex) {
                    log.warn("[结诊文书] 查 L1 未结算金额失败 recordNo={}", record.getRecordNo(), ex);
                }
                if (dueAmount.signum() <= 0) {
                    cell.addElement(createParagraph("本次费用已结清", smallFont));
                    cell.addElement(createParagraph("无需缴费", smallFont));
                } else {
                    cell.addElement(createParagraph("待缴费 ¥" + dueAmount.toPlainString(), smallFont));
                    cell.addElement(createParagraph("请到收费窗口结算", smallFont));
                }
                return;
            }
            QrPayResultVO payResult = qrPayFacadeService.createQrCode(pendingBills.get(0).getBillId());
            if (payResult == null || !TextUtil.hasText(payResult.getQrImage())) {
                cell.addElement(createParagraph("付款码暂不可用", smallFont));
                cell.addElement(createParagraph("请到收费窗口结算", smallFont));
                return;
            }
            com.lowagie.text.Image image = qrImageOf(payResult.getQrImage(), 130, 130);
            if (image == null) {
                cell.addElement(createParagraph("付款码暂不可用", smallFont));
                cell.addElement(createParagraph("请到收费窗口结算", smallFont));
                return;
            }
            cell.addElement(image);
            cell.addElement(createParagraph("微信/支付宝扫码缴费", smallFont));
            cell.addElement(createParagraph("金额：¥" + (payResult.getAmount() != null
                    ? payResult.getAmount().toPlainString() : "0"), smallFont));
        } catch (Exception e) {
            log.warn("结诊文书聚合支付码出码失败 recordNo={}", record.getRecordNo(), e);
            cell.addElement(createParagraph("付款码暂不可用", smallFont));
            cell.addElement(createParagraph("请到收费窗口结算", smallFont));
        }
    }

    /**
     * 把聚合支付返回的二维码 base64（data:image/png;base64,...）转成 openpdf Image。
     */
    private com.lowagie.text.Image qrImageOf(String dataUrl, int width, int height) {
        try {
            String base64 = dataUrl;
            int idx = dataUrl.indexOf("base64,");
            if (idx >= 0) {
                base64 = dataUrl.substring(idx + "base64,".length());
            }
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(bytes);
            image.scaleAbsolute(width, height);
            return image;
        } catch (Exception e) {
            log.error("聚合码图片解析失败", e);
            return null;
        }
    }

    /**
     * 生成处方签 PDF：结诊提交后由开方医师签名（电子签名 + 手写笔迹）锁定，供患者取药/核对。
     *
     * <p>按《处方管理办法》第六条第六款拆分：
     * <ul>
     *   <li>西药（type=1）+ 中成药（type=2）合并为一张<b>普通处方笺</b>（法规允许合一）；</li>
     *   <li>中药饮片（type=3）<b>单独一张</b>中药处方笺。</li>
     * </ul>
     * 普通处方不需要身份证号（只有麻精一处方才要）。
     *
     * @return PDF 相对路径（uploads/rx/yyyyMMdd/{recordNo}.pdf）；失败返回 null（不阻断结诊提交）
     */
    /**
     * 普通处方签（西药+中成药合一），法规允许合一。
     *
     * @return PDF 相对路径；没有西药/中成药处方返回 null
     */
    private String generateNormalRxPdf(BizMedicalRecord record) {
        return buildRxPdf(record, false, "normal", "处 方 笺");
    }

    /**
     * 中药饮片处方签，《处方管理办法》要求单独开具。
     *
     * @return PDF 相对路径；没有饮片处方返回 null
     */
    private String generateHerbalRxPdf(BizMedicalRecord record) {
        return buildRxPdf(record, true, "herbal", "中药处方笺");
    }

    /**
     * 画一张处方签 PDF（普通/饮片共用）。
     *
     * @param includeHerbal true=只画饮片(type=3)；false=只画普通(type=1/2)
     */
    private String buildRxPdf(BizMedicalRecord record, boolean includeHerbal, String fileSuffix, String pageTitle) {
        List<BizPrescription> prescriptions = bizPrescriptionMapper.selectList(
                new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getRecordId, record.getId())
                        .orderByAsc(BizPrescription::getId));
        if (prescriptions.isEmpty()) {
            return null;
        }
        List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .in(BizPrescriptionDetail::getPrescriptionId,
                                prescriptions.stream().map(BizPrescription::getId).collect(Collectors.toList())));
        Map<Long, List<BizPrescriptionDetail>> detailMap = details.stream()
                .collect(Collectors.groupingBy(BizPrescriptionDetail::getPrescriptionId));

        List<BizPrescription> matched = prescriptions.stream()
                .filter(p -> includeHerbal
                        ? Objects.equals(p.getPrescriptionType(), PrescriptionTypeEnum.TCM.getCode())
                        : !Objects.equals(p.getPrescriptionType(), PrescriptionTypeEnum.TCM.getCode()))
                .filter(p -> !detailMap.getOrDefault(p.getId(), List.of()).isEmpty())
                .collect(Collectors.toList());
        if (matched.isEmpty()) {
            return null;
        }

        String dateDir = LocalDate.now().format(DateFormats.COMPACT_DATE);
        String relativePath = "uploads/rx/" + dateDir + "/" + record.getRecordNo() + "_" + fileSuffix + ".pdf";
        String uploadBaseDir = System.getProperty("user.dir");
        String absolutePath = uploadBaseDir + "/" + relativePath;
        java.io.File dir = new java.io.File(uploadBaseDir + "/uploads/rx/" + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 36, 36, 36, 36);
            com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(absolutePath));
            document.open();

            com.lowagie.text.pdf.BaseFont bfChinese = com.lowagie.text.pdf.BaseFont.createFont(
                    "C:/Windows/Fonts/simsun.ttc,0", "Identity-H", com.lowagie.text.pdf.BaseFont.EMBEDDED);
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(bfChinese, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(bfChinese, 12, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(bfChinese, 10, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font smallFont = new com.lowagie.text.Font(bfChinese, 8, com.lowagie.text.Font.NORMAL);

            BizPrescription signed = prescriptions.stream()
                    .filter(p -> p.getDoctorSignId() != null)
                    .findFirst()
                    .orElse(null);

            renderRxPage(document, record, matched, detailMap, signed, pageTitle,
                    titleFont, headerFont, normalFont, smallFont);

            document.close();
            return relativePath;
        } catch (Exception e) {
            log.error("处方签 PDF 生成失败 absolutePath={}", absolutePath, e);
            return null;
        }
    }

    /**
     * 渲染一页处方笺（普通/中药共用）：前记（患者信息+二维码）→ Rp → 药品表格 → 医师签名。
     */
    private void renderRxPage(com.lowagie.text.Document document, BizMedicalRecord record,
                              List<BizPrescription> rxs,
                              Map<Long, List<BizPrescriptionDetail>> detailMap,
                              BizPrescription signed, String pageTitle,
                              com.lowagie.text.Font titleFont, com.lowagie.text.Font headerFont,
                              com.lowagie.text.Font normalFont, com.lowagie.text.Font smallFont) throws Exception {
        // 头部：处方签不画付款二维码（只有引导单上有聚合码），直接放标题 + 患者前记。
        // 注意：PdfPCell 不能直接 document.add()（会被静默丢弃），必须包进单列 PdfPTable 才会渲染。
        com.lowagie.text.pdf.PdfPTable headTable = new com.lowagie.text.pdf.PdfPTable(1);
        headTable.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell infoCell = new com.lowagie.text.pdf.PdfPCell();
        infoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        infoCell.setPadding(4);
        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(pageTitle, titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        infoCell.addElement(title);
        infoCell.addElement(createParagraph("姓名：" + record.getPatientName()
                + "    性别：" + SysGenderEnum.getLabel(record.getGender(), "未知")
                + "    年龄：" + (record.getAge() != null ? record.getAge() + "岁" : "-"), normalFont));
        infoCell.addElement(createParagraph("科别：" + record.getDeptName()
                + "    病历号：" + record.getRecordNo(), normalFont));
        infoCell.addElement(createParagraph("临床诊断：" + (TextUtil.hasText(record.getDiagnosisName()) ? record.getDiagnosisName() : "-"), normalFont));
        infoCell.addElement(createParagraph("开具日期：" + record.getVisitDate(), normalFont));
        headTable.addCell(infoCell);
        document.add(headTable);
        document.add(createParagraph(" ", normalFont));

        // Rp 标示（处方正文起首，法规附件1要求）
        document.add(createParagraph("Rp", headerFont));
        document.add(createParagraph(" ", normalFont));

        // 药品明细
        for (BizPrescription prescription : rxs) {
            List<BizPrescriptionDetail> rxDetails = detailMap.getOrDefault(prescription.getId(), List.of());
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.4f, 2.2f, 1.0f, 1.8f, 1.6f, 1.2f});
            addCell(table, "药品名称", headerFont);
            addCell(table, "规格", headerFont);
            addCell(table, "数量", headerFont);
            addCell(table, "用法", headerFont);
            addCell(table, "频次", headerFont);
            addCell(table, "天数", headerFont);
            for (BizPrescriptionDetail d : rxDetails) {
                addCell(table, nullToDash(d.getDrugName()), normalFont);
                addCell(table, nullToDash(d.getSpecification()), normalFont);
                addCell(table, (d.getQuantity() != null ? d.getQuantity().toPlainString() : "-")
                        + (TextUtil.hasText(d.getUnit()) ? " " + d.getUnit() : ""), normalFont);
                addCell(table, nullToDash(d.getUsageDosage()), normalFont);
                addCell(table, nullToDash(d.getFrequency()), normalFont);
                addCell(table, d.getDuration() != null ? String.valueOf(d.getDuration()) : "-", normalFont);
            }
            document.add(table);
            document.add(createParagraph(" ", normalFont));
        }

        // 医师签名区
        com.lowagie.text.Paragraph signTitle = new com.lowagie.text.Paragraph("医师签名：", headerFont);
        signTitle.setSpacingAfter(6);
        document.add(signTitle);
        if (signed != null) {
            if (TextUtil.hasText(signed.getDoctorSignature())) {
                com.lowagie.text.Image signImg = qrImageOf(signed.getDoctorSignature(), 160, 60);
                if (signImg != null) {
                    signImg.setSpacingAfter(4);
                    document.add(signImg);
                }
            }
            document.add(createParagraph((TextUtil.hasText(signed.getDoctorName()) ? signed.getDoctorName() : record.getDoctorName())
                    + (signed.getDoctorSignedTime() != null
                    ? "（电子签名 · " + signed.getDoctorSignedTime().format(DateFormats.DATETIME) + "，可验签）"
                    : "（电子签名，可验签）"), normalFont));
        } else {
            document.add(createParagraph(record.getDoctorName(), normalFont));
        }

        document.add(createParagraph(" ", normalFont));
        document.add(createParagraph("取药流程：请凭本处方笺前往一楼药房取药", smallFont));
        document.add(createParagraph("取药时间：周一至周五 8:00-17:30", smallFont));
        document.add(createParagraph("医院地址：湖南省长沙市岳麓区金州大道520号", smallFont));
        document.add(createParagraph("咨询电话：010-88888888", smallFont));
    }

    /**
     * 向 PDF 表格追加一个单元格（表头/内容通用）。
     */
    private void addCell(com.lowagie.text.pdf.PdfPTable table, String text, com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(
                text == null ? "" : text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String nullToDash(String value) {
        return TextUtil.hasText(value) ? value : "-";
    }

    /**
     * 生成病案首页
     */
    private void generateArchive(BizMedicalRecord record) {
        BizMedicalRecordArchive archive = new BizMedicalRecordArchive();
        archive.setArchiveNo(redisSequenceService.generateMedicalRecordArchiveNo());
        archive.setRecordId(record.getId());
        archive.setRecordNo(record.getRecordNo());
        archive.setPatientId(record.getPatientId());
        archive.setPatientNo(record.getPatientNo());
        archive.setPatientName(record.getPatientName());
        archive.setRegistId(record.getRegistId());
        archive.setVisitDate(record.getVisitDate());
        archive.setDeptId(record.getDeptId());
        archive.setDeptName(record.getDeptName());
        archive.setDoctorId(record.getDoctorId());
        archive.setDoctorName(record.getDoctorName());
        archive.setDiagnosis(record.getDiagnosisName());
        archive.setArchiveStatus(ArchiveStatusEnum.PENDING.getCode());
        archive.setArchiveTime(LocalDateTime.now());
        bizMedicalRecordArchiveMapper.insert(archive);
    }

    @Override
    public void asyncAfterSubmit(Long recordId, QcOperator operator) {
        if (recordId == null) {
            return;
        }
        // 收尾跑在 @Async 独立线程（AFTER_COMMIT），那里没有登录态——
        // 出 L2 待缴账单走 settle 时需要操作人归因（billById/billByName），
        // 否则 UserUtils.getCurrentUser() 返回 null → NPE。
        // 用结诊时点快照的身份绑进上下文，finally 里还原；同步保底分支（有真实登录态）不绑不清除。
        boolean bound = false;
        if (UserUtils.getCurrentUser() == null && operator != null) {
            SystemOperatorContext.bindEmployee(operator.getEmployeeId(), operator.getName());
            bound = true;
        }
        try {
            // 异步新事务里重新加载病历（内存对象在结诊事务提交后已不可用）
            BizMedicalRecord record = this.getById(recordId);
            if (record == null) {
                log.warn("[结诊收尾] 病历不存在，跳过 recordId={}", recordId);
                return;
            }

            // ① L2 待缴账单：按就诊聚合所有待结算 L1 行出一张汇总账单（患者扫一个码付全部）。
            //    放这里是为了不堵结诊事务；幂等，已有待缴账单直接返回。
            try {
                chargeFacadeService.createVisitBill(EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());
            } catch (Exception ex) {
                log.error("[结诊收尾] L2 待缴账单生成失败 recordNo={}", record.getRecordNo(), ex);
            }

            // ② 规则质控（事后提醒，失败只记日志）
            try {
                QcSystemExecuteDTO qcDto = new QcSystemExecuteDTO();
                qcDto.setRecordSource(QcRecordSourceEnum.OUTPATIENT.getCode());
                qcDto.setRecordId(recordId);
                qcDto.setQcType(RecordQcTypeEnum.COMPREHENSIVE.getCode());
                qcDto.setTriggerSource(QcTriggerSourceEnum.CLINIC_SUBMIT);
                qcDto.setOperator(operator);
                qualityControlService.executeQcBySystem(qcDto);
            } catch (Exception ex) {
                log.error("[结诊收尾] 规则质控失败 recordNo={}", record.getRecordNo(), ex);
            }
        } finally {
            if (bound) {
                SystemOperatorContext.clear();
            }
        }
    }

    /**
     * 医生点「打印就诊文书」时现场生成全部 PDF：
     * <ul>
     *   <li>就诊引导单（一张 A4：缴费二维码 + 费用汇总 + 检查指引）；</li>
     *   <li>普通处方签（西药+中成药合一）；</li>
     *   <li>中药饮片处方签。</li>
     * </ul>
     * 结诊时不生成任何 PDF，避免医生不点打印却白写盘；这里按需生成，
     * 每次都重画（患者可能中途加了检查/换了药）。
     *
     * @return 三个 PDF 的相对路径（uploads/...），没有就对应项为空串
     */
    public VisitDocsPrintVO printVisitDocs(Long recordId) {
        BizMedicalRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        // 确保 L2 待缴账单在（幂等），否则引导单左上角出不了聚合码
        chargeFacadeService.createVisitBill(EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());

        VisitDocsPrintVO vo = new VisitDocsPrintVO();
        vo.setGuidePdfPath(generateGuidePdf(record));
        vo.setRxNormalPdfPath(generateNormalRxPdf(record));
        vo.setRxHerbalPdfPath(generateHerbalRxPdf(record));
        return vo;
    }

    // ========== 医生撤回结诊 ==========
    // 真实 HIS 门诊病历撤回口径：医生刚结诊发现开错，在当前患者身上直接撤回，
    // 不是单独开菜单（那是病案修改申请流程，走审批）。闸门与反向动作见 EmrService.revokeRecord。
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeRecord(Long recordId, Long queueId, String reason) {
        if (recordId == null) {
            throw new BusinessException("请选择要撤回的病历");
        }
        BizMedicalRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        CurrentUser currentUser = UserUtils.getCurrentUser();

        // 闸门1：状态必须是已提交(2)。已归档(3)/已作废(4) 医生无权撤回。
        if (!Integer.valueOf(RecordStatusEnum.SUBMITTED.getCode()).equals(record.getRecordStatus())) {
            if (Integer.valueOf(RecordStatusEnum.ARCHIVED.getCode()).equals(record.getRecordStatus())) {
                throw new BusinessException("病历已归档，不允许医生撤回；如需修改请走病案修改申请流程");
            }
            throw new BusinessException("病历状态不是「已提交」，不能撤回");
        }
        // 闸门2：本人开的病历。
        if (!Objects.equals(record.getDoctorId(), currentUser.getEmployeeId())) {
            throw new BusinessException("只能撤回本人开具的病历");
        }
        // 闸门3：当日就诊（隔日病历走病案修改申请）。
        if (record.getVisitDate() != null && !LocalDate.now().equals(record.getVisitDate())) {
            throw new BusinessException("隔日病历不允许医生自助撤回，请联系病案室走修改申请");
        }
        // 闸门4：未缴费。pendingBillsOfEncounter 返回「未结清」账单（待支付/部分支付/挂账）。
        //   - 有待支付账单 → 可以撤回（红冲L1后账单自动作废）；
        //   - 无账单但有应收（sumNet > 0）→ 已缴费/已结算，拒绝（走收费台退费）；
        //   - 无账单且无应收（0元单，没开药没开检查）→ 允许撤回。
        List<BillBriefVO> pendingBills = chargeFacadeService.pendingBillsOfEncounter(
                EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());
        if (pendingBills == null || pendingBills.isEmpty()) {
            BigDecimal netDue = chargeFacadeService.sumNetGroupByItemType(
                            EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId()).stream()
                    .map(FeeTypeSumVO::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (netDue.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("该就诊已缴费/已结算，不能直接撤回；如需退费请到收费台办理");
            }
        }
        // 闸门5：检查/检验未开始执行。已签到/已采样的不能撤回。
        List<Long> applyIds = bizInspectionApplyMapper.selectList(
                        new LambdaQueryWrapper<BizInspectionApply>().eq(BizInspectionApply::getRecordId, recordId))
                .stream().map(BizInspectionApply::getId).toList();
        List<Long> labApplyIds = bizLaboratoryApplyMapper.selectList(
                        new LambdaQueryWrapper<BizLaboratoryApply>().eq(BizLaboratoryApply::getRecordId, recordId))
                .stream().map(BizLaboratoryApply::getId).toList();
        if (!applyIds.isEmpty()) {
            List<ExecStatusVO> exStatus = medicalTechFacadeService.listInspectionExecStatus(applyIds);
            boolean anyStarted = exStatus.stream().anyMatch(s -> Boolean.TRUE.equals(s.getStarted()));
            if (anyStarted) {
                throw new BusinessException("检查申请已开始执行，不能撤回；如需取消请到医技科办理");
            }
        }
        if (!labApplyIds.isEmpty()) {
            List<ExecStatusVO> labStatus = medicalTechFacadeService.listLaboratoryExecStatus(labApplyIds);
            boolean anyStarted = labStatus.stream().anyMatch(s -> Boolean.TRUE.equals(s.getStarted()));
            if (anyStarted) {
                throw new BusinessException("检验申请已开始执行，不能撤回；如需取消到检验科办理");
            }
        }

        // ---- 反向动作（同事务）----
        // 1. 病历状态：已提交 → 草稿，清提交时刻/审核状态/签名状态。
        record.setRecordStatus(RecordStatusEnum.DRAFT.getCode());
        record.setReviewStatus(0);
        record.setSubmitTime(null);
        record.setSignStatus(ObjectSignStatusEnum.UNSIGNED.getCode());
        this.updateById(record);

        // 2. 作废病历签名（P5.5）。listByBiz 拿到当前有效签，逐个 invalidate。
        try {
            List<SignatureVO> signs = emrSignatureService.listByBiz(SignBizTypeEnum.OUTPATIENT_RECORD.getCode(), recordId);
            if (signs != null) {
                for (SignatureVO s : signs) {
                    if (s.getSignStatus() != null && s.getSignStatus() == 1) { // 1=有效签名
                        emrSignatureService.invalidate(s.getId(),
                                "医生撤回结诊：" + (TextUtil.hasText(reason) ? reason : "未填原因"),
                                currentUser.getEmployeeId(), currentUser.getRealName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[撤回结诊] 病历签名作废失败 recordId={}：{}", recordId, e.getMessage());
        }

        // 3. 处方：签名作废 + 状态回退草稿。
        List<BizPrescription> prescriptions = bizPrescriptionMapper.selectList(
                new LambdaQueryWrapper<BizPrescription>().eq(BizPrescription::getRecordId, recordId));
        for (BizPrescription rx : prescriptions) {
            try {
                List<SignatureVO> rxSigns = emrSignatureService.listByBiz(SignBizTypeEnum.PRESCRIPTION.getCode(), rx.getId());
                if (rxSigns != null) {
                    for (SignatureVO s : rxSigns) {
                        if (s.getSignStatus() != null && s.getSignStatus() == 1) {
                            emrSignatureService.invalidate(s.getId(),
                                    "医生撤回结诊：处方随病历一起回退",
                                    currentUser.getEmployeeId(), currentUser.getRealName());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[撤回结诊] 处方签名作废失败 prescriptionId={}：{}", rx.getId(), e.getMessage());
            }
            rx.setPrescriptionStatus(PrescriptionStatusEnum.DRAFT.getCode());
            bizPrescriptionMapper.updateById(rx);
        }

        // 4. L1 记账行红冲：检查/检验/处方明细逐行 findBookedBySource + reverse。
        String revokeReason = "医生撤回结诊" + (TextUtil.hasText(reason) ? "：" + reason : "");
        for (BizInspectionApply apply : bizInspectionApplyMapper.selectList(
                new LambdaQueryWrapper<BizInspectionApply>().eq(BizInspectionApply::getRecordId, recordId))) {
            FeeRecordVO fee = chargeFacadeService.findBookedBySource(
                    FeeSourceTypeEnum.EXAM_APPLY.getCode(), apply.getId(), apply.getInspectionItemCode());
            if (fee != null) {
                chargeFacadeService.reverse(fee.getId(), revokeReason);
            }
        }
        for (BizLaboratoryApply apply : bizLaboratoryApplyMapper.selectList(
                new LambdaQueryWrapper<BizLaboratoryApply>().eq(BizLaboratoryApply::getRecordId, recordId))) {
            FeeRecordVO fee = chargeFacadeService.findBookedBySource(
                    FeeSourceTypeEnum.LAB_APPLY.getCode(), apply.getId(), apply.getLaboratoryItemCode());
            if (fee != null) {
                chargeFacadeService.reverse(fee.getId(), revokeReason);
            }
        }
        for (BizPrescription rx : prescriptions) {
            for (BizPrescriptionDetail pd : bizPrescriptionDetailMapper.selectList(
                    new LambdaQueryWrapper<BizPrescriptionDetail>().eq(BizPrescriptionDetail::getPrescriptionId, rx.getId()))) {
                FeeRecordVO fee = chargeFacadeService.findBookedBySource(
                        FeeSourceTypeEnum.PRESCRIPTION.getCode(), pd.getId(), pd.getDrugCode());
                if (fee != null) {
                    chargeFacadeService.reverse(fee.getId(), revokeReason);
                }
            }
        }

        // 5. 队列/挂号回退就诊中（结诊的反向）。
        if (record.getRegistId() != null && registStateFacadeService.getRegistRow(record.getRegistId()) != null) {
            registStateFacadeService.updateRegistStatus(record.getRegistId(),
                    AppointStatusEnum.ACCEPTED.getCode(),
                    List.of(AppointStatusEnum.COMPLETED.getCode()));
        }
        if (queueId != null && registStateFacadeService.getQueueRow(queueId) != null) {
            registStateFacadeService.finishQueue(queueId,
                    QueueStatusEnum.CONSULTING.getCode(), null,
                    List.of(QueueStatusEnum.COMPLETED.getCode()));
        }

        // 6. 归档记录作废（结诊时 generateArchive 落的那条待归档记录）。
        bizMedicalRecordArchiveMapper.delete(new LambdaQueryWrapper<BizMedicalRecordArchive>()
                .eq(BizMedicalRecordArchive::getRecordId, recordId));

        // 7. 留痕。
        addRecordLog(recordId, record.getRecordNo(), "撤回结诊", null, "已提交", reason);
    }
}
