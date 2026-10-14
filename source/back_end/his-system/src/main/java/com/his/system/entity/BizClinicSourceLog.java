package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊排班变更留痕（新增/修改/删除/停诊/启用/加号/批量退号/段级编辑/复刻）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_clinic_source_log")
public class BizClinicSourceLog extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除；留痕审计只写不删，恒为 0） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /**
     * 门诊排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 变更动作（1-新增排班 2-修改排班 3-删除排班 4-停诊 5-启用 6-加号 7-批量退号 8-段级编辑 9-复刻）
     */
    private Integer actionType;

    /**
     * 变更数量（加号数/退号数/批量或复刻条数）
     */
    private Integer amount;

    /**
     * 变更原因
     */
    private String reason;

    /**
     * 发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /**
     * 排班日期快照（排班可能随后被删/改期，审计需保留发生时的事实）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 科室名称快照
     */
    private String deptName;

    /**
     * 排班人员姓名快照
     */
    private String doctorName;
}
