package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.dto.InsurancePolicyQueryPageDTO;
import com.his.system.dto.InsurancePolicyUpsertDTO;
import com.his.system.entity.SysInsurancePolicy;
import com.his.system.mapper.SysInsurancePolicyMapper;
import com.his.system.service.InsurancePolicyService;
import com.his.system.vo.InsurancePolicyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 医保政策服务实现
 *
 * <p>本表没有逻辑删除标记，删除为物理删除。</p>
 */
@Slf4j
@Service
public class InsurancePolicyServiceImpl extends ServiceImpl<SysInsurancePolicyMapper, SysInsurancePolicy>
        implements InsurancePolicyService {

    @Override
    public PageResult<InsurancePolicyVO> queryPolicyPage(InsurancePolicyQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysInsurancePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getPolicyName()),
                        SysInsurancePolicy::getPolicyName, queryDTO.getPolicyName())
                .eq(StringUtils.hasText(queryDTO.getInsuranceType()),
                        SysInsurancePolicy::getInsuranceType, queryDTO.getInsuranceType())
                .eq(Objects.nonNull(queryDTO.getSettlementType()),
                        SysInsurancePolicy::getSettlementType, queryDTO.getSettlementType())
                .eq(Objects.nonNull(queryDTO.getStatus()),
                        SysInsurancePolicy::getStatus, queryDTO.getStatus())
                // 顺序不能依赖默认：先按结算方式，再按统筹比例降序，同档位再按 id 保证分页稳定
                .orderByAsc(SysInsurancePolicy::getSettlementType)
                .orderByDesc(SysInsurancePolicy::getCoverageRatio)
                .orderByAsc(SysInsurancePolicy::getId);

        IPage<SysInsurancePolicy> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        this.page(page, wrapper);
        List<InsurancePolicyVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public InsurancePolicyVO getPolicyInfo(Long id) {
        SysInsurancePolicy policy = this.getById(id);
        if (Objects.isNull(policy)) {
            throw new BusinessException("医保政策不存在");
        }
        return toVO(policy);
    }

    @Override
    public String upsertPolicy(InsurancePolicyUpsertDTO upsertDTO) {
        SysInsurancePolicy entity = new SysInsurancePolicy();
        BeanUtils.copyProperties(upsertDTO, entity);
        if (Objects.isNull(upsertDTO.getId())) {
            // 新增：医保类型 + 结算方式不允许重复配置
            if (existsDuplicate(upsertDTO.getSettlementType(), upsertDTO.getInsuranceType(), null)) {
                throw new BusinessException("该医保类型 + 结算方式已存在政策配置，请勿重复新增");
            }
            if (Objects.isNull(entity.getStatus())) {
                entity.setStatus(1);
            }
            this.save(entity);
            return "新增成功";
        }
        if (existsDuplicate(upsertDTO.getSettlementType(), upsertDTO.getInsuranceType(), upsertDTO.getId())) {
            throw new BusinessException("该医保类型 + 结算方式已存在政策配置，请勿重复");
        }
        this.updateById(entity);
        return "修改成功";
    }

    @Override
    public void removePolicy(Long id) {
        SysInsurancePolicy policy = this.getById(id);
        if (Objects.isNull(policy)) {
            throw new BusinessException("医保政策不存在");
        }
        this.removeById(id);
    }

    /**
     * 判断「结算方式 + 医保类型」是否已被别的政策占用
     */
    private boolean existsDuplicate(Integer settlementType, String insuranceType, Long excludeId) {
        LambdaQueryWrapper<SysInsurancePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInsurancePolicy::getSettlementType, settlementType)
                .eq(SysInsurancePolicy::getInsuranceType, insuranceType)
                .ne(Objects.nonNull(excludeId), SysInsurancePolicy::getId, excludeId);
        return this.count(wrapper) > 0;
    }

    private InsurancePolicyVO toVO(SysInsurancePolicy entity) {
        InsurancePolicyVO vo = new InsurancePolicyVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public BigDecimal getCoverageRatio(Integer settlementType, String medicalInsuranceType) {
        SysInsurancePolicy policy = findPolicy(settlementType, medicalInsuranceType);
        if (policy != null) {
            return policy.getCoverageRatio();
        }
        // 没有配置的，默认自费（不报销）
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getSelfPayRatio(Integer settlementType, String medicalInsuranceType) {
        SysInsurancePolicy policy = findPolicy(settlementType, medicalInsuranceType);
        if (policy != null) {
            return policy.getSelfPayRatio();
        }
        // 没有配置的，默认自费（100%自付）
        return new BigDecimal("100");
    }

    private SysInsurancePolicy findPolicy(Integer settlementType, String medicalInsuranceType) {
        if (settlementType == null || settlementType == 1) {
            return null; // 自费无政策
        }
        LambdaQueryWrapper<SysInsurancePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInsurancePolicy::getSettlementType, settlementType)
                .eq(SysInsurancePolicy::getStatus, 1);
        if (medicalInsuranceType != null && !medicalInsuranceType.isEmpty()) {
            wrapper.eq(SysInsurancePolicy::getInsuranceType, medicalInsuranceType);
        }
        wrapper.orderByAsc(SysInsurancePolicy::getId);
        wrapper.last("LIMIT 1");
        SysInsurancePolicy policy = this.getOne(wrapper);
        // 如果没有找到精确匹配的政策，尝试只按settlementType查找（医保类型命名不统一时兜底）
        if (policy == null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysInsurancePolicy::getSettlementType, settlementType)
                    .eq(SysInsurancePolicy::getStatus, 1)
                    .orderByAsc(SysInsurancePolicy::getId)
                    .last("LIMIT 1");
            policy = this.getOne(wrapper);
        }
        return policy;
    }
}
