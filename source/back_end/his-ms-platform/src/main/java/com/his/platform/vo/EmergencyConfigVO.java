package com.his.platform.vo;

import lombok.Data;

/**
 * 急诊参数配置 VO（来源 sys_config：emergency.* 键）
 */
@Data
public class EmergencyConfigVO {

    /** Ⅰ级（濒危）候诊应接诊时限（分钟，0 表示即刻） */
    private Integer waitDeadlineLevel1;

    /** Ⅱ级（危重）候诊应接诊时限（分钟） */
    private Integer waitDeadlineLevel2;

    /** Ⅲ级（急症）候诊应接诊时限（分钟） */
    private Integer waitDeadlineLevel3;

    /** Ⅳ级（非急症）候诊应接诊时限（分钟） */
    private Integer waitDeadlineLevel4;

    /** 候诊超时催办的末级接收人（用户名或员工ID），为空表示不兜底 */
    private String waitFallbackReceiver;

    /** 留观预警时长（小时）：留观到达该时长提示医生办理收住或离观 */
    private Integer observationWarnHours;

    /** 留观最大时长（小时）：超过即视为留观超时 */
    private Integer observationMaxHours;
}
