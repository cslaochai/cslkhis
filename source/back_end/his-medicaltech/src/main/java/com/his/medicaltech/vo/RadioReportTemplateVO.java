package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 放射报告模板出参（sql/138）。
 */
@Data
public class RadioReportTemplateVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 适用模态 */
    private Integer modality;

    /** 模态文案（字典 his_exam_device_type；modality 为空时这里也为空 = 通用模板） */
    private String modalityText;

    /** 适用检查项目编码 */
    private String itemCode;

    /** 适用检查部位 */
    private String bodyPart;

    /** 检查方法模板 */
    private String examMethod;

    /** 影像所见模板 */
    private String findingTpl;

    /** 影像诊断/印象模板 */
    private String impressionTpl;

    /** 建议模板 */
    private String suggestionTpl;

    /** 是否公用（1-科室公用 0-个人模板） */
    private Integer isPublic;

    /** 归属医生 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
