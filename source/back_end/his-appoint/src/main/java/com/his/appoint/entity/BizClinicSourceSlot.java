package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.appoint.mapper.BizClinicSourceSlotMapper;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 排班时间片段实体（半小时一档）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_clinic_source_slot")
public class BizClinicSourceSlot extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 所属排班ID（排班信息的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 段序（1起，按 start_time 升序）
     */
    private Integer seq;

    /**
     * 段开始 HH:mm（半小时一档）
     */
    private String startTime;

    /**
     * 段结束 HH:mm（不足半小时的尾段取班次结束时刻）
     */
    private String endTime;

    /**
     * 段号源总数
     */
    private Integer totalSource;

    /**
     * 段已挂号数（现场+线上）
     */
    private Integer usedSource;

    /**
     * 段剩余号源（total-used，冗余维护）
     */
    private Integer availableSource;

    /**
     * 段累计加号数（加号即追加段号源）
     */
    private Integer addedSource;

    /**
     * 段内线上预约预留（0=未划池，现场可占全部剩余）
     */
    private Integer appointmentSource;

    /**
     * 段内线上预约已用
     */
    private Integer usedAppointmentSource;

    /**
     * 段状态（0-停用 1-正常）
     */
    private Integer status;
}
