package com.his.schedule.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人力配置标准出参。
 */
@Data
public class StaffPlanRuleVO {

    /** 人力配置标准ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;
    /** 排班单元类型文案（由枚举 label 渲染） */
    private String orgTypeText;

    /** 排班单元ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称 */
    private String orgName;

    /**
     * 标准班次ID（0=该单元全部班次共用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 标准班次名称 */
    private String shiftName;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工） */
    private Integer staffType;
    /** 岗位类别文案（由枚举 label 渲染） */
    private String staffTypeName;

    /** 最低在岗人数 */
    private Integer minStaff;
    /** 最高在岗人数 */
    private Integer maxStaff;
    /** 单周工时上限 */
    private BigDecimal maxWeekHours;
    /** 连续夜班天数上限 */
    private Integer maxConsecutiveNightDays;
    /** 连续上班天数上限 */
    private Integer maxConsecutiveWorkDays;
    /** 状态（0-停用 1-启用） */
    private Integer status;
    /** 备注 */
    private String remark;
}
