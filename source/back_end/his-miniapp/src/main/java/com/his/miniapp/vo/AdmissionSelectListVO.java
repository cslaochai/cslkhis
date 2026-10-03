package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 患者端住院记录行（押金页选择入院单用）。
 *
 * <p>出参键名逐字保持改造前跨模块取数的下划线形状（小程序端直接按键取值，改名即空白）。
 */
@Data
public class AdmissionSelectListVO implements Serializable {

    /** 入院ID（字符串化防 BIGINT 精度丢失） */
    @JsonProperty("admission_id")
    private String admissionId;

    /** 住院号 */
    @JsonProperty("admission_no")
    private String admissionNo;

    /** 患者ID（字符串化防 BIGINT 精度丢失） */
    @JsonProperty("patient_id")
    private String patientId;

    /** 科室ID */
    @JsonProperty("dept_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 床位ID */
    @JsonProperty("bed_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /** 入院时间 */
    @JsonProperty("admit_time")
    private LocalDateTime admitTime;

    /** 出院时间 */
    @JsonProperty("discharge_time")
    private LocalDateTime dischargeTime;

    /** 入院状态（0-已出院 1-在院） */
    @JsonProperty("admit_status")
    private Integer admitStatus;

    /** 入院诊断 */
    private String diagnosis;
}
