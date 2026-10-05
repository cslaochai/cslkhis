package com.his.operation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 清点明细输入行。
 *
 * <p>{@code beforeQty} 在新建清点单时是"术前基数"；
 * 后续阶段的数量在 {@code CountPhaseDTO} 里按阶段填写 ——
 * 分两个 DTO 是因为两者发生的时间与签字人都不同，混在一个形状里必然出现
 * "改关体前数量时不小心覆盖了术前基数"这种错。
 */
@Data
public class CountItemInputUpsertDTO implements Serializable {

    /**
     * 类别（纱布/纱垫）（1-器械 2-敷料 3-缝针 4-刀片 5-其他）
     */
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
