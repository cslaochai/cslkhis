package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保政策配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_policy")
public class SysInsurancePolicy extends BaseEntity {

    /**
     * 政策名称
     */
    private String policyName;

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 结算方式（2-城镇职工医保 3-城乡居民医保 4-公费医疗）
     */
    private Integer settlementType;

    /**
     * 统筹比例（如85.00表示85%）
     */
    private BigDecimal coverageRatio;

    /**
     * 乙类药品自付比例（如10.00表示10%）
     */
    private BigDecimal selfPayRatio;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
