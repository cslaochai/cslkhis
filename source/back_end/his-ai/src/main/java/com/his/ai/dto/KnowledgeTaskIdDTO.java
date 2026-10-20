package com.his.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按任务 ID 查询请求（重建索引进度轮询）。
 */
@Data
public class KnowledgeTaskIdDTO {

    @NotNull(message = "任务 ID 不能为空")
    private Long taskId;
}
