package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * EMPI 概览指标（P5.1）。
 */
@Data
public class PatientIndexStatVO implements Serializable {

    /**
     * 建档总量
     */
    private Long patientTotal;

    /**
     * 已并入主档的影子档案数
     */
    private Long mergedCount;

    /**
     * 强依据（证件相同）重复组数
     */
    private Long strongDupGroups;

    /**
     * 缺证件号码的档案数
     */
    private Long idCardMissing;

    /**
     * 缺联系电话的档案数
     */
    private Long phoneMissing;

    /**
     * 缺过敏史的档案数
     */
    private Long allergyMissing;

    /**
     * 唯一性 %
     */
    private Double uniqueRate;

    /**
     * 证件号码完整率 %
     */
    private Double idCardCompleteRate;

    /**
     * 联系电话完整率 %
     */
    private Double phoneCompleteRate;

    /**
     * 过敏史完整率 %
     */
    private Double allergyCompleteRate;

    /**
     * 合并动作累计次数
     */
    private Long mergeActions;
}
