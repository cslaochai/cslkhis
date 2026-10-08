package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 心电报告模板（心电报告模板，sql/173）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ecg_template")
public class BizEcgTemplate extends BaseEntity {

    /**
     * 模板编码（唯一）
     */
    private String templateCode;

    /**
     * 模板名称
     */
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
