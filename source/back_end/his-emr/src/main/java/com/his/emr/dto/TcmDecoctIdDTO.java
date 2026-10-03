package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 代煎单主键入参（详情/打印回执）
 */
@Data
public class TcmDecoctIdDTO {

    @NotNull(message = "代煎单ID不能为空")
    private Long id;
}
