package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出院记录快照（跨模块裸 SQL：出院记录 + 入院记录 + 患者档案 + 科室）。
 *
 * <p>随访问卷要用它建单，所以出院时间给 {@link LocalDateTime} 而不是文本：
 * 早先为了绕开「裸 Map 取 DATETIME 拿到日期对象强转炸掉」统一 DATE_FORMAT 成字符串，
 * 代价是每个消费方都要再 parse 一次；换成有类型的类之后 MyBatis 自己会映射。
 */
@Data
public class DischargeSnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeId;

    private String dischargeNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private LocalDateTime dischargeTime;

    /**
     * 出院诊断（出院记录没写就退回入院诊断）
     */
    private String diagnosis;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    private String patientNo;

    private String patientName;

    private String phone;
}