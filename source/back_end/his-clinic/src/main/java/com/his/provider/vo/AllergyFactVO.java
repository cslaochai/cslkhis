package com.his.provider.vo;

import lombok.Data;

/**
 * 结构化过敏史（读侧契约）。
 */
@Data
public class AllergyFactVO {

    private Integer allergyType;

    private String allergenName;

    private Integer allergySeverity;
}
