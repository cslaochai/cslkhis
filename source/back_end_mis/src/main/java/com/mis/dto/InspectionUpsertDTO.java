package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 飞检任务新增/修改
 */
@Data
public class InspectionUpsertDTO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    /** 检查类型（专项检查/日常巡查/举报核查） */
    private String inspectType;

    /** 被查机构 */
    private String targetOrg;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEnd;

    /** 带队人 */
    private String leader;

    /** 检查组成员 */
    private String members;

    /** 检查范围 */
    private String scopeDesc;
}
