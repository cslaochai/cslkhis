package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账单行（L2 快照）：账单一旦出，项目名/价格/医保拆分不随字典与价格变更而漂移。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_settlement_bill_item")
public class BizSettlementBillItem extends BaseEntity {

    /**
     * 账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单号（快照）
     */
    private String billNo;

    /**
     * 来源记账行ID：唯一键 uk_bill_fee 保证一条费用只进一张账单
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊类型
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 费用归属科室（快照）：科室收入按行级算，跨科单不能整笔算到单头某一科
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 项目类型
     */
    private Integer itemType;

    /**
     * 项目编码（快照）
     */
    private String itemCode;

    /**
     * 项目名称（快照）
     */
    private String itemName;

    /**
     * 规格（快照）
     */
    private String specification;

    /**
     * 单位（快照）
     */
    private String unit;

    /**
     * 单价（快照）
     */
    private BigDecimal price;

    /**
     * 数量（快照）
     */
    private BigDecimal quantity;

    /**
     * 应收金额
     */
    private BigDecimal amount;

    /**
     * 行级分摊优惠
     */
    private BigDecimal discountAmount;

    /**
     * 行级医保统筹：2304 报盘要逐项目费率，split 必须落到行
     */
    private BigDecimal poolAmount;

    /**
     * 行级医保个账
     */
    private BigDecimal accountAmount;

    /**
     * 行级个人自付
     */
    private BigDecimal selfAmount;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类），抄自记账行，决定本行 split
     */
    private Integer catalogType;
}
