package com.his.system.vo;

import lombok.Data;

/**
 * 急诊参数配置 VO（来源 sys_config：emergency.* 键）
 */
@Data
public class EmergencyConfigVO {

    private Integer waitDeadlineLevel1;

    private Integer waitDeadlineLevel2;

    private Integer waitDeadlineLevel3;

    private Integer waitDeadlineLevel4;

    private String waitFallbackReceiver;

    private Integer observationWarnHours;

    private Integer observationMaxHours;
}
