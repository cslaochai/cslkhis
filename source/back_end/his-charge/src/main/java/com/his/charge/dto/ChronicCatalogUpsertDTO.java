package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 慢特病病种目录新增/修改（disease_code 全局唯一，id 空=新增）。
 */
@Data
public class ChronicCatalogUpsertDTO {

    private Long id;

    /**
     * 病种编码（MZ/MT+序号）
     */
    @NotBlank(message = "病种编码不能为空")
    private String diseaseCode;

    /**
     * 病种名称
     */
    @NotBlank(message = "病种名称不能为空")
    private String diseaseName;

    /**
     * 类别（1-慢性病 2-特殊病）
     */
    @NotNull(message = "病种类别不能为空")
    private Integer diseaseType;

    /**
     * 对应 ICD-10 主码
     */
    private String icdCode;

    /**
     * 默认有效期月数（空=长期）
     */
    private Integer defaultValidMonths;

    /**
     * 启用状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
