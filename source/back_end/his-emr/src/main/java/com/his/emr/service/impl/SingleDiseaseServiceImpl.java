package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.SingleDiseaseDTO;
import com.his.emr.entity.BizSingleDiseaseCase;
import com.his.emr.entity.SysSingleDisease;
import com.his.emr.enums.SingleDiseaseQcStatusEnum;
import com.his.emr.mapper.BizSingleDiseaseCaseMapper;
import com.his.emr.mapper.SysSingleDiseaseMapper;
import com.his.emr.service.SingleDiseaseService;
import com.his.emr.vo.InpatientSummarySnapshotVO;
import com.his.emr.vo.SingleDiseaseAutoEnrollStatVO;
import com.his.emr.vo.SingleDiseaseCandidateVO;
import com.his.emr.vo.SingleDiseaseVO;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 单病种质控服务（M4）。
 *
 * <p>纳入规则：出院首页主要诊断编码命中病种 ICD-10 前缀（前缀匹配）。
 * 病例字段全部取自病案首页快照，纳入后只允许改质控/疗效/上报三组字段（底账不可改）。
 * 指标口径：治愈率=治愈/纳入；死亡率=死亡（death_flag 或疗效=4）/纳入；平均住院日/费用按纳入例数。
 */
@Service
@RequiredArgsConstructor
public class SingleDiseaseServiceImpl extends ServiceImpl<BizSingleDiseaseCaseMapper, BizSingleDiseaseCase> implements SingleDiseaseService {
    private final SysSingleDiseaseMapper sysSingleDiseaseMapper;
    private final BizSingleDiseaseCaseMapper bizSingleDiseaseCaseMapper;
    private final RedisSequenceService redisSequenceService;
    private DictCacheService dictCacheService;

    public List<SingleDiseaseVO.Disease> diseaseList() {
        List<SysSingleDisease> diseases = sysSingleDiseaseMapper.selectList(
                new LambdaQueryWrapper<SysSingleDisease>().orderByAsc(SysSingleDisease::getDiseaseCode));
        return diseases.stream().map(d -> {
            SingleDiseaseVO.Disease vo = new SingleDiseaseVO.Disease();
            vo.setId(d.getId());
            vo.setDiseaseCode(d.getDiseaseCode());
            vo.setDiseaseName(d.getDiseaseName());
            vo.setIcd10Prefix(d.getIcd10Prefix());
            vo.setRemark(d.getRemark());
            vo.setCaseCount(bizSingleDiseaseCaseMapper.selectCount(new LambdaQueryWrapper<BizSingleDiseaseCase>()
                    .eq(BizSingleDiseaseCase::getDiseaseId, d.getId())));
            return vo;
        }).toList();
    }

    /**
     * 目录新增/修改（编码唯一）。
     */
    @Transactional(rollbackFor = Exception.class)
    public SingleDiseaseVO.Disease diseaseUpsert(SingleDiseaseDTO.DiseaseUpsert dto) {
        SysSingleDisease entity = new SysSingleDisease();
        entity.setDiseaseCode(dto.getDiseaseCode());
        entity.setDiseaseName(dto.getDiseaseName());
        entity.setIcd10Prefix(normalizePrefix(dto.getIcd10Prefix()));
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (dto.getId() != null) {
            SysSingleDisease exist = sysSingleDiseaseMapper.selectById(dto.getId());
            if (exist == null) {
                throw new BusinessException("病种不存在");
            }
            entity.setId(dto.getId());
            sysSingleDiseaseMapper.updateById(entity);
        } else {
            entity.setCreateBy(UserUtils.getCurrentUser().getRealName());
            sysSingleDiseaseMapper.insert(entity);
        }
        SingleDiseaseVO.Disease vo = new SingleDiseaseVO.Disease();
        vo.setId(entity.getId());
        vo.setDiseaseCode(entity.getDiseaseCode());
        vo.setDiseaseName(entity.getDiseaseName());
        vo.setIcd10Prefix(entity.getIcd10Prefix());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    /**
     * 物理删除（纯配置表，唯一键不含 del_flag）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void diseaseDelete(Long id) {
        Long cases = bizSingleDiseaseCaseMapper.selectCount(new LambdaQueryWrapper<BizSingleDiseaseCase>()
                .eq(BizSingleDiseaseCase::getDiseaseId, id));
        if (cases > 0) {
            throw new BusinessException("该病种已纳入 " + cases + " 例病例，不能删除（可停用维护）");
        }
        sysSingleDiseaseMapper.purgeById(id);
    }

    /**
     * 前缀归一：去空格、去空段、统一大写。
     */
    private String normalizePrefix(String prefix) {
        String[] parts = prefix.split("[,，]");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            String t = p.trim().toUpperCase();
            if (!t.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append(',');
                }
                sb.append(t);
            }
        }
        if (sb.isEmpty()) {
            throw new BusinessException("ICD-10 前缀格式不正确");
        }
        return sb.toString();
    }

    // 纳入

    /**
     * 手工纳入：首页快照 + 唯一校验（同病种同住院一次）。
     */
    @Transactional(rollbackFor = Exception.class)
    public SingleDiseaseVO.Case enroll(SingleDiseaseDTO.Enroll dto) {
        SysSingleDisease disease = sysSingleDiseaseMapper.selectById(dto.getDiseaseId());
        if (disease == null) {
            throw new BusinessException("病种不存在");
        }
        return doEnroll(disease, dto.getAdmissionId(), 2);
    }

    /**
     * 自动扫描：按 ICD 前缀扫出院首页，批量纳入未入组病例。
     */
    @Transactional(rollbackFor = Exception.class)
    public SingleDiseaseAutoEnrollStatVO autoEnroll(SingleDiseaseDTO.AutoEnroll dto) {
        SysSingleDisease disease = sysSingleDiseaseMapper.selectById(dto.getDiseaseId());
        if (disease == null) {
            throw new BusinessException("病种不存在");
        }
        List<SingleDiseaseCandidateVO> candidates = bizSingleDiseaseCaseMapper.selectAutoEnrollCandidates(
                disease.getId(),
                java.util.Arrays.stream(disease.getIcd10Prefix().split(",")).map(String::trim).toList(),
                dto.getBeginDate(), dto.getEndDate());
        int ok = 0;
        List<String> skipped = new ArrayList<>();
        for (SingleDiseaseCandidateVO row : candidates) {
            Long admissionId = row.getAdmissionId();
            try {
                doEnroll(disease, admissionId, 1);
                ok++;
            } catch (BusinessException e) {
                skipped.add(admissionId + ":" + e.getMessage());
            }
        }
        SingleDiseaseAutoEnrollStatVO result = new SingleDiseaseAutoEnrollStatVO();
        result.setScanned(candidates.size());
        result.setEnrolled(ok);
        result.setSkipped(skipped);
        return result;
    }

    /**
     * 纳入主体：取首页快照、唯一校验、建底账行。
     */
    private SingleDiseaseVO.Case doEnroll(SysSingleDisease disease, Long admissionId, int enrollWay) {
        InpatientSummarySnapshotVO snap = bizSingleDiseaseCaseMapper.selectSummarySnapshot(admissionId);
        if (snap == null) {
            throw new BusinessException("该住院记录无病案首页（或住院记录不存在），不能纳入");
        }
        String diagCode = snap.getMainDiagnosisCode();
        if (diagCode == null || !matchPrefix(disease.getIcd10Prefix(), diagCode)) {
            throw new BusinessException("首页主要诊断 " + diagCode + " 不在病种纳入范围（"
                    + disease.getIcd10Prefix() + "），不能纳入");
        }
        Long dup = bizSingleDiseaseCaseMapper.selectCount(new LambdaQueryWrapper<BizSingleDiseaseCase>()
                .eq(BizSingleDiseaseCase::getDiseaseId, disease.getId())
                .eq(BizSingleDiseaseCase::getAdmissionId, admissionId));
        if (dup > 0) {
            throw new BusinessException("该病历已纳入病种「" + disease.getDiseaseName() + "」");
        }
        BizSingleDiseaseCase c = new BizSingleDiseaseCase();
        c.setCaseNo(generateCaseNo());
        c.setDiseaseId(disease.getId());
        c.setAdmissionId(admissionId);
        c.setPatientId(snap.getPatientId());
        c.setPatientName(snap.getPatientName() == null ? "未知" : snap.getPatientName());
        c.setMainDiagnosisCode(diagCode);
        c.setMainDiagnosisName(snap.getMainDiagnosisName());
        c.setInpatientDays(snap.getInpatientDays());
        c.setTotalAmount(snap.getTotalAmount());
        c.setIsSurgery(snap.getIsSurgery() == null ? 0 : snap.getIsSurgery());
        c.setDeathFlag(snap.getDeathFlag() == null ? 0 : snap.getDeathFlag());
        c.setEnrollWay(enrollWay);
        c.setQcStatus(SingleDiseaseQcStatusEnum.PENDING.getCode());
        c.setReportStatus(YesOrNoEnum.NO.getCode());
        c.setCreateBy(UserUtils.getCurrentUser().getRealName());
        bizSingleDiseaseCaseMapper.insert(c);
        return toCaseVO(c, disease.getDiseaseName());
    }

    /**
     * 主要诊断编码是否命中任一前缀。
     */
    private boolean matchPrefix(String prefixes, String diagCode) {
        for (String p : prefixes.split(",")) {
            if (!p.isEmpty() && diagCode.toUpperCase().startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private String generateCaseNo() {
        return "SD" + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%05d", redisSequenceService.next("SINGLE_DISEASE_CASE"));
    }

    // 质控 / 上报

    /**
     * 质控判级：必填校验（疗效必填即入参；首页诊断/天数/费用缺失 → 异常）。
     * 通过 → qcStatus=1；异常 → qcStatus=2 + 异常项明细。死亡病例疗效自动带 4（death_flag=1 时）。
     */
    @Transactional(rollbackFor = Exception.class)
    public SingleDiseaseVO.Case qc(SingleDiseaseDTO.Qc dto) {
        BizSingleDiseaseCase c = bizSingleDiseaseCaseMapper.selectById(dto.getId());
        if (c == null) {
            throw new BusinessException("病例不存在");
        }
        if (c.getReportStatus() == 1) {
            throw new BusinessException("病例已上报，质控不可再改");
        }
        List<String> issues = new ArrayList<>();
        if (c.getMainDiagnosisCode() == null || c.getMainDiagnosisCode().isBlank()) {
            issues.add("主要诊断编码缺失");
        }
        if (c.getInpatientDays() == null || c.getInpatientDays() <= 0) {
            issues.add("住院天数缺失或非法");
        }
        if (c.getTotalAmount() == null || c.getTotalAmount().signum() < 0) {
            issues.add("住院总费用缺失或非法");
        }
        if (c.getDeathFlag() == 1 && dto.getCurativeEffect() != 4) {
            issues.add("首页死亡标志为 1 但疗效判定非死亡");
        }
        c.setCurativeEffect(dto.getCurativeEffect());
        if (issues.isEmpty()) {
            c.setQcStatus(SingleDiseaseQcStatusEnum.PASS.getCode());
            c.setQcIssues(null);
        } else {
            c.setQcStatus(SingleDiseaseQcStatusEnum.ABNORMAL.getCode());
            c.setQcIssues(String.join("；", issues));
        }
        c.setRemark(dto.getRemark());
        c.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizSingleDiseaseCaseMapper.updateById(c);
        return toCaseVO(c, diseaseName(c.getDiseaseId()));
    }

    /**
     * 上报打标（质控通过才可上报）。
     */
    @Transactional(rollbackFor = Exception.class)
    public SingleDiseaseVO.Case report(Long id) {
        BizSingleDiseaseCase c = bizSingleDiseaseCaseMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("病例不存在");
        }
        if (c.getQcStatus() != 1) {
            throw new BusinessException("病例未通过质控（状态 " + c.getQcStatus() + "），不能上报");
        }
        if (c.getReportStatus() == 1) {
            throw new BusinessException("病例已上报，不要重复上报");
        }
        c.setReportStatus(YesOrNoEnum.YES.getCode());
        c.setReportTime(TimeUtil.nowSeconds());
        c.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizSingleDiseaseCaseMapper.updateById(c);
        return toCaseVO(c, diseaseName(c.getDiseaseId()));
    }

    // 查询

    public PageResult<SingleDiseaseVO.Case> casePage(SingleDiseaseDTO.CaseQuery query) {
        LambdaQueryWrapper<BizSingleDiseaseCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getDiseaseId() != null, BizSingleDiseaseCase::getDiseaseId, query.getDiseaseId())
                .eq(query.getQcStatus() != null, BizSingleDiseaseCase::getQcStatus, query.getQcStatus())
                .eq(query.getReportStatus() != null, BizSingleDiseaseCase::getReportStatus, query.getReportStatus())
                .and(query.getKeyword() != null && !query.getKeyword().isBlank(), w -> w
                        .like(BizSingleDiseaseCase::getPatientName, query.getKeyword())
                        .or().like(BizSingleDiseaseCase::getCaseNo, query.getKeyword()))
                .orderByDesc(BizSingleDiseaseCase::getId);
        var page = bizSingleDiseaseCaseMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                query.getPageNum(), query.getPageSize()), wrapper);
        List<SingleDiseaseVO.Case> vos = page.getRecords().stream()
                .map(c -> toCaseVO(c, diseaseName(c.getDiseaseId()))).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    /**
     * 病种指标（服务端复算，不信任前端）。
     */
    public List<SingleDiseaseVO.Metric> metrics() {
        List<SingleDiseaseVO.Disease> diseases = diseaseList();
        List<SingleDiseaseVO.Metric> list = new ArrayList<>();
        for (SingleDiseaseVO.Disease d : diseases) {
            List<BizSingleDiseaseCase> cases = bizSingleDiseaseCaseMapper.selectList(
                    new LambdaQueryWrapper<BizSingleDiseaseCase>()
                            .eq(BizSingleDiseaseCase::getDiseaseId, d.getId()));
            SingleDiseaseVO.Metric m = new SingleDiseaseVO.Metric();
            m.setDiseaseId(d.getId());
            m.setDiseaseCode(d.getDiseaseCode());
            m.setDiseaseName(d.getDiseaseName());
            m.setCaseCount((long) cases.size());
            long cured = cases.stream().filter(c -> c.getCurativeEffect() != null && c.getCurativeEffect() == 1).count();
            long death = cases.stream().filter(c ->
                    (c.getDeathFlag() != null && c.getDeathFlag() == 1)
                            || (c.getCurativeEffect() != null && c.getCurativeEffect() == 4)).count();
            long qcPassed = cases.stream().filter(c -> c.getQcStatus() != null && c.getQcStatus() == 1).count();
            m.setCuredCount(cured);
            m.setCureRate(cases.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(cured)
                    .divide(BigDecimal.valueOf(cases.size()), 4, RoundingMode.HALF_UP));
            m.setDeathCount(death);
            m.setDeathRate(cases.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(death)
                    .divide(BigDecimal.valueOf(cases.size()), 4, RoundingMode.HALF_UP));
            m.setAvgInpatientDays(cases.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(cases.stream()
                            .mapToInt(c -> c.getInpatientDays() == null ? 0 : c.getInpatientDays()).average().orElse(0))
                    .setScale(2, RoundingMode.HALF_UP));
            m.setAvgTotalAmount(cases.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(cases.stream()
                    .mapToDouble(c -> c.getTotalAmount() == null ? 0 : c.getTotalAmount().doubleValue())
                    .average().orElse(0)).setScale(2, RoundingMode.HALF_UP));
            m.setQcPassedCount(qcPassed);
            list.add(m);
        }
        return list;
    }

    // 工具

    private String diseaseName(Long diseaseId) {
        SysSingleDisease d = sysSingleDiseaseMapper.selectById(diseaseId);
        return d == null ? null : d.getDiseaseName();
    }

    private SingleDiseaseVO.Case toCaseVO(BizSingleDiseaseCase c, String diseaseName) {
        SingleDiseaseVO.Case vo = new SingleDiseaseVO.Case();
        vo.setId(c.getId());
        vo.setCaseNo(c.getCaseNo());
        vo.setDiseaseId(c.getDiseaseId());
        vo.setDiseaseName(diseaseName);
        vo.setAdmissionId(c.getAdmissionId());
        vo.setPatientId(c.getPatientId());
        vo.setPatientName(c.getPatientName());
        vo.setMainDiagnosisCode(c.getMainDiagnosisCode());
        vo.setMainDiagnosisName(c.getMainDiagnosisName());
        vo.setInpatientDays(c.getInpatientDays());
        vo.setTotalAmount(c.getTotalAmount());
        vo.setIsSurgery(c.getIsSurgery());
        vo.setDeathFlag(c.getDeathFlag());
        vo.setCurativeEffect(c.getCurativeEffect());
        vo.setEnrollWay(c.getEnrollWay());
        vo.setQcStatus(c.getQcStatus());
        vo.setQcIssues(c.getQcIssues());
        vo.setReportStatus(c.getReportStatus());
        vo.setReportTime(c.getReportTime());
        vo.setCreateTime(c.getCreateTime());
        vo.setRemark(c.getRemark());
        return vo;
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Long l(Object o) {
        return o == null ? null : Long.valueOf(String.valueOf(o));
    }

    private Integer i(Object o) {
        return o == null ? null : Integer.valueOf(String.valueOf(o));
    }

    private BigDecimal bd(Object o) {
        return o == null ? null : new BigDecimal(String.valueOf(o));
    }
}