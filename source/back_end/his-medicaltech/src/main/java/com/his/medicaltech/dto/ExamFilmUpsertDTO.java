package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 胶片用量登记入参（sql/138）。
 */
@Data
public class ExamFilmUpsertDTO {

    /**
     * 胶片行ID（新增为空；修改张数时传）
     */
    private Long id;

    /**
     * 检查记录ID（必填：胶片挂在执行记录上）
     */
    @NotNull(message = "缺少检查记录")
    private Long recordId;

    /**
     * 胶片规格ID（必填，胶片规格价目的ID）
     */
    @NotNull(message = "请选择胶片规格")
    private Long specId;

    /**
     * 张数（必填，大于 0）
     */
    @NotNull(message = "缺少胶片张数")
    @Min(value = 1, message = "胶片张数至少 1 张")
    @Max(value = 200, message = "单次登记胶片张数不能超过 200")
    private Integer quantity;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;
}
