package com.his.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 问答观测反馈请求（顶/踩，同一记录重复提交覆盖）。
 */
@Data
public class KnowledgeFeedbackDTO {

    @NotNull(message = "日志 ID 不能为空")
    private Long id;

    @NotNull(message = "反馈不能为空")
    @Min(value = 1, message = "反馈取值不合法")
    @Max(value = 2, message = "反馈取值不合法")
    private Integer feedback;
}
