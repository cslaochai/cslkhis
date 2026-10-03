package com.his.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 智能导诊入参：患者自己描述的主诉原文。
 */
@Data
public class TriageRecommendDTO {

    /** 主诉描述 */
    @NotBlank(message = "请描述您的不适")
    private String description;
}
