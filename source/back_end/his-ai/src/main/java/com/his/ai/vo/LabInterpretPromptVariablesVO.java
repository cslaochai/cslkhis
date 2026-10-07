package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 检验结果解读提示词变量。
 *
 * <p>对应 {@code prompts/lab-interpret.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>把结果按「正常 / 异常 / 危急 / 未判定」四段切开摆给模型：
 * 异常判定本身由 {@code LabAbnormalJudge} 用参考区间算完（事实层代码算），
 * 模型只负责把已判定的异常讲成人话。
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