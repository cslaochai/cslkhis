package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 班次字典新增/修改 DTO
 */
@Data
public class ShiftUpsertDTO {

    /**
     * 主键ID（空=新增）
     */
    private Long id;

    /**
     * 班次名称
     */
    @NotBlank(message = "班次名称不能为空")
    private String shiftName;

    /**
     * 开始时间 HH:mm
     */
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    /**
     * 结束时间 HH:mm（跨零点班次指次日，如 18:00~08:00）
     */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /**
     * 跨零点标记（可传可不传——服务端按适用域与起止时间重算，前端传值不认）
     */
    private Integer crossDay;

    /**
     * 时长（分钟，可传可不传——服务端按起止时间重算，前端传值不认）
     */
    private Integer durationMinutes;

    /**
     * 适用科室ID（空=全院通用）
     */
    private Long deptId;

    /**
     * 班次类型（1-上午 2-下午 3-全天；空=不联动排班班次）
     */
    private Integer scheduleType;

    /**
     * 状态（0-停用 1-启用，空=启用）
     */
    private Integer status;

    /**
     * 适用域（1-门诊排班 2-病区护理；空=1，门诊排班页建的班次不该混进护理册）
     */
    private Integer useScope;

    /**
     * 适用岗位类别（StaffTypeEnum 码值；空=全部岗位通用）
     */
    private Integer applyStaffType;

    /**
     * 是否夜班（1-夜班 0-白班；空按 0 处理）。
     *
     * <p>连续夜班天数上限、班后最短休息这两条规则全靠它判定，建班次时就要说清楚。
     */
    private Integer isNight;

    /**
     * 下此班后最短休息小时数（0=不限制；夜班建议 16）。
     */
    private java.math.BigDecimal needRestHours;

    /**
     * 迟到宽限（分钟）：签到晚于「班次开始 + 这个数」才算迟到（sql/214）
     */
    private Integer lateGraceMinutes;

    /**
     * 备注
     */
    private String remark;
}
