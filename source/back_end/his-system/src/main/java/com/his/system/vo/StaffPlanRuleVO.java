package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人力配置标准出参。
 */
@Data
public class StaffPlanRuleVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Integer orgType;
    private String orgTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    /**
     * 标准班次ID（0=该单元全部班次共用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    private String shiftName;

    private Integer staffType;
    private String staffTypeName;

    private Integer minStaff;
    private Integer maxStaff;
    private BigDecimal maxWeekHours;
    private Integer maxConsecutiveNightDays;
    private Integer maxConsecutiveWorkDays;
    private Integer status;
    private String remark;
}
