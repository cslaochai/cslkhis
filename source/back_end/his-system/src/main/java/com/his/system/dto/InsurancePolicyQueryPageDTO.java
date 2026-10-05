package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保政策查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InsurancePolicyQueryPageDTO extends PageParam {

    /**
     * 政策名称，模糊匹配，如：城镇职工在职
     */
    private String policyName;

    /**
     * 医保类型，精确匹配
     */
    private String insuranceType;

    /**
     * 结算方式（2-城镇职工医保 3-城乡居民医保 4-公费医疗）
     */
    private Integer settlementType;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
