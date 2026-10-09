package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 目标性监测每日打卡（导管日留痕）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_infection_monitor_daily")
public class BizInfectionMonitorDaily extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 监测登记ID（院感目标性监测登记）
     */
    private Long monitorId;

    /**
     * 监测日期
     */
    private LocalDate monitorDate;

    /**
     * 记录人ID
     */
    private Long recorderId;

    /**
     * 记录人姓名
     */
    private String recorderName;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;
}
