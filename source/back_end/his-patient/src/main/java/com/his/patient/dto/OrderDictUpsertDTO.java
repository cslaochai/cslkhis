package com.his.patient.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 医嘱基础字典新增/修改入参。
 */
@Data
public class OrderDictUpsertDTO implements Serializable {

    /**
     * 字典数据ID（为空=新增，有值=修改）
     */
    private Long id;

    /**
     * 字典类型：his_order_route / his_order_freq / his_dose_unit（必填，服务端白名单校验）
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 字典值（新增时必填；修改时不允许变更）
     */
    @Size(max = 100, message = "字典值不能超过 100 字")
    private String dictValue;

    /**
     * 显示名
     */
    @NotBlank(message = "显示名不能为空")
    @Size(max = 100, message = "显示名不能超过 100 字")
    private String dictLabel;

    /**
     * 排序（不传排到最后）
     */
    private Integer dictSort;

    /**
     * 状态：1-启用 0-停用（不传按启用）
     */
    @Min(value = 0, message = "状态取值不合法（应为 0-停用 1-启用）")
    @Max(value = 1, message = "状态取值不合法（应为 0-停用 1-启用）")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;
}
