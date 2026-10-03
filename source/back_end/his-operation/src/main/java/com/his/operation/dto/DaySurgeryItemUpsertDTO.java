package com.his.operation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 日间手术准入目录新增 / 修改入参。
 *
 * <p>maxStayHours 默认 48：日间手术的命脉是"24~48 小时内离院"，
 * 不填就按 48 小时判超期，不让它静默变成 0（0 会让每一床都判超期）。
 */
@Data
public class DaySurgeryItemUpsertDTO implements Serializable {

    /** 主键ID */
    private Long id;

    /** 术式编码 */
    @NotBlank(message = "术式编码不能为空")
    private String itemCode;

    /** 术式名称 */
    @NotBlank(message = "术式名称不能为空")
    private String itemName;

    /** 适用科室ID */
    private Long deptId;

    /** 最长滞留小时数（默认 48） */
    private Integer maxStayHours;

    /** 麻醉方式（1-局部麻醉 2-椎管内麻醉 3-全身麻醉 4-神经阻滞 5-其他） */
    private Integer anesthesiaType;

    /** 标准费用 */
    private BigDecimal standardFee;

    /** 状态（1-启用 0-停用） */
    private Integer status;

    /**
     * 手术分级 1~4（sql/155，dict his_operation_level）。
     * <p>必填且不设默认：分级是临床分类而不是可缺省的配置，静默兜成 2 级
     * 会让一个四级术式以二级授权通过准入闸门（{@code maxStayHours} 兜 48 是业务常量，此处无对应常量可兜）。
     */
    @NotNull(message = "手术分级不能为空")
    @Min(value = 1, message = "手术分级只能为 1~4")
    @Max(value = 4, message = "手术分级只能为 1~4")
    private Integer operationLevel;

    /** 备注 */
    private String remark;
}
