package com.his.patient.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 营养膳食看板（"今天该干什么"）—— 筛查页与订餐页顶部卡的数据源。
 *
 * <p>回答四个问题：在院的人有多少还没筛（missedScreenCount）、筛出来有风险的有多少
 * （inHospitalRiskCount）、其中多少到期该复筛（reScreenDueCount）、
 * 今天的餐配了没有送完没有（todayMealPendingCount / todayMealSignRate）。
 */
@Data
public class NutritionOverviewVO {

    /** 在院患者数 */
    private Integer inHospitalCount;

    /** 在院中做过 NRS2002 筛查的人数 */
    private Integer inHospitalScreenedCount;

    /** 在院未筛人数（漏筛提醒：未筛者不进风险名单） */
    private Integer missedScreenCount;

    /** 在院中最新一次 NRS2002 判为有营养风险（总分≥3）的人数 */
    private Integer inHospitalRiskCount;

    /** 到期未复筛人数（下次筛查日期 <= 今天） */
    private Integer reScreenDueCount;

    /** 执行中且营养科还没接收的膳食方案数（今天要接的单） */
    private Integer pendingConfirmPlanCount;

    /** 今日订餐总条数（不含已取消） */
    private Integer todayMealCount;

    /** 今日订餐已签收条数 */
    private Integer todayMealSignedCount;

    /** 今日订餐还没配/没送的条数 */
    private Integer todayMealPendingCount;

    /** 今日订餐签收率（%） */
    private BigDecimal todayMealSignRate;

    /** 营养会诊未完成数（待应答 + 已应答） */
    private Integer consultUnfinishedCount;

    /** 其中已超时的条数（普通 >24h、急会诊 >10min 未应答） */
    private Integer consultOverdueCount;
}
