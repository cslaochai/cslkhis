package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保政策配置
 */
@Data
@TableName("sys_insurance_policy")
public class SysInsurancePolicy {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 备注
     */
    private String remark;
}
