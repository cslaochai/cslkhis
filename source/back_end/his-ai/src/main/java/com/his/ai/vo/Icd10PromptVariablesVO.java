package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * ICD-10 编码推荐提示词变量。
 *
 * <p>对应 {@code prompts/icd10-predict.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code candidates} + {@code candidateCount} 是「白名单 + 计数」组合：
 * 清单限定模型只能从候选里选，计数让它知道清单长度、不要重复输出同一条。
 * 模型给的编码不在候选集内会被丢弃（见 Icd10CapabilityImpl.toItems）。
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