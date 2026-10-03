package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 发布报告入参
 */
@Data
public class ReportPublishDTO {
    /**
     * 报告ID
     */
    private Long reportId;

    /**
     * 发布人（可选）
     */
    private String publishBy;
}
