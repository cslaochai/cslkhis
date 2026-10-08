package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 心电报告模板新增/修改入参（sql/173）。
 */
@Data
public class EcgTemplateUpsertDTO {

    /**
     * 模板ID（修改时传；新增为空）
     */
    private Long id;

    /**
     * 模板编码
     */
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 32, message = "模板编码不能超过 32 字")
    private String templateCode;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过 100 字")
    private String templateName;

    /**
     * 适用心电类型（字典 his_ecg_type；NULL=通用）
     */
    private Integer ecgType;

    /**
     * 心电图所见模板
     */
    private String findingTpl;

    /**
     * 心电图诊断模板
     */
    private String conclusionTpl;

    /**
     * 建议模板
     */
    private String suggestionTpl;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
