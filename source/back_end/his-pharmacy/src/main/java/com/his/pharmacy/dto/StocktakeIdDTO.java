package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 盘点单主键入参（提交复核用）
 */
@Data
public class StocktakeIdDTO {

    @NotNull(message = "盘点单ID不能为空")
    private Long id;
}
