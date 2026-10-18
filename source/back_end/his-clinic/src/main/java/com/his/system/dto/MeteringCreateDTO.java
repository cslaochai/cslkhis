package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 计量登记。
 */
@Data
public class MeteringCreateDTO implements Serializable {

    /** 设备ID */
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 计量类型（1-强检 2-校准）
     */
    @NotNull(message = "计量类型不能为空")
    @AllowedValues(values = {"1", "2"}, message = "计量类型取值不合法")
    private Integer meteringType;

    /** 计量日期 */
    @NotNull(message = "计量日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meteringDate;

    @NotNull(message = "有效期至不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 计量结果（1-合格 2-不合格）；不填默认按「合格」落库
     */
    @AllowedValues(values = {"1", "2"}, message = "计量结果取值不合法")
    private Integer meteringResult;

    private String certNo;

    /** 检定/校准机构 */
    private String agency;
}
