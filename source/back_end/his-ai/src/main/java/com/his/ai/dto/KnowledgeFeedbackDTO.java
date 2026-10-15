package com.his.ai.dto;

import com.his.common.validation.AllowedValues;
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
    @AllowedValues(values = {"1", "2"}, message = "反馈取值不合法")
    private Integer feedback;
}
