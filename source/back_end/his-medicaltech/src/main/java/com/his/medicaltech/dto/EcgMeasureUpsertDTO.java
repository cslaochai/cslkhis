package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 心电测量参数入参（sql/173）。
 */
@Data
public class EcgMeasureUpsertDTO {

    /**
     * 检查记录ID
     */
    @NotNull(message = "缺少检查记录")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 心率（次/分）
     */
    private Integer hr;

    /**
     * PR间期（ms）
     */
    private Integer prMs;

    /**
     * QRS时限（ms）
     */
    private Integer qrsMs;

    /**
     * QT间期（ms）
     */
    private Integer qtMs;

    /**
     * QTc校正间期（ms）
     */
    private Integer qtcMs;

    /**
     * P电轴（°）
     */
    private Integer pAxis;

    /**
     * QRS电轴（°）
     */
    private Integer qrsAxis;

    /**
     * T电轴（°）
     */
    private Integer tAxis;

    /**
     * 节律描述
     */
    @Size(max = 100, message = "节律描述不能超过 100 字")
    private String rhythmText;
}
