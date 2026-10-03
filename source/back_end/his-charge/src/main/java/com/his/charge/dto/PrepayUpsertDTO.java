package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预交金收退入参（充值 / 退款共用）。
 *
 * <p>金额 <b>一律传正数</b>，正负由 {@code prepayType} 决定，落库时才带符号：
 * 让收银员输入负数退款是最经典的账目事故（输成 -500 的退款会变成充值）。
 *
 * <p>{@code channelTxnNo} 是给患者端自助充值回调留的口子（那里有渠道真实交易号）；
 * 柜面扫码留空，由 L3 造模拟号与渠道账单勾对。
 */
@Data
public class PrepayUpsertDTO {

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 流水类型（必填）：1-充值 2-退款
     */
    @NotNull(message = "缺少流水类型（1-充值 2-退款）")
    private Integer prepayType;

    /**
     * 金额（必填，正数，单位元）
     */
    @NotNull(message = "缺少金额")
    @DecimalMin(value = "0.01", message = "金额必须大于 0（退款也传正数，方向由流水类型决定）")
    private BigDecimal amount;

    /**
     * 支付方式（字典 {@code his_pay_method}）：1-现金 2-微信 3-支付宝 6-银行卡 7-转账。
     * <b>4-医保个账与 5-院内余额不能当预交金来源</b>（个账是刷参保人卡的额度、
     * 余额没进过现金抽屉，日结会凭空多出一块），服务端当场拒绝。
     */
    private Integer payMethod;

    /**
     * 柜面纸质收据号（与流水勾对用；患者端自助充值无纸票，留空）
     */
    private String receiptNo;

    /**
     * 渠道真实交易号（患者端支付回调带回；柜面留空由服务端造模拟号）
     */
    private String channelTxnNo;

    /**
     * 收/退时间（不传取当前时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 备注（退款建议写清原因）
     */
    private String remark;
}
