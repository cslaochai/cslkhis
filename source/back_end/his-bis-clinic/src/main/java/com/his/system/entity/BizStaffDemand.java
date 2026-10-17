package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 人力需求（某单元 · 某天 · 某岗位类别 · 需要多少人）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_staff_demand")
public class BizStaffDemand extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
     * 排班单元名称
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
