package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 患者端待缴账单（账单头 + 逐条明细），对应
 */
@Data
public class PendingBillVO implements Serializable {

    /**
     * 账单ID（BigINT，序列化成字符串避免前端精度丢失）
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
     * 本次应收合计（元）
     */
    private BigDecimal payableAmount;

    /**
     * 出账时间
     */
    private java.time.LocalDateTime billTime;

    /**
     * 账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）
     */
    private Integer billStatus;

    /**
     * 摊行明细
     */
    private List<PendingBillItemVO> details;

    /**
     * 医保统筹合计（元，由明细汇总）
     */
    private BigDecimal poolAmount;

    /**
     * 医保个账合计（元，由明细汇总）
     */
    private BigDecimal accountAmount;

    /**
     * 自费合计（元，由明细汇总）
     */
    private BigDecimal selfAmount;
}