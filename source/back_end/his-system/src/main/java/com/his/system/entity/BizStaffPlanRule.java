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
 *
 * <p>原先只有病区护理有这套标准（最低人数、周工时上限、连续夜班上限），
 * 门诊医生侧根本没有「这个科上午至少几个医生」的表达，于是停诊/请假把人数减到 0 也不会有任何提示。
 * 泛化到全院后，单元可以是科室、病区或全院，班次传 0 表示该单元全部班次共用一条标准。
 *
 * <p><b>最低与上限的处置不同</b>：低于最低在岗是「今天这个班没人接」，属于必须拦住的事实；
 * 高于上限是「人力富余」，提示即可，不该让排班员保存不了。
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
     * 排班单元名称（快照）
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
