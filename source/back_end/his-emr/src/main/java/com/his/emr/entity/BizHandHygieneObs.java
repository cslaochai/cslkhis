package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 手卫生依从性观察记录（只增不改）：一次观察一行，聚合统计走后端。
 * 依从率 = SUM(comply_count) / SUM(opportunity_count)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hand_hygiene_obs")
public class BizHandHygieneObs extends BaseEntity {

    /**
     * 观察日期
     */
    private LocalDate obsDate;

    /**
     * 被观察科室ID
     */
    private Long deptId;

    /**
     * 被观察科室（快照）
     */
    private String deptName;

    /**
     * 观察对象（1医生/2护士/3工勤其他）
     */
    private Integer obsObject;

    /**
     * 手卫生时机数
     */
    private Integer opportunityCount;

    /**
     * 实际执行数（≤时机数）
     */
    private Integer complyCount;

    /**
     * 观察人ID
     */
    private Long observerId;

    /**
     * 观察人姓名（快照）
     */
    private String observerName;

    /**
     * 观察登记时间
     */
    private LocalDateTime obsTime;
}
