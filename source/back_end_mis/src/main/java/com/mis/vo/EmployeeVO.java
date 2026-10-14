package com.mis.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工出参（HIS 口径）
 * - 列表：idCard 已脱敏（前4后4打码），页面直接渲染
 * - 详情：idCard 明文（编辑回显，避免洗库）
 */
@Data
public class EmployeeVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String empCode;
    private String empName;
    /** 员工类型（mss_emp_type 字典） */
    private Integer empType;
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    private String idCard;
    private String phone;
    private String email;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    private String title;
    private String position;
    private String education;
    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 执业科室（所有岗位科室名，列表列展示） */
    private List<String> deptNames;

    /** 岗位行（角色 × 科室，详情回显） */
    private List<PostVO> posts;
}
