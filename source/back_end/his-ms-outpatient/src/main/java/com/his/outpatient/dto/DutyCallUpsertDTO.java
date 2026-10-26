package com.his.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发起听班呼叫入参（sql/297）。
 *
 * <p>被叫人不由前端传 —— 呼叫要叫的是「这一班的那个听班医生」，
 * 由后端按科室 + 层级 + 班次从排班解析。前端指定具体某个人会让呼叫变成
 * 「私聊某个熟人」，值班制度就废了。
 */
@Data
public class DutyCallUpsertDTO {

    /** 呼叫发起科室ID（留空取当前登录人所在科室） */
    private Long deptId;

    /** 值班日期（留空取今天） */
    private String dutyDate;

    /** 班次：1-白班 2-夜班（留空按当前时刻推断） */
    private Integer shiftType;

    /** 被叫层级：2-二线 3-三线（留空默认二线） */
    private Integer targetLevel;

    /** 呼叫事由 */
    @NotBlank(message = "呼叫事由不能为空")
    @Size(max = 500, message = "呼叫事由过长")
    private String callReason;

    /** 患者姓名 */
    @Size(max = 50, message = "患者姓名过长")
    private String patientName;

    /** 患者所在位置（病区/床号） */
    @Size(max = 100, message = "患者位置过长")
    private String patientLocation;

    /** 到岗时限（分钟，留空取系统参数 duty.call.timeout_minutes） */
    @NotNull(message = "到岗时限不能为空")
    private Integer timeoutMinutes;
}
