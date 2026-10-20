package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 随访话术生成提示词变量。
 */
@Data
public class FollowupComposePromptVariablesVO implements PromptVariables {

    /**
     * 随访类型名（模板类型，不含医学内容）
     */
    private String followupTypeName;

    /**
     * 病种上下文（诊断 + 在管慢病病名，空时为「暂无慢病档案与诊断信息」）
     */
    private String diseaseContext;
}