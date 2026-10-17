package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者端待缴账单行（含明细）。
 */
@Data
public class MiniPendingBillListVO implements Serializable {

    /**
     * 账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账单号
     */
    private String billNo;

    /**
     * 就诊流水号
     */
    private String encounterNo;

    /**
     * 应缴金额（元）
     */
    private BigDecimal payableAmount;

    /**
     * 结账时间
     */
    private LocalDateTime billTime;

    /**
     * 账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）
     */
    private Integer billStatus;

    /**
     * 账单级医保统筹合计（元），按明细行汇总
     */
    private BigDecimal poolAmount;

    /**
     * 账单级医保个账合计（元），按明细行汇总
     */
    private BigDecimal accountAmount;

    /**
     * 账单级个人自付合计（元），按明细行汇总
     */
    private BigDecimal selfAmount;

    /**
     * 账单明细
     */
    private List<MiniPendingBillItemVO> details;
}
