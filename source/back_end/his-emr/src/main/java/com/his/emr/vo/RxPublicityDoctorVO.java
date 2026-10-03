package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/** 公示页医师排名 VO（只统计已公示的不合理处方） */
@Data
public class RxPublicityDoctorVO {

    /** 医生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 医生姓名 */
    private String doctorName;

    /** 科室名称 */
    private String deptName;

    /** 被公示不合理处方数 */
    private Long reviewCount;

    /** 超常处方数（规范：≥3 次且无正当理由应警告并限制处方权） */
    private Long abnormalCount;

    /** 最近公示时间 */
    private LocalDateTime lastPublicityTime;

    /** 是否应约谈（超常 ≥3 次，后端算好前端直接标红） */
    private Boolean needTalk;
}
