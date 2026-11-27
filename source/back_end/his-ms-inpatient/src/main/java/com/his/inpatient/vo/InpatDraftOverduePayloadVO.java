package com.his.inpatient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 住院文书草稿超时催办消息的业务上下文载荷（消息中心点开提醒时渲染摘要 chip）。
 */
@Data
public class InpatDraftOverduePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 文书号
     */
    private String recordNo;

    /**
     * 文书类型文案
     */
    private String recordTypeText;

    /**
     * 已超时时长（小时）
     */
    private Long overdueHours;
}
