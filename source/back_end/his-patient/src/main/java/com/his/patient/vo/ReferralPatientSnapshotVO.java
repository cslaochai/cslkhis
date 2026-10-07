package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 转诊单患者快照（对应 {@code BizReferralMapper.selectPatientSnapshot}）。
 *
 * <p>转诊单要带出患者号/姓名/电话；admissionId 非空时再带住院号与诊断。
 * 全部列都属本域（患者基本信息 / 入院记录），但只取这 5 列、不开实体，
 * 避免把整张 biz_patient / biz_admission 挂到出参上。
 */
@Data
public class ReferralPatientSnapshotVO implements Serializable {

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 住院号（未关联入院记录时为空）
     */
    private String admissionNo;

    /**
     * 入院诊断（未关联入院记录时为空）
     */
    private String diagnosis;
}
