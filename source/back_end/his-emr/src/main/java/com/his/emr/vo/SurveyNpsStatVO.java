package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 满意度：NPS 分档（推荐者 9-10 / 中立 7-8 / 贬损者 0-6，只认已打分的有效卷）。
 */
@Data
public class SurveyNpsStatVO implements Serializable {

    /**
     * 已打分卷数（NPS 的分母）
     */
    private Long rated;

    /**
     * 推荐者数
     */
    private Long promoter;

    /**
     * 中立数
     */
    private Long passive;

    /**
     * 贬损者数
     */
    private Long detractor;
}