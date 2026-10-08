package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 急诊分诊建议能力的模型输出结构（与 prompts/emergency-triage.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmergencyTriageLlmOutputDTO {

    /**
     * 建议分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    private Integer suggestLevel;

    /**
     * 建议区域（红区/黄区/绿区）
     */
    private String suggestZone;

    /**
     * 建议绿色通道（胸痛中心/卒中中心/创伤中心/无）
     */
    private String suggestGreenChannel;

    /**
     * 判断依据
     */
    private String reasoning;

    /**
     * 识别到的红旗征象
     */
    private List<String> redFlags = new ArrayList<>();

    /**
     * 建议立即采取的措施
     */
    private List<String> recommendActions = new ArrayList<>();
}
