package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 主索引「档案关联业务数据量」原始计数行（对应 PatientIndexMapper.countDataByPatientIds 的
 */
@Data
public class PatientDataCountVO implements Serializable {

    /**
     * 数据来源表标识（见 PatientDataTables 的 key）
     */
    private String dataTable;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 该患者在该表下的数据条数
     */
    private Long cnt;
}
