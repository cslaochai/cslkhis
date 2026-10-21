package com.his.clinical.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.enums.QcRecordSourceEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.utils.UserUtils;
import com.his.clinical.entity.BizClinicalRuleCheck;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizPrescription;
import com.his.clinical.entity.BizPrescriptionDetail;
import com.his.clinical.entity.BizQualityControl;
import com.his.clinical.mapper.BizClinicalRuleCheckMapper;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.clinical.mapper.BizPrescriptionDetailMapper;
import com.his.clinical.mapper.BizPrescriptionMapper;
import com.his.clinical.mapper.BizQualityControlMapper;
import com.his.spi.dto.AiClinicalRuleCheckSaveDTO;
import com.his.spi.dto.AiQualityControlSaveDTO;
import com.his.spi.service.AiEmrDataFacadeService;
import com.his.spi.service.RedisSequenceService;
import com.his.spi.vo.AiMedicalRecordContentVO;
import com.his.spi.vo.AiPrescriptionBriefVO;
import com.his.spi.vo.AiPrescriptionDetailRowVO;
import com.his.spi.vo.AiQualityControlWriteVO;
import com.his.spi.vo.AiRuleCheckWriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AiEmrDataFacadeService 的病历域实现（叶子端口：只依赖本模块 mapper 与系统序号服务）。
 */
@Service
@RequiredArgsConstructor
public class AiEmrDataFacadeServiceImpl implements AiEmrDataFacadeService {

    /** biz_clinical_rule_check.rule_name varchar(100) */
    private static final int RULE_NAME_MAX = 100;
    /** biz_clinical_rule_check.rule_content / biz_quality_control.qc_content varchar(500) */
    private static final int CONTENT_MAX = 500;
    /** biz_clinical_rule_check.suggestion / biz_quality_control.remark varchar(500) */
    private static final int SUGGESTION_MAX = 500;
    /** error_detail varchar(1000) */
    private static final int ERROR_DETAIL_MAX = 1000;

    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final BizMedicalRecordMapper bizMedicalRecordMapper;
    private final BizClinicalRuleCheckMapper bizClinicalRuleCheckMapper;
    private final BizQualityControlMapper bizQualityControlMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    public AiPrescriptionBriefVO findPrescription(Long prescriptionId) {
        if (prescriptionId == null) {
            return null;
        }
        BizPrescription prescription = bizPrescriptionMapper.selectById(prescriptionId);
        if (prescription == null
                || Objects.equals(PrescriptionStatusEnum.CANCELLED.getCode(), prescription.getPrescriptionStatus())) {
            return null;
        }
        AiPrescriptionBriefVO vo = new AiPrescriptionBriefVO();
        vo.setId(prescription.getId());
        vo.setPrescriptionNo(prescription.getPrescriptionNo());
        vo.setPatientId(prescription.getPatientId());
        vo.setRecordId(prescription.getRecordId());
        vo.setGender(prescription.getGender());
        vo.setAge(prescription.getAge());
        vo.setDiagnosis(prescription.getDiagnosis());
        return vo;
    }

    @Override
    public List<AiPrescriptionDetailRowVO> listPrescriptionDetails(Long prescriptionId) {
        if (prescriptionId == null) {
            return Collections.emptyList();
        }
        return bizPrescriptionDetailMapper.selectList(new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId)
                        .orderByAsc(BizPrescriptionDetail::getId)).stream()
                .map(this::toDetailRow)
                .collect(Collectors.toList());
    }

    @Override
    public AiMedicalRecordContentVO findMedicalRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        BizMedicalRecord record = bizMedicalRecordMapper.selectById(recordId);
        if (record == null
                || Objects.equals(RecordStatusEnum.VOIDED.getCode(), record.getRecordStatus())) {
            return null;
        }
        AiMedicalRecordContentVO vo = new AiMedicalRecordContentVO();
        vo.setId(record.getId());
        vo.setPatientId(record.getPatientId());
        vo.setGender(record.getGender());
        vo.setAge(record.getAge());
        vo.setChiefComplaint(record.getChiefComplaint());
        vo.setPresentIllness(record.getPresentIllness());
        vo.setPastHistory(record.getPastHistory());
        vo.setPersonalHistory(record.getPersonalHistory());
        vo.setFamilyHistory(record.getFamilyHistory());
        vo.setAllergyHistory(record.getAllergyHistory());
        vo.setGeneralCondition(record.getGeneralCondition());
        vo.setSkinMucosa(record.getSkinMucosa());
        vo.setHeadNeck(record.getHeadNeck());
        vo.setChestLung(record.getChestLung());
        vo.setHeart(record.getHeart());
        vo.setAbdomen(record.getAbdomen());
        vo.setSpineLimbs(record.getSpineLimbs());
        vo.setNervousSystem(record.getNervousSystem());
        vo.setSpecialistExam(record.getSpecialistExam());
        vo.setAuxiliaryExam(record.getAuxiliaryExam());
        vo.setTemperature(record.getTemperature());
        vo.setPulse(record.getPulse());
        vo.setRespiration(record.getRespiration());
        vo.setSystolicPressure(record.getSystolicPressure());
        vo.setDiastolicPressure(record.getDiastolicPressure());
        vo.setDiagnosis(record.getDiagnosis());
        vo.setDiagnosisName(record.getDiagnosisName());
        vo.setTreatmentPlan(record.getTreatmentPlan());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRuleCheckWriteVO saveRuleCheck(AiClinicalRuleCheckSaveDTO dto) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizClinicalRuleCheck check = new BizClinicalRuleCheck();
        check.setCheckNo(redisSequenceService.generateRuleCheckAiNo());
        check.setRecordId(dto.getRecordId());
        check.setPatientId(dto.getPatientId());
        check.setRuleType(dto.getRuleType());
        check.setRuleName(TextUtil.cut(dto.getRuleName(), RULE_NAME_MAX));
        check.setRuleContent(TextUtil.cut(dto.getRuleContent(), CONTENT_MAX));
        check.setCheckResult(dto.getCheckResult());
        check.setErrorLevel(dto.getErrorLevel());
        check.setErrorDetail(TextUtil.cut(dto.getErrorDetail(), ERROR_DETAIL_MAX));
        check.setSuggestion(TextUtil.cut(dto.getSuggestion(), SUGGESTION_MAX));
        check.setCheckStatus(dto.getCheckStatus());
        check.setCheckBy(currentUser.getRealName());
        check.setCheckTime(TimeUtil.nowSeconds());
        bizClinicalRuleCheckMapper.insert(check);

        AiRuleCheckWriteVO vo = new AiRuleCheckWriteVO();
        vo.setId(check.getId());
        vo.setCheckNo(check.getCheckNo());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiQualityControlWriteVO saveQualityControl(AiQualityControlSaveDTO dto) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizQualityControl qc = new BizQualityControl();
        qc.setQcNo(redisSequenceService.generateEmrQcNo());
        qc.setRecordId(dto.getRecordId());
        qc.setRecordSource(QcRecordSourceEnum.OUTPATIENT.getCode());
        qc.setPatientId(dto.getPatientId());
        qc.setQcType(dto.getQcType());
        qc.setQcContent(TextUtil.cut(dto.getQcContent(), CONTENT_MAX));
        qc.setQcResult(dto.getQcResult());
        qc.setErrorCount(dto.getErrorCount());
        qc.setErrorDetail(TextUtil.cut(dto.getErrorDetail(), ERROR_DETAIL_MAX));
        qc.setQcStatus(dto.getQcStatus());
        qc.setQcBy(currentUser.getRealName());
        qc.setQcTime(TimeUtil.nowSeconds());
        qc.setRemark(TextUtil.cut(dto.getRemark(), SUGGESTION_MAX));
        bizQualityControlMapper.insert(qc);

        AiQualityControlWriteVO vo = new AiQualityControlWriteVO();
        vo.setId(qc.getId());
        vo.setQcNo(qc.getQcNo());
        return vo;
    }

    private AiPrescriptionDetailRowVO toDetailRow(BizPrescriptionDetail row) {
        AiPrescriptionDetailRowVO vo = new AiPrescriptionDetailRowVO();
        vo.setId(row.getId());
        vo.setDrugId(row.getDrugId());
        vo.setDrugName(row.getDrugName());
        vo.setGenericName(row.getGenericName());
        vo.setSpecification(row.getSpecification());
        vo.setSingleDosage(row.getSingleDosage());
        vo.setFrequency(row.getFrequency());
        vo.setRoute(row.getRoute());
        vo.setDuration(row.getDuration());
        vo.setQuantity(row.getQuantity());
        vo.setIsSkinTest(row.getIsSkinTest());
        return vo;
    }
}
