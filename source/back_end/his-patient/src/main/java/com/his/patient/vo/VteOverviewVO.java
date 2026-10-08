package com.his.patient.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * VTE 防控看板（"今天该干什么"）—— 名单页顶部四张卡的数据源。
 */
@Data
public class VteOverviewVO {

    /**
     * 在院患者数
     */
    private Integer inHospitalCount;

    /**
     * 在院中做过 Caprini 评估的人数
     */
    private Integer inHospitalAssessedCount;

    /**
     * 在院中高危人数（最新评估 risk_level>=2）
     */
    private Integer inHospitalHighRiskCount;

    /**
     * 在院中高危中尚未落实任何措施的人数（今天要干的事）
     */
    private Integer highRiskPendingCount;

    /**
     * 在院中高危措施落实率（%）
     */
    private BigDecimal highRiskPreventRate;

    /**
     * 本月（自然月）院内新发 VTE 患者数
     */
    private Integer monthVteEventCount;

    /**
     * 本月预防相关出血例数
     */
    private Integer monthBleedCount;

    /**
     * 至今未评 Caprini 的在院人数（漏评提醒）
     */
    private Integer missedAssessCount;
}
