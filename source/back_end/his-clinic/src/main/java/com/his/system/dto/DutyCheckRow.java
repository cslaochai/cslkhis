package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 全院总值班区间校验用行（按实际值班人解析：换班后按顶替人归并）。
 */
@Data
public class DutyCheckRow {

    /** 排班行ID */
    private Long id;
    /** 值班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;
    /** 班次（1-白班 2-夜班） */
    private Integer shiftType;
    /** 班内角色（1-主班 2-副班） */
    private Integer roleType;
    /** 值班点位ID */
    private Long postId;
    /** 值班点位名称 */
    private String postName;
    /** 实际值班人ID（换班后按顶替人） */
    private Long employeeId;
    /** 实际值班人姓名 */
    private String employeeName;
    /** 原属科室名称 */
    private String deptName;
}
