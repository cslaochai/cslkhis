package com.his.ai.dto;

import lombok.Data;

/**
 * 随访话术模型输出
 */
@Data
public class FollowupComposeLlmOutputDTO {

    /** 随访话术草稿（60~150 字，无诊断无剂量） */
    private String content;
}
