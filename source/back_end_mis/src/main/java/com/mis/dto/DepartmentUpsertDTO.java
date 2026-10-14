package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室新增/修改入参（HIS 口径：departmentUpsert）
 */
@Data
public class DepartmentUpsertDTO implements Serializable {

    private Long id;

    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    /** 科室类型（mss_dept_type 字典：1-前台经办 2-审核稽核 3-基金财务 4-综合管理） */
    private Integer deptType;

    private Long parentId;

    private Integer sortOrder;

    private Long deptLeaderId;

    private String phone;

    private String email;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    private String remark;
}
