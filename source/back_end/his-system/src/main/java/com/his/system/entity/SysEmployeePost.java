package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 员工岗位实体：一行 = 一个人在一个科室以某个角色执业（角色 × 科室）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee_post")
public class SysEmployeePost extends BaseEntity {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 角色ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 是否主科室（0-否 1-是）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer isPrimary;

    /**
     * 岗位生效日期（NULL=保存即生效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate effectiveDate;

    /**
     * 岗位失效日期（NULL=长期有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expireDate;
}
