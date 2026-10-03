package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预交金流水出参。
 *
 * <p>金额带符号（充值正、退款负），前端展示时不要取绝对值再猜方向 ——
 * 方向已经在这里了，猜就会出"退款显示成充值"的账。
 */
@Data
public class PrepayVO implements Serializable {

    /**
     * 预交金流水ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 预交金单号（唯一）
     */
    private String prepayNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 流水类型：1-充值 2-退款
     */
    private Integer prepayType;

    /**
     * 流水类型文案（后端给，前端不判码值）
     */
    private String prepayTypeText;

    /**
     * 金额（充值为正、退款为负）
     */
    private BigDecimal amount;

    /**
     * 本笔之后的余额快照
     */
    private BigDecimal balanceAfter;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-银行卡 5-转账）
     */
    private Integer payMethod;

    /**
     * 支付方式文案
     */
    private String payMethodText;

    /**
     * 票据号
     */
    private String receiptNo;

    /**
     * 收/退时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 操作人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;
}
