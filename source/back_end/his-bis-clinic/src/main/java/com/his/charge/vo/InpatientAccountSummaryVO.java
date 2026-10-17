package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院账务概览（医生站 / 护士站的欠费提示就靠它）。
 */
@Data
public class InpatientAccountSummaryVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 预交金余额
     */
    private BigDecimal prepayBalance;

    /**
     * 已发生费用合计
     */
    private BigDecimal totalAmount;

    /**
     * 是否欠费（余额不足以覆盖已发生费用）
     */
    private Boolean arrears;

    /**
     * 欠费金额（不欠费为 0）
     */
    private BigDecimal arrearsAmount;

    /**
     * 是否已结算
     */
    private Boolean settled;

    /**
     * 结算单号（未结算为 null）
     */
    private String settlementNo;

    /**
     * 结算状态文案
     */
    private String settleStatusText;

    /**
     * 提示文案（后端给，前端不自己拼）
     */
    private String hintText;

    /**
     * 本次查询是否新记了一条欠费告警（供验证脚本断言"只提示不阻断"）
     */
    private Boolean alertWritten;
}
