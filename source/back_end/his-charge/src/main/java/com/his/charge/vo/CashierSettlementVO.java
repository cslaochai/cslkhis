package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收费员交班单出参。
 */
@Data
public class CashierSettlementVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 交班单号
     */
    private String settlementNo;

    /**
     * 收费员工号
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cashierId;

    /**
     * 收费员姓名
     */
    private String cashierName;

    /**
     * 班次（1-白班 2-夜班 3-其他）
     */
    private Integer shiftType;

    /**
     * 统计区间起
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodBegin;

    /**
     * 统计区间止
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    /**
     * 所属院级日结单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long daySettlementId;

    /**
     * 收费笔数
     */
    private Integer chargeCount;

    /**
     * 收费金额
     */
    private BigDecimal chargeAmount;

    /**
     * 退费笔数
     */
    private Integer refundCount;

    /**
     * 退费金额
     */
    private BigDecimal refundAmount;

    /**
     * 净额 = 收费金额 - 退费金额
     */
    private BigDecimal netAmount;

    /**
     * 现金
     */
    private BigDecimal cashAmount;

    /**
     * 微信
     */
    private BigDecimal wechatAmount;

    /**
     * 支付宝
     */
    private BigDecimal alipayAmount;

    /**
     * 医保
     */
    private BigDecimal insuranceAmount;

    /**
     * 余额
     */
    private BigDecimal balanceAmount;

    /**
     * 支付方式为空/未知的金额
     */
    private BigDecimal unknownPayAmount;

    /**
     * 本时段开票张数
     */
    private Integer invoiceCount;

    /**
     * 本时段作废张数
     */
    private Integer invoiceVoidCount;

    /**
     * 实交现金
     */
    private BigDecimal handinCash;

    /**
     * 现金差异 = 实交现金 - 系统现金
     */
    private BigDecimal cashDiff;

    /**
     * 差异说明
     */
    private String diffReason;

    /**
     * 状态（1-已交班待日结 2-已日结 3-已审核）
     */
    private Integer settleStatus;

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
     * 审核意见
     */
    private String auditRemark;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;
}
