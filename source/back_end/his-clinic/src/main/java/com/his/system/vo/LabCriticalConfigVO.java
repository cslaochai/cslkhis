package com.his.system.vo;

import lombok.Data;

/**
 * 检验危急值参数配置 VO（来源 sys_config：lab.* 键）
 */
@Data
public class LabCriticalConfigVO {

    private Integer criticalValueDeadlineMinutes;

    private String criticalValueFallbackReceiver;
}
