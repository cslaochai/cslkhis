package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 执行日结入参。
 */
@Data
public class DaySettlementRunDTO {

    /**
     * 日结日期（yyyy-MM-dd）
     */
    @NotBlank(message = "日结日期不能为空")
    private String settleDate;

    /**
     * 对账不平时是否仍允许出单。
     *
     * <p>默认 false —— 有差异就抛错，逼人先看清楚差在哪。
     * 真实场景里有些差异是合法的（跨日退费、渠道延迟到账），所以允许人工放行，
     * 但**必须显式传 true 并把理由写在 remark 里**，不能默认宽容。
     */
    private Boolean allowDiff;

    /**
     * 备注（放行差异时必填理由）
     */
    private String remark;
}
