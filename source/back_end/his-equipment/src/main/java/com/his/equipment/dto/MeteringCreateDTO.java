package com.his.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.equipment.enums.MeteringResultEnum;
import com.his.equipment.enums.MeteringTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 计量登记。
 */
@Data
public class MeteringCreateDTO implements Serializable {

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 计量类型（1-强检 2-校准）
     */
    @NotNull(message = "计量类型不能为空")
    @InEnum(value = MeteringTypeEnum.class, message = "计量类型取值不合法（1-强检 2-校准）")
    private Integer meteringType;

    @NotNull(message = "计量日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meteringDate;

    @NotNull(message = "有效期至不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 计量结果（1-合格 2-不合格）；不填默认按「合格」落库
     */
    @InEnum(value = MeteringResultEnum.class, message = "计量结果取值不合法（1-合格 2-不合格）")
    private Integer meteringResult;

    private String certNo;

    private String agency;
}
