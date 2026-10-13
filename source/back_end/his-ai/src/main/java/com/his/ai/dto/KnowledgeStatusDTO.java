package com.his.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识文档停用/启用请求。
 */
@Data
public class KnowledgeStatusDTO {

    @NotNull(message = "ID 不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
