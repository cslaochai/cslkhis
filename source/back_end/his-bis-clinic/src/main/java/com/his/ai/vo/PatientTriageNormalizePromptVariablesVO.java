package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 患者分诊描述归一提示词变量。
 */
@Data
public class PatientTriageNormalizePromptVariablesVO implements PromptVariables {

    /**
     * 患者原话描述
     */
    private String description;
}