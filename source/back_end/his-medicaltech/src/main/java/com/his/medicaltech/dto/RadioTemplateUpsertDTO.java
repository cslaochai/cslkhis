package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 放射报告模板入参（sql/138）。
 */
@Data
public class RadioTemplateUpsertDTO {

    /** 模板ID（新增为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板编码 */
    @NotBlank(message = "缺少模板编码")
    @Size(max = 32, message = "模板编码不能超过 32 字")
    private String templateCode;

    /** 模板名称 */
    @NotBlank(message = "缺少模板名称")
    @Size(max = 100, message = "模板名称不能超过 100 字")
    private String templateName;

    /** 适用模态（字典 his_exam_device_type；空=通用） */
    private Integer modality;

    /** 适用检查项目编码（空=不限项目） */
    private String itemCode;

    /** 适用检查部位（空=不限部位） */
    @Size(max = 100, message = "部位不能超过 100 字")
    private String bodyPart;

    /** 检查方法模板 */
    @Size(max = 200, message = "检查方法不能超过 200 字")
    private String examMethod;

    /** 影像所见模板 */
    private String findingTpl;

    /** 影像诊断/印象模板 */
    private String impressionTpl;

    /** 建议模板 */
    private String suggestionTpl;

    /** 是否公用（1-科室公用 0-个人模板） */
    private Integer isPublic;

    /** 归属医生（isPublic=0 时必填） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
