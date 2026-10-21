package com.his.app.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者端号源下拉出参。
 */
@Data
@Schema(name = "MiniappScheduleSelectVO", description = "患者端号源下拉出参")
public class MiniScheduleSelectVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    private String doctorName;

    /** 排班日期 */
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
     * 门诊诊查费职级加收档位（1普通 2副主任医师 3主任医师 4知名专家）
     *
     * <p>给小程序号源卡片显示「副主任医师诊查费」这类标签用。患者看到 ¥31.00 时
     * 应当知道这是主任医师档而不是普通号加价——只给数字不说明来源，是价格投诉的常见起点。
     */
    private Integer diagnosisTier;

    /**
     * 专家号（0-否 1-是），仅作展示标记
     */
    private Integer isExpert;

    /**
     * 专家费（已废止，58号文后加收并入门诊诊查费，恒为 0）
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
