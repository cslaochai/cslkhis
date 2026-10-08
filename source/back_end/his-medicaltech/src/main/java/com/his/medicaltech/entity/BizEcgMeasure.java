package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 心电测量参数（心电测量参数，sql/173）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ecg_measure")
public class BizEcgMeasure extends BaseEntity {

    /**
     * 检查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 波形ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waveformId;

    /**
     * 心率（次/分）
     */
    private Integer hr;

    /**
     * PR间期（ms，正常 120~200）
     */
    private Integer prMs;

    /**
     * QRS时限（ms，正常 60~100）
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
     * QRS电轴（°，正常 -30~+90）
     */
    private Integer qrsAxis;

    /**
     * T电轴（°）
     */
    private Integer tAxis;

    /**
     * 节律描述（如窦性心律/心房颤动）
     */
    private String rhythmText;

    /**
     * 测量人
     */
    private String measureBy;

    /**
     * 测量时间
     */
    private LocalDateTime measureTime;
}
