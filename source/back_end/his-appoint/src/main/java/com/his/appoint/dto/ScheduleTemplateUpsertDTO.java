package com.his.appoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 排班模板新增/修改入参
 */
@Data
public class ScheduleTemplateUpsertDTO {

    /**
     * 模板ID，新增时为空
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
    private String deptName;

    /**
     * 排班人员ID（sql/195 起可以是任何岗位，不再是「医生ID」）
     */
    @NotNull(message = "请选择排班人员")
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他，见 {@code StaffTypeEnum}）
     */
    @NotNull(message = "请选择岗位类别")
    private Integer staffType;

    /**
     * 星期几（1-周一 ... 7-周日）
     */
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几不合法")
    @Max(value = 7, message = "星期几不合法")
    private Integer weekDay;

    /**
     * 单双周（0-每周 1-单周 2-双周；空默认每周）
     */
    @Min(value = 0, message = "单双周不合法")
    @Max(value = 2, message = "单双周不合法")
    private Integer weekParity;

    /**
     * 生效起始日期（空=不限）
     */
    private String validFrom;

    /**
     * 生效截止日期（空=不限）
     */
    private String validUntil;

    /**
     * 时间段：以班次字典为准，后端按 shiftId 覆盖
     */
    private String startTime;

    /**
     * 结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 班次ID（班次字典的ID）：模板生成排班时唯一的时间段/班别来源，必填
     */
    @NotNull(message = "请选择班次")
    private Long shiftId;

    /**
     * 号源总数：只对医生岗（staff_type=1）有意义。
     */
    @Min(value = 0, message = "号源数量不能为负")
    private Integer totalSource;

    /**
     * 诊室ID
     */
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 诊疗费
     */
    private BigDecimal diagnosisFee;

    /**
     * 是否专家（0-否 1-是）
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
     * 预约号源数（0-未划池）；划池时必须 ≤ 号源总数
     */
    @Min(value = 0, message = "预约号源数不能为负")
    private Integer appointmentSource;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 段级号源配置（可选，只对医生岗有意义）：
     */
    @Valid
    private List<ScheduleTemplateSlotItemDTO> slots;
}
