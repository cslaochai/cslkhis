package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 心电报告模板 VO（sql/173）。
 */
@Data
public class EcgTemplateVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 适用心电类型（字典 his_ecg_type；NULL=通用） */
    private Integer ecgType;

    private String ecgTypeText;

    /** 心电图所见模板 */
    private String findingTpl;

    /** 心电图诊断模板 */
    private String conclusionTpl;

    /** 建议模板 */
    private String suggestionTpl;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
