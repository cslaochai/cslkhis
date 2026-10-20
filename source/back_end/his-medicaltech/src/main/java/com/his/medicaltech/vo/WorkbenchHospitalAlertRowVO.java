package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数字卡「全院异常告警」（WorkbenchMetricMapper#hospitalAlertStats）。
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