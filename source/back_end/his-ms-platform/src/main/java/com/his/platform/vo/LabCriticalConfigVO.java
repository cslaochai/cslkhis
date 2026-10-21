package com.his.platform.vo;

import lombok.Data;

/**
 * 检验危急值参数配置 VO（来源 sys_config：lab.* 键）
 */
@Data
public class LabCriticalConfigVO {

    /** 危急值处理时限（分钟）：超时未签收逐级催办 */
    private Integer criticalValueDeadlineMinutes;

    /** 危急值无人接收时的兜底接收人（用户名或员工ID），为空表示不兜底 */
    private String criticalValueFallbackReceiver;
}
