package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数字卡「我（医生）的今日诊疗」（WorkbenchMetricMapper#doctorStats）。
 */
@Data
public class WorkbenchDoctorStatsRowVO implements Serializable {

    /**
     * 今日出诊排班数
     */
    private Long todayScheduleCount;

    /**
     * 候诊人数（排队状态 1候诊 2就诊中 3已叫号）
     */
    private Long waitingCount;

    /**
     * 我的住院患者数（在院且主治医师为我）
     */
    private Long myInpatientCount;

    /**
     * 待校对医嘱数
     */
    private Long todoVerifyOrderCount;

    /**
     * 待我会诊数
     */
    private Long todoConsultationCount;

    /**
     * 待处理危急值数（经检验单开单医生收敛）
     */
    private Long criticalValueCount;

    /**
     * 待归档病历数
     */
    private Long todoArchiveCount;
}