package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 急诊候诊超时催办的站内信业务上下文（随站内信下发）。
 */
@Data
public class EmergencyWaitTodoPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 急诊单号
     */
    private String emergencyNo;

    /**
     * 分级中文名
     */
    private String triageLevelText;

    /**
     * 急诊科名称
     */
    private String deptName;

    /**
     * 已候诊分钟数
     */
    private Long waitMinutes;

    /**
     * 该分级应就诊的目标时限（分钟）
     */
    private Integer targetSeeMinutes;
}
