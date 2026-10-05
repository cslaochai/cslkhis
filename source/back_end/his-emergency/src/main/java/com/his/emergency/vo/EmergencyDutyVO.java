package com.his.emergency.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 急诊台登记时的「此刻在岗值班医生」出参。
 * <p>
 * 为什么不让登记表单继续用门诊的 {@code /schedule/availableList}：那个接口的口径是
 * 「还有号源可挂」，急诊值班排班的号源恒为 0（否则前台能给急诊科挂门诊号、挤掉急诊资源），
 * 于是下拉永远为空、页面写死「该科室今日无排班医生」，而后端派单却查得到人 —— 两边说的不是一件事。
 * 本接口与 {@code applyDispatch} 用同一条在岗判定，界面显示的人就是系统会派给的人。
 */
@Data
public class EmergencyDutyVO {

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 班次起始时间（"08:00" 这类原样透出，界面只用来标注在哪个班）
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
