package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医保报盘报文台账出参。
 *
 * <p>分页出参不带报文全文（那一列动辄几十 KB），单条查询才带回。
 */
@Data
public class BizInsuranceReportVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 医保接口编号
     */
    private String msgType;

    /**
     * HIS 侧流水号，每次发送唯一
     */
    private String tradeNo;

    /**
     * 撤销报文回指的原上传流水号
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
     * 报盘总费用
     */
    private BigDecimal totalAmount;

    /**
     * 报盘医保统筹支付
     */
    private BigDecimal insurancePay;

    /**
     * 报盘个人账户支付
     */
    private BigDecimal personalPay;

    /**
     * 报盘自费金额
     */
    private BigDecimal selfPay;

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
