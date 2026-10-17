package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者挂号次数原始计数行（对应 BizPatientMapper.countRegistByPatientIds）。
 */
@Data
public class PatientRegistCountVO implements Serializable {

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 挂号（预约）次数
     */
    private Long cnt;
}
