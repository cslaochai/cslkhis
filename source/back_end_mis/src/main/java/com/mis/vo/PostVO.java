package com.mis.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工岗位出参（含员工/角色/科室回显）
 */
@Data
public class PostVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;
    private String empCode;
    private String empName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;
    private String roleCode;
    private String roleName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    /** 是否主岗位（0-否 1-是） */
    private Integer isPrimary;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;
}
