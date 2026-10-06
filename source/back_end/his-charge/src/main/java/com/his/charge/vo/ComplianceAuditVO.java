package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保合规审核记录出参
 */
@Data
public class ComplianceAuditVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 就诊锚点
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 审核类型：1-结算前自查 2-批量筛查 3-医保反馈复核
     */
    private Integer auditType;

    /**
     * 审核类型中文
     */
    private String auditTypeText;

    /**
     * 风险等级：0-未发现 1-提示 2-关注 3-高危
     */
    private Integer riskLevel;

    /**
     * 风险等级中文
     */
    private String riskLevelText;

    /**
     * 风险分
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
     * DRG分组编码
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
