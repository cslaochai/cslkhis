package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 检验结果解读提示词变量。
 */
@Data
public class LabInterpretPromptVariablesVO implements PromptVariables {

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄（含「岁」后缀，未填写时为「（未填写）」）
     */
    private String age;

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 全部结果项
     */
    private String results;

    /**
     * 异常结果项
     */
    private String abnormalResults;

    /**
     * 未判定结果项（无参考区间或区间无法匹配）
     */
    private String unjudgedResults;

    /**
     * 危急值项
     */
    private String criticalResults;

    /**
     * 历史趋势
     */
    private String trends;
}