package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 人力缺口出参（需求 − 在岗）。
 *
 * <p>三件事必须一起给前端：需求多少、已排多少、差多少。只给缺口数字，排班员看到的
 * 「缺 3 人」无法判断是「一个人都没排」还是「排了但还差」—— 那他就不知道该补几个人。
 *
 * <p>{@code calcBasis} 必须原样带到界面上：需求是算出来的，不是拍出来的，
 * 护士长第一反应一定是「凭什么说我缺 3 个」，这句话就是给他看的答案。
 */
@Data
public class StaffDemandGapVO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate demandDate;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    private String orgTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    /**
     * 岗位类别（1-医生 2-护理 …）
     */
    private Integer staffType;

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

    private String demandSourceText;

    /**
     * 测算依据（人话）
     */
    private String calcBasis;

    private String unitLabel() {
        return (orgType != null && orgType == 2 ? "病区" : "科室") + " · " + orgName;
    }
}
