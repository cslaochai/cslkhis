package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 员工岗位摘要：一行 = 一名员工在一个科室挂一个角色（身份的唯一口径）。
 */
@Data
@NoArgsConstructor
public class EmployeePostBriefVO {

    /**
     * 岗位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 员工ID
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
    private Integer isPrimary;

    /**
     * 岗位生效日期（null=不限，自始有效）
     */
    private LocalDate effectiveDate;

    /**
     * 岗位失效日期（null=不限，长期有效）
     */
    private LocalDate expireDate;
}
