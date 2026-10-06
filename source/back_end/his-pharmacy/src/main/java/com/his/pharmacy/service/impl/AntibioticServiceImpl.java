package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.AntibioticAliasUpsertDTO;
import com.his.pharmacy.dto.AntibioticAuthQueryPageDTO;
import com.his.pharmacy.dto.AntibioticAuthUpsertDTO;
import com.his.pharmacy.dto.AntibioticCatalogLevelUpsertDTO;
import com.his.pharmacy.dto.AntibioticCatalogQueryPageDTO;
import com.his.pharmacy.entity.BizAntibioticAlias;
import com.his.pharmacy.entity.BizAntibioticAuth;
import com.his.pharmacy.mapper.AntibioticCatalogMapper;
import com.his.pharmacy.mapper.AntibioticEmployeeMapper;
import com.his.pharmacy.mapper.BizAntibioticAliasMapper;
import com.his.pharmacy.mapper.BizAntibioticAuthMapper;
import com.his.pharmacy.service.AntibioticService;
import com.his.pharmacy.vo.AntibioticAliasVO;
import com.his.pharmacy.vo.AntibioticAuthCheckVO;
import com.his.pharmacy.vo.AntibioticAuthVO;
import com.his.pharmacy.vo.AntibioticCatalogVO;
import com.his.pharmacy.vo.AntibioticDoctorSelectListVO;
import com.his.pharmacy.vo.AntibioticDrugSelectListVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抗菌药物分级目录与处方权授权。
 *
 * <p>口径见 sql/161 头注释与 {@link AntibioticService} 接口注释；这里只写实现层面的要点：
 * ① 分级文案唯一出口是 {@link AntibioticLevelEnum}，前端不自己翻译；
 * ② 开方闸 {@link #assertCanPrescribe} 抛异常让整张处方回滚，而不是返回错误码让调用方忘判；
 * ③ 授权唯一键 (doctor_id, auth_level) 撞了要给人话提示，不能抛出 Duplicat entry 让用户看不懂。
 */
@Service
@RequiredArgsConstructor
public class AntibioticServiceImpl implements AntibioticService {
    @Autowired
    private DictCacheService dictText;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AntibioticCatalogMapper catalogMapper;
    private final AntibioticEmployeeMapper employeeMapper;
    private final BizAntibioticAuthMapper authMapper;
    private final BizAntibioticAliasMapper aliasMapper;

    // 分级目录

    @Override
    public PageResult<AntibioticCatalogVO> catalogListPage(AntibioticCatalogQueryPageDTO query) {
        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        Page<AntibioticCatalogVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<AntibioticCatalogVO> list = catalogMapper.selectCatalogPage(page, keyword, query.getLevelFilter());
        for (AntibioticCatalogVO vo : list) {
            fillLevelText(vo);
        }
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AntibioticCatalogVO catalogLevelUpsert(AntibioticCatalogLevelUpsertDTO dto) {
        Integer level = dto.getAntibioticLevel();
        // B 类：DDD 两项只在纳入目录（level>0）时必填，条件必填不能下沉成 @NotNull
        if (level > 0) {
            if (dto.getDddValue() == null || dto.getDddValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("纳入抗菌药物目录必须填写 WHO DDD 值（g/日）");
            }
            if (dto.getDddUnitGram() == null || dto.getDddUnitGram().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("纳入抗菌药物目录必须填写每发药单位含药量（克）—— 发药单位是盒/瓶/支，不是最小制剂单位");
            }
        } else {
            // 移出目录：DDD 值一并清空，避免"在目录里但算不进指标"的假覆盖
            dto.setDddValue(null);
            dto.setDddUnitGram(null);
        }
        int rows = catalogMapper.updateLevel(dto.getId(), level, dto.getDddValue(), dto.getDddUnitGram());
        if (rows == 0) {
            throw new BusinessException("药品不存在或已删除");
        }
        AntibioticCatalogVO vo = new AntibioticCatalogVO();
        vo.setId(dto.getId());
        vo.setAntibioticLevel(level);
        vo.setDddValue(dto.getDddValue());
        vo.setDddUnitGram(dto.getDddUnitGram());
        fillLevelText(vo);
        return vo;
    }

    // 别名

    @Override
    public List<AntibioticAliasVO> aliasList(Long drugId) {
        LambdaQueryWrapper<BizAntibioticAlias> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(drugId != null, BizAntibioticAlias::getDrugId, drugId)
                .orderByAsc(BizAntibioticAlias::getId);
        return aliasMapper.selectList(wrapper).stream().map(this::toAliasVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AntibioticAliasVO aliasUpsert(AntibioticAliasUpsertDTO dto) {
        String alias = dto.getAliasName() == null ? null : dto.getAliasName().trim();
        AntibioticDrugSelectListVO drug = catalogMapper.selectAntibioticDrugs().stream()
                .filter(d -> d.getId().equals(dto.getDrugId()))
                .findFirst().orElse(null);
        if (drug == null) {
            throw new BusinessException("该药品不在抗菌药物分级目录内（请先把药品纳入目录）");
        }
        BizAntibioticAlias entity;
        if (dto.getId() != null) {
            entity = aliasMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("别名记录不存在");
            }
        } else {
            entity = new BizAntibioticAlias();
            entity.setCreateBy(UserUtils.getCurrentEmployeeName());
        }
        entity.setDrugId(dto.getDrugId());
        entity.setDrugName(drug.getDrugName());
        entity.setAliasName(alias);
        entity.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        if (entity.getId() == null) {
            aliasMapper.insert(entity);
        } else {
            aliasMapper.updateById(entity);
        }
        return toAliasVO(entity);
    }

    @Override
    public void aliasDeleteById(Long id) {
        BizAntibioticAlias entity = aliasMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("别名记录不存在");
        }
        aliasMapper.deleteById(id);
    }

    @Override
    public List<AntibioticDrugSelectListVO> antibioticDrugSelectList() {
        List<AntibioticDrugSelectListVO> list = catalogMapper.selectAntibioticDrugs();
        for (AntibioticDrugSelectListVO vo : list) {
            vo.setAntibioticLevelText(AntibioticLevelEnum.getText(vo.getAntibioticLevel()));
        }
        return list;
    }

    @Override
    public List<AntibioticDoctorSelectListVO> doctorSelectList(String keyword) {
        String kw = keyword == null ? null : keyword.trim();
        return employeeMapper.selectDoctors(kw);
    }

    // 处方权授权

    @Override
    public PageResult<AntibioticAuthVO> authListPage(AntibioticAuthQueryPageDTO query) {
        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        LambdaQueryWrapper<BizAntibioticAuth> wrapper = new LambdaQueryWrapper<>();
        // ⚠ like(cond, col, v) 是普通方法调用：实参先 trim() 存局部变量，否则未传参时 NPE（AGENTS §4 坑）
        wrapper.and(StringUtils.hasText(keyword), w -> w
                        .like(BizAntibioticAuth::getDoctorName, keyword)
                        .or().like(BizAntibioticAuth::getDeptName, keyword))
                .eq(query.getAuthLevel() != null, BizAntibioticAuth::getAuthLevel, query.getAuthLevel())
                .eq(query.getStatus() != null, BizAntibioticAuth::getStatus, query.getStatus())
                .orderByDesc(BizAntibioticAuth::getId);
        if (Boolean.TRUE.equals(query.getOnlyEffective())) {
            wrapper.eq(BizAntibioticAuth::getStatus, BizAntibioticAuth.STATUS_VALID)
                    .ge(BizAntibioticAuth::getExpireDate, LocalDate.now());
        }
        Page<BizAntibioticAuth> page = authMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        List<AntibioticAuthVO> vos = page.getRecords().stream().map(this::toAuthVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AntibioticAuthVO authUpsert(AntibioticAuthUpsertDTO dto) {
        if (dto.getExpireDate().isBefore(dto.getAuthDate())) {
            throw new BusinessException("有效期至不能早于授权日期");
        }
        Integer status = dto.getStatus() == null ? BizAntibioticAuth.STATUS_VALID : dto.getStatus();
        // B 类：只有暂停/取消（status=2/3）才必填原因，条件必填下沉成 @NotBlank 会挡死正常新授权
        if (status != BizAntibioticAuth.STATUS_VALID && !StringUtils.hasText(dto.getRevokeReason())) {
            throw new BusinessException("暂停或取消授权必须填写原因");
        }
        AntibioticDoctorSelectListVO doctor = employeeMapper.selectDoctorById(dto.getDoctorId());
        if (doctor == null) {
            throw new BusinessException("医师不存在（请从医师下拉里选择在职医师）");
        }

        BizAntibioticAuth auth;
        if (dto.getId() != null) {
            auth = authMapper.selectById(dto.getId());
            if (auth == null) {
                throw new BusinessException("授权记录不存在");
            }
            // 换医师/换级别不允许：那是一条新的授权证据，覆盖掉就查不到中间那次取消
            if (!auth.getDoctorId().equals(dto.getDoctorId()) || !auth.getAuthLevel().equals(dto.getAuthLevel())) {
                throw new BusinessException("不允许改换医师或授权级别：请另立一条授权记录");
            }
        } else {
            BizAntibioticAuth exist = authMapper.selectOne(new LambdaQueryWrapper<BizAntibioticAuth>()
                    .eq(BizAntibioticAuth::getDoctorId, dto.getDoctorId())
                    .eq(BizAntibioticAuth::getAuthLevel, dto.getAuthLevel()));
            if (exist != null) {
                throw new BusinessException("该医师已有" + AntibioticLevelEnum.labelOrUnknown(dto.getAuthLevel())
                        + "的授权记录（" + exist.getAuthNo() + "），请直接修改那条而不是重复新增");
            }
            auth = new BizAntibioticAuth();
            auth.setAuthNo(nextAuthNo());
            auth.setDoctorId(dto.getDoctorId());
            auth.setAuthLevel(dto.getAuthLevel());
            auth.setCreateBy(UserUtils.getCurrentEmployeeName());
        }
        auth.setDoctorName(doctor.getDoctorName());
        auth.setDeptId(doctor.getDeptId());
        auth.setDeptName(doctor.getDeptName());
        auth.setTitle(doctor.getTitle());
        auth.setAuthBasis(StringUtils.hasText(dto.getAuthBasis()) ? dto.getAuthBasis().trim() : null);
        auth.setAuthDate(dto.getAuthDate());
        auth.setExpireDate(dto.getExpireDate());
        auth.setStatus(status);
        auth.setAuthorizer(StringUtils.hasText(dto.getAuthorizer()) ? dto.getAuthorizer().trim() : null);
        auth.setAuthorizeOrg(StringUtils.hasText(dto.getAuthorizeOrg()) ? dto.getAuthorizeOrg().trim() : null);
        auth.setRevokeReason(StringUtils.hasText(dto.getRevokeReason()) ? dto.getRevokeReason().trim() : null);
        auth.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);

        if (auth.getId() == null) {
            authMapper.insert(auth);
        } else {
            authMapper.updateById(auth);
        }
        return toAuthVO(auth);
    }

    // 开方闸

    @Override
    public AntibioticAuthCheckVO checkAuthority(Long doctorId, List<Long> drugIds) {
        AntibioticAuthCheckVO vo = new AntibioticAuthCheckVO();
        vo.setBlockedDrugs(new ArrayList<>());
        if (doctorId == null || CollectionUtils.isEmpty(drugIds)) {
            vo.setAllowed(true);
            vo.setTip(null);
            return vo;
        }
        Integer authLevel = maxValidLevel(doctorId);
        vo.setAuthLevel(authLevel);
        vo.setAuthLevelText(authLevel == null ? "无有效授权" : dictText.getDicDataLabel("biz_pharmacy_antibioticLevelEnum", authLevel));

        List<AntibioticDrugSelectListVO> drugs = catalogMapper.selectAntibioticByIds(drugIds);
        for (AntibioticDrugSelectListVO drug : drugs) {
            if (authLevel == null || authLevel < drug.getAntibioticLevel()) {
                AntibioticAuthCheckVO.BlockedDrug b = new AntibioticAuthCheckVO.BlockedDrug();
                b.setDrugName(drug.getDrugName());
                b.setAntibioticLevel(drug.getAntibioticLevel());
                b.setAntibioticLevelText(AntibioticLevelEnum.getText(drug.getAntibioticLevel()));
                b.setRequiredLevel(drug.getAntibioticLevel());
                b.setRequiredLevelText(AntibioticLevelEnum.getText(drug.getAntibioticLevel()));
                vo.getBlockedDrugs().add(b);
            }
        }
        boolean allowed = vo.getBlockedDrugs().isEmpty();
        vo.setAllowed(allowed);
        if (allowed) {
            vo.setTip(null);
        } else {
            String names = vo.getBlockedDrugs().stream()
                    .map(b -> b.getDrugName() + "（" + b.getAntibioticLevelText() + "）")
                    .distinct()
                    .reduce((a, b) -> a + "、" + b).orElse("");
            vo.setTip(authLevel == null
                    ? "您没有有效的抗菌药物处方权授权，不能开具：" + names
                    : "您当前的抗菌药物处方权为" + dictText.getDicDataLabel("biz_pharmacy_antibioticLevelEnum", authLevel) + "，不能开具：" + names
                      + "。请改用同级可开品种，或由具有相应处方权的医师开具。");
        }
        return vo;
    }

    @Override
    public void assertCanPrescribe(Long doctorId, List<Long> drugIds) {
        if (doctorId == null || CollectionUtils.isEmpty(drugIds)) {
            return;
        }
        AntibioticAuthCheckVO check = checkAuthority(doctorId, drugIds);
        if (!Boolean.TRUE.equals(check.getAllowed())) {
            throw new BusinessException(check.getTip());
        }
    }

    @Override
    public Integer maxValidLevel(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        return authMapper.selectMaxValidLevel(doctorId, LocalDate.now());
    }

    // 内部

    private String nextAuthNo() {
        String day = LocalDate.now().format(DAY_FMT);
        String max = authMapper.selectMaxAuthNo(day);
        int seq = 1;
        if (StringUtils.hasText(max) && max.length() >= 4) {
            try {
                seq = Integer.parseInt(max.substring(max.length() - 4)) + 1;
            } catch (NumberFormatException ignored) {
                seq = 1;
            }
        }
        return "KJ" + day + String.format("%04d", seq);
    }

    private void fillLevelText(AntibioticCatalogVO vo) {
        vo.setAntibioticLevelText(AntibioticLevelEnum.getText(vo.getAntibioticLevel()));
        vo.setInCatalog(vo.getAntibioticLevel() != null && vo.getAntibioticLevel() > 0);
    }

    private AntibioticAuthVO toAuthVO(BizAntibioticAuth e) {
        AntibioticAuthVO vo = new AntibioticAuthVO();
        vo.setId(e.getId());
        vo.setAuthNo(e.getAuthNo());
        vo.setDoctorId(e.getDoctorId());
        vo.setDoctorName(e.getDoctorName());
        vo.setDeptId(e.getDeptId());
        vo.setDeptName(e.getDeptName());
        vo.setTitle(e.getTitle());
        vo.setAuthLevel(e.getAuthLevel());
        vo.setAuthLevelText(AntibioticLevelEnum.getText(e.getAuthLevel()));
        vo.setAuthBasis(e.getAuthBasis());
        vo.setAuthDate(e.getAuthDate());
        vo.setExpireDate(e.getExpireDate());
        vo.setStatus(e.getStatus());
        vo.setStatusText(AntibioticAuthStatusEnum.getText(e.getStatus()));
        vo.setEffective(e.getStatus() != null
                && e.getStatus() == BizAntibioticAuth.STATUS_VALID
                && e.getExpireDate() != null
                && !e.getExpireDate().isBefore(LocalDate.now()));
        vo.setAuthorizer(e.getAuthorizer());
        vo.setAuthorizeOrg(e.getAuthorizeOrg());
        vo.setRevokeReason(e.getRevokeReason());
        vo.setCreateBy(e.getCreateBy());
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());
        vo.setRemark(e.getRemark());
        return vo;
    }

    private AntibioticAliasVO toAliasVO(BizAntibioticAlias e) {
        AntibioticAliasVO vo = new AntibioticAliasVO();
        vo.setId(e.getId());
        vo.setDrugId(e.getDrugId());
        vo.setDrugCode(e.getDrugCode());
        vo.setDrugName(e.getDrugName());
        vo.setAliasName(e.getAliasName());
        vo.setCreateBy(e.getCreateBy());
        vo.setCreateTime(e.getCreateTime());
        vo.setRemark(e.getRemark());
        return vo;
    }
}