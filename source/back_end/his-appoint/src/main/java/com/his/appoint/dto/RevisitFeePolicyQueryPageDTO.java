package com.his.appoint.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 复诊收费策略分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RevisitFeePolicyQueryPageDTO extends PageParam {

    /**
     * 策略名称（模糊）
     */
    private String policyName;

    /**
     * 复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊；0-不限）
     */
    private Integer revisitSource;

    /**
     * 收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）
     */
    private Integer chargeMode;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
