package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 人力缺口出参（需求 − 在岗）。
 */
@Data
public class StaffDemandGapVO {

    /** 缺口测算日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate demandDate;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /** 排班单元类型文案（由枚举 label 渲染） */
    private String orgTypeText;

    /** 排班单元ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称 */
    private String orgName;

    /**
     * 岗位类别（1-医生 2-护理 …）
     */
    private Integer staffType;

    /** 岗位类别文案（由枚举 label 渲染） */
    private String staffTypeText;

    /**
     * 需求人数
     */
    private Integer requiredCount;

    /**
     * 已排在岗人数（只数出勤状态=上班的人）
     */
    private Integer scheduledCount;

    /**
     * 缺口 = 需求 − 在岗（负数=富余）
     */
    private Integer gapCount;

    /**
     * 来源（1-门诊出诊派生 2-住院患者派生 3-手工调整）
     */
    private Integer demandSource;

    /** 需求来源文案（由枚举 label 渲染） */
    private String demandSourceText;

    /**
     * 测算依据（人话）
     */
    private String calcBasis;

    private String unitLabel() {
        return (orgType != null && orgType == 2 ? "病区" : "科室") + " · " + orgName;
    }
}
