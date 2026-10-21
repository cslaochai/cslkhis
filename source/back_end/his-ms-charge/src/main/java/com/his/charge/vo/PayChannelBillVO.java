package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付渠道对账台账出参。
 */
@Data
public class PayChannelBillVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 支付渠道：2-微信 3-支付宝 6-银行卡
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
     * 渠道侧金额：收款为正、退款为负
     */
    private BigDecimal amount;

    /**
     * 来源：1-渠道拉取 2-手工登记
     */
    private Integer importWay;

    /**
     * 勾对的本地支付流水号
     */
    private String localTxnNo;

    /**
     * 勾对的本地支付流水ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long localTxnId;

    /**
     * 勾对流水方向快照：1-收款 2-退款
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
     * 勾对人姓名
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

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
