package com.his.emr.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.AuditStatusEnum;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.ObjectSignStatus;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.EmrSignatureService;
import com.his.common.vo.SignatureVO;
import com.his.common.support.TcmGramUnits;
import com.his.emr.dto.InspectionApplyUpsertDTO;
import com.his.emr.dto.LaboratoryApplyUpsertDTO;
import com.his.emr.dto.MedicalRecordQueryDTO;
import com.his.emr.dto.MedicalRecordQueryPageDTO;
import com.his.emr.dto.MedicalRecordSaveDTO;
import com.his.emr.dto.QcExecuteDTO;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.enums.AppointStatusEnum;
import com.his.appoint.enums.QueueStatusEnum;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.entity.BizMedicalRecordArchive;
import com.his.emr.entity.BizChronicRecord;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionAuditLog;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizMedicalRecordLog;
import com.his.emr.entity.BizFollowupTask;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.emr.mapper.BizMedicalRecordArchiveMapper;
import com.his.emr.mapper.BizChronicRecordMapper;
import com.his.emr.mapper.BizPrescriptionAuditLogMapper;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.mapper.BizMedicalRecordLogMapper;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.mapper.BizFollowupTaskMapper;
import com.his.emr.support.QcRecordSource;
import com.his.appoint.service.DoctorStatusCacheService;
import com.his.emr.service.EmrService;
import com.his.emr.service.QualityControlService;
import com.his.emr.service.ApplyExecStatusGateway;
import com.his.fee.dto.FeeBookDTO;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.service.FeeRecordService;
import com.his.fee.support.FeeCatalogResolver;
import com.his.fee.vo.FeeTypeSumVO;
import com.his.pharmacy.service.AntibioticService;
import com.his.emr.vo.BizInspectionApplyVO;
import com.his.emr.vo.BizLaboratoryApplyVO;
import com.his.emr.vo.BizPrescriptionDetailVO;
import com.his.emr.vo.BizPrescriptionVO;
import com.his.emr.vo.BizMedicalRecordVO;
import com.his.emr.vo.EmrRecordDetailVO;
import com.his.emr.vo.MyMedicalRecordVO;
import com.his.patient.service.PatientGuardianService;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizPatientMapper;
import com.his.system.entity.SysDrug;
import com.his.system.mapper.SysDrugMapper;
import com.his.pharmacy.service.PharmacyService;
import com.his.security.CurrentUser;
import com.his.security.DeptScopeGuard;
import com.his.security.UserUtils;
import com.his.system.entity.SysInspectionItem;
import com.his.system.entity.SysLaboratoryItem;
import com.his.system.mapper.SysInspectionItemMapper;
import com.his.system.mapper.SysLaboratoryItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.his.emr.enums.RxAuditActionEnum;

import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.enums.RecordQcTypeEnum;
import com.his.emr.enums.PrescriptionDetailStatusEnum;
import com.his.emr.enums.FollowupTaskStatusEnum;
import com.his.emr.enums.ArchiveStatusEnum;
import com.his.emr.enums.FollowupTypeEnum;
/**
 * 医生工作站服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrServiceImpl extends ServiceImpl<BizMedicalRecordMapper, BizMedicalRecord> implements EmrService {

    static final String[] HUIFANG_TYPE = {"糖尿病", "高血压", "冠心病"};
    private static final AtomicInteger TASK_SEQ = new AtomicInteger(0);
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private final BizPrescriptionMapper prescriptionMapper;
    private final BizPrescriptionDetailMapper prescriptionDetailMapper;
    private final BizPrescriptionAuditLogMapper prescriptionAuditLogMapper;
    private final BizInspectionApplyMapper inspectionApplyMapper;
    private final BizLaboratoryApplyMapper laboratoryApplyMapper;
    private final BizMedicalRecordLogMapper recordLogMapper;
    private final PharmacyService pharmacyService;
    private final FeeRecordService feeRecordService;
    private final BizMedicalRecordArchiveMapper archiveMapper;
    private final BizPatientMapper patientMapper;
    /** 就诊人越权闸：员工放行，患者只能读自己绑定的就诊人 */
    private final PatientGuardianService patientGuardianService;
    private final BizAppointInfoMapper appointInfoMapper;
    private final BizQueueMapper queueMapper;
    private final SysInspectionItemMapper inspectionItemMapper;
    private final SysLaboratoryItemMapper laboratoryItemMapper;
    private final SysDrugMapper drugMapper;
    /** 电子签名（P5.5）：结诊提交即签名，签名即锁定 */
    private final EmrSignatureService signatureService;
    private final RedisSequenceService sequenceService;
    private final DoctorStatusCacheService doctorStatusCacheService;
    private final BizMedicalRecordArchiveMapper medicalRecordArchiveMapper;
    private final QualityControlService qualityControlService;
    private final BizFollowupTaskMapper followupTaskMapper;
    private final BizChronicRecordMapper chronicRecordMapper;
    /**
     * 抗菌药物处方权闸（sql/161）：开方落库前校验医师授权级别 ≥ 药品分级。
     * 只在这里闸一次 —— 目录和授权表本身只是台账，闸不住就等于"系统里谁都能开限制级"。
     */
    private final AntibioticService antibioticService;
    /**
     * 执行进度查询（批次E/E5）：实现在 his-medicaltech。用 ObjectProvider 取，
     * 取不到（例如只跑 his-emr 相关单测、没有医技模块）时降级为「不显示执行进度」，
     * 而不是让整个医生站起不来。
     */
    private final ObjectProvider<ApplyExecStatusGateway> applyExecStatusGatewayProvider;

    @Override
    public List<BizMedicalRecordVO> getByPatientId(MedicalRecordQueryDTO queryDTO) {
        // 工作站接口，患者 token 同样调得到：不校验就是「改个 patientId 读别人病历」
        if (patientGuardianService.patientScopeViolated(queryDTO.getPatientId())) {
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
        if (Objects.isNull(one)) {
            return new BizMedicalRecordVO();
        }
        BizMedicalRecordVO vo = BeanUtil.copyProperties(one, BizMedicalRecordVO.class);
        if (patientGuardianService.patientScopeViolated(vo.getPatientId())) {
            throw new BusinessException("无权查看该病历");
        }
        return vo;
    }

    /** 取某次就诊最新一份病历；同 registId 有多份时取 id 最大（最新）的。 */
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
    public PageResult<BizMedicalRecordVO> listPage(MedicalRecordQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        // 科室数据权限收口（M6）：先越权校验显式 deptId，再按授权科室集合收敛（不传时不再等于看全院）。
        Long scopedDeptId = DeptScopeGuard.resolveDeptId(queryDTO.getDeptId());
        if (scopedDeptId != null) {
            queryDTO.setDeptId(scopedDeptId);
        }
        wrapper.eq(queryDTO.getPatientId() != null, BizMedicalRecord::getPatientId, queryDTO.getPatientId())
                .eq(queryDTO.getDoctorId() != null, BizMedicalRecord::getDoctorId, queryDTO.getDoctorId())
                .eq(queryDTO.getDeptId() != null, BizMedicalRecord::getDeptId, queryDTO.getDeptId())
                .in(DeptScopeGuard.isScoped() && scopedDeptId == null,
                        BizMedicalRecord::getDeptId, DeptScopeGuard.allowedDeptIds())
                .eq(queryDTO.getVisitDate() != null, BizMedicalRecord::getVisitDate, queryDTO.getVisitDate())
                .ge(queryDTO.getVisitDateStart() != null, BizMedicalRecord::getVisitDate, queryDTO.getVisitDateStart())
                .le(queryDTO.getVisitDateEnd() != null, BizMedicalRecord::getVisitDate, queryDTO.getVisitDateEnd())
                .eq(queryDTO.getRecordStatus() != null, BizMedicalRecord::getRecordStatus, queryDTO.getRecordStatus())
                .eq(queryDTO.getReviewStatus() != null, BizMedicalRecord::getReviewStatus, queryDTO.getReviewStatus())
                .and(StringUtils.hasText(queryDTO.getKeyword()), w -> w
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
        if (patientGuardianService.patientScopeViolated(record.getPatientId())) {
            throw new BusinessException("无权查看该病历");
        }

        // 查询处方列表
        LambdaQueryWrapper<BizPrescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.eq(BizPrescription::getRecordId, recordId);
        List<BizPrescription> prescriptions = prescriptionMapper.selectList(prescriptionWrapper);

        // 查询检查申请列表
        LambdaQueryWrapper<BizInspectionApply> inspectionWrapper = new LambdaQueryWrapper<>();
        inspectionWrapper.eq(BizInspectionApply::getRecordId, recordId);
        List<BizInspectionApply> inspectionApplies = inspectionApplyMapper.selectList(inspectionWrapper);

        // 查询检验申请列表
        LambdaQueryWrapper<BizLaboratoryApply> laboratoryWrapper = new LambdaQueryWrapper<>();
        laboratoryWrapper.eq(BizLaboratoryApply::getRecordId, recordId);
        List<BizLaboratoryApply> laboratoryApplies = laboratoryApplyMapper.selectList(laboratoryWrapper);

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
        vo.setRecordStatusText(RecordStatusEnum.labelOf(record.getRecordStatus()));
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
    public boolean addPrescription(BizPrescription prescription) {
        // 抗菌药物处方权闸：必须在落库之前 —— 开出去再拦，处方号已经生成、库存已经锁定，
        // 那是"事后擦屁股"，不是管控。抛异常让整张处方回滚。
        List<Long> abxDrugIds = prescription.getDetails() == null ? Collections.emptyList()
                : prescription.getDetails().stream()
                        .map(BizPrescriptionDetail::getDrugId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        antibioticService.assertCanPrescribe(prescription.getDoctorId(), abxDrugIds);

        // 生成处方号
        prescription.setPrescriptionNo("RX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
        prescription.setPrescriptionStatus(PrescriptionStatusEnum.DRAFT.getCode());

        // 保存处方主表
        this.prescriptionMapper.insert(prescription);

        // 保存处方明细并锁定库存
        if (prescription.getDetails() != null && !prescription.getDetails().isEmpty()) {
            for (BizPrescriptionDetail detail : prescription.getDetails()) {
                detail.setPrescriptionId(prescription.getId());
                detail.setPrescriptionNo(prescription.getPrescriptionNo());
                detail.setDetailStatus(PrescriptionDetailStatusEnum.NORMAL.getCode());
                this.prescriptionDetailMapper.insert(detail);

                // 锁定库存（按药品跨批次，与发药 FEFO 同一口径）
                if (detail.getDrugId() != null && detail.getQuantity() != null) {
                    pharmacyService.lockStockByDrug(detail.getDrugId(), detail.getQuantity());
                }
            }
        }

        // 开方签名：必须在明细落库之后 —— 被签内容包括处方明细，先签会签出一份"没有药"的处方
        signPrescription(prescription);

        return true;
    }

    /**
     * 开方医师签名（场景 RX_CREATE）。
     *
     * <p>签名失败即抛出让整个开方事务回滚：**一张"开出去了但没有医师签名"的处方
     * 比一张开不出去的处方更危险** —— 它会安静地流到发药窗口。
     */
    private void signPrescription(BizPrescription p) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizType.PRESCRIPTION.getCode());
        cmd.setBizId(p.getId());
        cmd.setSignScene(SignScene.RX_CREATE.getCode());
        cmd.setSignerId(p.getDoctorId());
        cmd.setSignerName(p.getDoctorName());
        cmd.setSignerDeptId(p.getDeptId());
        cmd.setSignerDeptName(p.getDeptName());
        try {
            SignatureVO sig = signatureService.sign(cmd);
            // 同步回内存实体：本方法之后若还有 updateById(prescription)，旧值会把锚点覆盖掉
            p.setDoctorSignId(sig.getId());
            p.setDoctorSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("处方 " + p.getPrescriptionNo() + " 开方签名失败：" + e.getMessage());
        }
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
     */
    private void signPrescriptionsOfRecord(BizMedicalRecord r) {
        List<BizPrescription> list = prescriptionMapper.selectList(
                new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getRecordId, r.getId())
                        .isNull(BizPrescription::getDoctorSignId)
                        .orderByAsc(BizPrescription::getId));
        for (BizPrescription p : list) {
            signPrescription(p);
        }
    }

    @Override
    public BizPrescription getPrescriptionDetail(Long prescriptionId) {
        BizPrescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription != null) {
            LambdaQueryWrapper<BizPrescriptionDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId);
            List<BizPrescriptionDetail> details = prescriptionDetailMapper.selectList(wrapper);
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
        return prescriptionMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addInspectionApply(BizInspectionApply apply) {
        // 生成申请单号
        apply.setApplyNo("INS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
        apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
        apply.setSubmitTime(LocalDateTime.now());
        return inspectionApplyMapper.insert(apply) > 0;
        // 注意：检查记录在患者缴费后才创建（由 ChargeController.processPayment 处理）
    }

    @Override
    public List<BizInspectionApply> selectInspectionApplyList(Long patientId, Long doctorId) {
        LambdaQueryWrapper<BizInspectionApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizInspectionApply::getPatientId, patientId)
                .eq(doctorId != null, BizInspectionApply::getDoctorId, doctorId)
                .orderByDesc(BizInspectionApply::getCreateTime);
        return inspectionApplyMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addLaboratoryApply(BizLaboratoryApply apply) {
        // 生成申请单号
        apply.setApplyNo("LAB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
        apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode()); // 1-已提交（未缴费）
        apply.setSubmitTime(LocalDateTime.now());
        return laboratoryApplyMapper.insert(apply) > 0;
        // 注意：检验记录在患者缴费后才创建（由 ChargeController.processPayment 处理）
    }

    @Override
    public List<BizLaboratoryApply> selectLaboratoryApplyList(Long patientId, Long doctorId) {
        LambdaQueryWrapper<BizLaboratoryApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizLaboratoryApply::getPatientId, patientId)
                .eq(doctorId != null, BizLaboratoryApply::getDoctorId, doctorId)
                .orderByDesc(BizLaboratoryApply::getCreateTime);
        return laboratoryApplyMapper.selectList(wrapper);
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
                    recordLogMapper.insert(log);
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
        recordLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePrescriptionsByRecordId(Long recordId) {
        if (recordId == null) return false;
        // 删除处方明细并解锁库存
        LambdaQueryWrapper<BizPrescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.eq(BizPrescription::getRecordId, recordId);
        List<BizPrescription> prescriptions = prescriptionMapper.selectList(prescriptionWrapper);
        for (BizPrescription prescription : prescriptions) {
            // 查询处方明细
            LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId());
            List<BizPrescriptionDetail> details = prescriptionDetailMapper.selectList(detailWrapper);

            // 解锁库存（跨批次退回，只退实际锁着的量）
            for (BizPrescriptionDetail detail : details) {
                if (detail.getDrugId() != null && detail.getQuantity() != null) {
                    pharmacyService.unlockStockByDrug(detail.getDrugId(), detail.getQuantity());
                }
            }

            // 删除处方明细
            prescriptionDetailMapper.delete(detailWrapper);
        }
        // 删除处方主表
        return prescriptionMapper.delete(prescriptionWrapper) >= 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInspectionAppliesByRecordId(Long recordId) {
        if (recordId == null) return false;
        // 批次E/E2：按 recordId 全删是个危险动作（会把已缴费、已出报告的申请单一起抹掉），
        // 现在逐条走带保护的 deleteInspectionApply —— 任何一个删不掉就整批失败并说明原因。
        // 注意：saveMedicalRecord 已经不再调用本方法（申请单改为「开单即落库 + 结诊回填」）。
        List<BizInspectionApply> list = inspectionApplyMapper.selectList(new LambdaQueryWrapper<BizInspectionApply>()
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
        List<BizLaboratoryApply> list = laboratoryApplyMapper.selectList(new LambdaQueryWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getRecordId, recordId));
        for (BizLaboratoryApply apply : list) {
            deleteLaboratoryApply(apply.getId());
        }
        return true;
    }

    // 检查/检验申请单（批次E：开单即落库 + 删除保护 + 结果回显）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInspectionApplyVO upsertInspectionApply(InspectionApplyUpsertDTO dto) {
        BizAppointInfo appoint = requireAppoint(dto.getRegistId());
        BizPatient patient = requirePatient(dto.getPatientId());
        SysInspectionItem item = inspectionItemMapper.selectById(dto.getInspectionItemId());
        if (item == null) {
            throw new BusinessException("检查项目不存在: " + dto.getInspectionItemId());
        }

        BizInspectionApply apply;
        if (dto.getId() != null) {
            apply = inspectionApplyMapper.selectById(dto.getId());
            if (apply == null) {
                throw new BusinessException("检查申请单不存在");
            }
            requireApplyEditable(apply.getApplyNo(), apply.getApplyStatus());
            requireApplyUnsigned(apply.getApplyNo(), apply.getSignStatus());
        } else {
            apply = new BizInspectionApply();
            apply.setApplyNo(genApplyNo("INS"));
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
            inspectionApplyMapper.updateById(apply);
        } else {
            inspectionApplyMapper.insert(apply);
        }
        // 开单即签：签名失败整单回滚（与门诊病历"结诊即签"同一口径，不留"开了单没签名"的半成品）
        signInspectionApply(apply);
        return toInspectionApplyVO(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizLaboratoryApplyVO upsertLaboratoryApply(LaboratoryApplyUpsertDTO dto) {
        BizAppointInfo appoint = requireAppoint(dto.getRegistId());
        BizPatient patient = requirePatient(dto.getPatientId());
        SysLaboratoryItem item = laboratoryItemMapper.selectById(dto.getLaboratoryItemId());
        if (item == null) {
            throw new BusinessException("检验项目不存在: " + dto.getLaboratoryItemId());
        }

        BizLaboratoryApply apply;
        if (dto.getId() != null) {
            apply = laboratoryApplyMapper.selectById(dto.getId());
            if (apply == null) {
                throw new BusinessException("检验申请单不存在");
            }
            requireApplyEditable(apply.getApplyNo(), apply.getApplyStatus());
            requireApplyUnsigned(apply.getApplyNo(), apply.getSignStatus());
        } else {
            apply = new BizLaboratoryApply();
            apply.setApplyNo(genApplyNo("LAB"));
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
        apply.setSpecimenType(StringUtils.hasText(item.getSpecimenType()) ? item.getSpecimenType()
                : (StringUtils.hasText(dto.getSpecimenType()) ? dto.getSpecimenType() : "血液"));
        apply.setLaboratoryPurpose(dto.getLaboratoryPurpose());
        apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
        apply.setIsFasting(dto.getIsFasting() == null ? 0 : dto.getIsFasting());
        apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
        bindRecord(apply, dto.getRecordId(), appoint.getId());

        if (apply.getId() != null) {
            laboratoryApplyMapper.updateById(apply);
        } else {
            laboratoryApplyMapper.insert(apply);
        }
        signLaboratoryApply(apply);
        return toLaboratoryApplyVO(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInspectionApply(Long id) {
        if (id == null) {
            return false;
        }
        BizInspectionApply apply = inspectionApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("检查申请单不存在或已删除");
        }
        String block = inspectionDeleteBlock(apply.getId(), apply.getApplyStatus(),
                inspectionExecMap(List.of(id)).get(id), apply.getSignStatus());
        if (block != null) {
            throw new BusinessException("检查申请单 " + apply.getApplyNo() + " 不允许删除：" + block);
        }
        return inspectionApplyMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLaboratoryApply(Long id) {
        if (id == null) {
            return false;
        }
        BizLaboratoryApply apply = laboratoryApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("检验申请单不存在或已删除");
        }
        String block = laboratoryDeleteBlock(apply.getId(), apply.getApplyStatus(),
                laboratoryExecMap(List.of(id)).get(id), apply.getSignStatus());
        if (block != null) {
            throw new BusinessException("检验申请单 " + apply.getApplyNo() + " 不允许删除：" + block);
        }
        return laboratoryApplyMapper.deleteById(id) > 0;
    }

    @Override
    public List<BizInspectionApplyVO> listInspectionApplies(Long registId) {
        if (registId == null) {
            return new ArrayList<>();
        }
        // 按**挂号**过滤而不是按患者：同一个患者这次开的检查和上次的检查必须分开，
        // 原实现只按 patientId 查，切到新患者身上还会显示上一次就诊的检查单。
        List<BizInspectionApply> list = inspectionApplyMapper.selectList(new LambdaQueryWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getRegistId, registId)
                .orderByDesc(BizInspectionApply::getCreateTime)
                .orderByDesc(BizInspectionApply::getId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        Map<Long, ApplyExecStatusGateway.ExecStatus> execMap = inspectionExecMap(
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
        List<BizLaboratoryApply> list = laboratoryApplyMapper.selectList(new LambdaQueryWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getRegistId, registId)
                .orderByDesc(BizLaboratoryApply::getCreateTime)
                .orderByDesc(BizLaboratoryApply::getId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        Map<Long, ApplyExecStatusGateway.ExecStatus> execMap = laboratoryExecMap(
                list.stream().map(BizLaboratoryApply::getId).collect(Collectors.toList()));
        List<BizLaboratoryApplyVO> vos = new ArrayList<>(list.size());
        for (BizLaboratoryApply apply : list) {
            vos.add(toLaboratoryApplyVO(apply, execMap.get(apply.getId())));
        }
        return vos;
    }

    // 申请单：内部辅助

    private BizAppointInfo requireAppoint(Long registId) {
        // C 类保留：私有兜底被多个申请单入口与内部流程共用，Bean Validation 覆盖不到这一层
        if (registId == null) {
            throw new BusinessException("挂号ID不能为空");
        }
        BizAppointInfo appoint = appointInfoMapper.selectById(registId);
        if (appoint == null) {
            throw new BusinessException("挂号记录不存在");
        }
        return appoint;
    }

    private BizPatient requirePatient(Long patientId) {
        // C 类保留：私有兜底被多个入口与内部流程共用，Bean Validation 覆盖不到这一层
        if (patientId == null) {
            throw new BusinessException("患者ID不能为空");
        }
        BizPatient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        return patient;
    }

    /**
     * 申请单可编辑闸门：只有「已提交未缴费」能改。
     * 已缴费的申请单不许改项目/价格 —— 那是已经算进收费单里的东西。
     */
    private void requireApplyEditable(String applyNo, Integer applyStatus) {
        if (!Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), applyStatus)) {
            throw new BusinessException("申请单 " + applyNo + " 当前状态为「"
                    + applyStatusText(applyStatus) + "」，不允许修改；已缴费请先走退费流程");
        }
    }

    /** 签名即锁定：已签名的申请单不许改内容，要改先去签名中心作废签名（作废留痕）。 */
    private void requireApplyUnsigned(String applyNo, Integer signStatus) {
        if (Objects.equals(ObjectSignStatus.SIGNED.getCode(), signStatus)) {
            throw new BusinessException("申请单 " + applyNo + " 已电子签名（签名即锁定），不允许直接修改；"
                    + "如需修改，请先在「签名中心」作废该签名，作废后再次保存会自动完成补签");
        }
    }

    /** 检查申请单开单即签（业务类型=7，场景=申请开立）；签名失败随本事务回滚。 */
    private void signInspectionApply(BizInspectionApply apply) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizType.INSPECTION_APPLY.getCode());
        cmd.setBizId(apply.getId());
        cmd.setSignScene(SignScene.APPLY_CREATE.getCode());
        cmd.setSignerId(apply.getDoctorId());
        cmd.setSignerName(apply.getDoctorName());
        cmd.setSignerDeptId(apply.getDeptId());
        cmd.setSignerDeptName(apply.getDeptName());
        try {
            SignatureVO sig = signatureService.sign(cmd);
            apply.setSignStatus(ObjectSignStatus.SIGNED.getCode());
            apply.setSignId(sig.getId());
            apply.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("检查申请开单失败（" + apply.getApplyNo() + "）：" + e.getMessage());
        }
    }

    /** 检验申请单开单即签（业务类型=8），规则同 {@link #signInspectionApply}。 */
    private void signLaboratoryApply(BizLaboratoryApply apply) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizType.LAB_APPLY.getCode());
        cmd.setBizId(apply.getId());
        cmd.setSignScene(SignScene.APPLY_CREATE.getCode());
        cmd.setSignerId(apply.getDoctorId());
        cmd.setSignerName(apply.getDoctorName());
        cmd.setSignerDeptId(apply.getDeptId());
        cmd.setSignerDeptName(apply.getDeptName());
        try {
            SignatureVO sig = signatureService.sign(cmd);
            apply.setSignStatus(ObjectSignStatus.SIGNED.getCode());
            apply.setSignId(sig.getId());
            apply.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("检验申请开单失败（" + apply.getApplyNo() + "）：" + e.getMessage());
        }
    }

    private String applyStatusText(Integer status) {
        if (Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), status)) return "已提交";
        if (Objects.equals(ApplyStatusEnum.PAID.getCode(), status)) return "已缴费";
        if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), status)) return "已取消";
        return status == null ? "未知" : "未知(" + status + ")";
    }

    private String genApplyNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000);
    }

    /** 患者 / 科室 / 医生快照 —— 申请单是独立单据，必须自带这些信息，不能靠关联查。 */
    private void fillInspectionApplySnapshot(BizInspectionApply apply, BizAppointInfo appoint, BizPatient patient) {
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

    private void fillLaboratoryApplySnapshot(BizLaboratoryApply apply, BizAppointInfo appoint, BizPatient patient) {
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
    private LocalDate visitDateOf(BizAppointInfo appoint) {
        return appoint.getVisitDate() != null ? appoint.getVisitDate() : LocalDate.now();
    }

    /**
     * 开单医生：优先取当前登录医生（开单这件事是"我"做的），
     * 取不到再退回挂号记录的医生；两者都空则取 0 触发 NOT NULL 约束暴露问题，
     * 而不是静默写个 0 看起来像正常数据。
     */
    private Long doctorIdOf(BizAppointInfo appoint) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user != null && user.getEmployeeId() != null) {
            return user.getEmployeeId();
        }
        return appoint.getDoctorId();
    }

    private String doctorNameOf(BizAppointInfo appoint) {
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
                                                     ApplyExecStatusGateway.ExecStatus exec) {
        BizInspectionApplyVO vo = new BizInspectionApplyVO();
        BeanUtils.copyProperties(apply, vo);
        String text;
        if (Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), apply.getApplyStatus())) {
            text = "待缴费";
        } else if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), apply.getApplyStatus())) {
            text = "已取消";
        } else if (exec != null && StringUtils.hasText(exec.getExecStatusText())) {
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
                                                     ApplyExecStatusGateway.ExecStatus exec) {
        BizLaboratoryApplyVO vo = new BizLaboratoryApplyVO();
        BeanUtils.copyProperties(apply, vo);
        String text;
        if (Objects.equals(ApplyStatusEnum.SUBMITTED.getCode(), apply.getApplyStatus())) {
            text = "待缴费";
        } else if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), apply.getApplyStatus())) {
            text = "已取消";
        } else if (exec != null && StringUtils.hasText(exec.getExecStatusText())) {
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
                                         ApplyExecStatusGateway.ExecStatus exec, Integer signStatus) {
        if (Objects.equals(ObjectSignStatus.SIGNED.getCode(), signStatus)) {
            return "该申请单已电子签名（签名即锁定），请先在「签名中心」作废签名";
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
                                         ApplyExecStatusGateway.ExecStatus exec, Integer signStatus) {
        if (Objects.equals(ObjectSignStatus.SIGNED.getCode(), signStatus)) {
            return "该申请单已电子签名（签名即锁定），请先在「签名中心」作废签名";
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

    /** 检查记录「已取消(7)」——退费留下的墓碑，不占用申请单。 */
    private boolean isCancelledInspection(Integer recordStatus) {
        return recordStatus != null && recordStatus == 7;
    }

    /** 检验记录「已取消(8)」——与检查不是同一套码表，别合并判断。 */
    private boolean isCancelledLaboratory(Integer recordStatus) {
        return recordStatus != null && recordStatus == 8;
    }

    /** 是否已产生费用记账行。查询失败按"已记账"处理 —— 宁可拒删。 */
    private boolean feeBookedExists(Integer sourceType, Long sourceId) {
        try {
            return feeRecordService.findBookedBySource(sourceType, sourceId, null) != null;
        } catch (Exception e) {
            log.warn("[申请单] 记账行引用校验失败，按「已记账」保守处理，sourceType={} sourceId={}", sourceType, sourceId, e);
            return true;
        }
    }

    /** 项目类型码 → 引导单上的费用名（字典 his_charge_item_type，权威在 PaymentItemTypeEnum）。 */
    private String feeItemTypeName(Integer itemType) {
        PaymentItemTypeEnum typeEnum = PaymentItemTypeEnum.getByCode(itemType);
        return (typeEnum == null ? "其他" : typeEnum.getDesc()) + "费";
    }

    /** 引导单「取药流程」的判据：本次应收里有没有药品类项目（2-西药 3-中成药 4-中药饮片）。 */
    private static boolean isDrugItemType(Integer itemType) {
        return Objects.equals(PaymentItemTypeEnum.WESTERN_MEDICINE.getCode(), itemType)
                || Objects.equals(PaymentItemTypeEnum.CHINESE_PATENT_MEDICINE.getCode(), itemType)
                || Objects.equals(PaymentItemTypeEnum.CHINESE_HERBAL_MEDICINE.getCode(), itemType);
    }

    private Map<Long, ApplyExecStatusGateway.ExecStatus> inspectionExecMap(List<Long> applyIds) {
        ApplyExecStatusGateway gw = applyExecStatusGatewayProvider.getIfAvailable();
        if (gw == null || CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyMap();
        }
        try {
            List<ApplyExecStatusGateway.ExecStatus> list = gw.listInspectionExecStatus(applyIds);
            return indexExecStatus(list);
        } catch (Exception e) {
            log.warn("[申请单] 查询检查执行进度失败，降级为不显示进度", e);
            return Collections.emptyMap();
        }
    }

    private Map<Long, ApplyExecStatusGateway.ExecStatus> laboratoryExecMap(List<Long> applyIds) {
        ApplyExecStatusGateway gw = applyExecStatusGatewayProvider.getIfAvailable();
        if (gw == null || CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyMap();
        }
        try {
            List<ApplyExecStatusGateway.ExecStatus> list = gw.listLaboratoryExecStatus(applyIds);
            return indexExecStatus(list);
        } catch (Exception e) {
            log.warn("[申请单] 查询检验执行进度失败，降级为不显示进度", e);
            return Collections.emptyMap();
        }
    }

    private Map<Long, ApplyExecStatusGateway.ExecStatus> indexExecStatus(
            List<ApplyExecStatusGateway.ExecStatus> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<Long, ApplyExecStatusGateway.ExecStatus> map = new HashMap<>();
        for (ApplyExecStatusGateway.ExecStatus s : list) {
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
        inspectionApplyMapper.update(null, new LambdaUpdateWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getRegistId, registId)
                .isNull(BizInspectionApply::getRecordId)
                .set(BizInspectionApply::getRecordId, recordId)
                .set(BizInspectionApply::getRecordNo, recordNo));
        laboratoryApplyMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getRegistId, registId)
                .isNull(BizLaboratoryApply::getRecordId)
                .set(BizLaboratoryApply::getRecordId, recordId)
                .set(BizLaboratoryApply::getRecordNo, recordNo));
    }

    /**
     * 兼容老调用方：病历 DTO 里仍然带着申请单数组时，按 (registId + 项目ID) **幂等补齐**。
     *
     * <p>这里刻意不做「先删后增」：申请单已经不是病历的附属物了（批次E/E1 起开单即落库），
     * 每次保存病历都重建一遍会把已缴费的单子删掉、让执行记录的 apply_id 悬空。
     */
    private void ensureInspectionApplies(Long registId, BizAppointInfo appoint, BizPatient patient,
                                         List<MedicalRecordSaveDTO.InspectionApplyDTO> dtos) {
        if (registId == null || CollectionUtils.isEmpty(dtos)) {
            return;
        }
        for (MedicalRecordSaveDTO.InspectionApplyDTO dto : dtos) {
            if (dto.getItemId() == null) {
                continue;
            }
            Long exists = inspectionApplyMapper.selectCount(new LambdaQueryWrapper<BizInspectionApply>()
                    .eq(BizInspectionApply::getRegistId, registId)
                    .eq(BizInspectionApply::getInspectionItemId, dto.getItemId()));
            if (exists != null && exists > 0) {
                continue;
            }
            SysInspectionItem item = inspectionItemMapper.selectById(dto.getItemId());
            if (item == null) {
                throw new BusinessException("检查项目不存在: " + dto.getItemId());
            }
            BizInspectionApply apply = new BizInspectionApply();
            apply.setApplyNo(genApplyNo("INS"));
            apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
            apply.setSubmitTime(LocalDateTime.now());
            fillInspectionApplySnapshot(apply, appoint, patient);
            apply.setInspectionItemId(item.getId());
            apply.setInspectionItemCode(item.getItemCode());
            apply.setInspectionItemName(item.getItemName());
            apply.setInspectionDeptId(item.getDeptId());
            apply.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
            apply.setBodyPart(dto.getBodyPart());
            apply.setInspectionPurpose(dto.getPurpose());
            apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
            apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
            inspectionApplyMapper.insert(apply);
        }
    }

    private void ensureLaboratoryApplies(Long registId, BizAppointInfo appoint, BizPatient patient,
                                         List<MedicalRecordSaveDTO.LaboratoryApplyDTO> dtos) {
        if (registId == null || CollectionUtils.isEmpty(dtos)) {
            return;
        }
        for (MedicalRecordSaveDTO.LaboratoryApplyDTO dto : dtos) {
            if (dto.getItemId() == null) {
                continue;
            }
            Long exists = laboratoryApplyMapper.selectCount(new LambdaQueryWrapper<BizLaboratoryApply>()
                    .eq(BizLaboratoryApply::getRegistId, registId)
                    .eq(BizLaboratoryApply::getLaboratoryItemId, dto.getItemId()));
            if (exists != null && exists > 0) {
                continue;
            }
            SysLaboratoryItem item = laboratoryItemMapper.selectById(dto.getItemId());
            if (item == null) {
                throw new BusinessException("检验项目不存在: " + dto.getItemId());
            }
            BizLaboratoryApply apply = new BizLaboratoryApply();
            apply.setApplyNo(genApplyNo("LAB"));
            apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
            apply.setSubmitTime(LocalDateTime.now());
            fillLaboratoryApplySnapshot(apply, appoint, patient);
            apply.setLaboratoryItemId(item.getId());
            apply.setLaboratoryItemCode(item.getItemCode());
            apply.setLaboratoryItemName(item.getItemName());
            apply.setLaboratoryDeptId(item.getDeptId());
            apply.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
            apply.setSpecimenType(StringUtils.hasText(dto.getSpecimenType()) ? dto.getSpecimenType()
                    : (StringUtils.hasText(item.getSpecimenType()) ? item.getSpecimenType() : "血液"));
            apply.setLaboratoryPurpose(dto.getPurpose());
            apply.setClinicalDiagnosis(dto.getClinicalDiagnosis());
            apply.setIsFasting(dto.getIsFasting() == null ? 0 : dto.getIsFasting());
            apply.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
            laboratoryApplyMapper.insert(apply);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveMedicalRecord(MedicalRecordSaveDTO recordSaveDTO, boolean isSubmit) {
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
            if (java.util.Objects.equals(ObjectSignStatus.SIGNED.getCode(), bizMedicalRecord.getSignStatus())) {
                throw new BusinessException("病历 " + bizMedicalRecord.getRecordNo()
                        + " 已由 " + bizMedicalRecord.getDoctorName() + " 签名（"
                        + bizMedicalRecord.getSignedTime() + "），签名即锁定，不允许修改；"
                        + "如确需更正，请先在「签名中心」作废该签名（作废会留痕并解除锁定）");
            }
        } else {
            // 新增病历
            bizMedicalRecord = new BizMedicalRecord();
            bizMedicalRecord.setRecordNo("MR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", SEQ.incrementAndGet() % 10000));
            bizMedicalRecord.setRecordStatus(RecordStatusEnum.DRAFT.getCode());
        }

        // 从患者表查询患者信息
        BizPatient patientInfo = patientMapper.selectById(recordSaveDTO.getPatientId());
        if (patientInfo == null) {
            throw new BusinessException("患者不存在");
        }

        // 从挂号表查询挂号信息（科室、医生）
        BizAppointInfo appointInfo = appointInfoMapper.selectById(recordSaveDTO.getRegistId());
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

        // 用实体自身是否有 id 判断新增/更新，不再看 DTO：按 registId 兜底命中的既有病历也要走更新
        if (bizMedicalRecord.getId() != null) {
            this.updateById(bizMedicalRecord);
        } else {
            this.save(bizMedicalRecord);
        }
        Long recordId = bizMedicalRecord.getId();

        // 2. 保存处方（先删后增）。L7 审方退回重开闭环：删除前先看该病历下被退回（7）的旧单 ——
        // 医生改方重新保存 = 重提，新处方继承历史退回次数，并落一条「退回后重提」流水（record_id 串轮次）
        List<BizPrescription> returnedOld = prescriptionMapper.selectList(
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
                // sql/139 中药饮片口径：剂数挂处方头，明细的克数是「每剂」量。
                // 不在服务端挡一把的话，前端漏传剂数 = 后面按 quantity 记账时少乘一次，
                // 而 quantity 此刻已经被界面当成剂数填进来了（历史上正是这么错的）。
                boolean tcmDecoctionRx = Integer.valueOf(3).equals(prescriptionDTO.getPrescriptionType());
                if (tcmDecoctionRx) {
                    // B 类保留：条件必填——仅中药饮片处方要求剂数/煎服方式（且 1~30 属取值范围），普通处方不传
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
                // M1 慢病长处方：标记长处方前必须确认患者存在「已认定」的慢病档案，
                // 且用药天数 ≤ 90 —— 不认定的患者线上改参数就能把一次处方撑到 90 天。
                boolean longRx = Boolean.TRUE.equals(prescriptionDTO.getIsLongPrescription());
                if (longRx) {
                    Integer days = prescriptionDTO.getLongPrescriptionDays();
                    if (days == null || days < 1 || days > 90) {
                        throw new BusinessException("长处方用药天数必须在 1~90 天之间");
                    }
                    Long chronicCount = chronicRecordMapper.selectCount(
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
                prescription.setPrescriptionNo("RX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + String.format("%04d", SEQ.incrementAndGet() % 10000));
                this.prescriptionMapper.insert(prescription);

                // L7 重提流水（只增）：仅当旧单里存在被退回的处方，本次保存才视为「退回后重提」
                if (!returnedOld.isEmpty()) {
                    BizPrescriptionAuditLog resubmit = new BizPrescriptionAuditLog();
                    resubmit.setPrescriptionId(prescription.getId());
                    resubmit.setPrescriptionNo(prescription.getPrescriptionNo());
                    resubmit.setRecordId(recordId);
                    resubmit.setRegistId(prescription.getRegistId());
                    resubmit.setRoundNo((int) (prescriptionAuditLogMapper.selectCount(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizPrescriptionAuditLog>()
                                    .eq(BizPrescriptionAuditLog::getRecordId, recordId)) + 1));
                    resubmit.setAction(RxAuditActionEnum.RESUBMIT.getCode());
                    resubmit.setAuditorId(prescription.getDoctorId());
                    resubmit.setAuditorName(prescription.getDoctorName());
                    resubmit.setOpinion("医生修改处方后重新提交");
                    prescriptionAuditLogMapper.insert(resubmit);
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

                        // 从药品信息表获取编码、价格并计算金额
                        SysDrug drug = drugMapper.selectById(detail.getDrugId());
                        // sql/139：饮片方的 quantity 一律是「每剂克数 × 剂数」的总克数，
                        // 界面传进来的 quantity（历史上是剂数）一律作废，不看它。
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

                        this.prescriptionDetailMapper.insert(detail);

                        // 锁定库存：锁的是**档案单位**（kg/袋），不是克数 —— 105g 黄芪要锁 0.11kg
                        if (detail.getDrugId() != null && detail.getQuantity() != null) {
                            BigDecimal lockQty = gramDosed
                                    ? TcmGramUnits.toStockUnits(grams, drug.getGramPerUnit())
                                    : detail.getQuantity();
                            pharmacyService.lockStockByDrug(detail.getDrugId(), lockQty);
                        }
                    }

                    // 更新处方主表的总金额和药品数量
                    LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
                    detailWrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId());
                    List<BizPrescriptionDetail> savedDetails = prescriptionDetailMapper.selectList(detailWrapper);
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
                    this.prescriptionMapper.updateById(prescription);
                }
            }
        }

        // 3. 检查申请（批次E/E1 改造）——**不再"先删后增"**。
        //    申请单在医生"开单那一刻"就已经落库，病历只是它的载体之一：
        //    每次保存病历都重建申请单，会把已缴费的单子删掉、把执行记录的 apply_id 删成悬空。
        //    这里只做两件事：① 兼容老调用方（DTO 仍带数组）按 (挂号+项目) 幂等补齐；
        ensureInspectionApplies(recordSaveDTO.getRegistId(), appointInfo, patientInfo,
                recordSaveDTO.getInspectionApplies());
        backfillApplyRecord(recordSaveDTO.getRegistId(), recordId, bizMedicalRecord.getRecordNo());

        // 4. 检验申请（同 3）
        ensureLaboratoryApplies(recordSaveDTO.getRegistId(), appointInfo, patientInfo,
                recordSaveDTO.getLaboratoryApplies());

        // 5. 如果是结诊，还需要提交病历并生成收费单
        if (isSubmit) {
            // 将预约的状态改成已完成
            if (appointInfo != null) {
                appointInfo.setRegistStatus(AppointStatusEnum.COMPLETED.getCode());
                appointInfoMapper.updateById(appointInfo);
            }

            // 将排队状态改成已就诊完成
            BizQueue queue = queueMapper.selectById(recordSaveDTO.getQueueId());
            if (queue != null) {
                queue.setQueueStatus(QueueStatusEnum.COMPLETED.getCode());
                queue.setEndTime(LocalDateTime.now());
                queueMapper.updateById(queue);

                // 清除医生接诊状态缓存
                if (queue.getDoctorId() != null) {
                    doctorStatusCacheService.finishConsulting(queue.getDoctorId());
                }
            }

            //提交病历
            bizMedicalRecord.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
            bizMedicalRecord.setReviewStatus(AuditStatusEnum.PENDING_AUDIT.getCode());
            bizMedicalRecord.setSubmitTime(LocalDateTime.now());
            this.updateById(bizMedicalRecord);
            addRecordLog(recordId, bizMedicalRecord.getRecordNo(), "提交病历", null, null, "结诊提交");

            // 3. 按类型生成收费单
            generateChargeRecords(bizMedicalRecord, recordSaveDTO);

            // 5. 生成患者引导单
            String guidePdfPath = generateGuidePdf(bizMedicalRecord);
            bizMedicalRecord.setGuidePdfPath(guidePdfPath);
            this.updateById(bizMedicalRecord);

            // 6. 生成病案首页
            generateArchive(bizMedicalRecord);

            // 10. 生成随访任务（慢病或需要复诊的患者）
            String diagnosis = recordSaveDTO.getDiagnosis();
            if (diagnosis != null && Arrays.stream(HUIFANG_TYPE).anyMatch(diagnosis::contains)) {
                BizFollowupTask task = new BizFollowupTask();
                task.setTaskNo("FT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + String.format("%04d", TASK_SEQ.incrementAndGet() % 10000));
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
                followupTaskMapper.insert(task);
            }

            // 11. 生成病历归档记录
            BizMedicalRecordArchive archive = new BizMedicalRecordArchive();
            archive.setArchiveNo("MA" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", TASK_SEQ.incrementAndGet() % 10000));
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
            medicalRecordArchiveMapper.insert(archive);

            // 12. 提交即质控：跑一次**真实**的规则质控（原来的实现是写死 qc_result=1、
            //     error_count=0，而且 record_id 塞的是归档单 ID —— 那条质控单无法定位到病历）。
            //     质控失败不能把归档也拖下水，所以整体兜住异常；但绝不兜住之后假装质控成功。
            try {
                QcExecuteDTO qcDto = new QcExecuteDTO();
                qcDto.setRecordSource(QcRecordSource.OUTPATIENT.getCode());
                qcDto.setRecordId(bizMedicalRecord.getId());
                qcDto.setQcType(RecordQcTypeEnum.COMPREHENSIVE.getCode());
                qualityControlService.executeQc(qcDto);
            } catch (Exception ex) {
                log.error("[病历归档] 门诊病历 {} 自动质控失败，归档流程继续",
                        bizMedicalRecord.getRecordNo(), ex);
            }

            // 13. 结诊提交即签（P5.5）—— 放在最后，因为它必须排**在所有内容写入之后**：
            //     签名绑的是内容摘要，先把病历内容全部写定再签，才能保证"签完之后内容不再变"。
            //     同时也必须在"已提交状态落库之后"：签名服务是从库里读对象状态做放行判断的，
            //     只改内存对象没用（住院病历那边实测被这条拦过）。
            signOutpatientRecord(bizMedicalRecord);

            // 13b. 同一时刻签本次就诊的处方（开方医师签名，P5.6）。
            //      处方在本次保存里是"先删后增"重建出来的，到这里必然都是未签名的；
            //      草稿阶段的临时保存不会进这个分支（本块整体在「结诊提交」里）。
            signPrescriptionsOfRecord(bizMedicalRecord);
        }

        return recordId;
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
     */
    private void signOutpatientRecord(BizMedicalRecord r) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizType.OUTPATIENT_RECORD.getCode());
        cmd.setBizId(r.getId());
        cmd.setSignScene(SignScene.SUBMIT.getCode());
        cmd.setSignerId(r.getDoctorId());
        cmd.setSignerName(r.getDoctorName());
        cmd.setSignerDeptId(r.getDeptId());
        cmd.setSignerDeptName(r.getDeptName());
        try {
            SignatureVO sig = signatureService.sign(cmd);
            r.setSignStatus(ObjectSignStatus.SIGNED.getCode());
            r.setSignId(sig.getId());
            r.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("门诊病历提交失败（病历 " + r.getRecordNo() + "）：" + e.getMessage());
        }
    }

    /**
     * 中药饮片：本味实发总克数 = 每剂克数 × 剂数（sql/139 第二条口径）。
     *
     * <p>{@code single_dosage} 是 varchar，界面上「15」「15g」「15克」都算合法，
     * 因此只取其中的数字、单位一律按克；取不到数字就拒 —— 让它按 0 克走会把一整味药开丢，
     * 事后账单上只表现为「这味没收钱」，没人会发现。
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
     *
     * <p>医保分摊不在这里算：应收只表达"这个项目多少钱"，统筹/自付属于结算层（L2），
     * 旧写法把报销部分塞进明细的"优惠金额"列，等于借了一个不属于它的字段。
     * 目录类别由 {@code FeeCatalogResolver} 按项目类型推定并快照到行上 —— 结算层按它算统筹，
     * 目录调类不改写历史应收。
     */
    private void generateChargeRecords(BizMedicalRecord record, MedicalRecordSaveDTO recordSaveDTO) {
        // 查询挂号记录获取 registNo
        BizAppointInfo appointInfo = appointInfoMapper.selectById(record.getRegistId());
        if (Objects.isNull(appointInfo)) {
            throw new BusinessException("未能找到挂号记录");
        }
        String registNo = appointInfo.getRegistNo();

        List<FeeBookDTO> books = new ArrayList<>();

        // 1. 检查明细
        LambdaQueryWrapper<BizInspectionApply> inspectionWrapper = new LambdaQueryWrapper<>();
        inspectionWrapper.eq(BizInspectionApply::getRecordId, record.getId());
        List<BizInspectionApply> inspections = inspectionApplyMapper.selectList(inspectionWrapper);
        for (BizInspectionApply apply : inspections) {
            FeeBookDTO fee = newVisitFee(record, registNo, PaymentItemTypeEnum.EXAMINATION.getCode());
            fee.setSourceType(FeeSourceTypeEnum.EXAM_APPLY.getCode());
            fee.setItemCode(apply.getInspectionItemCode());
            fee.setItemName(apply.getInspectionItemName());
            fee.setQuantity(BigDecimal.ONE);
            fee.setPrice(nz(apply.getPrice()));
            fee.setSourceId(apply.getId());
            fee.setSourceNo(apply.getApplyNo());
            books.add(fee);
        }

        // 2. 检验明细
        LambdaQueryWrapper<BizLaboratoryApply> laboratoryWrapper = new LambdaQueryWrapper<>();
        laboratoryWrapper.eq(BizLaboratoryApply::getRecordId, record.getId());
        List<BizLaboratoryApply> laboratories = laboratoryApplyMapper.selectList(laboratoryWrapper);
        for (BizLaboratoryApply apply : laboratories) {
            FeeBookDTO fee = newVisitFee(record, registNo, PaymentItemTypeEnum.LABORATORY_TEST.getCode());
            fee.setSourceType(FeeSourceTypeEnum.LAB_APPLY.getCode());
            fee.setItemCode(apply.getLaboratoryItemCode());
            fee.setItemName(apply.getLaboratoryItemName());
            fee.setQuantity(BigDecimal.ONE);
            fee.setPrice(nz(apply.getPrice()));
            fee.setSourceId(apply.getId());
            fee.setSourceNo(apply.getApplyNo());
            books.add(fee);
        }

        // 3. 药品明细（从所有处方获取，锚点仍是处方明细ID，与申请单口径一致）
        LambdaQueryWrapper<BizPrescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.eq(BizPrescription::getRecordId, record.getId());
        List<BizPrescription> prescriptions = prescriptionMapper.selectList(prescriptionWrapper);
        for (BizPrescription prescription : prescriptions) {
            LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId());
            List<BizPrescriptionDetail> prescriptionDetails = prescriptionDetailMapper.selectList(detailWrapper);
            for (BizPrescriptionDetail pd : prescriptionDetails) {
                FeeBookDTO fee = newVisitFee(record, registNo, PaymentItemTypeEnum.WESTERN_MEDICINE.getCode());
                fee.setSourceType(FeeSourceTypeEnum.PRESCRIPTION.getCode());
                fee.setItemCode(pd.getDrugCode());
                fee.setItemName(pd.getDrugName());
                fee.setSpecification(pd.getSpecification());
                fee.setUnit(pd.getUnit());
                fee.setQuantity(pd.getQuantity() == null ? BigDecimal.ONE : pd.getQuantity());
                fee.setPrice(nz(pd.getPrice()));
                fee.setSourceId(pd.getId());
                fee.setSourceNo(prescription.getPrescriptionNo());
                books.add(fee);
            }
        }

        if (books.isEmpty()) {
            return;
        }
        List<BizFeeRecord> booked = feeRecordService.bookBatch(books);
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
        dto.setCatalogType(FeeCatalogResolver.byItemType(itemType));
        return dto;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 生成患者引导单PDF（包含实际费用、项目明细和付款二维码）
     */
    private String generateGuidePdf(BizMedicalRecord record) {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
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

        // 打印路径信息用于调试
        System.out.println("=== PDF生成路径 ===");
        System.out.println("工作目录: " + uploadBaseDir);
        System.out.println("绝对路径: " + absolutePath);
        System.out.println("相对路径: " + relativePath);

        // 费用来自 L1 记账行：按项目类型聚合的应收净额，分组求和只由记账层做一次
        List<FeeTypeSumVO> receivables = feeRecordService.sumNetGroupByItemType(
                EncounterTypeEnum.OUTPATIENT.getCode(), record.getRegistId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (FeeTypeSumVO receivable : receivables) {
            totalAmount = totalAmount.add(receivable.getAmount() != null ? receivable.getAmount() : BigDecimal.ZERO);
        }

        // 查询检验检查项目
        LambdaQueryWrapper<BizLaboratoryApply> labWrapper = new LambdaQueryWrapper<>();
        labWrapper.eq(BizLaboratoryApply::getRecordId, record.getId());
        List<BizLaboratoryApply> labApplies = laboratoryApplyMapper.selectList(labWrapper);

        LambdaQueryWrapper<BizInspectionApply> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(BizInspectionApply::getRecordId, record.getId());
        List<BizInspectionApply> insApplies = inspectionApplyMapper.selectList(insWrapper);

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

            // 标题
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("患者就诊引导单", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 患者基本信息
            document.add(createParagraph("患者姓名：" + record.getPatientName(), normalFont));
            document.add(createParagraph("病历号：" + record.getRecordNo(), normalFont));
            document.add(createParagraph("就诊科室：" + record.getDeptName(), normalFont));
            document.add(createParagraph("接诊医生：" + record.getDoctorName(), normalFont));
            document.add(createParagraph("就诊日期：" + record.getVisitDate(), normalFont));
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

            // 缴费指引
            document.add(createParagraph("【缴费指引】", headerFont));
            document.add(createParagraph("请扫描下方二维码完成在线缴费", normalFont));
            document.add(createParagraph("支持支付方式：微信、支付宝、医保电子凭证", normalFont));
            document.add(createParagraph(" ", normalFont));

            // 生成付款二维码
            String paymentUrl = "https://his.example.com/pay?recordNo=" + record.getRecordNo() + "&amount=" + totalAmount.toPlainString();
            com.lowagie.text.Image qrCode = generateQrCodeImage(paymentUrl, 150, 150);
            if (qrCode != null) {
                // 使用 Paragraph 包装图片并居中
                com.lowagie.text.Paragraph qrParagraph = new com.lowagie.text.Paragraph();
                qrParagraph.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                qrCode.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                qrCode.setIndentationLeft(0);
                qrCode.setIndentationRight(0);
                qrParagraph.add(qrCode);
                document.add(qrParagraph);
            }
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

            // 检查文件是否生成成功
            java.io.File pdfFile = new java.io.File(absolutePath);
            System.out.println("PDF文件是否存在: " + pdfFile.exists());
            System.out.println("PDF文件大小: " + pdfFile.length() + " bytes");

            return relativePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private com.lowagie.text.Paragraph createParagraph(String text, com.lowagie.text.Font font) {
        com.lowagie.text.Paragraph p = new com.lowagie.text.Paragraph(text, font);
        p.setSpacingAfter(4);
        return p;
    }

    private com.lowagie.text.Image generateQrCodeImage(String content, int width, int height) {
        try {
            // 使用 Hutool 生成二维码
            BufferedImage qrImage = QrCodeUtil.generate(content, new QrConfig().setWidth(width).setHeight(height));
            // 转换为 iText Image
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(qrImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(bytes);
            image.scaleAbsolute(width, height);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成病案首页
     */
    private void generateArchive(BizMedicalRecord record) {
        BizMedicalRecordArchive archive = new BizMedicalRecordArchive();
        archive.setArchiveNo("ARC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
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
        archiveMapper.insert(archive);
    }
}
