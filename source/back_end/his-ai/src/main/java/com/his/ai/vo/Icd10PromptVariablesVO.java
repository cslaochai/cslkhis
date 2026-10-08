package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * ICD-10 编码推荐提示词变量。
 */
@Data
public class Icd10PromptVariablesVO implements PromptVariables {

    /**
     * 候选编码条数
     */
    private String candidateCount;

    /**
     * 候选编码清单（编码 + 名称）
     */
    private String candidates;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 专科检查
     */
    private String specialistExam;

    /**
     * 诊断
     */
    private String diagnosis;
}