package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 换班/代班入参。
 *
 * <p>两种形态由 {@code toScheduleId} 是否给值区分：给了就是<b>互换</b>（两条排班对调排班对象），
 * 不给就是<b>代班</b>（这一班改由 {@code substituteEmployeeId} 承接）。
 * 日期与单元都取自排班行本身，不在入参里重复一遍（重复就会出现两条互相矛盾的口径）。
 */
@Data
public class StaffScheduleSwapDTO {

    /** 被换掉的排班 */
    @NotNull(message = "请选择要换的班次")
    private Long fromScheduleId;

    /** 对方的排班（传了=换班互换，不传=代班单向） */
    private Long toScheduleId;

    /** 代班时承接的人（换班互换时不传，由对方排班上的人承接） */
    private Long substituteEmployeeId;

    /** 变更原因 */
    @NotBlank(message = "请填写换班原因")
    private String reason;
}
