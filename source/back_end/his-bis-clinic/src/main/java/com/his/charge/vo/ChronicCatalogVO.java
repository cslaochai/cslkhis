package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 慢特病病种目录行。
 */
@Data
public class ChronicCatalogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 病种编码
     */
    private String diseaseCode;

    /**
     * 病种名称
     */
    private String diseaseName;

    /**
     * 类别（字典 his_chronic_disease_type：1-慢性病 2-特殊病）
     */
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
