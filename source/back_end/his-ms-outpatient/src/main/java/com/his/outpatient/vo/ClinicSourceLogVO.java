package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门诊排班变更留痕出参。
 */
@Data
public class ClinicSourceLogVO {

    /** 留痕记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 门诊排班ID：变更发生时挂靠的班次 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 排班日期（按留痕时关联的排班行补）
     */
    private String scheduleDate;

    /**
     * 科室名
     */
    private String deptName;

    /**
     * 医生名
     */
    private String doctorName;

    /** 变更动作（1-新增排班 2-修改排班 3-删除排班 4-停诊 5-启用 6-加号 7-批量退号 8-段级编辑 9-复刻） */
    private Integer actionType;

    /** 变更动作文案（由枚举 label 渲染） */
    private String actionTypeText;

    /** 变更数量（加号数/退号数/批量或复刻条数） */
    private Integer amount;

    /** 变更原因 */
    private String reason;

    /** 发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /**
     * 操作人
     */
    private String createBy;
}
