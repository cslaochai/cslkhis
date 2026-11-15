package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 工作流节点/意图路由的提示词变量。
 * <p>字段名即占位符名：{{text}} {{channel}} {{options}} {{inputJson}}。
 */
@Data
public class FlowPromptVariablesVO implements PromptVariables {

    /** 用户原话（意图路由用） */
    private String text;

    /** 入口渠道 */
    private String channel;

    /** 候选流程清单，一行一条：编码 | 名称 | 说明（意图路由用） */
    private String options;

    /** 节点输入 JSON（自动节点用） */
    private String inputJson;
}
