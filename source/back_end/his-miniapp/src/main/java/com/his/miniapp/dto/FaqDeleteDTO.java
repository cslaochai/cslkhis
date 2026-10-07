package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按主键删除的通用入参（物理删的表用它，不复用业务 DTO）。
 */
@Data
@Schema(name = "IdDeleteDTO", description = "按主键删除入参")
public class FaqDeleteDTO {

    @NotNull(message = "id不能为空")
    @Schema(description = "主键")
    private Long id;
}
