package com.his.schedule.entity;

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
 * 护理排班审批与变更留痕（280）。只写不删：提交/审核发布/撤回/调班/改格/删格/整周流转/复制上周。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_log")
public class BizNurseScheduleLog extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除；审计只写不删，恒为 0） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 排班行ID（整周提交/发布/复制等无单行的动作可为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /** 动作（1-新增 2-修改/调班 3-删除 4-提交 5-审核发布 6-撤回 7-进入调班 8-整周提交 9-整周发布 10-复制上周） */
    private Integer actionType;

    /** 变更数量（批量铺/复制/整周发布条数） */
    private Integer amount;

    /** 变更/审批原因或意见 */
    private String reason;

    /** 发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /** 排班日期快照（排班被删/改后审计仍可读） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 排班单元名称快照（病区/门诊科室） */
    private String orgName;

    /** 科室名称快照 */
    private String deptName;

    /** 护士姓名快照 */
    private String nurseName;

    /** 变更前快照（班次/状态文本，调班/改格用） */
    private String fromSnapshot;

    /** 变更后快照（班次/状态文本） */
    private String toSnapshot;
}
