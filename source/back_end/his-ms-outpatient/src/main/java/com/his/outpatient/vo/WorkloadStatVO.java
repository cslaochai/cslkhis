package com.his.outpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 全岗位月度工时统计VO。
 */
@Data
@Schema(name = "WorkloadStatVO", description = "全岗位月度工时统计")
public class WorkloadStatVO implements Serializable {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "工号")
    private String empCode;

    @Schema(description = "姓名")
    private String employeeName;

    @Schema(description = "岗位类型码")
    private Integer staffType;

    @Schema(description = "岗位类型文案")
    private String staffTypeName;

    @Schema(description = "科室ID")
    private Long deptId;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "出勤天数（上班的天数）")
    private Integer workDays;

    @Schema(description = "总工时（小时）")
    private BigDecimal totalHours;

    @Schema(description = "夜班天数")
    private Integer nightDays;

    @Schema(description = "休息天数")
    private Integer restDays;
}
