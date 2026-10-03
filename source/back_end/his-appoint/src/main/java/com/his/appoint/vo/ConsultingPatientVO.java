package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 就诊中患者VO
 */
@Data
public class ConsultingPatientVO {

    /**
     * 队列ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 排队序号
     */
    private String queueNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;
}
