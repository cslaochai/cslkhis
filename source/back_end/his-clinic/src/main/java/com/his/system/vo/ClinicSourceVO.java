package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者端号源下拉出参（his-api 契约）。
 *
 * <p>字段口径与 his-system 的 {@code BizClinicSource} 及 miniapp 的 {@code MiniScheduleSelectVO} 对齐，
 * 便于两侧 {@code BeanUtils.copyProperties} 直接搬运。
 */
@Data
public class ClinicSourceVO implements Serializable {

    private Long id;

    private Long deptId;

    private String deptName;

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
