package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 值班排班新增/修改入参（全院总值班 + 科室医师值班共用）。
 *
 * <p><b>查重口径分两条</b>（sql/202 起）：
 * <ul>
 *   <li><b>传了 {@code postId}</b>：按「日期 + 点位」改。{@code uk_duty_post_date} 保证
 *       一个位一天只能排一个人 —— 8 个临床科室各有一线/二线/三线白班位，
 *       同一天同班次同角色有十几行，按老键查重会 LIMIT 1 抓到别的科室的行改掉
 *       （想排内科结果把外科那行改了）。</li>
 *   <li><b>没传 {@code postId}</b>：维持老语义＝全院总值班，只落「全院行政」点位。</li>
 * </ul>
 */
@Data
public class DutyRosterUpsertDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 值班点位ID（留空＝全院总值班，自动落行政点位）。
     *
     * <p>传了就以点位为权威：班次、班内角色、值班层级、响应形态、所属排班单元全部由点位带出，
     * 入参里的 {@code shiftType}/{@code roleType} 只做合法性校验不再参与定位。
     */
    private Long postId;

    /**
     * 值班日期（必填；夜班填开始日）
     */
    @NotNull(message = "值班日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /**
     * 班次（1-白班 2-夜班 00-次日08）
     */
    @NotNull(message = "班次不能为空")
    @Min(value = 1, message = "班次取值不合法（1-白班 2-夜班）")
    @Max(value = 2, message = "班次取值不合法（1-白班 2-夜班）")
    private Integer shiftType;

    /**
     * 班内角色（1-主班 2-副班）
     */
    @NotNull(message = "班内角色不能为空")
    @Min(value = 1, message = "班内角色取值不合法（1-主班 2-副班）")
    @Max(value = 2, message = "班内角色取值不合法（1-主班 2-副班）")
    private Integer roleType;

    /**
     * 值班人（员工的ID，必填且必须在职）
     */
    @NotNull(message = "值班人不能为空")
    private Long employeeId;

    /**
     * 值班联系电话（留空回落员工档案手机）
     */
    private String phone;

    /**
     * 班次开始 HH:mm（留空按班次口径：白班 08:00 / 夜班 18:00）
     */
    private String startTime;

    /**
     * 班次结束 HH:mm（留空按班次口径：白班 18:00 / 夜班 08:00）
     */
    private String endTime;

    /**
     * 1-有效 0-停用（留空按 1）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
