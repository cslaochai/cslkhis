package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 放射报告模板（放射报告模板，sql/138）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_radio_report_template")
public class BizRadioReportTemplate extends BaseEntity {

    /**
     * 模板编码（唯一）
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 适用模态（字典 his_exam_device_type；NULL=通用）
     */
    private Integer modality;

    /**
     * 适用检查项目编码（NULL=不限项目）
     */
    private String itemCode;

    /**
     * 适用检查项目名称
     */
    private String itemName;

    /**
     * 适用检查部位（NULL=不限部位）
     */
    private String bodyPart;

    /**
     * 检查方法模板
     */
    private String examMethod;

    /**
     * 影像所见模板
     */
    private String findingTpl;

    /**
     * 影像诊断/印象模板
     */
    private String impressionTpl;

    /**
     * 建议模板
     */
    private String suggestionTpl;

    /**
     * 是否公用（1-科室公用 0-个人模板）
     */
    private Integer isPublic;

    /**
     * 归属医生（is_public=0 时必填）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
