package com.his.patient.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * 会诊邀请通知载荷（对应 InpatientConsultationServiceImpl 的会诊邀请）。
 */
@Data
public class InpatientConsultInvitePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 会诊单号
     */
    private String consultationNo;

    /**
     * 会诊科室名称
     */
    private String toDeptName;

    /**
     * 会诊理由
     */
    private String reason;

    /**
     * 是否急会诊（对外键名 isUrgent）
     */
    @Alias("isUrgent")
    private Boolean urgent;
}
