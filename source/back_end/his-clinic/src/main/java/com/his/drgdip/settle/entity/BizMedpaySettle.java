package com.his.drgdip.settle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 按病种付费（DRG/DIP）结算结果实体（P4 结算层）。
 * <p>一张出院结算账单一条，按 (settle_bill_id, pay_mode, version) 幂等覆盖。
 * DRG：应付 = 权重 × 费率；DIP：应付 = 分值（基准 × 系数）× 点值；参数未下发放 NULL，不编造。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_medpay_settle")
public class BizMedpaySettle extends BaseEntity {

    /**
     * 结算账单ID（biz_settlement_bill.id，bill_type=4 出院结算）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settleBillId;

    /**
     * 账单号（快照）
     */
    private String billNo;

    /**
     * 病案首页ID（biz_inpatient_summary.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long summaryId;

    /**
     * 住院ID（biz_inpatient_summary.admission_id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 付费方式（DRG / DIP，按 medpay.mode 路由时快照）
     */
    private String payMode;

    /**
     * 分组方案版本（DRG=chs-drg-2.0/3.0；DIP=2.0/3.0）
     */
    private String version;

    /**
     * 组码/病种码（DRG=drg_code；DIP=disease_code；未入组为空）
     */
    private String groupCode;

    /**
     * 组名/病种名
     */
    private String groupName;

    /**
     * DIP 病种类型（1-核心病种 2-综合病种；DRG 为 NULL）
     */
    private Integer groupType;

    /**
     * DRG 权重（官方包未含时为 NULL）
     */
    private BigDecimal weight;

    /**
     * DRG 费率（元/权重，统筹区公布，快照）
     */
    private BigDecimal rate;

    /**
     * DIP 基准分值
     */
    private BigDecimal baseScore;

    /**
     * DIP 病例组合系数（辅助目录命中系数，未命中 1.0000）
     */
    private BigDecimal coeff;

    /**
     * DIP 结算分值 = 基准分值 × 系数
     */
    private BigDecimal score;

    /**
     * DIP 点值（元/分值，快照）
     */
    private BigDecimal pointValue;

    /**
     * 医保应付（DRG=权重×费率；DIP=分值×点值；参数未下发为 NULL）
     */
    private BigDecimal insuranceAmount;

    /**
     * 实际住院费用（结算账单 total_amount）
     */
    private BigDecimal actualAmount;

    /**
     * 盈亏 = 医保应付 - 实际费用（任一侧为 NULL 则为 NULL）
     */
    private BigDecimal profitAmount;

    /**
     * 结算状态（1-已入组计算 2-未入组）
     */
    private Integer calcStatus;

    /**
     * 命中路径说明
     */
    private String ruleNote;

    /**
     * 结算计算时间
     */
    private LocalDateTime settleTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
