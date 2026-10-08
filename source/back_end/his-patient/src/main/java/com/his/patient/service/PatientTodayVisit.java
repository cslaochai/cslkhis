package com.his.patient.service;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者「今日就诊」概览。
 */
@Data
public class PatientTodayVisit implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long patientId;

    /**
     * 2-候诊中 3-就诊中 4-已就诊（与 {@code QueueStatusEnum} 同码）
     */
    private Integer status;

    /**
     * 状态文案，由提供方渲染（前端不再自己造映射）
     */
    private String statusText;

    /**
     * 今日就诊科室名
     */
    private String deptName;

    /**
     * 今日接诊医生名
     */
    private String doctorName;

    /**
     * 门诊序号，如「3 号」
     */
    private String queueNo;

    /**
     * 是否属于当前登录用户本人/本科室的业务（决定是否置顶）
     */
    private Boolean mine;
}
