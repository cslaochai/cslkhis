package com.his.emr.vo;

import com.his.emr.support.PrevisitQuestionnaireSupport;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 预问诊量表出参（患者端拉题目，结构由后端 PrevisitQuestionnaireSupport 版本化）
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
    private List<PrevisitQuestionnaireSupport.Option> mainSymptoms;

    /**
     * 通用问
     */
    private List<PrevisitQuestionnaireSupport.Question> commonQuestions;

    /**
     * 主症状追问组（key=主症状 code）
     */
    private Map<String, List<PrevisitQuestionnaireSupport.Question>> symptomQuestions;
}
