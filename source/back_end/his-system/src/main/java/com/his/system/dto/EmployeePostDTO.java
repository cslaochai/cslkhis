package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 岗位入参：一行 = 「以某角色在某科室执业」。
 */
@Data
public class EmployeePostDTO {

    /**
     * 角色编码（角色.role_code）
     */
    @NotBlank(message = "岗位的角色不能为空")
    private String roleCode;

    /**
     * 科室ID（科室的ID）
     */
    @NotNull(message = "岗位的科室不能为空")
    private Long deptId;

    /**
     * 是否主岗位（= 该员工的主科室）：1-是 0/不传-否。一人最多一条，多勾只认第一条
     */
    private Integer isPrimary;

    /**
     * 岗位生效日期（NULL=保存即生效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    /**
     * 岗位失效日期（NULL=长期有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;
}
