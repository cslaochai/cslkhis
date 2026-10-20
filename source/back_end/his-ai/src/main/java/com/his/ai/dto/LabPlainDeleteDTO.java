package com.his.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LabPlainDeleteDTO {
    @NotNull(message = "id不能为空")
    private Long id;
}