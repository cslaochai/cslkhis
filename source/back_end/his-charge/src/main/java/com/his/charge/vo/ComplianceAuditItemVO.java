package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 医保合规审核命中明细出参
 */
@Data
public class ComplianceAuditItemVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 审核ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditId;

    /**
     * 规则编码，如 A01
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则分组：A/B/C/D
     */
    private String ruleGroup;

    /**
     * 规则分组中文
     */
    private String ruleGroupText;

    /**
     * 结果：1-命中 2-通过 3-不适用（缺依据，未评估）
     */
    private Integer result;

    /**
     * 结果中文（三态：命中/通过/不适用）
     *
     * <p>前端必须渲染这个字段而不是自己按 result 拼中文 ——
     * 否则很容易把 3 写成「通过」，而这三态的差别正是审核报告可信度的全部。</p>
     */
    private String resultText;

    /**
     * 风险等级：1-提示 2-关注 3-高危
     */
    private Integer riskLevel;

    /**
     * 风险等级中文
     */
    private String riskLevelText;

    /**
     * 对象：0-清单级 1-诊断 2-手术操作
     */
    private Integer targetType;

    /**
     * 对象ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    /**
     * 对象编码
     */
    private String targetCode;

    /**
     * 对象名称
     */
    private String targetName;

    /**
     * 判定依据（含「不适用」的原因）
     */
    private String evidence;

    /**
     * 整改建议
     */
    private String suggestion;

    /**
     * 备注
     */
    private String remark;
}
