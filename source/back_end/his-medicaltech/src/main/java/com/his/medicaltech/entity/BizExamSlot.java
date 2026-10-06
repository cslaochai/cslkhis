package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 检查设备号源时段（检查设备号源时段）—— 半小时（或设备自定义粒度）一档的格子。
 *
 * <p>网格锚在设备自身的开放开始时刻上（MR 机房 08:30 开放，第一格就是 08:30-09:00），
 * 不是墙上时钟的整点/半点 —— 否则 08:00-08:30 这段没开放的窗口也会长出号源。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_slot")
public class BizExamSlot extends BaseEntity {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 号源日期
     */
    private LocalDate slotDate;

    /**
     * 段序（1 起，按 start_time 升序，跨上下午连续编号）
     */
    private Integer seq;

    /**
     * 段开始时间（HH:mm）
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
     * 已占号数：事实是检查预约单的区间覆盖，本列冗余维护（/slotRecalc 对账）
     */
    private Integer usedSource;

    /**
     * 段剩余号源
     */
    private Integer availableSource;

    /**
     * 段状态（0-停用锁号 1-正常）
     */
    private Integer status;
}
