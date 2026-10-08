package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 急诊台登记时的「此刻在岗值班医生」出参。
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
