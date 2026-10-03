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
 * 医保报盘报文台账实体：每一次发给医保的数据（上传/撤销）都在这里留痕。
 * payload 即"发送给医保的数据到底长啥样"的全文；对账以本表 + bill_date 为口径。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_report")
public class BizInsuranceReport extends BaseEntity {

    /**
     * 医保结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 结算清单号（冗余）
     */
    private String settlementNo;

    /**
     * 报文类型（1-上传 2-撤销）
     */
    private Integer reportType;

    /**
     * 医保接口编号（2304-结算上传 2305-撤销；占位，以前置机规范为准）
     */
    private String msgType;

    /**
     * HIS 侧流水号，每次发送唯一
     */
    private String tradeNo;

    /**
     * 撤销报文回指的原上传 tradeNo
     */
    private String origTradeNo;

    /**
     * 医保端回执编号
     */
    private String receiptNo;

    /**
     * 出参报文全文（JSON）
     */
    private String payload;

    /**
     * 回执报文全文（JSON）
     */
    private String replyPayload;

    /**
     * 报文状态（0-待发送 1-回执成功 2-回执失败 3-已被撤销）
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errMsg;

    /**
     * 发出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 回执时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    /**
     * 账期日（对账口径=发出日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    /**
     * 冗余：报盘总费用
     */
    private BigDecimal totalAmount;

    /**
     * 冗余：医保统筹支付
     */
    private BigDecimal insurancePay;

    /**
     * 冗余：个人账户支付
     */
    private BigDecimal personalPay;

    /**
     * 冗余：自费金额
     */
    private BigDecimal selfPay;
}
