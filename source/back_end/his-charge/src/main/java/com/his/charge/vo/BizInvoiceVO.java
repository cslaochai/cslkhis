package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票信息出参
 */
@Data
public class BizInvoiceVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 发票号
     */
    private String invoiceNo;

    /**
     * 发票类型（字典 his_invoice_type：1-普通发票 2-电子发票 3-数电发票）
     */
    private Integer invoiceType;

    /**
     * 结算账单ID（L4：一票对一账单）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 结算账单号（快照）
     */
    private String billNo;

    /**
     * 红冲链：本票冲销的原发票ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origInvoiceId;

    /**
     * 旧收费单ID（历史票遗留，四层新票为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chargeId;

    /**
     * 旧收费单号
     */
    private String chargeNo;


    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 发票金额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 发票状态（字典 his_invoice_status：1-已开具 2-已打印 3-已作废 4-已红冲换开）
     */
    private Integer invoiceStatus;

    /**
     * 开票时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invoiceTime;

    /**
     * 打印时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printTime;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;

    /**
     * 作废原因
     */
    private String voidReason;

    /**
     * 电子发票地址
     */
    private String electronicUrl;

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
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
