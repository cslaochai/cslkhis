package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班新增/修改入参
 */
@Data
public class ScheduleUpsertDTO {
    /**
     * 排班ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 科室ID
     */
    @NotNull(message = "科室不能为空")
    private Long deptId;

    /**
     * 科室名称
     */
    @NotNull(message = "科室不能为空")
    private String deptName;

    /**
     * 排班人员ID（sql/195 起可以是医生/护士/技师/药师/收费员等任何岗位，不再是「医生ID」）
     */
    @NotNull(message = "请选择排班人员")
    private Long doctorId;

    /**
     * 排班人员姓名
     */
    @NotNull(message = "排班人员姓名不能为空")
    private String doctorName;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他，见 {@code StaffTypeEnum}）
     */
    @NotNull(message = "请选择岗位类别")
    private Integer staffType;

    /**
     * 排班日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 开始时间（HH:mm:ss）：以班次字典为准，后端按 shiftId 覆盖，前端传什么不算
     */
    @NotNull(message = "开始时间不能为空")
    private String startTime;

    /**
     * 结束时间（HH:mm:ss）
     */
    @NotNull(message = "结束时间不能为空")
    private String endTime;

    /**
     * 班次ID（班次字典的ID）：排班唯一的时间段/班别来源，必填，缺失由 service 拦下
     */
    private Long shiftId;

    /**
     * 总号源数
     */
    private Integer totalSource;
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
     * 诊室ID
     *
     * <p><b>只有医生岗（staff_type=1）才要诊室</b>：分诊台按诊室编号发号，医生没诊室患者找不到房间；
     * 护士/收费/技师这些出勤岗没有「诊室」概念，所以这里不挂 {@code @NotNull}，
     * 「启用态医生排班必须排诊室」由 {@code ScheduleServiceImpl#checkRoomRequired} 按岗位收口。
     */
    private Long roomId;

    /**
     * 诊室名称（同上，仅医生岗有意义）
     */
    private String roomName;

    /**
     * 状态（0-停诊 1-正常 2-已满 3-已过期）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
