package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 危急值站内信的业务上下文载荷（上报催办与超时升级两条消息共用）。
 */
@Data
public class CriticalValueMessagePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 危急值号
     */
    private String criticalNo;

    /**
     * 危急值描述（含阈值与临床意义）
     */
    private String criticalDesc;

    /**
     * 处置时限（分钟，仅上报催办那条填）
     */
    private Integer deadlineMinutes;

    /**
     * 是否升级催办（仅超时升级那条为 true）
     */
    private Boolean escalate;

    /**
     * 是否发给科室上级（true=催科主任，false=催本人/兜底接收人）
     */
    private Boolean toLeader;
}