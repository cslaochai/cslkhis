package com.his.operation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 清点明细输入行。
 */
@Data
public class CountItemInputUpsertDTO implements Serializable {

    /**
     * 类别（纱布/纱垫）（1-器械 2-敷料 3-缝针 4-刀片 5-其他）
     */
    @NotNull(message = "清点项类别不能为空")
    @Min(value = 1, message = "清点项类别取值不合法（应为 1-器械 2-敷料 3-缝针 4-刀片 5-其他）")
    @Max(value = 5, message = "清点项类别取值不合法（应为 1-器械 2-敷料 3-缝针 4-刀片 5-其他）")
    private Integer itemCategory;

    /**
     * 名称（如：止血钳 / 纱布块 / 圆针）
     */
    @NotBlank(message = "清点项名称不能为空")
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 术前数量（≥0；0 也是合法数量 —— 这台手术确实没用这类东西）
     */
    @Min(value = 0, message = "数量不能为负")
    private Integer beforeQty;

    /**
     * 备注
     */
    private String remark;
}
