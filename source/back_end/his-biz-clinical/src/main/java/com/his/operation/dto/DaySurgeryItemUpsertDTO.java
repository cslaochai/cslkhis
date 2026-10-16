package com.his.operation.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 日间手术准入目录新增 / 修改入参。
 */
@Data
public class DaySurgeryItemUpsertDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 术式编码
     */
    @NotBlank(message = "术式编码不能为空")
    private String itemCode;

    /**
     * 术式名称
     */
    @NotBlank(message = "术式名称不能为空")
    private String itemName;

    /**
     * 适用科室ID
     */
    private Long deptId;

    /**
     * 最长滞留小时数（默认 48）
     */
    private Integer maxStayHours;

    /**
     * 麻醉方式（1-局部麻醉 2-椎管内麻醉 3-全身麻醉 4-神经阻滞 5-其他）
     */
    @AllowedValues(values = {"1", "2", "3", "4", "5"}, message = "麻醉方式取值不合法")
    private Integer anesthesiaType;

    /**
     * 标准费用
     */
    private BigDecimal standardFee;

    /**
     * 状态（1-启用 0-停用）
     */
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;

    /**
     * 手术分级 1~4
     */
    @NotNull(message = "手术分级不能为空")
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "手术分级取值不合法")
    private Integer operationLevel;

    /**
     * 备注
     */
    private String remark;
}
