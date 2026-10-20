package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 催办消息的业务上下文载荷（消息中心点开提醒时按这些字段渲染摘要 chip）。
 */
@Data
public class MessagePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 病案号（病历归档/病案借阅催办用）
     */
    private String recordNo;

    /**
     * 处方号（审方结果通知用）
     */
    private String prescriptionNo;

    /**
     * 质控单号（病历质控问题通知用）
     */
    private String qcNo;

    /**
     * 审方意见（处方退回通知用）
     */
    private String opinion;

    /**
     * 就诊日（归档催办用）
     */
    private String visitDate;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 条数（通用计数）
     */
    private Long count;

    /**
     * 问题数（质控通知用）
     */
    private Integer issueCount;

    /**
     * 等级/评级（质控通知用）
     */
    private String grade;

    /**
     * 超期天数（归档/借阅催办用；跨月差值可能为负，所以用 Long 不用 int）
     */
    private Long overdueDays;

    /**
     * 处理人姓名（审核人/编码员/催办责任人）
     */
    private String handlerName;
}