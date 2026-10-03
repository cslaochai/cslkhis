package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 追加一条麻醉生命体征采样。
 *
 * <p><b>采样时刻必填且必须精确到秒</b>：同一时刻插两条会在 UNIQUE 上直接失败，
 * 服务端也会先查重给出人话错误 —— 时间轴上一个点只能有一个真值。
 */
@Data
public class AnesthesiaVitalUpsertDTO implements Serializable {

    /** 麻醉记录ID */
    @NotNull(message = "麻醉记录单ID不能为空")
    private Long recordId;

    /** 采样时刻 */
    @NotNull(message = "采样时刻不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sampleTime;

    /** 收缩压（mmHg） */
    private Integer systolic;

    /** 舒张压（mmHg） */
    private Integer diastolic;

    /** 心率（次/分） */
    private Integer heartRate;

    /** 呼吸频率（次/分） */
    private Integer respiration;

    /** 体温（℃） */
    private BigDecimal temperature;

    /** 脉搏血氧饱和度（%） */
    private Integer spo2;

    /** 呼气末二氧化碳分压（mmHg） */
    private Integer etco2;

    /** 备注 */
    private String remark;
}
