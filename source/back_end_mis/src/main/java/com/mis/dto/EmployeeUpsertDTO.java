package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工新增/修改入参（HIS 口径：employeeUpsert）
 * - id 为空走新增：工号自动生成（EMP 自增）
 * - posts 非空：员工岗位整单替换（角色 × 科室）
 * - 主岗位科室回写 dept_id / dept_name 快照
 */
@Data
public class EmployeeUpsertDTO implements Serializable {

    private Long id;

    private String empCode;

    @NotBlank(message = "员工姓名不能为空")
    private String empName;

    /** 员工类型（mss_emp_type 字典：1-经办员 2-审核员 3-管理员 4-其他） */
    private Integer empType;

    /** 性别（1-男 2-女 3-未知） */
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    private String idCard;
    private String phone;
    private String email;
    private String title;
    private String position;
    private String education;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 岗位（角色 × 科室）整单替换 */
    private List<PostItemDTO> posts;
}
