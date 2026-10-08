package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 渠道账单拉取入参（M7 留口子：当前走控制台打印）
 */
@Data
public class PayChannelImportDTO {

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
}
