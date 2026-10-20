package com.his.patient.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.spi.dto.ClosedLoopRecordDTO;
import com.his.spi.dto.EmergencyPatientCreateDTO;
import com.his.spi.dto.InpatientAdmitDTO;
import com.his.spi.dto.SurgeryOperationDTO;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.util.TextUtil;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.AdmissionBriefVO;
import com.his.spi.vo.AdmissionVO;
import com.his.spi.vo.AllergyFactVO;
import com.his.spi.vo.BedVO;
import com.his.spi.vo.ClosedLoopRecordVO;
import com.his.common.vo.DeptRefVO;
import com.his.spi.vo.InpatientRecordBriefVO;
import com.his.spi.vo.NursingVitalFactVO;
import com.his.spi.vo.PatientBriefVO;
import com.his.spi.vo.PatientMasterVO;
import com.his.spi.vo.PatientProfileScoreVO;
import com.his.spi.vo.SurgeryOperationVO;
import com.his.spi.vo.WardNursingFactsVO;
import com.his.spi.vo.WardVO;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizInpatientOperation;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientAllergy;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.BizInpatientOrderMapper;
import com.his.patient.mapper.BizInpatientRecordMapper;
import com.his.patient.mapper.BizPatientAllergyMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.BizPatientService;
import com.his.patient.service.InpatientNursingService;
import com.his.patient.service.InpatientRecordService;
import com.his.patient.service.InpatientService;
import com.his.patient.service.PatientGuardianService;
import com.his.patient.service.PatientIndexService;
import com.his.patient.support.PatientProfileFields;
import com.his.patient.support.PatientProfileValidator;
import com.his.system.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientFacadeServiceImpl implements PatientFacadeService {

    private final BizPatientMapper bizPatientMapper;

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizInpatientOrderMapper bizInpatientOrderMapper;

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

    private final BizPatientAllergyMapper bizPatientAllergyMapper;

    private final PatientGuardianService patientGuardianService;

    private final BizPatientService bizPatientService;

    private final InpatientService inpatientService;

    private final InpatientRecordService inpatientRecordService;

    private final InpatientNursingService inpatientNursingService;

    private final PatientIndexService patientIndexService;

    private final RedisSequenceService redisSequenceService;

    @Override
    public PatientBriefVO findPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return toBrief(bizPatientMapper.selectById(patientId));
    }

    @Override
    public AdmissionBriefVO findAdmission(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        return toBrief(bizAdmissionMapper.selectById(admissionId));
    }

    @Override
    public DeptRefVO findDeptByOrderNo(String orderNo) {
        if (!TextUtil.hasText(orderNo)) {
            return null;
        }
        BizInpatientOrder order = bizInpatientOrderMapper.selectOne(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        return order == null ? null
                : new DeptRefVO(order.getDeptId(), order.getDeptName());
    }

    // ---------- 就诊人绑定 / 越权判定 ----------

    @Override
    public boolean canAccessPatient(Long patientId) {
        return patientGuardianService.canAccessPatient(patientId);
    }

    @Override
    public boolean patientScopeViolated(Long patientId) {
        return patientGuardianService.patientScopeViolated(patientId);
    }

    @Override
    public void bindOpenid(String openid) {
        patientGuardianService.bindOpenid(openid);
    }

    // ---------- 患者主档 ----------

    @Override
    public PatientMasterVO getPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return toMaster(bizPatientMapper.selectById(patientId));
    }

    @Override
    public List<AllergyFactVO> listAllergies(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        return bizPatientAllergyMapper.selectList(new LambdaQueryWrapper<BizPatientAllergy>()
                        .eq(BizPatientAllergy::getPatientId, patientId))
                .stream().map(e -> {
                    AllergyFactVO vo = new AllergyFactVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public PatientMasterVO createEmergencyPatient(EmergencyPatientCreateDTO dto) {
        BizPatient patient = new BizPatient();
        patient.setPatientNo(redisSequenceService.generatePatientNo());
        patient.setPatientName(dto.getPatientName());
        patient.setGender(dto.getGender());
        patient.setAge(dto.getAge());
        patient.setPhone(dto.getPhone());
        // 身份证：急诊可以没有，但填了就要落库 —— 否则 EMPI 的同证去重拦不住急诊建档
        patient.setIdCard(dto.getIdCard());
        // 有身份证就以它为准补出生日期/年龄（和主档建档同一口径，见 PatientController#fillBirthDateAndAge）
        LocalDate birthDate = PatientProfileValidator.birthDateOfIdCard(dto.getIdCard());
        if (birthDate != null) {
            patient.setBirthDate(birthDate);
            patient.setAge(Period.between(birthDate, LocalDate.now()).getYears());
        }
        patient.setStatus(YesOrNoEnum.YES.getCode());
        // 急诊建档是第二条写患者基本信息的路径，同样要过写入口校验：
        // 三无患者允许没有身份证，但姓名和性别不能省（下游性别判断全靠它）
        PatientProfileValidator.validateForEmergency(patient);
        bizPatientMapper.insert(patient);
        return toMaster(patient);
    }

    @Override
    public void markFirstVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                               Long doctorId, String doctorName) {
        bizPatientService.markFirstVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
    }

    @Override
    public void markLastVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                              Long doctorId, String doctorName) {
        bizPatientService.markLastVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
    }

    @Override
    public PatientProfileScoreVO profileScore(Long patientId, Set<String> coveredKeys) {
        BizPatient patient = patientId == null ? null : bizPatientMapper.selectById(patientId);
        if (patient == null) {
            return null;
        }
        PatientProfileFields.ProfileScore score = PatientProfileFields.applyProfileCoverage(
                PatientProfileFields.score(patient), coveredKeys);
        PatientProfileScoreVO vo = new PatientProfileScoreVO();
        vo.setCompleteRate((int) Math.floor(score.rate()));
        vo.setMissingFields(score.missingFields());
        return vo;
    }

    // ---------- 住院 ----------

    @Override
    public void fillInsuranceIfAbsent(Long patientId, String medicalInsuranceType, String medicalInsuranceNo) {
        BizPatient patient = patientId == null ? null : bizPatientMapper.selectById(patientId);
        if (patient == null) {
            return;
        }
        boolean needUpdate = false;
        if (TextUtil.hasText(medicalInsuranceType) && !TextUtil.hasText(patient.getMedicalInsuranceType())) {
            patient.setMedicalInsuranceType(medicalInsuranceType);
            needUpdate = true;
        }
        if (TextUtil.hasText(medicalInsuranceNo) && !TextUtil.hasText(patient.getMedicalInsuranceNo())) {
            patient.setMedicalInsuranceNo(medicalInsuranceNo);
            needUpdate = true;
        }
        if (needUpdate) {
            bizPatientMapper.updateById(patient);
        }
    }

    @Override
    public AdmissionVO getAdmission(Long admissionId) {
        BizAdmission e = inpatientService.getAdmissionById(admissionId);
        if (e == null) {
            return null;
        }
        AdmissionVO vo = new AdmissionVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    @Override
    public WardVO getWard(Long wardId) {
        return inpatientService.getWardById(wardId);
    }

    @Override
    public String getBedNo(Long bedId) {
        return inpatientService.getBedNoById(bedId);
    }

    @Override
    public List<WardVO> listWards() {
        return inpatientService.listWards();
    }

    @Override
    public List<BedVO> listBeds(Long wardId, Long deptId, Integer bedStatus) {
        return inpatientService.listBeds(wardId, deptId, bedStatus);
    }

    @Override
    public Long admit(InpatientAdmitDTO dto) {
        return inpatientService.admit(dto);
    }

    @Override
    public void occupyBedForObservation(Long bedId, Long patientId) {
        inpatientService.occupyBedForObservation(bedId, patientId);
    }

    @Override
    public void releaseBedForObservation(Long bedId, Long patientId) {
        inpatientService.releaseBedForObservation(bedId, patientId);
    }

    @Override
    public boolean isSummaryArchived(Long admissionId) {
        return inpatientService.isSummaryArchived(admissionId);
    }

    @Override
    public void markSummaryTransfused(Long admissionId) {
        inpatientService.markSummaryTransfused(admissionId);
    }

    @Override
    public SurgeryOperationVO appendSurgeryOperation(SurgeryOperationDTO dto) {
        BizInpatientOperation op = new BizInpatientOperation();
        BeanUtils.copyProperties(dto, op);
        op = inpatientService.appendSurgeryOperation(op);
        SurgeryOperationVO vo = new SurgeryOperationVO();
        vo.setId(op.getId());
        return vo;
    }

    // ---------- 病历文书 ----------

    @Override
    public ClosedLoopRecordVO appendClosedLoopRecord(ClosedLoopRecordDTO dto) {
        BizInpatientRecord record = new BizInpatientRecord();
        BeanUtils.copyProperties(dto, record);
        record = inpatientRecordService.appendClosedLoopRecord(record);
        ClosedLoopRecordVO vo = new ClosedLoopRecordVO();
        vo.setId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        return vo;
    }

    @Override
    public InpatientRecordBriefVO getInpatientRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        BizInpatientRecord e = bizInpatientRecordMapper.selectById(recordId);
        if (e == null) {
            return null;
        }
        InpatientRecordBriefVO vo = new InpatientRecordBriefVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    // ---------- 护理事实面 ----------

    @Override
    public NursingVitalFactVO latestVitalByAdmission(Long admissionId, LocalDateTime since) {
        return inpatientNursingService.latestVitalByAdmission(admissionId, since);
    }

    @Override
    public List<NursingVitalFactVO> latestVitalsByWard(Long wardId, LocalDateTime since) {
        return inpatientNursingService.latestVitalsByWard(wardId, since);
    }

    @Override
    public WardNursingFactsVO wardShiftFacts(Long wardId, LocalDateTime begin, LocalDateTime end, Integer shift) {
        return inpatientNursingService.wardShiftFacts(wardId, begin, end, shift);
    }

    // ---------- EMPI ----------

    @Override
    public List<Long> resolvePatientIds(Long patientId) {
        return patientIndexService.resolvePatientIds(patientId);
    }

    // ---------------------------------------------------------------- 转换器

    private PatientBriefVO toBrief(BizPatient e) {
        if (e == null) {
            return null;
        }
        PatientBriefVO brief = new PatientBriefVO();
        brief.setId(e.getId());
        brief.setPatientNo(e.getPatientNo());
        brief.setPatientName(e.getPatientName());
        brief.setGender(e.getGender());
        brief.setAge(e.getAge());
        brief.setPhone(e.getPhone());
        brief.setIdCard(e.getIdCard());
        brief.setMedicalInsuranceType(e.getMedicalInsuranceType());
        brief.setMedicalInsuranceNo(e.getMedicalInsuranceNo());
        return brief;
    }

    private AdmissionBriefVO toBrief(BizAdmission e) {
        if (e == null) {
            return null;
        }
        AdmissionBriefVO brief = new AdmissionBriefVO();
        brief.setAdmissionId(e.getAdmissionId());
        brief.setAdmissionNo(e.getAdmissionNo());
        brief.setPatientId(e.getPatientId());
        brief.setAdmitTime(e.getAdmitTime());
        brief.setAdmitDoctorId(e.getAdmitDoctorId());
        brief.setDiagnosis(e.getDiagnosis());
        return brief;
    }

    private PatientMasterVO toMaster(BizPatient e) {
        if (e == null) {
            return null;
        }
        PatientMasterVO vo = new PatientMasterVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }
}
