package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保合规审核主表
 *
 * <p>每次审核留一条，不覆盖历史 —— 医保申诉要能拿出「当时审过什么、结论是什么」。
 * DRG 相关字段（drg_code / drg_weight / pay_standard / cost_ratio）在
 * `DRG 分组与权重` 为空时**一律留空**，绝不用硬编码权重算一个看起来专业的假倍率：
 * 权重错一位，整个倍率审计就是错的，假数据比没有更危险。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_compliance_audit")
public class BizComplianceAudit extends BaseEntity {

    /**
     * 审核单号
     */
    private String auditNo;

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 就诊锚点：挂号ID（依据聚合键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 审核类型（1-结算前自查 2-批量筛查 3-医保反馈复核）
     */
    private Integer auditType;

    /**
     * 风险等级（0-未发现 1-提示 2-关注 3-高危）
     */
    private Integer riskLevel;

    /**
     * 风险分（按命中规则权重累加）
     */
    private Integer riskScore;

    /**
     * 命中规则数
     */
    private Integer hitCount;

    /**
     * 通过规则数
     */
    private Integer passCount;

    /**
     * 不适用规则数（缺依据，未评估）
     */
    private Integer naCount;

    /**
     * DRG分组编码（需分组器）
     */
    private String drgCode;

    /**
     * DRG权重
     */
    private BigDecimal drgWeight;

    /**
     * 病组支付标准（元）
     */
    private BigDecimal payStandard;

    /**
     * 实际总费用（元）
     */
    private BigDecimal actualCost;

    /**
     * 费用倍率 = 实际 / 支付标准
     */
    private BigDecimal costRatio;

    /**
     * 审核结论
     */
    private String conclusion;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
}
