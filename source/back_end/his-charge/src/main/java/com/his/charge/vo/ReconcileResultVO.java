package com.his.charge.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 医保报盘日对账结果：本地清单口径 vs 医保侧（网关）口径 + 差异明细
 */
@Data
public class ReconcileResultVO {

    /**
     * 账期日 yyyy-MM-dd
     */
    private String billDate;

    /**
     * 本地口径：当日报盘成功的结算清单笔数
     */
    private Integer localCount;
    private BigDecimal localTotal;
    private BigDecimal localInsurancePay;

    /**
     * 医保侧口径（网关 queryDayBill 返回）
     */
    private Integer remoteCount;
    private BigDecimal remoteTotal;
    private BigDecimal remoteInsurancePay;

    /**
     * 两边笔数金额是否一致
     */
    private Boolean matched;

    /**
     * 报文台账当日统计：上传成功/失败/已被撤销/撤销报文数
     */
    private Integer uploadSuccess;
    private Integer uploadFail;
    private Integer uploadCancelled;
    private Integer cancelSent;

    private List<Diff> diffs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Diff {
        private String settlementNo;
        private String tradeNo;
        /**
         * 差异说明：医保侧无回执 / 医保侧多出差笔 / 金额不符
         */
        private String issue;
        private BigDecimal localAmount;
        private BigDecimal remoteAmount;
    }
}
