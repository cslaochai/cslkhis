package com.his.spi.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 预问诊量表出参（患者端拉题目，题目结构由 clinical 域 PrevisitQuestionnaireSupport 版本化）
 */
@Data
public class PrevisitQuestionnaireVO {

    /**
     * 量表版本
     */
    private String version;

    /**
     * 主症状清单
     */
    private List<Option> mainSymptoms;

    /**
     * 通用问
     */
    private List<Question> commonQuestions;

    /**
     * 主症状追问组（key=主症状 code）
     */
    private Map<String, List<Question>> symptomQuestions;

    /**
     * 选项
     */
    public record Option(String code, String label) {
    }

    /**
     * 问题
     */
    public record Question(String key, String label, String type, List<String> options) {
    }
}
