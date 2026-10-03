package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 手工登记渠道侧流水（渠道有、台账无的场景，如对账文件补录）
 */
@Data
public class PayChannelManualDTO {

    /**
     * 支付渠道：2-微信 3-支付宝 6-银行卡（对齐支付资金流水的支付方式）
     */
    @NotNull(message = "支付渠道不能为空")
    private Integer channel;

    /**
     * 账单日期
     */
    @NotNull(message = "账单日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    /**
     * 渠道流水号
     */
    @NotBlank(message = "渠道流水号不能为空")
    private String channelTradeNo;

    /**
     * 渠道侧金额
     */
    @NotNull(message = "金额不能为空（退款流水填负数）")
    private BigDecimal amount;

    /**
     * 渠道交易时间（空取当前时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeTime;

    /**
     * 备注（为什么手工登记）
     */
    private String remark;
}
