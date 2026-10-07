package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院记录行（患者端押金页选入院单用），对应 {@code MiniappAdmissionMapper#selectByPatientId}。
 *
 * <p>出参键名逐字保持改造前的下划线形状（小程序端直接按键取值，改名即空白）。
 * {@code admit_time}/{@code discharge_time} 是 DATETIME 列，MyBatis 直接映射成
 * {@code LocalDateTime}，改造前为绕开裸 Map 取值才在 SQL 里做过类型收敛。
 */
@Data
public class AdmissionRowVO implements Serializable {

    /**
     * 入院ID（SQL 已 CAST 成字符串，防 BIGINT 精度丢失）
     */
    @JsonProperty("admission_id")
    private String admissionId;

    /**
     * 住院号
     */
    @JsonProperty("admission_no")
    private String admissionNo;

    /**
     * 患者ID（SQL 已 CAST 成字符串，防 BIGINT 精度丢失）
     */
    @JsonProperty("patient_id")
    private String patientId;

    /**
     * 科室ID
     */
    @JsonProperty("dept_id")
    private Long deptId;

    /**
     * 床位ID
     */
    @JsonProperty("bed_id")
    private Long bedId;

    /**
     * 入院时间
     */
    @JsonProperty("admit_time")
    private LocalDateTime admitTime;

    /**
     * 出院时间
     */
    @JsonProperty("discharge_time")
    private LocalDateTime dischargeTime;

    /**
     * 入院状态（0-已出院 1-在院）
     */
    @JsonProperty("admit_status")
    private Integer admitStatus;

    /**
     * 入院诊断
     */
    @JsonProperty("diagnosis")
    private String diagnosis;
}
