package com.his.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 智能导诊
 */
@Data
public class TriageRecommendDTO {

    /**
     * 主诉描述
     */
    @NotBlank(message = "请描述您的不适")
    private String description;
}
