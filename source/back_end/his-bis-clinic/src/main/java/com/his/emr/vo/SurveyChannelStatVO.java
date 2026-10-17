package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 满意度：按渠道的发放/回收（把「发出去永远收不回」的渠道显式暴露出来）。
 */
@Data
public class SurveyChannelStatVO implements Serializable {

    /**
     * 发放渠道
     */
    private Integer k;

    /**
     * 该渠道发放总数
     */
    private Long total;

    /**
     * 该渠道已回收数
     */
    private Long recycled;
}