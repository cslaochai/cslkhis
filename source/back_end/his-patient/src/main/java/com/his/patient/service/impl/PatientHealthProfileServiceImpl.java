package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictTypeConst;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.mapper.*;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.support.HealthProfileEnums;
import com.his.patient.support.PatientProfileValidator;
import com.his.patient.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysDictData;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 患者健康档案六组业务闭环（详见 PatientHealthProfileService 的口径说明）。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatientHealthProfileServiceImpl extends ServiceImpl<BizPatientContactMapper, BizPatientContact> implements PatientHealthProfileService {

    /**
     * 与患者关系字典（患者联系方式.relationship 的码值来源）
     */
    public static final String RELATION_DICT = DictTypeConst.PATIENT_RELATION;

    /**
     * 拼摘要时的分隔符：与 CDR 健康档案卡片、页面上的多值展示同一口径
     */
    private static final String JOINER = "、";

    /**
     * 迁移来源标记：写进明细行的 remark，页面上能分辨这一行是怎么来的
     */
    private static final String MIGRATED_NOTE = "由主档自由文本迁移生成";

    private final BizPatientMapper bizPatientMapper;

    private final BizPatientAllergyMapper bizPatientAllergyMapper;

    private final BizPatientPastDiseaseMapper bizPatientPastDiseaseMapper;

    private final BizPatientSurgeryHistoryMapper bizPatientSurgeryHistoryMapper;

    private final BizPatientFamilyHistoryMapper bizPatientFamilyHistoryMapper;

    private final BizPatientMedicationHistoryMapper bizPatientMedicationHistoryMapper;

    private final BizPatientContactMapper bizPatientContactMapper;

    private final DictCacheService dictCacheService;

    /* ==================== 读 ==================== */

    @Override
    public PatientHealthProfileVO getProfile(Long patientId) {
        // C-非 web 入参：BizPatientServiceImpl#getPatientDetail 直调本方法（service 间调用不过 HTTP 参数绑定），
        // Bean Validation 不覆盖，保留
        if (patientId == null) {
            throw new BusinessException("患者信息不能为空");
        }
        BizPatient patient = bizPatientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在或已删除");
        }
        PatientHealthProfileVO vo = new PatientHealthProfileVO();
        vo.setPatientId(patient.getId());
        vo.setPatientNo(patient.getPatientNo());
        vo.setPatientName(patient.getPatientName());
        vo.setGender(patient.getGender());
        vo.setBirthDate(patient.getBirthDate());
        vo.setAge(patient.getAge());

        List<PatientAllergyVO> allergies = listAllergies(patientId);
        List<PatientPastDiseaseVO> pastDiseases = listPastDiseases(patientId);
        List<PatientContactVO> contacts = listContacts(patientId);
        vo.setAllergies(allergies);
        vo.setPastDiseases(pastDiseases);
        vo.setSurgeryHistories(listSurgeries(patientId));
        vo.setFamilyHistories(listFamilies(patientId));
        vo.setMedications(listMedications(patientId));
        vo.setContacts(contacts);

        vo.setAllergyHistoryText(patient.getAllergyHistory());
        vo.setMedicalHistoryText(patient.getMedicalHistory());
        vo.setContactNameText(patient.getContactName());
        vo.setContactPhoneText(patient.getContactPhone());
        vo.setContactRelationText(patient.getContactRelation());

        // 分叉判据只有一种（见 VO 注释）：明细为空但文本有值 = 只以自由文本存在、页面上不可维护
        vo.setAllergyTextOnly(allergies.isEmpty() && TextUtil.hasText(patient.getAllergyHistory()));
        vo.setPastDiseaseTextOnly(pastDiseases.isEmpty() && TextUtil.hasText(patient.getMedicalHistory()));
        vo.setContactTextOnly(contacts.isEmpty()
                && (TextUtil.hasText(patient.getContactName()) || TextUtil.hasText(patient.getContactPhone())));
        return vo;
    }

    private List<PatientAllergyVO> listAllergies(Long patientId) {
        // 只按 id 倒序。**不按 allergySeverity 排序** —— 那是 varchar，
        // 排序结果是「未/危/轻/重」的字符序，看着像按严重程度排、实际毫无临床含义。
        // 真要按严重程度排，得用 CASE 显式定义档位，别让字符序冒充业务序。
        return bizPatientAllergyMapper.selectList(new LambdaQueryWrapper<BizPatientAllergy>()
                        .eq(BizPatientAllergy::getPatientId, patientId)
                        .orderByDesc(BizPatientAllergy::getId))
                .stream().map(e -> {
                    PatientAllergyVO vo = new PatientAllergyVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    private List<PatientPastDiseaseVO> listPastDiseases(Long patientId) {
        return bizPatientPastDiseaseMapper.selectList(new LambdaQueryWrapper<BizPatientPastDisease>()
                        .eq(BizPatientPastDisease::getPatientId, patientId)
                        .orderByDesc(BizPatientPastDisease::getId))
                .stream().map(e -> {
                    PatientPastDiseaseVO vo = new PatientPastDiseaseVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    private List<PatientSurgeryHistoryVO> listSurgeries(Long patientId) {
        return bizPatientSurgeryHistoryMapper.selectList(new LambdaQueryWrapper<BizPatientSurgeryHistory>()
                        .eq(BizPatientSurgeryHistory::getPatientId, patientId)
                        .orderByDesc(BizPatientSurgeryHistory::getSurgeryDate)
                        .orderByDesc(BizPatientSurgeryHistory::getId))
                .stream().map(e -> {
                    PatientSurgeryHistoryVO vo = new PatientSurgeryHistoryVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    private List<PatientFamilyHistoryVO> listFamilies(Long patientId) {
        return bizPatientFamilyHistoryMapper.selectList(new LambdaQueryWrapper<BizPatientFamilyHistory>()
                        .eq(BizPatientFamilyHistory::getPatientId, patientId)
                        .orderByAsc(BizPatientFamilyHistory::getId))
                .stream().map(e -> {
                    PatientFamilyHistoryVO vo = new PatientFamilyHistoryVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    private List<PatientMedicationHistoryVO> listMedications(Long patientId) {
        return bizPatientMedicationHistoryMapper.selectList(new LambdaQueryWrapper<BizPatientMedicationHistory>()
                        .eq(BizPatientMedicationHistory::getPatientId, patientId)
                        .orderByDesc(BizPatientMedicationHistory::getStartDate)
                        .orderByDesc(BizPatientMedicationHistory::getId))
                .stream().map(e -> {
                    PatientMedicationHistoryVO vo = new PatientMedicationHistoryVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    private List<PatientContactVO> listContacts(Long patientId) {
        return bizPatientContactMapper.selectList(new LambdaQueryWrapper<BizPatientContact>()
                        .eq(BizPatientContact::getPatientId, patientId)
                        .orderByDesc(BizPatientContact::getIsPrimary)
                        .orderByAsc(BizPatientContact::getId))
                .stream().map(e -> {
                    PatientContactVO vo = new PatientContactVO();
                    BeanUtils.copyProperties(e, vo);
                    vo.setRelationshipText(relationLabel(e.getRelationship()));
                    return vo;
                }).collect(Collectors.toList());
    }

    /* ==================== 过敏史 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientAllergyVO saveAllergy(PatientAllergyUpsertDTO dto) {
        // allergy_type / allergy_severity / allergy_symptoms 三列都是 NOT NULL：
        // 少拦一个，用户"没选类型"就会拿到 500「系统内部错误」而不是可读提示。
        // 类型**必填**（表列注释的取值就是药物/食物/其他，没有"未知"档，
        // 替用户编一个「其他」等于替他做了一次临床判断）；严重程度缺失则落「未评估」
        // —— 与主档文本迁移同一口径，说的是实话而不是编一个档位。
        requireInEnum("过敏类型", dto.getAllergyType(), HealthProfileEnums.ALLERGY_TYPE);
        if (!TextUtil.hasText(dto.getAllergySeverity())) {
            dto.setAllergySeverity(HealthProfileEnums.SEVERITY_UNKNOWN);
        }
        requireInEnum("过敏严重程度", dto.getAllergySeverity(), HealthProfileEnums.ALLERGY_SEVERITY);
        if (dto.getAllergySymptoms() == null) {
            dto.setAllergySymptoms("");
        }

        BizPatientAllergy entity = new BizPatientAllergy();
        BeanUtils.copyProperties(dto, entity);
        Long patientId = resolvePatientId(dto.getId(), dto.getPatientId(),
                id -> {
                    BizPatientAllergy old = bizPatientAllergyMapper.selectById(id);
                    return old == null ? null : old.getPatientId();
                }, "过敏史");
        entity.setPatientId(patientId);
        if (entity.getOccurrenceCount() == null) {
            entity.setOccurrenceCount(1);
        }
        persist(entity, dto.getId(), bizPatientAllergyMapper::insert, bizPatientAllergyMapper::updateById);
        syncAllergyProjection(patientId);

        PatientAllergyVO vo = new PatientAllergyVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllergy(Long id) {
        Long patientId = requireExistingPatientId(id, "过敏史", bizPatientAllergyMapper::selectById,
                BizPatientAllergy::getPatientId);
        bizPatientAllergyMapper.deleteById(id);
        syncAllergyProjection(patientId);
    }

    @Override
    public void syncAllergyProjection(Long patientId) {
        List<String> parts = listAllergies(patientId).stream()
                .map(a -> {
                    // 严重程度为空或「未评估」时不加括号：一行摘要是给警示条读的，
                    // 「青霉素（未评估）」里的括号只增加噪音；详情页照实显示未评估。
                    String sev = a.getAllergySeverity();
                    boolean showSev = TextUtil.hasText(sev)
                            && !HealthProfileEnums.SEVERITY_UNKNOWN.equals(sev);
                    return showSev ? a.getAllergenName() + "（" + sev + "）" : a.getAllergenName();
                })
                .filter(TextUtil::hasText)
                .collect(Collectors.toList());
        writePatientText(patientId, "allergy_history", String.join(JOINER, parts));
    }

    /* ==================== 既往疾病史 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientPastDiseaseVO savePastDisease(PatientPastDiseaseUpsertDTO dto) {
        requireInEnum("控制情况", dto.getCurrentStatus(), HealthProfileEnums.DISEASE_CURRENT_STATUS);

        BizPatientPastDisease entity = new BizPatientPastDisease();
        BeanUtils.copyProperties(dto, entity);
        Long patientId = resolvePatientId(dto.getId(), dto.getPatientId(),
                id -> {
                    BizPatientPastDisease old = bizPatientPastDiseaseMapper.selectById(id);
                    return old == null ? null : old.getPatientId();
                }, "既往疾病史");
        entity.setPatientId(patientId);
        if (entity.getRelapseCount() == null) {
            entity.setRelapseCount(0);
        }
        persist(entity, dto.getId(), bizPatientPastDiseaseMapper::insert, bizPatientPastDiseaseMapper::updateById);
        syncPastDiseaseProjection(patientId);

        PatientPastDiseaseVO vo = new PatientPastDiseaseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePastDisease(Long id) {
        Long patientId = requireExistingPatientId(id, "既往疾病史", bizPatientPastDiseaseMapper::selectById,
                BizPatientPastDisease::getPatientId);
        bizPatientPastDiseaseMapper.deleteById(id);
        syncPastDiseaseProjection(patientId);
    }

    @Override
    public void syncPastDiseaseProjection(Long patientId) {
        String summary = listPastDiseases(patientId).stream()
                .map(PatientPastDiseaseVO::getDiseaseName)
                .filter(TextUtil::hasText)
                .collect(Collectors.joining(JOINER));
        writePatientText(patientId, "medical_history", summary);
    }

    /* ==================== 手术外伤史 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientSurgeryHistoryVO saveSurgeryHistory(PatientSurgeryHistoryUpsertDTO dto) {
        requireInEnum("手术类型", dto.getSurgeryType(), HealthProfileEnums.SURGERY_TYPE);
        requireInEnum("恢复情况", dto.getRecoveryStatus(), HealthProfileEnums.RECOVERY_STATUS);

        BizPatientSurgeryHistory entity = new BizPatientSurgeryHistory();
        BeanUtils.copyProperties(dto, entity);
        entity.setPatientId(resolvePatientId(dto.getId(), dto.getPatientId(),
                id -> {
                    BizPatientSurgeryHistory old = bizPatientSurgeryHistoryMapper.selectById(id);
                    return old == null ? null : old.getPatientId();
                }, "手术外伤史"));
        persist(entity, dto.getId(), bizPatientSurgeryHistoryMapper::insert, bizPatientSurgeryHistoryMapper::updateById);

        PatientSurgeryHistoryVO vo = new PatientSurgeryHistoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSurgeryHistory(Long id) {
        requireExistingPatientId(id, "手术外伤史", bizPatientSurgeryHistoryMapper::selectById,
                BizPatientSurgeryHistory::getPatientId);
        bizPatientSurgeryHistoryMapper.deleteById(id);
    }

    /* ==================== 家族史 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientFamilyHistoryVO saveFamilyHistory(PatientFamilyHistoryUpsertDTO dto) {
        if (Integer.valueOf(0).equals(dto.getIsAlive()) && !TextUtil.hasText(dto.getCauseOfDeath())) {
            // ①条件必填：只有选了「已故」才必填死亡原因，@NotNull 一刀切会挡掉合法的在世提交
            throw new BusinessException("已故亲属必须填写死亡原因");
        }

        BizPatientFamilyHistory entity = new BizPatientFamilyHistory();
        BeanUtils.copyProperties(dto, entity);
        entity.setPatientId(resolvePatientId(dto.getId(), dto.getPatientId(),
                id -> {
                    BizPatientFamilyHistory old = bizPatientFamilyHistoryMapper.selectById(id);
                    return old == null ? null : old.getPatientId();
                }, "家族史"));
        if (entity.getIsAlive() == null) {
            entity.setIsAlive(1);
        }
        persist(entity, dto.getId(), bizPatientFamilyHistoryMapper::insert, bizPatientFamilyHistoryMapper::updateById);

        PatientFamilyHistoryVO vo = new PatientFamilyHistoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFamilyHistory(Long id) {
        requireExistingPatientId(id, "家族史", bizPatientFamilyHistoryMapper::selectById,
                BizPatientFamilyHistory::getPatientId);
        bizPatientFamilyHistoryMapper.deleteById(id);
    }

    /* ==================== 既往用药史 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientMedicationHistoryVO saveMedication(PatientMedicationHistoryUpsertDTO dto) {
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            // D-业务规则：跨字段的临床时序约束（停药不能早于开始），DTO 注解无处安放
            throw new BusinessException("停药日期不能早于开始用药日期");
        }
        requireInEnum("药物类型", dto.getDrugType(), HealthProfileEnums.DRUG_TYPE);
        requireInEnum("给药途径", dto.getRoute(), HealthProfileEnums.DRUG_ROUTE);
        requireInEnum("用药状态", dto.getStatus(), HealthProfileEnums.MEDICATION_STATUS);

        BizPatientMedicationHistory entity = new BizPatientMedicationHistory();
        BeanUtils.copyProperties(dto, entity);
        entity.setPatientId(resolvePatientId(dto.getId(), dto.getPatientId(),
                id -> {
                    BizPatientMedicationHistory old = bizPatientMedicationHistoryMapper.selectById(id);
                    return old == null ? null : old.getPatientId();
                }, "用药史"));
        // 不依赖表列 DEFAULT '已完成'（那个值不在列注释的状态枚举里），显式给默认状态
        if (!TextUtil.hasText(entity.getStatus())) {
            entity.setStatus(HealthProfileEnums.DEFAULT_MEDICATION_STATUS);
        }
        persist(entity, dto.getId(), bizPatientMedicationHistoryMapper::insert, bizPatientMedicationHistoryMapper::updateById);

        PatientMedicationHistoryVO vo = new PatientMedicationHistoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMedication(Long id) {
        requireExistingPatientId(id, "用药史", bizPatientMedicationHistoryMapper::selectById,
                BizPatientMedicationHistory::getPatientId);
        bizPatientMedicationHistoryMapper.deleteById(id);
    }

    /* ==================== 联系人 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientContactVO saveContact(PatientContactUpsertDTO dto) {
        // 码值必须命中字典：不校验的话「配偶」这种文案会被 MySQL 隐式转成 0 静默落库
        if (!relationCodes().contains(dto.getRelationship())) {
            // D-业务规则：码值合法性，与"字段填没填"无关
            throw new BusinessException("与患者关系取值不合法：" + dto.getRelationship()
                    + "（请用字典「与患者关系」的码值，如 2-配偶 3-父亲 99-其他）");
        }
        if (TextUtil.hasText(dto.getPhone()) && !PatientProfileValidator.isLegalPhone(dto.getPhone().trim())) {
            // D-业务规则：电话是选填项，填了才校格式，不是"必填"判断
            throw new BusinessException("联系人电话格式不正确：应为 11 位手机号（1 开头）");
        }

        BizPatientContact entity = new BizPatientContact();
        BeanUtils.copyProperties(dto, entity);
        Long patientId = resolvePatientId(dto.getId(), dto.getPatientId(),
                id -> {
                    BizPatientContact old = bizPatientContactMapper.selectById(id);
                    return old == null ? null : old.getPatientId();
                }, "联系人");
        entity.setPatientId(patientId);
        if (entity.getIsPrimary() == null) {
            entity.setIsPrimary(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        // 主要联系人唯一：把一个联系人设为主要时，同患者其它联系人降级，
        // 否则主档的三列投影取谁就成了看 id 顺序的随机结果
        if (Integer.valueOf(1).equals(entity.getIsPrimary())) {
            clearOtherPrimary(patientId, dto.getId());
        }
        persist(entity, dto.getId(), bizPatientContactMapper::insert, bizPatientContactMapper::updateById);
        syncContactProjection(patientId);

        PatientContactVO vo = new PatientContactVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setRelationshipText(relationLabel(entity.getRelationship()));
        return vo;
    }

    @Override
    public PatientContactVO getContact(Long contactId) {
        BizPatientContact row = bizPatientContactMapper.selectById(contactId);
        if (row == null) {
            throw new BusinessException("联系人不存在或已删除");
        }
        PatientContactVO vo = new PatientContactVO();
        BeanUtils.copyProperties(row, vo);
        vo.setRelationshipText(relationLabel(row.getRelationship()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContact(Long id) {
        Long patientId = requireExistingPatientId(id, "联系人", bizPatientContactMapper::selectById,
                BizPatientContact::getPatientId);
        bizPatientContactMapper.deleteById(id);
        syncContactProjection(patientId);
    }

    @Override
    public void syncContactProjection(Long patientId) {
        List<PatientContactVO> contacts = listContacts(patientId);
        if (contacts.isEmpty()) {
            writePatientContact(patientId, null, null, null);
            return;
        }
        // listContacts 已按 is_primary desc, id asc 排好，取第一条即主要联系人（无主要时取最早一条）
        PatientContactVO main = contacts.get(0);
        writePatientContact(patientId, main.getContactName(), main.getPhone(),
                relationLabel(main.getRelationship()));
    }

    private void clearOtherPrimary(Long patientId, Long keepId) {
        bizPatientContactMapper.update(null, new LambdaUpdateWrapper<BizPatientContact>()
                .eq(BizPatientContact::getPatientId, patientId)
                .ne(keepId != null, BizPatientContact::getId, keepId)
                .set(BizPatientContact::getIsPrimary, 0));
    }

    /* ==================== 主档保存后的同步 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncAfterPatientSave(BizPatient patient) {
        if (patient == null || patient.getId() == null) {
            return;
        }
        Long patientId = patient.getId();
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String operator = operatorUser.getRealName();

        // 过敏史
        if (!listAllergies(patientId).isEmpty()) {
            // 已有明细 → 明细权威，文本按明细重算（主档那个文本框只是投影）
            syncAllergyProjection(patientId);
        } else if (TextUtil.hasText(patient.getAllergyHistory())) {
            // 没有明细 → 文本是快速录入的内容，落成一条明细。
            // 严重程度写「未评估」而不是默认一个「中度」—— 编出来的严重程度会真的被当成临床信息读，
            // 「未评估」至少在页面上说的是实话。
            BizPatientAllergy a = new BizPatientAllergy();
            a.setPatientId(patientId);
            a.setAllergenName(TextUtil.cut(patient.getAllergyHistory().trim(), 200));
            a.setAllergyType(HealthProfileEnums.guessAllergyType(patient.getAllergyHistory()));
            a.setAllergySeverity(HealthProfileEnums.SEVERITY_UNKNOWN);
            a.setAllergySymptoms("");
            a.setOccurrenceCount(1);
            a.setRemark(MIGRATED_NOTE + "（严重程度未评估）");
            a.setCreateBy(operator);
            a.setUpdateBy(operator);
            bizPatientAllergyMapper.insert(a);
            syncAllergyProjection(patientId);
        }
        // else：明细为空且文本也为空 —— **什么都不做**。
        // 这里绝不能顺手调一次 syncAllergyProjection()：它会把文本重算成空，
        // 于是「患者主档里改了个电话」就能把还没迁移成明细的过敏史文本洗掉。
        // 实测存量有 14 份这样的档案（文本有值、明细为空），一次误调就是 14 条临床信息消失。
        // 清空文本只允许由「用户显式删掉该组的最后一条明细」触发，见 deleteAllergy。

        // 既往病史
        if (!listPastDiseases(patientId).isEmpty()) {
            syncPastDiseaseProjection(patientId);
        } else if (TextUtil.hasText(patient.getMedicalHistory())) {
            BizPatientPastDisease d = new BizPatientPastDisease();
            d.setPatientId(patientId);
            d.setDiseaseName(TextUtil.cut(patient.getMedicalHistory().trim(), 200));
            d.setRelapseCount(0);
            d.setRemark(MIGRATED_NOTE + "（整句作为病名，未拆分）");
            d.setCreateBy(operator);
            d.setUpdateBy(operator);
            bizPatientPastDiseaseMapper.insert(d);
            syncPastDiseaseProjection(patientId);
        }
        // else：同上，明细与文本都空时不动（理由见过敏史那段）

        // 联系人（主档的三列合成一条主要联系人）
        if (!listContacts(patientId).isEmpty()) {
            syncContactProjection(patientId);
        } else if (TextUtil.hasText(patient.getContactName())
                || TextUtil.hasText(patient.getContactPhone())) {
            BizPatientContact c = new BizPatientContact();
            c.setPatientId(patientId);
            c.setContactName(TextUtil.hasText(patient.getContactName())
                    ? TextUtil.cut(patient.getContactName().trim(), 100) : "未填姓名");
            c.setRelationship(relationCode(patient.getContactRelation()));
            c.setPhone(patient.getContactPhone());
            c.setIsPrimary(1);
            c.setStatus(1);
            c.setRemark(MIGRATED_NOTE);
            c.setCreateBy(operator);
            c.setUpdateBy(operator);
            bizPatientContactMapper.insert(c);
            syncContactProjection(patientId);
        }
        // else：同上，明细与文本都空时不动（理由见过敏史那段）
    }

    /* ==================== 公共工具 ==================== */

    /**
     * 主档文本投影的统一写入口：必须用 LambdaUpdateWrapper.set，才能把值真正置成 null
     */
    private void writePatientText(Long patientId, String column, String value) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String text = TextUtil.hasText(value) ? value : null;
        LambdaUpdateWrapper<BizPatient> wrapper = new LambdaUpdateWrapper<BizPatient>()
                .eq(BizPatient::getId, patientId);
        if ("allergy_history".equals(column)) {
            wrapper.set(BizPatient::getAllergyHistory, text);
        } else {
            wrapper.set(BizPatient::getMedicalHistory, text);
        }
        wrapper.set(BizPatient::getUpdateBy, operatorUser.getRealName());
        bizPatientMapper.update(null, wrapper);
    }

    private void writePatientContact(Long patientId, String name, String phone, String relationText) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        bizPatientMapper.update(null, new LambdaUpdateWrapper<BizPatient>()
                .eq(BizPatient::getId, patientId)
                .set(BizPatient::getContactName, name)
                .set(BizPatient::getContactPhone, phone)
                .set(BizPatient::getContactRelation, relationText)
                .set(BizPatient::getUpdateBy, operatorUser.getRealName()));
    }

    /**
     * 解析这次写操作属于哪个患者。
     *
     * <p>新增：用入参 patientId（必填）。修改：**以库中记录的 patientId 为准**，
     * 不信入参 —— 否则改一条自己的过敏史、把 patientId 换成别人，就能把记录挪到别人档案下，
     * 而这种「挪档案」在页面看不出任何异常。
     */
    private Long resolvePatientId(Long id, Long paramPatientId, java.util.function.Function<Long, Long> ownerOf,
                                  String what) {
        if (id == null) {
            // B-条件必填：只有新增（id==null）才必填 patientId，修改时以库中记录为准，
            // @NotNull 会把合法修改挡成 400，DTO 注解无法表达，保留
            if (paramPatientId == null) {
                throw new BusinessException("患者信息不能为空");
            }
            BizPatient patient = bizPatientMapper.selectById(paramPatientId);
            if (patient == null) {
                throw new BusinessException("患者不存在或已删除");
            }
            return paramPatientId;
        }
        Long owner = ownerOf.apply(id);
        if (owner == null) {
            throw new BusinessException("要修改的" + what + "不存在或已删除");
        }
        return owner;
    }

    private <T> Long requireExistingPatientId(Long id, String what,
                                              java.util.function.Function<Long, T> loader,
                                              java.util.function.Function<T, Long> ownerOf) {
        T row = loader.apply(id);
        if (row == null) {
            throw new BusinessException("要删除的" + what + "不存在或已删除");
        }
        return ownerOf.apply(row);
    }

    /**
     * 新增/修改二选一：判据是**入参有没有 id**，不是实体有没有
     */
    private <T> void persist(T entity, Long id,
                             java.util.function.Function<T, Integer> inserter,
                             java.util.function.Function<T, Integer> updater) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String operator = operatorUser.getRealName();
        if (id == null) {
            applyCreateBy(entity, operator);
            inserter.apply(entity);
        } else {
            applyUpdateBy(entity, operator);
            updater.apply(entity);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyCreateBy(Object entity, String operator) {
        if (entity instanceof com.his.common.base.BaseEntity be) {
            be.setCreateBy(operator);
            be.setUpdateBy(operator);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyUpdateBy(Object entity, String operator) {
        if (entity instanceof com.his.common.base.BaseEntity be) {
            be.setUpdateBy(operator);
            // 更新时不带 create_by（null 会被 MP 忽略），避免把它冲掉
            be.setCreateBy(null);
        }
    }

    private void requireInEnum(String label, String value, java.util.Set<String> allowed) {
        if (!TextUtil.hasText(value)) {
            return;
        }
        if (!allowed.contains(value)) {
            throw new BusinessException(label + "取值不合法：" + value + "（可选："
                    + String.join("/", allowed) + "）");
        }
    }

    /* ---- 与患者关系字典 ---- */

    private List<SysDictData> relationDict() {
        return dictCacheService.getDictDataByType(RELATION_DICT);
    }

    /**
     * 码值 → 文案（走字典翻译）；命中不了返回空串，不回落成看似合法的值
     */
    private String relationLabel(Integer code) {
        if (code == null) {
            return null;
        }
        return relationDict().stream()
                .filter(d -> String.valueOf(code).equals(d.getDictValue()))
                .map(SysDictData::getDictLabel)
                .findFirst()
                .orElse("");
    }

    private java.util.Set<Integer> relationCodes() {
        return relationDict().stream()
                .map(d -> {
                    try {
                        return Integer.valueOf(d.getDictValue());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 文案/码值 → 码值（迁移用）。
     *
     * <p>兼容两种历史写法：有的人把码值当文案填了（'2'、'14'），有的人填了称谓（'配偶'）。
     * 都命中不了时落 99-其他，**绝不**落 null —— 表列是 NOT NULL，
     * 而且落 null 会让这条联系人彻底没法在页面上维护。
     */
    private Integer relationCode(String text) {
        List<SysDictData> dict = relationDict();
        if (TextUtil.hasText(text) && dict != null) {
            String t = text.trim();
            for (SysDictData d : dict) {
                if (t.equals(d.getDictLabel()) || t.equals(d.getDictValue())) {
                    try {
                        return Integer.valueOf(d.getDictValue());
                    } catch (Exception ignored) {
                        // 字典里配了非数字 value，按命中不了处理
                    }
                }
            }
        }
        return dict.stream()
                .filter(d -> "99".equals(d.getDictValue()))
                .findFirst()
                .map(d -> 99)
                .orElse(null);
    }
}
