package com.his.spi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 意图路由的候选流程。
 */
@Data
public class FlowIntentOptionDTO implements Serializable {

    private String flowCode;

    private String flowName;

    /**
     * 该流程负责什么，供模型判断（写清楚边界，别写成流程名同义词）
     */
    private String description;
}
