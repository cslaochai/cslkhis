package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 护理交接班提示词变量。
 */
@Data
public class NursingHandoverPromptVariablesVO implements PromptVariables {

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 班次说明（如「白班 08:00-20:00」）
     */
    private String shiftText;

    /**
     * 本班次时间窗说明
     */
    private String windowText;

    /**
     * 在院人数/出院/新入院/异常事件/风险评估的事实清单
     */
    private String factsText;
}