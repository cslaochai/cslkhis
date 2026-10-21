package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出院记录快照（跨模块裸 SQL：出院记录 + 入院记录 + 患者档案 + 科室）。
 */
@Data
public class DischargeSnapshotVO implements Serializable {

    /** 出院记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeId;

    /** 出院记录号 */
    private String dischargeNo;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 出院时间 */
    private LocalDateTime dischargeTime;

    /**
     * 出院诊断（出院记录没写就退回入院诊断）
     */
    private String diagnosis;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 手机号码 */
    private String phone;
}