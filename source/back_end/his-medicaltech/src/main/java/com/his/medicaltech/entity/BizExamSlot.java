package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检查设备号源时段（检查设备号源时段）—— 半小时（或设备自定义粒度）一档的格子。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_slot")
public class BizExamSlot extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
