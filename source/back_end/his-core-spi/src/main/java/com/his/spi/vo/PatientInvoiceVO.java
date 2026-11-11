package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 患者端-电子票据（小程序「电子票据」列表行与详情）。
 */
@Data
public class PatientInvoiceVO {

    /**
     * 票据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long invoiceId;

    /**
     * 患者ID（供调用方做归属校验，前端无需渲染）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 票据号
     */
    private String invoiceNo;

    /**
     * 票据类型码值（1-普通发票 2-电子发票 3-数电发票）
     */
    private Integer invoiceType;

    /**
     * 结算账单号
     */
    private String billNo;

    /**
     * 票据金额（元）
     */
    private BigDecimal totalAmount;

    /**
     * 票据状态码值（1-已开具 2-已打印 3-已作废 4-已红冲换开）
     */
    private Integer invoiceStatus;

    /**
     * 票据状态文案
     */
    private String invoiceStatusText;

    /**
     * 开票时间（yyyy-MM-dd HH:mm:ss）
     */
    private String invoiceTime;

    /**
     * 电子票据地址（空=院内尚未回写外部平台链接）
     */
    private String electronicUrl;
}
