package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者端号源下拉出参。
 *
 * <p>与后台排班下拉分开是有意的：小程序要按「科室 → 专家/普通号 → 可约号源」逐级筛选并展示费用明细，
 * 而后台排班下拉只要号源本身。两边共用一个类就会各自多带一倍用不上的列。
 */
@Data
@Schema(name = "MiniappScheduleSelectVO", description = "患者端号源下拉出参")
public class MiniappScheduleSelectVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    private String doctorName;

    private LocalDate scheduleDate;

    private String startTime;

    private String endTime;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 诊查费
     */
    private BigDecimal diagnosisFee;

    /**
     * 专家号（0-否 1-是）
     */
    private Integer isExpert;

    /**
     * 专家费
     */
    private BigDecimal expertFee;

    /**
     * 是否开放预约（0-否 1-是）
     */
    private Integer isAppointment;

    /**
     * 预约号源总数
     */
    private Integer appointmentSource;

    /**
     * 已用预约号源
     */
    private Integer usedAppointmentSource;
}
