package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 急诊分诊复评到期待办负载（站内信 payload，与急诊待办消息卡片同构）
 */
@Data
public class EmergencyReevalDuePayloadVO {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 急诊号
     */
    private String emergencyNo;

    /**
     * 分诊级别文案
     */
    private String triageLevelText;

    /**
     * 就诊科室名称
     */
    private String deptName;

    /**
     * 已候诊分钟数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waitMinutes;

    /**
     * 应接诊时限（分钟）
     */
    private Integer targetSeeMinutes;

    /**
     * 登记/上次复评时的 MEWS 评分（分诊台复评时的参照）
     */
    private Integer mewsScore;
}
