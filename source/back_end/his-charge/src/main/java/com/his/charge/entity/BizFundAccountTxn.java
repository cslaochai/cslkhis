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
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 资金账户流水（L3）：账户只回答"这里有多少钱"，账单只回答"该收多少"，
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_fund_account_txn")
public class BizFundAccountTxn extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 账户流水号
     */
    private String txnNo;

    /**
     * 账户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 账户主体（冗余，字典 {@code his_account_owner_type}）
     */
    private Integer ownerType;

    /**
     * 主体ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerId;

    /**
     * 流水类型，字典 {@code his_account_txn_type}
     */
    private Integer txnType;

    /**
     * 变动金额：入账为正、扣用为负，余额 = SUM(amount)
     */
    private BigDecimal amount;

    /**
     * 本笔后余额快照（审计用，不是余额来源）
     */
    private BigDecimal balanceAfter;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 关联账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 关联支付流水ID（余额作为一种支付方式收进账单时的那笔支付资金流水）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long paymentTxnId;

    /**
     * 充值/退款走的渠道
     */
    private Integer payMethod;

    /**
     * 渠道流水号
     */
    private String channelTxnNo;

    /**
     * 操作人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime txnTime;

    /**
     * 状态（1-成功 2-已冲正）
     */
    private Integer txnStatus;

    /**
     * 冲正指向的原流水ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origTxnId;
}
