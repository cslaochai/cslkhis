package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排班模板时间片段实体（模板下的半小时段配额）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_schedule_slot_template")
public class BizScheduleSlotTemplate extends BaseEntity {

    /**
     * 排班模板ID（排班周模板的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 段序（1起，按 start_time 升序）
     */
    private Integer seq;

    /**
     * 段开始 HH:mm（半小时一档）
     */
    private String startTime;

    /**
     * 段结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 段号源总数
     */
    private Integer totalSource;

    /**
     * 段内线上预约预留（0=未划池）
     */
    private Integer appointmentSource;
}
