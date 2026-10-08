package com.his.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按主键操作请求（获取 / 删除）。
 */
@Data
public class KnowledgeIdDTO {

    @NotNull(message = "ID 不能为空")
    private Long id;
}
