package com.his.ai.dto;

import lombok.Data;

/**
 * 预问诊摘要模型输出
 */
@Data
public class PrevisitSummaryLlmOutputDTO {

    /** 病史摘要（≤200 字，只陈述患者自述内容） */
    private String summary;
}
