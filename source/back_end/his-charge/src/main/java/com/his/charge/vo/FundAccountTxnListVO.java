package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金账户流水出参（L3 台账）：金额带符号，入账为正、扣用为负。
 *
 * <p>本笔后余额是审计快照，不是余额来源；账户详情弹框一次拿全，不再分页反查。
 */
@Data
public class FundAccountTxnListVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 流水类型（字典 {@code his_account_txn_type}）
     */
    private Integer txnType;

    /**
     * 变动金额：入账为正、扣用为负
     */
    private BigDecimal amount;

    /**
     * 本笔后余额快照
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
     * 关联支付流水ID
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
     * 操作人姓名（快照）
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

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
