package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保政策新增/修改入参
 */
@Data
public class InsurancePolicyUpsertDTO {

    /**
     * 政策ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 政策名称，如：城镇职工在职人员
     */
    @NotBlank(message = "政策名称不能为空")
    private String policyName;

    /**
     * 医保类型，如：城镇职工、城乡居民；自费无政策
     */
    @NotBlank(message = "医保类型不能为空")
    private String insuranceType;

    /** 结算方式（2-城镇职工医保 3-城乡居民医保 4-公费医疗） */
    @NotNull(message = "结算方式不能为空")
    private Integer settlementType;

    /**
     * 统筹比例（如85.00表示85%）
     */
    @NotNull(message = "统筹比例不能为空")
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
