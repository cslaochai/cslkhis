package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者挂号次数原始计数行（对应 {@code BizPatientMapper.countRegistByPatientIds}）。
 *
 * <p>只供服务层回填"某患者挂过几次号"，从不单独出参；从未挂过号的患者不出现在结果里。
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
