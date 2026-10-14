package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工岗位条目（角色 × 科室；兼容 roleId 与 roleCode 两种入参）
 */
@Data
public class PostItemDTO implements Serializable {

    /** MSS 口径：角色ID */
    private Long roleId;

    /** HIS 口径：角色编码（非空时优先，service 映射为 roleId） */
    private String roleCode;

    private Long deptId;
    /** 是否主岗位（0-否 1-是，每人恰好一条） */
    private Integer isPrimary;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;
}
