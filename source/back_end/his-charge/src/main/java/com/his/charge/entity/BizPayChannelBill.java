package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付渠道对账流水台账（M7 渠道侧"对方账"）。
 *
 * <p>幂等键 (channel, channel_trade_no)：同一渠道同一笔流水只落一次，
 * 重复导入由唯一键兜底跳过。勾对是人工动作：渠道流水 → 本地<b>支付流水号</b>
 * （local_txn_no ↔ 支付资金流水的流水编号，唯一键 ux_bill_txn 挡住一笔流水被两条台账勾走），
 * 金额不符由人登记长款/短款，系统不自动定性质。
 *
 * <p>为什么勾流水而不是勾收费单（AGENTS §7）：渠道退了一笔钱，本地事实是「一笔负数流水」，
 * 与收费单不是一对一；且收费四层下 billing 单已退役，勾单号的口径没有右值可查。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pay_channel_bill")
public class BizPayChannelBill extends BaseEntity {

    /**
     * 支付渠道：2-微信 3-支付宝 6-银行卡（对齐支付资金流水的支付方式；4 是医保个人账户，不是渠道）
     */
    private Integer channel;

    /**
     * 账单日期（对账日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    /**
     * 渠道流水号（渠道侧唯一）
     */
    private String channelTradeNo;

    /**
     * 渠道交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeTime;

    /**
     * 渠道侧金额：收款为正、退款为负（与支付资金流水的金额同符号口径，勾对时直接比）
     */
    private BigDecimal amount;

    /**
     * 来源：1-渠道拉取 2-手工登记
     */
    private Integer importWay;

    /**
     * 勾对的本地支付流水号（支付资金流水的流水编号）
     */
    private String localTxnNo;

    /**
     * 勾对的本地支付流水ID：与 localTxnNo 互指，唯一键 ux_bill_txn 保证一笔流水只被一条台账勾走
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long localTxnId;

    /**
     * 勾对流水方向快照：1-收款 2-退款（渠道侧退了一笔钱对应的是负数流水，不是一张收费单）
     */
    private Integer txnDirection;

    /**
     * 勾对状态：0-待勾对 1-已勾对 2-长款（渠道有本地无） 3-短款（金额不符待核）
     */
    private Integer matchStatus;

    /**
     * 勾对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime matchTime;

    /**
     * 勾对人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long matchedById;

    /**
     * 勾对人姓名（快照）
     */
    private String matchedByName;

    /**
     * 勾对差额（渠道-本地）
     */
    private BigDecimal diffAmount;

    /**
     * 长款/短款处理说明（定性依据）
     */
    private String handleRemark;

    /**
     * 导入批次号
     */
    private String importBatchNo;
}
