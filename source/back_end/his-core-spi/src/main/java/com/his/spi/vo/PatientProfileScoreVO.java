package com.his.spi.vo;

import lombok.Data;

import java.util.List;

/**
 * 患者主档完整度评分（读侧契约）。
 */
@Data
public class PatientProfileScoreVO {

    /**
     * 完整度（0-100，已向下取整）
     */
    private int completeRate;

    /**
     * 缺失字段名清单
     */
    private List<String> missingFields;
}
