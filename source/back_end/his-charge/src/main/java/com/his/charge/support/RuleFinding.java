package com.his.charge.support;


import com.his.charge.enums.AuditResultStateEnum;
import com.his.charge.enums.RuleCatalogEnum;
import lombok.Data;

/**
 * 一条规则在一个对象上的判定结论。
 *
 * <p>每条规则至少产出一条 finding —— 判定不了就产一条 {@link AuditResultStateEnum#NOT_APPLICABLE}，
 * 并带上原因。这样「规则没评估」永远在明细里可见，不会被统计口径吃掉。</p>
 */
@Data
public class RuleFinding {

    private RuleCatalogEnum rule;

    /**
     * 三态结果
     */
    private AuditResultStateEnum result;

    /**
     * 对象类型：0-清单 1-诊断 2-手术操作
     */
    private Integer targetType;

    private Long targetId;

    private String targetCode;

    private String targetName;

    /**
     * 判定依据：写明依据来自哪里、原文是什么，或为什么判不了
     */
    private String evidence;

    /**
     * 整改建议，空则用规则目录里的默认建议
     */
    private String suggestion;

    public static RuleFinding of(RuleCatalogEnum rule, AuditResultStateEnum result) {
        RuleFinding f = new RuleFinding();
        f.rule = rule;
        f.result = result;
        f.targetType = rule.getTargetType();
        return f;
    }

    public static RuleFinding hit(RuleCatalogEnum rule, String evidence) {
        RuleFinding f = of(rule, AuditResultStateEnum.HIT);
        f.evidence = evidence;
        return f;
    }

    public static RuleFinding pass(RuleCatalogEnum rule, String evidence) {
        RuleFinding f = of(rule, AuditResultStateEnum.PASS);
        f.evidence = evidence;
        return f;
    }

    /**
     * 不适用必须带原因，所以这里强制传 reason
     */
    public static RuleFinding na(RuleCatalogEnum rule, String reason) {
        RuleFinding f = of(rule, AuditResultStateEnum.NOT_APPLICABLE);
        f.evidence = reason;
        return f;
    }

    /**
     * 设置判定对象
     */
    public RuleFinding on(Integer targetType, Long targetId, String targetCode, String targetName) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetCode = targetCode;
        this.targetName = targetName;
        return this;
    }

    public RuleFinding withSuggestion(String suggestion) {
        this.suggestion = suggestion;
        return this;
    }
}
