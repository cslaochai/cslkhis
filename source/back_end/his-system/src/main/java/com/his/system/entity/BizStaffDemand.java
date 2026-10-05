package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 人力需求（某单元 · 某天 · 某岗位类别 · 需要多少人）。
 *
 * <p><b>为什么必须单独一张表</b>：排班原先只有「排了什么」，没有「需要什么」。于是
 * 「在岗 5 人」这句话是没有意义的 —— 需求 6 人时它叫缺 1 人，需求 4 人时它叫富余 1 人。
 * 本表就是那个分母：门诊侧由出诊计划派生，住院侧由在院患者 × 护理等级派生。
 *
 * <p><b>需求 = MAX(业务派生, 该单元核定的下限, 1)</b>：病区护理不是纯按患者数配的。
 * 一个病区哪怕只剩 1 个患者，白班/前夜/后夜三班也得有人顶，这是最低运营配置，
 * 与患者数无关。只按患者工时算会得到「4 个患者 → 1 个人」，而实际在岗 6 人、核定下限 5 人，
 * 那需求层就是在骗人。下限取自 {@code biz_staff_plan_rule.min_staff} —— 需求层和
 * 人力标准在这一刻才真的咬在一起。
 *
 * <p>三种来源（{@link #demandSource}）：1-门诊出诊派生、2-住院患者派生、3-手工调整。
 * 前两种每次重算都会覆盖，<b>第三种不会被派生覆盖</b> —— 系统算的永远不能盖掉人拍板的数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_staff_demand")
public class BizStaffDemand extends BaseEntity {

    /**
     * 需求日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate demandDate;

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
     * 时段（0-全天 1-上午 2-下午 3-夜间）
     */
    private Integer periodCode;

    /**
     * 班次ID（0-不限班次）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）
     */
    private Integer staffType;

    /**
     * 需求人数
     */
    private Integer requiredCount;

    /**
     * 能级下限（0-不限；依赖能级档案 G-01，未做前一律空）
     */
    private Integer requiredLevel;

    /**
     * 来源（1-门诊出诊派生 2-住院患者派生 3-手工调整）
     */
    private Integer demandSource;

    /**
     * 来源业务ID（出诊计划ID/病区ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceBizId;

    /**
     * 测算依据：怎么算出来的，写给人看的一句话
     */
    private String calcBasis;

    /**
     * 状态（0-停用 1-生效）
     */
    private Integer status;
}
