package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 排班模板时间片段实体（模板下的半小时段配额）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_clinic_source_slot_template")
public class BizClinicSourceSlotTemplate extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
