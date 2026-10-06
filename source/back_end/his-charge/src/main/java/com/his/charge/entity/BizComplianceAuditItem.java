package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保合规审核命中明细（逐条留痕）
 *
 * <p>result 刻意做成**三态**（1-命中 / 2-通过 / 3-不适用），理由与检验的
 * 「未判定 ≠ 正常」完全相同：把「没评估」渲染成「通过」，会让审核报告整体偏乐观，
 * 而这正是医保飞检抓的地方。凡 result=3，evidence 或 remark 必须写明缺什么依据。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_compliance_audit_item")
public class BizComplianceAuditItem extends BaseEntity {

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
     * 规则分组：A-编码依据一致性 B-逻辑排他 C-住院指征 D-分组倍率
     */
    private String ruleGroup;

    /**
     * 结果：1-命中 2-通过 3-不适用（缺依据，未评估）
     */
    private Integer result;

    /**
     * 风险等级（1-提示 2-关注 3-高危）
     */
    private Integer riskLevel;

    /**
     * 对象（0-清单级 1-诊断 2-手术操作）
     */
    private Integer targetType;

    /**
     * 对象ID（诊断/手术明细ID）
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
     * 判定依据（引用到的原文/数据）
     */
    private String evidence;

    /**
     * 整改建议
     */
    private String suggestion;
}
