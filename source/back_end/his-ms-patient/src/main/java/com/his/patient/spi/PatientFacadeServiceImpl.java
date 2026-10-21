package com.his.patient.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.UserTypeEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.utils.UserUtils;
import com.his.common.util.TextUtil;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientAllergy;
import com.his.patient.entity.BizPatientGuardian;
import com.his.patient.entity.SysPatientTag;
import com.his.patient.mapper.BizPatientAllergyMapper;
import com.his.patient.mapper.BizPatientGuardianMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.mapper.SysPatientTagMapper;
import com.his.patient.support.PatientProfileFields;
import com.his.patient.support.PatientProfileValidator;
import com.his.spi.dto.EmergencyPatientCreateDTO;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.service.RedisSequenceService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.AllergyFactVO;
import com.his.spi.vo.PatientBriefVO;
import com.his.spi.vo.PatientMasterVO;
import com.his.spi.vo.PatientProfileScoreVO;
import com.his.spi.vo.PatientTagBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 患者域窄端口适配器：只依赖本域 mapper 与 system/spi 基础服务，不牵住院编排服务，避免 Bean 环。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatientFacadeServiceImpl implements PatientFacadeService {

    private final BizPatientMapper bizPatientMapper;

    private final BizPatientAllergyMapper bizPatientAllergyMapper;

    private final BizPatientGuardianMapper bizPatientGuardianMapper;

    private final SysPatientTagMapper sysPatientTagMapper;

    private final SystemFacadeService systemFacadeService;

    private final RedisSequenceService redisSequenceService;

    @Override
    public PatientBriefVO findPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return toBrief(bizPatientMapper.selectById(patientId));
    }

    // ---------- 就诊人绑定 / 越权判定 ----------

    @Override
    public boolean canAccessPatient(Long patientId) {
        if (patientId == null) {
            return false;
        }
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getUserId() == null
                || !Integer.valueOf(UserTypeEnum.PATIENT.getCode()).equals(user.getUserType())) {
            return false;
        }
        // 本人档案恒可访问
        if (patientId.equals(user.getPatientId())) {
            return true;
        }
        return bizPatientGuardianMapper.selectCount(new LambdaQueryWrapper<BizPatientGuardian>()
                        .eq(BizPatientGuardian::getUserId, user.getUserId())
                        .eq(BizPatientGuardian::getPatientId, patientId)
                        .eq(BizPatientGuardian::getStatus, 1))
                > 0;
    }

    @Override
    public boolean patientScopeViolated(Long patientId) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || !Integer.valueOf(UserTypeEnum.PATIENT.getCode()).equals(user.getUserType())) {
            return false;
        }
        return !canAccessPatient(patientId);
    }

    @Override
    public void bindOpenid(String openid) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || !Integer.valueOf(UserTypeEnum.PATIENT.getCode()).equals(user.getUserType())) {
            throw new BusinessException("仅患者账号可管理就诊人");
        }
        if (!TextUtil.hasText(openid) || openid.length() > 64) {
            throw new BusinessException("openid 不合法");
        }
        // 只更新自己账号的 openid；唯一性冲突（该微信已绑别的账号）由 system 侧拒绝
        systemFacadeService.bindOpenid(user.getUserId(), openid);
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
        // 有身份证就以它为准补出生日期/年龄（和主档建档同一口径）
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
        if (patientId == null) {
            return;
        }
        bizPatientMapper.markFirstVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
    }

    @Override
    public void markLastVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                              Long doctorId, String doctorName) {
        if (patientId == null) {
            return;
        }
        bizPatientMapper.markLastVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
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

    // ---------- EMPI ----------

    @Override
    public List<Long> resolvePatientIds(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        BizPatient p = bizPatientMapper.selectById(patientId);
        if (p == null) {
            return List.of(patientId);
        }
        Long masterId = p.getMasterId() != null ? p.getMasterId() : p.getId();
        LambdaQueryWrapper<BizPatient> w = new LambdaQueryWrapper<>();
        w.and(q -> q.eq(BizPatient::getId, masterId).or().eq(BizPatient::getMasterId, masterId));
        List<BizPatient> all = bizPatientMapper.selectList(w);
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(masterId);
        all.forEach(x -> ids.add(x.getId()));
        return new ArrayList<>(ids);
    }

    // ---------- 患者标签 ----------

    @Override
    public List<PatientTagBriefVO> listPatientTagsByIds(Collection<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return toPatientTagBriefList(sysPatientTagMapper.selectBatchIds(tagIds));
    }

    @Override
    public List<PatientTagBriefVO> listPatientTagsByNames(Collection<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        return toPatientTagBriefList(sysPatientTagMapper.selectList(
                new LambdaQueryWrapper<SysPatientTag>().in(SysPatientTag::getTagName, tagNames)));
    }

    private List<PatientTagBriefVO> toPatientTagBriefList(List<SysPatientTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        List<PatientTagBriefVO> result = new ArrayList<>(tags.size());
        for (SysPatientTag tag : tags) {
            PatientTagBriefVO vo = new PatientTagBriefVO();
            vo.setTagId(tag.getTagId());
            vo.setTagName(tag.getTagName());
            vo.setShortName(tag.getShortName());
            vo.setTagColor(tag.getTagColor());
            result.add(vo);
        }
        return result;
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

    private PatientMasterVO toMaster(BizPatient e) {
        if (e == null) {
            return null;
        }
        PatientMasterVO vo = new PatientMasterVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }
}
