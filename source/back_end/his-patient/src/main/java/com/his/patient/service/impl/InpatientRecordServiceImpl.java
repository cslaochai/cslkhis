package com.his.patient.service.impl;
import com.his.common.util.TimeUtil;
import com.his.patient.enums.AgeUnitEnum;
import com.his.patient.enums.InpatientRecordTypeEnum;
import com.his.patient.enums.SummaryStatusEnum;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.RecordDocTypeEnum;
import com.his.patient.mapper.*;
import com.his.patient.service.InpatientRecordService;
import com.his.patient.enums.InpatientRecordStatusEnum;
import com.his.patient.enums.NursingDocFieldEnum;
import com.his.patient.support.RecordStructuredFields;
import com.his.patient.vo.*;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 住院病历文书服务实现（P2）。
 *
 * <p>本类固化这些**至少会被追问一次**的点：
 *
 * <ol>
 *   <li><b>结构化率只有一个口径</b>：要素清单在 {@link RecordStructuredFields} 定义一次，
 *       统计 / 缺项定位 / 详情页的要素明细全走它。分母按文书类型算（病程才有病程正文），
 *       否则入院记录的"永远差一项"会逼医生填废话凑分。</li>
 *   <li><b>数值 0 不是"没填"</b>：大便 0 次、尿量 0ml 是合法观测值。
 *       把 0 当空值，三测单统计就永远差几条 —— 与「未判定 ≠ 正常」同一条线。</li>
 *   <li><b>修改 = 传什么覆盖什么 + 逐字段 diff 留痕</b>：只有值真的变了才写日志行。
 *       "每次保存写全字段快照"会让日志表膨胀到无法回答"这句话是谁改的"。</li>
 *   <li><b>批量动作先全量校验再写</b>：批量归档里有一份还是草稿就整批拒绝，
 *       并指名是哪一份。部分成功会让病案室不知道哪些生效了。</li>
 *   <li><b>入院记录的必填要素由服务层强制</b>（主诉/现病史/既往史/过敏史/诊断 + 五项体征）：
 *       这是三甲评审的硬口径，放前端"提醒"等于没有约束。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientRecordServiceImpl implements InpatientRecordService {
    private final DeptScopeProvider deptScopeProvider;

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final BizInpatientRecordMapper recordMapper;
    private final BizInpatientRecordLogMapper logMapper;
    private final BizNursingRecordMapper nursingRecordMapper;
    private final BizAdmissionMapper admissionMapper;
    private final BizPatientMapper patientMapper;
    private final SysBedMapper bedMapper;
    /**
     * 电子签名（P5.5）：提交即签名、归档补签、签名即锁定
     */
    private final com.his.common.service.EmrSignatureService signatureService;

    // 保存（新增 / 修改）

    /**
     * 签名状态文案：未知码值渲染成「未知(n)」，不回落成"未签名" —— 那是两次不同的事实
     */
    private static String signStatusText(Integer signStatus) {
        return com.his.common.enums.ObjectSignStatus.textOf(signStatus);
    }

    /**
     * 已签名时给前端一句能照做的话；没锁死时返回 null（前端不显示提示条）
     */
    private static String signLockHint(BizInpatientRecord r) {
        if (!Objects.equals(1, r.getSignStatus())) {
            return null;
        }
        return "本文书已于 " + r.getSignedTime() + " 完成电子签名（签名即锁定），内容不可再修改；"
                + "如确需更正，请先在「签名中心」作废该签名，再修改并重新提交";
    }

    private static String asText(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(v);
    }

    private static String rateText(BigDecimal rate) {
        return rate == null ? "—" : rate.toPlainString() + "%";
    }

    // 校验

    // 查询

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InpatientRecordDetailVO save(InpatientRecordUpsertDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("入参不能为空");
        }
        if (dto.getId() == null) {
            return create(dto);
        }
        return update(dto);
    }

    private InpatientRecordDetailVO create(InpatientRecordUpsertDTO dto) {
        // 保留（类别①条件必填）：save 按 dto.id 分新增/修改，这两项只在新增分支必填，
        // 修改分支可省略；挂 @NotNull 会把合法的修改请求挡成 400
        if (dto.getAdmissionId() == null) {
            throw new BusinessException("入院ID不能为空");
        }
        if (dto.getRecordType() == null) {
            throw new BusinessException("文书类型不能为空");
        }
        // 9-会诊记录 / 10-转科记录是**系统文书**（由各自闭环完成时回写）：单独给出可执行的提示，
        // 而不是笼统地说"类型不合法" —— 类型字典里有这两个码，说它们不合法会把人绕晕。
        if (InpatientRecordTypeEnum.isConsultRecord(dto.getRecordType())) {
            throw new BusinessException("会诊记录由会诊完成时自动回写，不支持手工新增；"
                    + "请在「住院会诊」里申请并由会诊科室完成");
        }
        if (InpatientRecordTypeEnum.isTransferRecord(dto.getRecordType())) {
            throw new BusinessException("转科记录由转科接收时自动回写，不支持手工新增；"
                    + "请在「转科 / 交接班」里发起并由转入科室接收");
        }

        BizAdmission admission = admissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        BizInpatientRecord record = new BizInpatientRecord();
        record.setRecordNo(nextRecordNo());
        record.setAdmissionId(admission.getAdmissionId());
        record.setPatientId(admission.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());
        record.setGender(patient.getGender());
        fillAge(record, patient);

        // 归属快照：文书上写下的必须是"书写当时"的科室/病区/床位（换床、转科不该追溯改历史文书）
        Long deptId = admission.getDeptId();
        String deptName = null;
        String wardName = null;
        if (admission.getWardId() != null) {
            WardVO ward = bedMapper.selectWardById(admission.getWardId());
            if (ward != null) {
                wardName = ward.getWardName();
                deptName = ward.getDeptName();
                if (deptId == null) {
                    deptId = ward.getDeptId();
                }
            }
        }
        record.setDeptId(deptId);
        record.setDeptName(deptName);
        record.setWardId(admission.getWardId());
        record.setWardName(wardName);
        String bedNo = null;
        if (admission.getBedId() != null) {
            SysBed bed = bedMapper.selectById(admission.getBedId());
            if (bed != null) {
                bedNo = bed.getBedNo();
            }
        }
        record.setBedNo(bedNo);

        record.setRecordType(dto.getRecordType());
        record.setRecordTitle(StringUtils.hasText(dto.getRecordTitle())
                ? dto.getRecordTitle() : InpatientRecordTypeEnum.getText(dto.getRecordType()));
        record.setRecordTime(TimeUtil.toSeconds(dto.getRecordTime() != null ? dto.getRecordTime() : LocalDateTime.now()));

        applyContent(record, dto);
        record.setRecordStatus(RecordStatusEnum.DRAFT.getCode());
        record.setDoctorId(currentEmpId());
        record.setDoctorName(currentName());
        record.setRemark(dto.getRemark());

        // 新建入院记录时把患者档案里的过敏史带出来当初始值 —— 仍是医生可改的快照，
        // 但"忘填过敏史"这种最危险的缺项从源头少一次
        if (Objects.equals(1, dto.getRecordType())
                && !StringUtils.hasText(record.getAllergyHistory())
                && StringUtils.hasText(patient.getAllergyHistory())) {
            record.setAllergyHistory(patient.getAllergyHistory());
        }

        validateContent(record);
        recordMapper.insert(record);
        writeActionLog(record, "创建");
        log.info("新建病历文书 recordNo={} admissionId={} type={} 医生={}",
                record.getRecordNo(), record.getAdmissionId(),
                InpatientRecordTypeEnum.getText(record.getRecordType()), record.getDoctorName());
        return detail(record.getId());
    }

    private InpatientRecordDetailVO update(InpatientRecordUpsertDTO dto) {
        BizInpatientRecord record = recordMapper.selectById(dto.getId());
        if (record == null) {
            throw new BusinessException("病历文书不存在");
        }
        if (InpatientRecordStatusEnum.isArchived(record.getRecordStatus())) {
            throw new BusinessException("病历文书 " + record.getRecordNo()
                    + " 已归档，不允许修改（归档是单向门；如确需更正请走病案室流程）");
        }
        // 签名即锁定：锁挂在**内容**上，而不是挂在"已归档"这个状态字段上。
        // 只有这一条能防住"已提交（但还没归档）的病历被悄悄改掉"——
        // 归档单向门管不到这一段，而这一段恰恰是签名生效的区间。
        if (Objects.equals(1, record.getSignStatus())) {
            throw new BusinessException("病历文书 " + record.getRecordNo()
                    + " 已由 " + record.getDoctorName() + " 签名（" + record.getSignedTime()
                    + "），签名即锁定，不允许修改；如确需更正，请先在「签名中心」作废该签名"
                    + "（作废会留痕并解除锁定），改完重新提交");
        }
        if (dto.getRecordType() != null && !Objects.equals(dto.getRecordType(), record.getRecordType())) {
            throw new BusinessException("不能修改文书类型（改类型 = 换一份文书，会毁掉质控统计口径）；请另建新文书");
        }

        // 逐字段 diff：先留痕，再落库（顺序反过来，异常时日志和库会不一致）
        List<BizInpatientRecordLog> changes = diffContent(record, dto);
        record.setRecordTitle(StringUtils.hasText(dto.getRecordTitle())
                ? dto.getRecordTitle() : record.getRecordTitle());
        if (dto.getRecordTime() != null) {
            record.setRecordTime(TimeUtil.toSeconds(dto.getRecordTime()));
        }
        record.setRemark(dto.getRemark());
        applyContent(record, dto);

        validateContent(record);
        recordMapper.updateById(record);
        for (BizInpatientRecordLog row : changes) {
            logMapper.insert(row);
        }
        if (!changes.isEmpty()) {
            log.info("修改病历文书 recordNo={} 变更字段数={} 医生={}",
                    record.getRecordNo(), changes.size(), currentName());
        }
        return detail(record.getId());
    }

    /**
     * 把入参内容拷到实体（"传什么覆盖什么"，不传 = 置空，配合逐字段 diff 才留得住"医生删掉了主诉"）
     */
    private void applyContent(BizInpatientRecord record, InpatientRecordUpsertDTO dto) {
        record.setChiefComplaint(dto.getChiefComplaint());
        record.setPresentIllness(dto.getPresentIllness());
        record.setPastHistory(dto.getPastHistory());
        record.setPersonalHistory(dto.getPersonalHistory());
        record.setFamilyHistory(dto.getFamilyHistory());
        record.setAllergyHistory(dto.getAllergyHistory());
        record.setTemperature(dto.getTemperature());
        record.setPulse(dto.getPulse());
        record.setRespiration(dto.getRespiration());
        record.setSystolicPressure(dto.getSystolicPressure());
        record.setDiastolicPressure(dto.getDiastolicPressure());
        record.setHeight(dto.getHeight());
        record.setWeight(dto.getWeight());
        record.setGeneralCondition(dto.getGeneralCondition());
        record.setSkinMucosa(dto.getSkinMucosa());
        record.setHeadNeck(dto.getHeadNeck());
        record.setChestLung(dto.getChestLung());
        record.setHeart(dto.getHeart());
        record.setAbdomen(dto.getAbdomen());
        record.setSpineLimbs(dto.getSpineLimbs());
        record.setNervousSystem(dto.getNervousSystem());
        record.setSpecialistExam(dto.getSpecialistExam());
        record.setAuxiliaryExam(dto.getAuxiliaryExam());
        record.setDiagnosisName(dto.getDiagnosisName());
        record.setDiagnosisCode(dto.getDiagnosisCode());
        record.setTreatmentPlan(dto.getTreatmentPlan());
        record.setCourseNote(dto.getCourseNote());
    }

    /**
     * 内容校验。入院记录的必填要素是**服务层强制**的（不是前端提醒）：
     * 主诉 / 现病史 / 既往史 / 过敏史 / 诊断 + 五项体征，一次说清缺哪几项。
     */
    private void validateContent(BizInpatientRecord record) {
        int type = record.getRecordType() == null ? 0 : record.getRecordType();

        // 会诊记录（9）是**系统文书**：只由会诊完成时回写，不接受手工新增/修改。
        // 允许手工写，就会出现"会诊表里没有这次会诊、病历里却有一份会诊记录"的假证据。
        if (InpatientRecordTypeEnum.isConsultRecord(type)) {
            throw new BusinessException("会诊记录由会诊完成时自动回写，不支持手工新增或修改；"
                    + "请在「住院会诊」里申请并由会诊科室完成");
        }

        // 转科记录（10）同理：只由转科「接收」时回写。
        // 手工能写，就会出现"转科表里没有这次转科、病历里却有一份转科记录"的假证据 ——
        // 而转科记录恰恰是病案首页"入院科别 / 出院科别"的推断依据。
        if (InpatientRecordTypeEnum.isTransferRecord(type)) {
            throw new BusinessException("转科记录由转科接收时自动回写，不支持手工新增或修改；"
                    + "请在「转科 / 交接班」里发起并由转入科室接收");
        }

        // 保留（类别③业务规则 + 类别①条件必填）：这一组是入院记录（type=1）独有的三甲评审硬口径要素，
        // 且校验对象是服务端组装好的文书实体（不是入参字段），注解校验覆盖不到
        if (type == 1) {
            List<String> missing = new ArrayList<>();
            if (!StringUtils.hasText(record.getChiefComplaint())) {
                missing.add("主诉");
            }
            if (!StringUtils.hasText(record.getPresentIllness())) {
                missing.add("现病史");
            }
            if (!StringUtils.hasText(record.getPastHistory())) {
                missing.add("既往史");
            }
            if (!StringUtils.hasText(record.getAllergyHistory())) {
                missing.add("过敏史（无过敏史也必须显式写「否认」，不允许留空代替）");
            }
            if (!StringUtils.hasText(record.getDiagnosisName())) {
                missing.add("诊断名称");
            }
            if (record.getTemperature() == null) {
                missing.add("体温");
            }
            if (record.getPulse() == null) {
                missing.add("脉搏");
            }
            if (record.getRespiration() == null) {
                missing.add("呼吸");
            }
            if (record.getSystolicPressure() == null || record.getDiastolicPressure() == null) {
                missing.add("血压");
            }
            if (!missing.isEmpty()) {
                throw new BusinessException("入院记录缺少必填要素：" + String.join("、", missing));
            }
        }

        // 保留（类别①条件必填）：诊断名称只对「要求诊断的文书类型」必填，是否必填取决于同批内容
        if (InpatientRecordTypeEnum.requiresDiagnosis(type) && !StringUtils.hasText(record.getDiagnosisName())) {
            throw new BusinessException(InpatientRecordTypeEnum.labelOrUnknown(type) + "必须填写诊断名称");
        }

        // 保留（类别③）：体征值域与「收缩压>舒张压」是临床取值合理性规则，注解（@Max 一类）表达不了成对比较
        if (record.getTemperature() != null
                && (record.getTemperature().compareTo(new BigDecimal("34")) < 0
                || record.getTemperature().compareTo(new BigDecimal("43")) > 0)) {
            throw new BusinessException("体温取值超出可信范围（34~43℃），请核对单位（℃）");
        }
        if (record.getPulse() != null && (record.getPulse() < 20 || record.getPulse() > 250)) {
            throw new BusinessException("脉搏取值超出可信范围（20~250 次/分），请核对");
        }
        if (record.getRespiration() != null && (record.getRespiration() < 5 || record.getRespiration() > 80)) {
            throw new BusinessException("呼吸取值超出可信范围（5~80 次/分），请核对");
        }
        if (record.getSystolicPressure() != null && record.getDiastolicPressure() != null
                && record.getSystolicPressure() <= record.getDiastolicPressure()) {
            throw new BusinessException("收缩压必须大于舒张压（当前 "
                    + record.getSystolicPressure() + "/" + record.getDiastolicPressure() + "）");
        }
    }

    @Override
    public InpatientRecordDetailVO detail(Long id) {
        BizInpatientRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("病历文书不存在");
        }
        return toDetail(record);
    }

    @Override
    public IPage<InpatientRecordVO> listPage(InpatientRecordQueryPageDTO query) {
        // 科室数据权限收口（M6）：文书归属科室（dept_id），受限角色只看授权科室的文书。
        // scopeDeptIds 是服务端专用字段，先清掉前端可能伪造的值。
        query.setScopeDeptIds(deptScopeProvider.isScoped()
                ? List.copyOf(deptScopeProvider.allowedDeptIds()) : null);
        IPage<BizInpatientRecord> page = recordMapper.selectRecordPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        List<InpatientRecordVO> rows = new ArrayList<>(page.getRecords().size());
        for (BizInpatientRecord r : page.getRecords()) {
            rows.add(toRow(r));
        }
        Page<InpatientRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(rows);
        return result;
    }

    private InpatientRecordVO toRow(BizInpatientRecord r) {
        InpatientRecordVO vo = new InpatientRecordVO();
        vo.setId(r.getId());
        vo.setRecordNo(r.getRecordNo());
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setWardName(r.getWardName());
        vo.setBedNo(r.getBedNo());
        vo.setRecordType(r.getRecordType());
        vo.setRecordTypeText(InpatientRecordTypeEnum.getText(r.getRecordType()));
        vo.setRecordTitle(r.getRecordTitle());
        vo.setRecordTime(r.getRecordTime());
        vo.setRecordStatus(r.getRecordStatus());
        vo.setRecordStatusText(SummaryStatusEnum.getText(r.getRecordStatus()));
        vo.setDoctorName(r.getDoctorName());
        vo.setChiefComplaint(r.getChiefComplaint());
        vo.setDiagnosisName(r.getDiagnosisName());
        vo.setArchiveTime(r.getArchiveTime());
        vo.setArchiveByName(r.getArchiveByName());
        vo.setSignStatus(r.getSignStatus());
        vo.setSignStatusText(signStatusText(r.getSignStatus()));
        vo.setSignId(r.getSignId());
        vo.setSignedTime(r.getSignedTime());
        fillStructured(r, vo::setStructuredFilled, vo::setStructuredTotal,
                vo::setStructuredRate, vo::setStructuredRateText, vo::setMissingLabels);
        fillButtons(vo::setCanEdit, vo::setCanSubmit, vo::setCanArchive, r.getRecordStatus(), r.getSignStatus());
        return vo;
    }

    // 提交 / 归档

    private InpatientRecordDetailVO toDetail(BizInpatientRecord r) {
        InpatientRecordDetailVO vo = new InpatientRecordDetailVO();
        vo.setId(r.getId());
        vo.setRecordNo(r.getRecordNo());
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientNo(r.getPatientNo());
        vo.setPatientName(r.getPatientName());
        vo.setGender(r.getGender());
        vo.setGenderText(SysGenderEnum.getText(r.getGender()));
        vo.setAge(r.getAge());
        vo.setAgeUnit(r.getAgeUnit());
        vo.setAgeUnitText(AgeUnitEnum.getText(r.getAgeUnit()));
        vo.setAgeText(r.getAge() == null ? "—" : r.getAge() + AgeUnitEnum.getText(r.getAgeUnit()));
        vo.setDeptName(r.getDeptName());
        vo.setWardName(r.getWardName());
        vo.setBedNo(r.getBedNo());
        vo.setRecordType(r.getRecordType());
        vo.setRecordTypeText(InpatientRecordTypeEnum.getText(r.getRecordType()));
        vo.setRecordTitle(r.getRecordTitle());
        vo.setRecordTime(r.getRecordTime());
        vo.setChiefComplaint(r.getChiefComplaint());
        vo.setPresentIllness(r.getPresentIllness());
        vo.setPastHistory(r.getPastHistory());
        vo.setPersonalHistory(r.getPersonalHistory());
        vo.setFamilyHistory(r.getFamilyHistory());
        vo.setAllergyHistory(r.getAllergyHistory());
        vo.setTemperature(r.getTemperature());
        vo.setPulse(r.getPulse());
        vo.setRespiration(r.getRespiration());
        vo.setSystolicPressure(r.getSystolicPressure());
        vo.setDiastolicPressure(r.getDiastolicPressure());
        vo.setHeight(r.getHeight());
        vo.setWeight(r.getWeight());
        vo.setVitalSignsText(vitalText(r));
        vo.setGeneralCondition(r.getGeneralCondition());
        vo.setSkinMucosa(r.getSkinMucosa());
        vo.setHeadNeck(r.getHeadNeck());
        vo.setChestLung(r.getChestLung());
        vo.setHeart(r.getHeart());
        vo.setAbdomen(r.getAbdomen());
        vo.setSpineLimbs(r.getSpineLimbs());
        vo.setNervousSystem(r.getNervousSystem());
        vo.setSpecialistExam(r.getSpecialistExam());
        vo.setAuxiliaryExam(r.getAuxiliaryExam());
        vo.setDiagnosisName(r.getDiagnosisName());
        vo.setDiagnosisCode(r.getDiagnosisCode());
        vo.setTreatmentPlan(r.getTreatmentPlan());
        vo.setCourseNote(r.getCourseNote());
        vo.setRecordStatus(r.getRecordStatus());
        vo.setRecordStatusText(SummaryStatusEnum.getText(r.getRecordStatus()));
        vo.setDoctorName(r.getDoctorName());
        vo.setSubmitTime(r.getSubmitTime());
        vo.setArchiveTime(r.getArchiveTime());
        vo.setArchiveByName(r.getArchiveByName());
        vo.setRemark(r.getRemark());
        vo.setSignStatus(r.getSignStatus());
        vo.setSignStatusText(signStatusText(r.getSignStatus()));
        vo.setSignId(r.getSignId());
        vo.setSignedTime(r.getSignedTime());
        vo.setSignLockHint(signLockHint(r));
        fillStructured(r, vo::setStructuredFilled, vo::setStructuredTotal,
                vo::setStructuredRate, vo::setStructuredRateText, vo::setMissingLabels);
        vo.setElements(elementDetails(r));
        fillButtons(vo::setCanEdit, vo::setCanSubmit, vo::setCanArchive, r.getRecordStatus(), r.getSignStatus());
        return vo;
    }

    private String vitalText(BizInpatientRecord r) {
        if (r.getTemperature() == null && r.getPulse() == null && r.getRespiration() == null
                && r.getSystolicPressure() == null) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        if (r.getTemperature() != null) {
            sb.append("T").append(r.getTemperature()).append("℃ ");
        }
        if (r.getPulse() != null) {
            sb.append("P").append(r.getPulse()).append("次/分 ");
        }
        if (r.getRespiration() != null) {
            sb.append("R").append(r.getRespiration()).append("次/分 ");
        }
        if (r.getSystolicPressure() != null && r.getDiastolicPressure() != null) {
            sb.append("BP").append(r.getSystolicPressure()).append("/").append(r.getDiastolicPressure()).append("mmHg");
        }
        return sb.toString().trim();
    }

    private List<InpatientRecordDetailVO.ElementVO> elementDetails(BizInpatientRecord r) {
        List<InpatientRecordDetailVO.ElementVO> list = new ArrayList<>();
        Map<String, String> groups = RecordStructuredFields.groupLabels();
        for (RecordStructuredFields.KeyElement e : RecordStructuredFields.elementsFor(r.getRecordType())) {
            InpatientRecordDetailVO.ElementVO el = new InpatientRecordDetailVO.ElementVO();
            el.setCode(e.code());
            el.setLabel(e.label());
            el.setGroup(e.group());
            el.setGroupLabel(groups.getOrDefault(e.group(), e.group()));
            Object value = e.getter().apply(r);
            el.setFilled(RecordStructuredFields.isFilled(value));
            el.setValue(RecordStructuredFields.isFilled(value) ? String.valueOf(value) : null);
            list.add(el);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submit(InpatientRecordSubmitDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("请选择要提交的文书");
        }
        List<BizInpatientRecord> records = loadForBatch(dto.getIds());

        // 先全量校验再写：部分成功会让医生不知道哪些生效了
        for (BizInpatientRecord r : records) {
            if (!Objects.equals(RecordStatusEnum.DRAFT.getCode(), r.getRecordStatus())) {
                throw new BusinessException("文书 " + r.getRecordNo() + " 当前状态为「"
                        + SummaryStatusEnum.labelOrUnknown(r.getRecordStatus())
                        + "」，只有「草稿」可以提交");
            }
        }
        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());
        Long empId = currentEmpId();
        String name = currentName();
        for (BizInpatientRecord r : records) {
            // **先把「已提交」落到库，再签名**。签名服务是从库里读状态做放行判断的，
            // 只改内存对象没用 —— 反过来的话 blockReason 会读到「还是草稿」，
            // 提交动作会被自己的签名校验拦死（这条不是理论，是实测踩出来的）。
            r.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
            r.setSubmitTime(now);
            recordMapper.updateById(r);

            com.his.common.vo.SignatureVO sig = signOrFail(r, com.his.common.enums.SignScene.SUBMIT,
                    "病历提交");
            if (sig != null) {
                // 签名服务已经 UPDATE 过锚点。把新值同步回内存实体后再写一次，
                // 否则内存里的旧 signStatus=0 会在别处 updateById 时把锚点**覆盖回去**
                // （这是最难查的一类"签名莫名消失"）。
                r.setSignStatus(1);
                r.setSignId(sig.getId());
                r.setSignedTime(sig.getSignedTime());
                recordMapper.updateById(r);
            }
            BizInpatientRecordLog row = actionLog(r, "提交");
            row.setUserId(empId);
            row.setUserName(name);
            row.setRemark(dto.getRemark());
            logMapper.insert(row);
        }
        log.info("提交病历文书 {} 份：{}", records.size(),
                records.stream().map(BizInpatientRecord::getRecordNo).toList());
        return records.size();
    }

    // 修改日志

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archive(InpatientRecordArchiveDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("请选择要归档的文书");
        }
        List<BizInpatientRecord> records = loadForBatch(dto.getIds());

        for (BizInpatientRecord r : records) {
            if (!Objects.equals(RecordStatusEnum.SUBMITTED.getCode(), r.getRecordStatus())) {
                throw new BusinessException("文书 " + r.getRecordNo() + " 当前状态为「"
                        + SummaryStatusEnum.labelOrUnknown(r.getRecordStatus())
                        + "」，只有「已提交」可以归档（草稿请先提交）");
            }
        }
        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());
        Long empId = currentEmpId();
        String name = currentName();
        for (BizInpatientRecord r : records) {
            // 归档补签：正常情况下提交时已经签过，这里只兜住"提交时签名失败/历史数据"两种缺口。
            // 已签名的直接跳过（blockReason 会拒绝重复签，不能把整个归档批次拖挂）。
            if (!Objects.equals(1, r.getSignStatus())) {
                com.his.common.vo.SignatureVO sig = signOrFail(r,
                        com.his.common.enums.SignScene.ARCHIVE, "病历归档");
                if (sig != null) {
                    r.setSignStatus(1);
                    r.setSignId(sig.getId());
                    r.setSignedTime(sig.getSignedTime());
                }
            }
            r.setRecordStatus(RecordStatusEnum.ARCHIVED.getCode());
            r.setArchiveTime(now);
            r.setArchiveBy(empId);
            r.setArchiveByName(name);
            recordMapper.updateById(r);
            BizInpatientRecordLog row = actionLog(r, "归档");
            row.setUserId(empId);
            row.setUserName(name);
            row.setRemark(dto.getRemark());
            logMapper.insert(row);
        }
        log.info("归档病历文书 {} 份：{}", records.size(),
                records.stream().map(BizInpatientRecord::getRecordNo).toList());
        return records.size();
    }

    /**
     * 给一份文书签名；失败时抛业务异常（**不吞**）。
     *
     * <p>为什么让签名失败直接中断提交/归档，而不是"记个日志继续"：
     * 签名是这套能力的唯一产出，静默失败等于"页面显示已提交、实际没有签名证据"，
     * 事后无法分辨是漏签还是被篡改。失败时医生/病案室能立刻看到原因并重试，
     * 代价远小于留下无法追溯的缺口。
     */
    private com.his.common.vo.SignatureVO signOrFail(BizInpatientRecord r,
                                                     com.his.common.enums.SignScene scene,
                                                     String actionLabel) {
        com.his.common.dto.SignCommandDTO cmd = new com.his.common.dto.SignCommandDTO();
        cmd.setBizType(com.his.common.enums.SignBizType.INPATIENT_RECORD.getCode());
        cmd.setBizId(r.getId());
        cmd.setSignScene(scene.getCode());
        cmd.setSignerId(currentEmpId());
        cmd.setSignerName(currentName());
        cmd.setSignerDeptId(currentDeptId());
        cmd.setSignerDeptName(currentDeptName());
        try {
            return signatureService.sign(cmd);
        } catch (BusinessException e) {
            throw new BusinessException(actionLabel + "失败（文书 " + r.getRecordNo() + "）："
                    + e.getMessage());
        }
    }

    /**
     * 批量加载（保持入参顺序），逐个确认存在
     */
    private List<BizInpatientRecord> loadForBatch(List<Long> ids) {
        List<BizInpatientRecord> records = new ArrayList<>(ids.size());
        for (Long id : ids) {
            BizInpatientRecord r = recordMapper.selectById(id);
            if (r == null) {
                throw new BusinessException("病历文书不存在（id=" + id + "）");
            }
            records.add(r);
        }
        return records;
    }

    @Override
    public IPage<InpatientRecordLogVO> logPage(InpatientRecordLogQueryPageDTO query) {
        LambdaQueryWrapper<BizInpatientRecordLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getDocType() != null, BizInpatientRecordLog::getDocType, query.getDocType());
        wrapper.eq(query.getRecordId() != null, BizInpatientRecordLog::getRecordId, query.getRecordId());
        wrapper.eq(StringUtils.hasText(query.getRecordNo()),
                BizInpatientRecordLog::getRecordNo, query.getRecordNo());
        if (query.getAdmissionId() != null) {
            // 日志表不存 admission_id：按"这次住院全部文书的号"圈定范围（病历 + 护理一起给，
            // 除非显式指定了 docType —— 查"这次住院"的人关心的是轨迹，不是单据类型）
            List<String> nos = new ArrayList<>();
            if (query.getDocType() == null || query.getDocType() == RecordDocTypeEnum.MEDICAL.getCode()) {
                nos.addAll(recordMapper.selectRecordNosByAdmission(query.getAdmissionId()));
            }
            if (query.getDocType() == null || query.getDocType() == 2) {
                nos.addAll(nursingRecordMapper.selectRecordNosByAdmission(query.getAdmissionId()));
            }
            if (nos.isEmpty()) {
                return new Page<>(query.getPageNum(), query.getPageSize());
            }
            wrapper.in(BizInpatientRecordLog::getRecordNo, nos);
        }
        wrapper.orderByDesc(BizInpatientRecordLog::getCreateTime).orderByDesc(BizInpatientRecordLog::getId);

        IPage<BizInpatientRecordLog> page = logMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        Page<InpatientRecordLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<InpatientRecordLogVO> rows = new ArrayList<>(page.getRecords().size());
        for (BizInpatientRecordLog l : page.getRecords()) {
            rows.add(toLogVO(l));
        }
        result.setRecords(rows);
        return result;
    }

    // 结构化率统计

    @Override
    public List<InpatientRecordLogVO> logList(Integer docType, Long recordId) {
        // 保留（类别②）：入参是普通 Integer/Long（GET @RequestParam 绑定，非 request DTO 字段），注解无处安放
        if (docType == null || recordId == null) {
            throw new BusinessException("单据类型与单据ID不能为空");
        }
        List<BizInpatientRecordLog> list = logMapper.selectByRecord(docType, recordId);
        List<InpatientRecordLogVO> rows = new ArrayList<>(list.size());
        for (BizInpatientRecordLog l : list) {
            rows.add(toLogVO(l));
        }
        return rows;
    }

    // 下拉

    private InpatientRecordLogVO toLogVO(BizInpatientRecordLog l) {
        InpatientRecordLogVO vo = new InpatientRecordLogVO();
        vo.setId(l.getId());
        vo.setDocType(l.getDocType());
        vo.setDocTypeText(RecordDocTypeEnum.getText(l.getDocType()));
        vo.setRecordId(l.getRecordId());
        vo.setRecordNo(l.getRecordNo());
        vo.setRecordType(l.getRecordType());
        vo.setFieldName(l.getFieldName());
        vo.setFieldLabel(fieldLabel(l.getDocType(), l.getFieldName()));
        vo.setOperation(l.getOperation());
        vo.setUserId(l.getUserId());
        vo.setUserName(l.getUserName());
        vo.setOldValue(l.getOldValue());
        vo.setNewValue(l.getNewValue());
        vo.setCreateTime(l.getCreateTime());
        vo.setRemark(l.getRemark());
        return vo;
    }

    /**
     * 字段中文名：病历走结构化要素清单，护理走护理字段表；查不到**原样返回不猜**
     */
    private String fieldLabel(Integer docType, String code) {
        if (code == null) {
            return "—";
        }
        if (Objects.equals(RecordDocTypeEnum.MEDICAL.getCode(), docType)) {
            for (RecordStructuredFields.KeyElement e : RecordStructuredFields.allElements()) {
                if (e.code().equals(code)) {
                    return e.label();
                }
            }
            if ("record_title".equals(code)) {
                return "文书标题";
            }
            if ("record_time".equals(code)) {
                return "记录时间";
            }
            if ("remark".equals(code)) {
                return "备注";
            }
            return code;
        }
        return NursingDocFieldEnum.getText(code);
    }

    @Override
    public RecordQualityStatVO qualityStat(Long admissionId) {
        // 保留（类别②）：入参是普通 Long（GET @RequestParam 绑定，非 request DTO 字段），注解无处安放
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        BizAdmission admission = admissionMapper.selectById(admissionId);
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        List<BizInpatientRecord> records = recordMapper.selectByAdmission(admissionId);

        RecordQualityStatVO stat = new RecordQualityStatVO();
        stat.setAdmissionId(admissionId);
        stat.setPatientName(patientNameOf(admission.getPatientId()));
        stat.setRecordCount(records.size());
        stat.setGroups(new ArrayList<>());
        stat.setElements(new ArrayList<>());
        stat.setRecords(new ArrayList<>());

        if (records.isEmpty()) {
            // 没有文书：总体率是"算不出来"（null），不是 0%，更不是 100%
            stat.setElementTotal(0);
            stat.setElementFilled(0);
            stat.setStructuredRate(null);
            stat.setStructuredRateText("—");
            return stat;
        }

        int totalFilled = 0;
        int totalAll = 0;
        Map<String, int[]> groupAcc = new LinkedHashMap<>();
        for (String g : RecordStructuredFields.groupLabels().keySet()) {
            groupAcc.put(g, new int[2]);
        }
        Map<String, int[]> elementAcc = new LinkedHashMap<>();
        List<RecordQualityStatVO.RecordStatVO> recordStats = new ArrayList<>(records.size());

        for (BizInpatientRecord r : records) {
            List<RecordStructuredFields.KeyElement> elements = RecordStructuredFields.elementsFor(r.getRecordType());
            int filled = 0;
            List<String> missing = new ArrayList<>();
            for (RecordStructuredFields.KeyElement e : elements) {
                boolean ok = RecordStructuredFields.isFilled(e.getter().apply(r));
                if (ok) {
                    filled++;
                } else {
                    missing.add(e.label());
                }
                groupAcc.get(e.group())[0] += ok ? 1 : 0;
                groupAcc.get(e.group())[1] += 1;
                int[] acc = elementAcc.computeIfAbsent(e.code(), k -> new int[3]);
                acc[0] += ok ? 1 : 0;
                acc[1] += 1;
            }
            totalFilled += filled;
            totalAll += elements.size();

            RecordQualityStatVO.RecordStatVO rs = new RecordQualityStatVO.RecordStatVO();
            rs.setRecordId(r.getId());
            rs.setRecordNo(r.getRecordNo());
            rs.setRecordType(r.getRecordType());
            rs.setRecordTypeText(InpatientRecordTypeEnum.getText(r.getRecordType()));
            rs.setRecordStatusText(SummaryStatusEnum.getText(r.getRecordStatus()));
            rs.setFilled(filled);
            rs.setTotal(elements.size());
            rs.setRate(RecordStructuredFields.rate(filled, elements.size()));
            rs.setRateText(rateText(rs.getRate()));
            rs.setMissingLabels(missing);
            recordStats.add(rs);
        }
        stat.setElementFilled(totalFilled);
        stat.setElementTotal(totalAll);
        stat.setStructuredRate(RecordStructuredFields.rate(totalFilled, totalAll));
        stat.setStructuredRateText(rateText(stat.getStructuredRate()));

        Map<String, String> groupLabels = RecordStructuredFields.groupLabels();
        for (Map.Entry<String, int[]> en : groupAcc.entrySet()) {
            RecordQualityStatVO.GroupStatVO g = new RecordQualityStatVO.GroupStatVO();
            g.setGroup(en.getKey());
            g.setGroupLabel(groupLabels.getOrDefault(en.getKey(), en.getKey()));
            g.setElementFilled(en.getValue()[0]);
            g.setElementTotal(en.getValue()[1]);
            g.setRate(RecordStructuredFields.rate(g.getElementFilled(), g.getElementTotal()));
            g.setRateText(rateText(g.getRate()));
            stat.getGroups().add(g);
        }

        for (RecordStructuredFields.KeyElement e : RecordStructuredFields.allElements()) {
            int[] acc = elementAcc.get(e.code());
            if (acc == null) {
                continue;
            }
            RecordQualityStatVO.ElementStatVO es = new RecordQualityStatVO.ElementStatVO();
            es.setCode(e.code());
            es.setLabel(e.label());
            es.setGroup(e.group());
            es.setGroupLabel(groupLabels.getOrDefault(e.group(), e.group()));
            es.setFilledCount(acc[0]);
            es.setMissingCount(acc[1] - acc[0]);
            es.setRate(RecordStructuredFields.rate(acc[0], acc[1]));
            es.setRateText(rateText(es.getRate()));
            stat.getElements().add(es);
        }
        stat.setRecords(recordStats);
        return stat;
    }

    // diff 留痕

    @Override
    public List<CodeOptionVO> typeOptions() {
        List<CodeOptionVO> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(new CodeOptionVO(i, InpatientRecordTypeEnum.getText(i)));
        }
        return list;
    }

    @Override
    public List<CodeOptionVO> statusOptions() {
        List<CodeOptionVO> list = new ArrayList<>();
        list.add(new CodeOptionVO(RecordStatusEnum.DRAFT.getCode(), SummaryStatusEnum.getText(RecordStatusEnum.DRAFT.getCode())));
        list.add(new CodeOptionVO(RecordStatusEnum.SUBMITTED.getCode(), SummaryStatusEnum.getText(RecordStatusEnum.SUBMITTED.getCode())));
        list.add(new CodeOptionVO(RecordStatusEnum.ARCHIVED.getCode(), SummaryStatusEnum.getText(RecordStatusEnum.ARCHIVED.getCode())));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInpatientRecord appendClosedLoopRecord(BizInpatientRecord record) {
        record.setRecordNo(nextRecordNo());
        recordMapper.insert(record);
        return record;
    }

    /**
     * 逐字段 diff：只对「值真的变了」的字段生成日志行。
     * 字段清单与 {@code RecordStructuredFields} 的要素编码保持同源（再加标题/记录时间/备注）。
     */
    private List<BizInpatientRecordLog> diffContent(BizInpatientRecord oldRecord, InpatientRecordUpsertDTO dto) {
        List<Object[]> pairs = new ArrayList<>();
        pairs.add(new Object[]{"record_title", oldRecord.getRecordTitle(), dto.getRecordTitle()});
        if (dto.getRecordTime() != null) {
            pairs.add(new Object[]{"record_time",
                    oldRecord.getRecordTime() == null ? null : oldRecord.getRecordTime().toString(),
                    TimeUtil.toSeconds(dto.getRecordTime()).toString()});
        }
        pairs.add(new Object[]{"chief_complaint", oldRecord.getChiefComplaint(), dto.getChiefComplaint()});
        pairs.add(new Object[]{"present_illness", oldRecord.getPresentIllness(), dto.getPresentIllness()});
        pairs.add(new Object[]{"past_history", oldRecord.getPastHistory(), dto.getPastHistory()});
        pairs.add(new Object[]{"personal_history", oldRecord.getPersonalHistory(), dto.getPersonalHistory()});
        pairs.add(new Object[]{"family_history", oldRecord.getFamilyHistory(), dto.getFamilyHistory()});
        pairs.add(new Object[]{"allergy_history", oldRecord.getAllergyHistory(), dto.getAllergyHistory()});
        pairs.add(new Object[]{"temperature", oldRecord.getTemperature(), dto.getTemperature()});
        pairs.add(new Object[]{"pulse", oldRecord.getPulse(), dto.getPulse()});
        pairs.add(new Object[]{"respiration", oldRecord.getRespiration(), dto.getRespiration()});
        pairs.add(new Object[]{"systolic_pressure", oldRecord.getSystolicPressure(), dto.getSystolicPressure()});
        pairs.add(new Object[]{"diastolic_pressure", oldRecord.getDiastolicPressure(), dto.getDiastolicPressure()});
        pairs.add(new Object[]{"height", oldRecord.getHeight(), dto.getHeight()});
        pairs.add(new Object[]{"weight", oldRecord.getWeight(), dto.getWeight()});
        pairs.add(new Object[]{"general_condition", oldRecord.getGeneralCondition(), dto.getGeneralCondition()});
        pairs.add(new Object[]{"skin_mucosa", oldRecord.getSkinMucosa(), dto.getSkinMucosa()});
        pairs.add(new Object[]{"head_neck", oldRecord.getHeadNeck(), dto.getHeadNeck()});
        pairs.add(new Object[]{"chest_lung", oldRecord.getChestLung(), dto.getChestLung()});
        pairs.add(new Object[]{"heart", oldRecord.getHeart(), dto.getHeart()});
        pairs.add(new Object[]{"abdomen", oldRecord.getAbdomen(), dto.getAbdomen()});
        pairs.add(new Object[]{"spine_limbs", oldRecord.getSpineLimbs(), dto.getSpineLimbs()});
        pairs.add(new Object[]{"nervous_system", oldRecord.getNervousSystem(), dto.getNervousSystem()});
        pairs.add(new Object[]{"specialist_exam", oldRecord.getSpecialistExam(), dto.getSpecialistExam()});
        pairs.add(new Object[]{"auxiliary_exam", oldRecord.getAuxiliaryExam(), dto.getAuxiliaryExam()});
        pairs.add(new Object[]{"diagnosis_name", oldRecord.getDiagnosisName(), dto.getDiagnosisName()});
        pairs.add(new Object[]{"diagnosis_code", oldRecord.getDiagnosisCode(), dto.getDiagnosisCode()});
        pairs.add(new Object[]{"treatment_plan", oldRecord.getTreatmentPlan(), dto.getTreatmentPlan()});
        pairs.add(new Object[]{"course_note", oldRecord.getCourseNote(), dto.getCourseNote()});
        if (dto.getRemark() != null) {
            pairs.add(new Object[]{"remark", oldRecord.getRemark(), dto.getRemark()});
        }

        List<BizInpatientRecordLog> changes = new ArrayList<>();
        for (Object[] p : pairs) {
            String code = (String) p[0];
            Object oldV = p[1];
            Object newV = p[2];
            if (Objects.equals(asText(oldV), asText(newV))) {
                continue;
            }
            BizInpatientRecordLog row = actionLog(oldRecord, "修改");
            row.setFieldName(code);
            row.setOldValue(asText(oldV));
            row.setNewValue(asText(newV));
            changes.add(row);
        }
        return changes;
    }

    // 私有辅助

    /**
     * 动作日志（创建/提交/归档）：不带字段三件套，靠 operation 表达
     */
    private BizInpatientRecordLog actionLog(BizInpatientRecord record, String operation) {
        BizInpatientRecordLog row = new BizInpatientRecordLog();
        row.setDocType(RecordDocTypeEnum.MEDICAL.getCode());
        row.setRecordId(record.getId());
        row.setRecordNo(record.getRecordNo());
        row.setRecordType(record.getRecordType());
        row.setUserId(currentEmpId());
        row.setUserName(currentName());
        row.setOperation(operation);
        return row;
    }

    private void writeActionLog(BizInpatientRecord record, String operation) {
        logMapper.insert(actionLog(record, operation));
    }

    private void fillStructured(BizInpatientRecord r,
                                java.util.function.Consumer<Integer> filledSetter,
                                java.util.function.Consumer<Integer> totalSetter,
                                java.util.function.Consumer<BigDecimal> rateSetter,
                                java.util.function.Consumer<String> rateTextSetter,
                                java.util.function.Consumer<List<String>> missingSetter) {
        int filled = RecordStructuredFields.filledCount(r);
        int total = RecordStructuredFields.totalCount(r);
        BigDecimal rate = RecordStructuredFields.rate(filled, total);
        filledSetter.accept(filled);
        totalSetter.accept(total);
        rateSetter.accept(rate);
        rateTextSetter.accept(rateText(rate));
        missingSetter.accept(RecordStructuredFields.missingLabels(r));
    }

    private void fillButtons(java.util.function.Consumer<Boolean> edit,
                             java.util.function.Consumer<Boolean> submit,
                             java.util.function.Consumer<Boolean> archive,
                             Integer status,
                             Integer signStatus) {
        // 已签名 = 内容锁定：按钮先灰掉，别让医生白填一遍才被服务端拒绝
        boolean signed = Objects.equals(1, signStatus);
        edit.accept(!InpatientRecordStatusEnum.isArchived(status) && !signed);
        submit.accept(Objects.equals(RecordStatusEnum.DRAFT.getCode(), status) && !signed);
        archive.accept(Objects.equals(RecordStatusEnum.SUBMITTED.getCode(), status));
    }

    private String patientNameOf(Long patientId) {
        BizPatient p = patientMapper.selectById(patientId);
        return p == null ? null : p.getPatientName();
    }

    /**
     * 年龄：档案里有 age 用 age（单位=岁）；没有则按出生日期算（算不出就留空，不猜）
     */
    private void fillAge(BizInpatientRecord record, BizPatient patient) {
        if (patient.getAge() != null) {
            record.setAge(patient.getAge());
            record.setAgeUnit(1);
            return;
        }
        if (patient.getBirthDate() != null) {
            int years = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
            if (years < 1) {
                long months = ChronoUnit.MONTHS.between(patient.getBirthDate(), LocalDate.now());
                record.setAge((int) Math.max(months, 0));
                record.setAgeUnit(2);
            } else {
                record.setAge(years);
                record.setAgeUnit(1);
            }
        }
    }

    private String nextRecordNo() {
        String prefix = "BL" + LocalDate.now().format(NO_DATE);
        long seq = recordMapper.countByRecordNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 文书的医生留痕一律用**员工ID**（不是用户的ID），与 P1 医嘱同一口径
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

    /**
     * 签名人的科室快照：证书与签名行上都要记"签名当时在哪个科室"（人可能转科）
     */
    private Long currentDeptId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            return user == null ? null : user.getDeptId();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentDeptName() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            return user == null ? null : user.getDeptName();
        } catch (Exception e) {
            return null;
        }
    }
}
