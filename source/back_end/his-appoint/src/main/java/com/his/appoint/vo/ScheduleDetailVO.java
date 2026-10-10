package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班详情VO
 */
@Data
public class ScheduleDetailVO {

    /**
     * 排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 排班人员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 排班人员姓名
     */
    private String doctorName;

    /**
     * 排班对象岗位类别（2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 岗位类别名称（派生只读，按 {@link #staffType} 从 {@code StaffTypeEnum} 带出，表里没有这一列）
     */
    private String staffTypeName;

    /**
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 班别（1-上午 2-下午 3-全天 4-凌晨 5-夜班）——<b>派生只读</b>：
     * 由 shift_id 关联班次字典带出，排班表上不存这一列（见 ScheduleService#fillShiftDisplay）
     */
    private Integer scheduleType;

    /**
     * 班次名称（班次字典的班次名称，派生只读；班次被删过则为空）
     */
    private String shiftName;

    /**
     * 开始时间（HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm:ss）
     */
    private String endTime;

    /**
     * 标准班次ID（班次字典的ID）：编辑弹窗的「标准班次」下拉靠它回显；NULL = 当时没选标准班次
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 总号源数
     */
    private Integer totalSource;

    /**
     * 已用号源数
     */
    private Integer usedSource;

    /**
     * 可用号源数
     */
    private Integer availableSource;

    /**
     * 挂号费，单位：元
     */
    private BigDecimal registFee;

    /**
     * 诊查费，单位：元
     */
    private BigDecimal diagnosisFee;

    /**
     * 是否专家号：0-否 1-是
     */
    private Integer isExpert;

    /**
     * 专家费，单位：元
     */
    private BigDecimal expertFee;

    /**
     * 是否可预约：0-否 1-是
     */
    private Integer isAppointment;

    /**
     * 预约号源数
     */
    private Integer appointmentSource;

    /**
     * 预约池已用号源（剩余 = appointmentSource - usedAppointmentSource）
     */
    private Integer usedAppointmentSource;

    /**
     * 状态（0-停诊 1-正常 2-已满 3-已过期）
     */
    private Integer status;

    /**
     * 就诊状态（0-待开始 1-接诊中 2-暂停）
     */
    private Integer consultStatus;

    /**
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;
}
