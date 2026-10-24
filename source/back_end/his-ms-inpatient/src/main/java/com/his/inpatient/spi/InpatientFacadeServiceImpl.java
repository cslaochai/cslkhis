package com.his.inpatient.spi;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.AdmissionOrderStatusEnum;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.BedAllocateStatusEnum;
import com.his.common.enums.BedStatusEnum;
import com.his.common.enums.BedWaitStatusEnum;
import com.his.common.enums.BizTypeEnum;
import com.his.common.enums.NursingAssessTypeEnum;
import com.his.common.enums.SummaryStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.vo.DeptRefVO;
import com.his.inpatient.entity.BizAdmission;
import com.his.inpatient.entity.BizAdmissionOrder;
import com.his.inpatient.entity.BizBedAllocate;
import com.his.inpatient.entity.BizBedWait;
import com.his.inpatient.entity.BizInpatientOperation;
import com.his.inpatient.entity.BizInpatientOrder;
import com.his.inpatient.entity.BizInpatientRecord;
import com.his.inpatient.entity.BizInpatientSummary;
import com.his.inpatient.entity.BizNursingAssessment;
import com.his.inpatient.entity.BizNursingRecord;
import com.his.inpatient.entity.SysBed;
import com.his.inpatient.mapper.BedCenterMapper;
import com.his.inpatient.mapper.BizAdmissionMapper;
import com.his.inpatient.mapper.BizAdmissionOrderMapper;
import com.his.inpatient.mapper.BizBedWaitMapper;
import com.his.inpatient.mapper.BizInpatientOperationMapper;
import com.his.inpatient.mapper.BizInpatientOrderMapper;
import com.his.inpatient.mapper.BizInpatientRecordMapper;
import com.his.inpatient.mapper.BizInpatientSummaryMapper;
import com.his.inpatient.mapper.BizNursingAssessmentMapper;
import com.his.inpatient.mapper.BizNursingRecordMapper;
import com.his.inpatient.mapper.SysBedMapper;
import com.his.inpatient.support.SummaryOperationSeq;
import com.his.inpatient.vo.InpatientAdmitNotifyPayloadVO;
import com.his.spi.dto.ClosedLoopRecordDTO;
import com.his.spi.dto.InpatientAdmitDTO;
import com.his.spi.dto.SurgeryOperationDTO;
import com.his.spi.service.DictCacheService;
import com.his.spi.service.InpatientFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.service.PatientVisitFacadeService;
import com.his.spi.service.RedisSequenceService;
import com.his.spi.service.SysMessageService;
import com.his.spi.vo.AdmissionBriefVO;
import com.his.spi.vo.AdmissionVO;
import com.his.spi.vo.BedVO;
import com.his.spi.vo.ClosedLoopRecordVO;
import com.his.spi.vo.InpatientRecordBriefVO;
import com.his.spi.vo.NursingAssessmentVO;
import com.his.spi.vo.NursingVitalFactVO;
import com.his.spi.vo.PatientMasterVO;
import com.his.spi.vo.SurgeryOperationVO;
import com.his.spi.vo.WardNursingFactsVO;
import com.his.spi.vo.WardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 住院域窄端口适配器：只依赖本域 mapper、患者/就诊次 spi 端口与 common 支持类，
 * 不注入本模块编排 Service，避免 Bean 环。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InpatientFacadeServiceImpl implements InpatientFacadeService {

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizInpatientOrderMapper bizInpatientOrderMapper;

    private final BizAdmissionOrderMapper bizAdmissionOrderMapper;

    private final SysBedMapper sysBedMapper;

    private final BizInpatientSummaryMapper bizInpatientSummaryMapper;

    private final BizInpatientOperationMapper bizInpatientOperationMapper;

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

    private final BizNursingRecordMapper bizNursingRecordMapper;

    private final BizNursingAssessmentMapper bizNursingAssessmentMapper;

    private final BizBedWaitMapper bizBedWaitMapper;

    private final BedCenterMapper bedCenterMapper;

    private final PatientFacadeService patientFacadeService;

    private final PatientVisitFacadeService patientVisitFacadeService;

    private final RedisSequenceService redisSequenceService;

    private final SysMessageService sysMessageService;

    private final DictCacheService dictCacheService;

    // ---------- 入院 ----------

    @Override
    public AdmissionBriefVO findAdmission(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        return toBrief(bizAdmissionMapper.selectById(admissionId));
    }

    @Override
    public Long getAdmissionDeptId(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        BizAdmission admission = bizAdmissionMapper.selectById(admissionId);
        return admission == null ? null : admission.getDeptId();
    }

    @Override
    public DeptRefVO findDeptByOrderNo(String orderNo) {
        if (!TextUtil.hasText(orderNo)) {
            return null;
        }
        BizInpatientOrder order = bizInpatientOrderMapper.selectOne(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (order == null) {
            return null;
        }
        return new DeptRefVO(order.getDeptId(), order.getDeptName());
    }

    @Override
    public AdmissionVO getAdmission(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        BizAdmission e = bizAdmissionMapper.selectById(admissionId);
        if (e == null) {
            return null;
        }
        AdmissionVO vo = new AdmissionVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    @Override
    public WardVO getWard(Long wardId) {
        return wardId == null ? null : sysBedMapper.selectWardById(wardId);
    }

    @Override
    public String getBedNo(Long bedId) {
        if (bedId == null) {
            return null;
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        return bed == null ? null : bed.getBedNo();
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
            b.setBedStatusText(BedStatusEnum.getLabel(b.getBedStatus()));
        }
        return beds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long admit(InpatientAdmitDTO dto) {
        // C-非 web 入参：除 Controller 外还被急诊等内部路径以构造的 DTO 直调，Bean Validation 不覆盖，保留判空
        if (dto.getPatientId() == null) {
            throw new BusinessException("患者不能为空");
        }
        if (dto.getWardId() == null) {
            throw new BusinessException("病区不能为空");
        }
        if (dto.getBedId() == null) {
            throw new BusinessException("床位不能为空");
        }
        if (dto.getAdmitDoctorId() == null) {
            throw new BusinessException("入院医生不能为空");
        }

        // 住院证（门诊转住院）：有证途径由证性质决定（必为门诊）；无证直接入院，途径必须显式给出
        BizAdmissionOrder order = null;
        if (dto.getAdmissionOrderId() != null) {
            order = requireAdmittable(dto.getAdmissionOrderId());
            if (!Objects.equals(order.getPatientId(), dto.getPatientId())) {
                throw new BusinessException("住院证与所选患者不一致（证上患者：" + order.getPatientName() + "）");
            }
            if (dto.getAdmitWay() != null && !Objects.equals(1, dto.getAdmitWay())) {
                log.warn("有住院证（门诊转住院）时忽略传入的入院途径 {}，强制为「门诊」", dto.getAdmitWay());
            }
        } else {
            if (dto.getAdmitWay() == null) {
                throw new BusinessException("入院途径不能为空（病案首页必填项）");
            }
        }
        int admitWay = order != null ? 1 : dto.getAdmitWay();

        PatientMasterVO patient = patientFacadeService.getPatient(dto.getPatientId());
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
        // 床位可用性 = 空闲，或床位中心给本患者预留的锁定床（锁定床必须挂同一个患者才放行）
        boolean reservedForSelf = Objects.equals(BedStatusEnum.LOCKED.getCode(), bed.getBedStatus())
                && Objects.equals(dto.getPatientId(), bed.getPatientId());
        if (!Objects.equals(BedStatusEnum.FREE.getCode(), bed.getBedStatus()) && !reservedForSelf) {
            throw new BusinessException("床位当前不可用（" + BedStatusEnum.getLabelOrUnknown(bed.getBedStatus()) + "），请选择空闲床位");
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
        admission.setAdmissionNo(redisSequenceService.generateAdmissionNo());
        admission.setPatientId(dto.getPatientId());
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

        // 收治回填必须放在最后：入院记录真的落库了，才把证置为已收治
        if (order != null) {
            markAdmitted(order, admission.getAdmissionId(), deptId, admitTime);
            notifyAdmitted(order, patient, ward, bed, admission.getAdmissionNo());
        }

        // 收治 → 等床队列收口（同一事务）；队列记账失败不回滚收治
        try {
            markAdmittedByPatient(dto.getPatientId(), admission.getAdmissionId(), admitTime);
        } catch (Exception e) {
            log.warn("[入院登记] 回填床位排队队列失败 admissionNo={} patientId={}",
                    admission.getAdmissionNo(), dto.getPatientId(), e);
        }

        log.info("入院登记成功 admissionNo={} admissionId={} patientId={} bedId={} 住院证={} 挂号={} 就诊次={}",
                admission.getAdmissionNo(), admission.getAdmissionId(), dto.getPatientId(), dto.getBedId(),
                order == null ? "-" : order.getOrderNo(), registNo == null ? "-" : registNo, admission.getVisitId());
        return admission.getAdmissionId();
    }

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
            throw new BusinessException("床位当前不可用（" + BedStatusEnum.getLabelOrUnknown(bed.getBedStatus()) + "），请选择空闲床位");
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

    // ---------- 病案首页 ----------

    @Override
    public boolean isSummaryArchived(Long admissionId) {
        BizInpatientSummary summary = bizInpatientSummaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, admissionId)
                .last("LIMIT 1"));
        return summary != null && Objects.equals(SummaryStatusEnum.ARCHIVED.getCode(), summary.getSummaryStatus());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurgeryOperationVO appendSurgeryOperation(SurgeryOperationDTO dto) {
        BizInpatientOperation operation = new BizInpatientOperation();
        BeanUtils.copyProperties(dto, operation);

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

        operation.setSeqNo(existing.size() + 1);
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

        SurgeryOperationVO vo = new SurgeryOperationVO();
        vo.setId(operation.getId());
        return vo;
    }

    // ---------- 病历文书 ----------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosedLoopRecordVO appendClosedLoopRecord(ClosedLoopRecordDTO dto) {
        BizInpatientRecord record = new BizInpatientRecord();
        BeanUtils.copyProperties(dto, record);
        record.setRecordNo(redisSequenceService.generateInpatientRecordNo());
        bizInpatientRecordMapper.insert(record);
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
        // C-非 web 入参：his-ai 能力跨模块直调（内部派生 admissionId），Bean Validation 不覆盖，保留
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        List<BizNursingRecord> rows = bizNursingRecordMapper.selectList(new LambdaQueryWrapper<BizNursingRecord>()
                .eq(BizNursingRecord::getAdmissionId, admissionId)
                .ge(since != null, BizNursingRecord::getMeasureTime, since)
                .and(w -> w.isNotNull(BizNursingRecord::getTemperature)
                        .or().isNotNull(BizNursingRecord::getPulse)
                        .or().isNotNull(BizNursingRecord::getRespiration)
                        .or().isNotNull(BizNursingRecord::getSystolicPressure)
                        .or().isNotNull(BizNursingRecord::getDiastolicPressure)
                        .or().isNotNull(BizNursingRecord::getSpo2))
                .orderByDesc(BizNursingRecord::getMeasureTime)
                .last("LIMIT 1"));
        return rows.isEmpty() ? null : toVitalFact(rows.get(0));
    }

    @Override
    public List<NursingVitalFactVO> latestVitalsByWard(Long wardId, LocalDateTime since) {
        // C-非 web 入参：his-ai 能力跨模块直调（内部派生 wardId），Bean Validation 不覆盖，保留
        if (wardId == null) {
            throw new BusinessException("病区ID不能为空");
        }
        List<BizNursingRecord> rows = bizNursingRecordMapper.selectList(new LambdaQueryWrapper<BizNursingRecord>()
                .eq(BizNursingRecord::getWardId, wardId)
                .ge(since != null, BizNursingRecord::getMeasureTime, since)
                .and(w -> w.isNotNull(BizNursingRecord::getTemperature)
                        .or().isNotNull(BizNursingRecord::getPulse)
                        .or().isNotNull(BizNursingRecord::getRespiration)
                        .or().isNotNull(BizNursingRecord::getSystolicPressure)
                        .or().isNotNull(BizNursingRecord::getDiastolicPressure)
                        .or().isNotNull(BizNursingRecord::getSpo2)));
        Map<Long, BizNursingRecord> latest = new HashMap<>();
        for (BizNursingRecord r : rows) {
            if (r.getAdmissionId() == null || r.getMeasureTime() == null) {
                continue;
            }
            BizNursingRecord kept = latest.get(r.getAdmissionId());
            if (kept == null || r.getMeasureTime().isAfter(kept.getMeasureTime())) {
                latest.put(r.getAdmissionId(), r);
            }
        }
        List<NursingVitalFactVO> result = new ArrayList<>(latest.size());
        for (BizNursingRecord r : latest.values()) {
            result.add(toVitalFact(r));
        }
        result.sort((a, b) -> b.getMeasureTime().compareTo(a.getMeasureTime()));
        return result;
    }

    @Override
    public WardNursingFactsVO wardShiftFacts(Long wardId, LocalDateTime begin, LocalDateTime end, Integer shift) {
        // C-非 web 入参：his-ai 交接班能力跨模块直调（内部派生病区与时间窗），Bean Validation 不覆盖，保留
        if (wardId == null || begin == null || end == null) {
            throw new BusinessException("病区ID与时间窗不能为空");
        }
        WardNursingFactsVO vo = new WardNursingFactsVO();
        vo.setWardId(wardId);
        WardVO ward = sysBedMapper.selectWardById(wardId);
        vo.setWardName(ward == null ? null : ward.getWardName());
        vo.setShift(shift);
        vo.setShiftText(dictCacheService.getDicDataLabel(DictTypeConst.NURSING_SHIFT, shift));
        vo.setWindowBegin(begin);
        vo.setWindowEnd(end);

        vo.setVitalRows(bizNursingRecordMapper.selectList(new LambdaQueryWrapper<BizNursingRecord>()
                        .eq(BizNursingRecord::getWardId, wardId)
                        .ge(BizNursingRecord::getMeasureTime, begin)
                        .lt(BizNursingRecord::getMeasureTime, end)
                        .orderByAsc(BizNursingRecord::getMeasureTime))
                .stream().map(this::toVitalFact).collect(Collectors.toList()));

        vo.setAssessmentRows(bizNursingAssessmentMapper.selectList(new LambdaQueryWrapper<BizNursingAssessment>()
                        .eq(BizNursingAssessment::getWardId, wardId)
                        .ge(BizNursingAssessment::getAssessTime, begin)
                        .lt(BizNursingAssessment::getAssessTime, end)
                        .orderByAsc(BizNursingAssessment::getAssessTime))
                .stream().map(this::toAssessVO).collect(Collectors.toList()));

        WardNursingFactsVO.Census census = new WardNursingFactsVO.Census();
        census.setInHospitalCount(Math.toIntExact(bizAdmissionMapper.selectCount(new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getWardId, wardId)
                .eq(BizAdmission::getAdmitStatus, AdmitStatusEnum.IN_HOSPITAL.getCode()))));
        census.setDischargeCount(Math.toIntExact(bizAdmissionMapper.selectCount(new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getWardId, wardId)
                .eq(BizAdmission::getAdmitStatus, AdmitStatusEnum.DISCHARGED.getCode())
                .ge(BizAdmission::getDischargeTime, begin)
                .lt(BizAdmission::getDischargeTime, end))));
        List<BizAdmission> newAdmissions = bizAdmissionMapper.selectList(new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getWardId, wardId)
                .ge(BizAdmission::getAdmitTime, begin)
                .lt(BizAdmission::getAdmitTime, end)
                .orderByAsc(BizAdmission::getAdmitTime));
        for (BizAdmission a : newAdmissions) {
            WardNursingFactsVO.AdmissionBrief brief = new WardNursingFactsVO.AdmissionBrief();
            brief.setAdmissionId(a.getAdmissionId());
            brief.setPatientName(patientNameOf(a.getPatientId()));
            brief.setBedNo(bedNoOf(a.getBedId()));
            brief.setAdmitTime(a.getAdmitTime());
            brief.setAdmitDiagnosisName(a.getAdmitDiagnosisName());
            census.getNewAdmissions().add(brief);
        }
        vo.setCensus(census);
        return vo;
    }

    // ---------------------------------------------------------------- 私有辅助

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

    private BigDecimal percent(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private Long ensureVisit(Long patientId, Long registId) {
        return patientVisitFacadeService.ensureOpenVisit(patientId, registId);
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
     * 床位释放：状态置空闲 + 清患者。必须显式 set(patientId, null)——updateById 的 NOT_NULL
     * 策略会跳过 null 字段，导致患者一直挂在床位上（脏占用）。
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

    private void createSummaryDraft(BizAdmission admission, PatientMasterVO patient, WardVO ward, SysBed bed) {
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

    private BizAdmissionOrder requireAdmittable(Long orderId) {
        if (orderId == null) {
            throw new BusinessException("住院证ID不能为空");
        }
        BizAdmissionOrder order = bizAdmissionOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("住院证不存在");
        }
        if (!Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), order.getOrderStatus())) {
            throw new BusinessException("住院证当前状态为「"
                    + AdmissionOrderStatusEnum.getLabelOrUnknown(order.getOrderStatus()) + "」，不能收治");
        }
        if (order.getValidUntil() != null && order.getValidUntil().isBefore(TimeUtil.nowSeconds())) {
            throw new BusinessException("住院证已于 " + order.getValidUntil() + " 过期，不能再收治，请重新开证");
        }
        return order;
    }

    private void markAdmitted(BizAdmissionOrder order, Long admissionId, Long admitDeptId, LocalDateTime admitTime) {
        // 重新取一次并再判状态：并发下两个入院处同时点"收治"时，靠这里的乐观判断挡住第二个人
        BizAdmissionOrder latest = bizAdmissionOrderMapper.selectById(order.getId());
        if (latest == null) {
            throw new BusinessException("住院证不存在");
        }
        if (!Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), latest.getOrderStatus())) {
            throw new BusinessException("住院证已被收治或作废，不能重复收治");
        }
        latest.setOrderStatus(AdmissionOrderStatusEnum.ADMITTED.getCode());
        latest.setAdmissionId(admissionId);
        latest.setAdmitTime(admitTime);
        latest.setAdmitDeptId(admitDeptId);
        bizAdmissionOrderMapper.updateById(latest);
        log.info("住院证已收治 orderNo={} admissionId={} admitDeptId={}",
                latest.getOrderNo(), admissionId, admitDeptId);
    }

    /**
     * 收治完成 → 站内信通知开证医生（bizType=admit，通知型：read_status 即闭环）。
     * 通知失败只留 warn 日志，不回滚收治事务。
     */
    private void notifyAdmitted(BizAdmissionOrder order, PatientMasterVO patient, WardVO ward, SysBed bed, String admissionNo) {
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

    private void markAdmittedByPatient(Long patientId, Long admissionId, LocalDateTime admitTime) {
        if (patientId == null || admissionId == null) {
            return;
        }
        List<BizBedWait> rows = bizBedWaitMapper.selectList(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getPatientId, patientId)
                .in(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode(), BedWaitStatusEnum.ARRANGED.getCode()));
        if (rows.isEmpty()) {
            return;
        }
        LocalDateTime time = TimeUtil.toSeconds(admitTime == null ? LocalDateTime.now() : admitTime);
        for (BizBedWait w : rows) {
            BizBedWait upd = new BizBedWait();
            upd.setId(w.getId());
            upd.setWaitStatus(BedWaitStatusEnum.ADMITTED.getCode());
            upd.setAdmissionId(admissionId);
            upd.setAdmitTime(time);
            bizBedWaitMapper.updateById(upd);

            LambdaUpdateWrapper<BizBedAllocate> allocUpd = new LambdaUpdateWrapper<>();
            allocUpd.eq(BizBedAllocate::getWaitId, w.getId())
                    .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())
                    .set(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.ADMITTED.getCode())
                    .set(BizBedAllocate::getAdmissionId, admissionId);
            bedCenterMapper.update(null, allocUpd);
        }
        log.info("入院回填队列：patientId={} 收治={} 条，admissionId={}", patientId, rows.size(), admissionId);
    }

    private NursingVitalFactVO toVitalFact(BizNursingRecord r) {
        NursingVitalFactVO vo = new NursingVitalFactVO();
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setBedNo(r.getBedNo());
        vo.setWardId(r.getWardId());
        vo.setWardName(r.getWardName());
        vo.setMeasureTime(r.getMeasureTime());
        vo.setShift(r.getShift());
        vo.setTemperature(r.getTemperature());
        vo.setPulse(r.getPulse());
        vo.setRespiration(r.getRespiration());
        vo.setSystolicPressure(r.getSystolicPressure());
        vo.setDiastolicPressure(r.getDiastolicPressure());
        vo.setSpo2(r.getSpo2());
        vo.setNursingLevel(r.getNursingLevel());
        vo.setNursingContent(r.getNursingContent());
        return vo;
    }

    private NursingAssessmentVO toAssessVO(BizNursingAssessment row) {
        NursingAssessmentVO vo = new NursingAssessmentVO();
        vo.setId(row.getId());
        vo.setAssessNo(row.getAssessNo());
        vo.setAdmissionId(row.getAdmissionId());
        vo.setPatientId(row.getPatientId());
        vo.setPatientNo(row.getPatientNo());
        vo.setPatientName(row.getPatientName());
        vo.setWardName(row.getWardName());
        vo.setBedNo(row.getBedNo());
        vo.setAssessType(row.getAssessType());
        vo.setAssessTypeText(NursingAssessTypeEnum.getLabel(row.getAssessType()));
        vo.setTotalScore(row.getTotalScore());
        vo.setRiskLevel(row.getRiskLevel());
        vo.setRiskLevelText(dictCacheService.getDicDataLabel(DictTypeConst.ASSESS_RISK_LEVEL, row.getRiskLevel()));
        vo.setItemsJson(row.getItemsJson());
        vo.setAssessTime(row.getAssessTime());
        vo.setAssessNurseId(row.getAssessNurseId());
        vo.setAssessNurseName(row.getAssessNurseName());
        vo.setRemark(row.getRemark());
        return vo;
    }

    private String patientNameOf(Long patientId) {
        PatientMasterVO p = patientFacadeService.getPatient(patientId);
        return p == null ? null : p.getPatientName();
    }

    private String bedNoOf(Long bedId) {
        SysBed b = sysBedMapper.selectById(bedId);
        return b == null ? null : b.getBedNo();
    }
}
