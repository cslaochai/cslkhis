package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 转诊单患者快照（对应 BizReferralMapper.selectPatientSnapshot）。
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
