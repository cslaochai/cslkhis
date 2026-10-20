package com.his.medicaltech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检验危急值参数配置 upsert DTO（sys_config：lab.* 键）
 */
@Data
public class LabCriticalConfigUpsertDTO {

    @NotNull(message = "危急值处理时限不能为空")
    @Min(value = 0, message = "危急值处理时限不能为负数")
    private Integer criticalValueDeadlineMinutes;

    /**
     * 危急值无人接收时的兜底接收人（用户名或员工ID），可留空表示不兜底
     */
    private String criticalValueFallbackReceiver;
}
