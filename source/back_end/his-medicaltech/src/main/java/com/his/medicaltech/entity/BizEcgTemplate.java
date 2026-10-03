package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 心电报告模板（心电报告模板，sql/173）。
 *
 * <p>与放射模板（sql/138）同构：报告三段（所见/诊断/建议）各一个模板段，
 * 按心电类型分流（常规 vs Holter）。{@code ecgType} 允许为 NULL = 通用模板。
 *
 * <p>template_code 唯一键不含 del_flag → 删除走物理删（见 BizEcgTemplateMapper.purgeById）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ecg_template")
public class BizEcgTemplate extends BaseEntity {

    /** 模板编码（唯一） */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 适用心电类型（字典 his_ecg_type；NULL=通用） */
    private Integer ecgType;

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
