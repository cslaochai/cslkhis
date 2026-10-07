package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数字卡「全院异常告警」（{@code WorkbenchMetricMapper#hospitalAlertStats}）。
 *
 * <p>与 {@link WorkbenchHospitalCoreRowVO} 分开两个方法而不是一次查完：
 * 两段口径差别太大（概况是"今天怎么样"，告警是"还欠着多少"），
 * 合成一段 SQL 会让"告警"跟着"今日"的时间窗走 —— 欠费患者是存量，按日窗算就永远只显示当天的。
 *
 * <p>字段名同为前端契约（{@code METRIC_SPECS.hospitalToday} 第二组）。
 * 这四项前端都标了 {@code danger: true}（值 > 0 标红），是"待处理类积压"。
 */
@Data
public class WorkbenchHospitalAlertRowVO implements Serializable {

    /**
     * 待处理危急值数（状态 1-待接收 2-已接收）
     */
    private Long criticalValuePending;

    /**
     * 待审核处方数
     */
    private Long prescriptionPending;

    /**
     * 质控不通过病历数
     */
    private Long qcFailCount;

    /**
     * 欠费住院患者数（记账净额 > 已收，按当前在院状态筛）
     */
    private Long arrearsCount;
}