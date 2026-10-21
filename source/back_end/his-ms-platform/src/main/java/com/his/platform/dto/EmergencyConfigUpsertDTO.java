package com.his.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 急诊参数配置 upsert DTO（sys_config：emergency.* 键）
 */
@Data
public class EmergencyConfigUpsertDTO {

    @NotNull(message = "Ⅰ级濒危候诊时限不能为空")
    @Min(value = 0, message = "Ⅰ级濒危候诊时限不能为负数")
    private Integer waitDeadlineLevel1;

    @NotNull(message = "Ⅱ级危重候诊时限不能为空")
    @Min(value = 0, message = "Ⅱ级危重候诊时限不能为负数")
    private Integer waitDeadlineLevel2;

    @NotNull(message = "Ⅲ级急症候诊时限不能为空")
    @Min(value = 0, message = "Ⅲ级急症候诊时限不能为负数")
    private Integer waitDeadlineLevel3;

    @NotNull(message = "Ⅳ级非急症候诊时限不能为空")
    @Min(value = 0, message = "Ⅳ级非急症候诊时限不能为负数")
    private Integer waitDeadlineLevel4;

    /**
     * 候诊超时催办末级无人时的兜底接收人（用户名或员工ID），可留空表示不兜底
     */
    private String waitFallbackReceiver;

    @NotNull(message = "留观预警时长不能为空")
    @Min(value = 0, message = "留观预警时长不能为负数")
    private Integer observationWarnHours;

    @NotNull(message = "留观最大时长不能为空")
    @Min(value = 0, message = "留观最大时长不能为负数")
    private Integer observationMaxHours;
}
