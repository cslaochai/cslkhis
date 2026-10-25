package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 护理排班模板明细：模板里一行 = 一个护士在某星期几的出勤（上班+班次 / 休息 / 请假 / 培训 / 停班）。
 * 套用模板时按 weekDay 落到目标周对应日期。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_template_item")
public class BizNurseScheduleTemplateItem extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除；手动维护，删除走置位更新） */
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 所属模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 护士ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 护士姓名（快照，报错点名用） */
    private String employeeName;

    /** 星期几（1-周一 … 7-周日） */
    private Integer weekDay;

    /** 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
    private Integer scheduleStatus;

    /** 班次ID（仅状态=1-上班时有效；非上班由服务端强制置空，不接受前端残留值） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;
}
