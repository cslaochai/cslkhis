package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.InsurancePolicyQueryPageDTO;
import com.his.system.dto.InsurancePolicyUpsertDTO;
import com.his.system.entity.SysInsurancePolicy;
import com.his.system.vo.InsurancePolicyVO;

import java.math.BigDecimal;

/**
 * 医保政策服务接口
 */
public interface InsurancePolicyService extends IService<SysInsurancePolicy> {

    /**
     * 医保政策分页查询
     */
    PageResult<InsurancePolicyVO> queryPolicyPage(InsurancePolicyQueryPageDTO queryDTO);

    /**
     * 医保政策详情，不存在时抛业务异常
     */
    InsurancePolicyVO getPolicyInfo(Long id);

    /**
     * 新增或修改医保政策，返回结果文案
     */
    String upsertPolicy(InsurancePolicyUpsertDTO upsertDTO);

    /**
     * 删除医保政策（物理删除），不存在时抛业务异常
     */
    void removePolicy(Long id);

    /**
     * 根据结算方式和医保类型获取统筹比例
     */
    BigDecimal getCoverageRatio(Integer settlementType, String medicalInsuranceType);

    /**
     * 根据结算方式和医保类型获取乙类自付比例
     */
    BigDecimal getSelfPayRatio(Integer settlementType, String medicalInsuranceType);
}
