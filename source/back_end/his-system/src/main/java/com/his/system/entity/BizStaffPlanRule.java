package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 人力配置标准（一个排班单元 × 一个班次 × 一个岗位类别，该配多少人）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_staff_plan_rule")
public class BizStaffPlanRule extends BaseEntity {

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 排班单元名称
     */
    private String orgName;

    /**
     * 标准班次ID（0-该单元全部班次）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 最低在岗人数
     */
    private Integer minStaff;

    /**
     * 最高在岗人数
     */
    private Integer maxStaff;

    /**
     * 单周工时上限
     */
    private BigDecimal maxWeekHours;

    /**
     * 连续夜班天数上限
     */
    private Integer maxConsecutiveNightDays;

    /**
     * 连续上班天数上限
     */
    private Integer maxConsecutiveWorkDays;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
