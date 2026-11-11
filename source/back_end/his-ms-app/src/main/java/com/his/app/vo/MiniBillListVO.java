package com.his.app.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 患者端-账单列表行（只出小程序要渲染的字段）。
 */
@Data
public class MiniBillListVO {

    /**
     * 账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单号
     */
    private String billNo;

    /**
     * 账单类型码值
     */
    private Integer billType;

    /**
     * 账单类型文案
     */
    private String billTypeText;

    /**
     * 账单状态码值（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）
     */
    private Integer billStatus;

    /**
     * 账单状态文案
     */
    private String billStatusText;

    /**
     * 总金额（元）
     */
    private BigDecimal totalAmount;

    /**
     * 应缴金额（元）
     */
    private BigDecimal payableAmount;

    /**
     * 已缴金额（元）
     */
    private BigDecimal paidAmount;

    /**
     * 未缴金额（元）
     */
    private BigDecimal unpaidAmount;

    /**
     * 开单时间（yyyy-MM-dd HH:mm:ss）
     */
    private String billTime;
}
