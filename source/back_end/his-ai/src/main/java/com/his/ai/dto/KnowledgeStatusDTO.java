package com.his.ai.dto;

import com.his.common.validation.AllowedValues;
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
    @AllowedValues(values = {"0", "1"}, message = "状态不合法")
    private Integer status;
}
