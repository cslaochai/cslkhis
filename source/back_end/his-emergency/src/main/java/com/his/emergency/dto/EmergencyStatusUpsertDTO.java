package com.his.emergency.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.his.common.validation.InEnum;
import com.his.emergency.enums.EmergencyTransitionStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新急诊状态入参
 */
@Data
public class EmergencyStatusUpsertDTO {
    /**
     * 急诊记录ID
     */
    @NotNull(message = "急诊记录不能为空")
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;

    /**
     * 目标状态：2-接诊 3-留观 5-离院 6-死亡（4-转住院走 /emergency/admit，需要真实入院登记）
     */
    @NotNull(message = "状态不能为空")
    @InEnum(value = EmergencyTransitionStatusEnum.class, message = "状态取值不合法（2-接诊 3-留观 5-离院 6-死亡）")
    private Integer status;

    /**
     * 留观病区ID（转留观必填）
     */
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long observationWardId;

    /**
     * 留观床位ID（转留观必填，占用床位，防止住院分床把同一张床再发出去）
     */
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long observationBedId;
}
