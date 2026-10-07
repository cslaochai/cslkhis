package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 门诊输液患者快照（{@code BizOutpInfusionMapper#selectPatientSnapshot}）。
 *
 * <p>服务端重查，<b>不信任前端传来的姓名/性别/年龄</b>：输液单是要打印出来贴在床头的，
 * 上面写着别人的姓名就是医疗事故，不是显示问题。
 */
@Data
public class InfusionPatientSnapshotVO implements Serializable {

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;
}