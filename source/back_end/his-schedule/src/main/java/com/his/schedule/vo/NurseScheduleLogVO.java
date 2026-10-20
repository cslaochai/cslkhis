package com.his.schedule.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 护理排班审批与变更留痕出参（280）。
 */
@Data
public class NurseScheduleLogVO {

    /** 留痕记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 排班行ID（整周/复制类可为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /** 排班日期快照 */
    private String scheduleDate;

    /** 排班单元名称快照（病区/门诊科室） */
    private String orgName;

    /** 科室名称快照 */
    private String deptName;

    /** 护士姓名快照 */
    private String nurseName;

    /** 动作码值 */
    private Integer actionType;

    /** 动作文案（枚举 label 渲染） */
    private String actionTypeText;

    /** 变更数量（批量/整周条数） */
    private Integer amount;

    /** 变更/审批原因或意见 */
    private String reason;

    /** 变更前快照（班次/状态文本） */
    private String fromSnapshot;

    /** 变更后快照（班次/状态文本） */
    private String toSnapshot;

    /** 发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /** 操作人 */
    private String createBy;
}
