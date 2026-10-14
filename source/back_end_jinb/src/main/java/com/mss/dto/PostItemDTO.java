package com.mss.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工岗位条目（角色 × 科室）
 */
@Data
public class PostItemDTO implements Serializable {

    private Long roleId;
    private Long deptId;
    /** 是否主岗位（0-否 1-是，每人恰好一条） */
    private Integer isPrimary;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;
}
