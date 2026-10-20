package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 检查预约站内信的业务上下文载荷（消息中心点开提醒时按这些字段渲染摘要 chip）。
 */
@Data
public class ExamApptMessagePayloadVO implements Serializable {

    /**
     * 预约单号
     */
    private String apptNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 设备名称
     */
    private String deviceName;
}