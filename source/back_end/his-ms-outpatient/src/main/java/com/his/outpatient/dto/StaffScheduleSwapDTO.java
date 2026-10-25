package com.his.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 换班/代班入参。
 */
@Data
public class StaffScheduleSwapDTO {

    /**
     * 被换掉的排班
     */
    @NotNull(message = "请选择要换的班次")
    private Long fromScheduleId;

    /**
     * 对方的排班（传了=换班互换，不传=代班单向）
     */
    private Long toScheduleId;

    /**
     * 代班时承接的人（换班互换时不传，由对方排班上的人承接）
     */
    private Long substituteEmployeeId;

    /**
     * 变更原因
     */
    @NotBlank(message = "请填写换班原因")
    private String reason;

    /**
     * 排班域（1-岗位出勤 2-急诊值班）：换班/代班只许动本域的行。
     * 为空=不校验（兼容内部调用，如 DutyRosterServiceImpl 代班路径）。
     */
    private Integer scope;
}
