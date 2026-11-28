package com.his.inpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 住院腕带打印明细（入科后佩戴的患者身份腕带，版式写死 250x30mm）
 */
@Data
public class WristbandVO implements Serializable {

    /**
     * 入院ID（即 biz_admission.admission_id，打印台账的 bizId）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号（腕带条码码值）
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别：1-男 2-女 9-未知
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 血型（A/B/O/AB）
     */
    private String bloodType;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 住院状态：0-已出院 1-在院
     */
    private Integer admitStatus;

    /**
     * 入院时间
     */
    private String admitTime;

    /**
     * 已打印次数（0 = 从未打印）
     */
    private Integer printCount;

    /**
     * 渲染器标识（版式写死在代码里）
     */
    private String renderCode;

    /**
     * 纸张规格
     */
    private String paperSpec;
}
