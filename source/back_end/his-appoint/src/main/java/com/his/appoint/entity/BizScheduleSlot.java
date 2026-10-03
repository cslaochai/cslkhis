package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.appoint.mapper.BizScheduleSlotMapper;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排班时间片段实体（半小时一档）。
 *
 * <p>时间片段化改造（2026-09-21）后，号源与占用的事实都在段上：
 * 现场扣段（不许吃段内预约池剩余）、线上从段池内扣、退号按段释放；
 * 排班信息上的 6 个号源字段退化为 Σ段汇总冗余，写路径同事务双写。
 *
 * <p>uk_slot(schedule_id, start_time) 不含 del_flag（铁律：唯一索引不含 del_flag），
 * 段的删除一律物理删（{@link BizScheduleSlotMapper#physicalDeleteByScheduleId}）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_schedule_slot")
public class BizScheduleSlot extends BaseEntity {

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
