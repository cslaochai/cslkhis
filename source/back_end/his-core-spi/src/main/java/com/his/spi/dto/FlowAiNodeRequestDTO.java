package com.his.spi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作流 AI 节点的一次调用请求（工作流模块 → AI 模块的唯一入口）。
 */
@Data
public class FlowAiNodeRequestDTO implements Serializable {

    /**
     * 能力标识，对应 AI 模块的能力开关与审计
     */
    private String capabilityKey;

    /**
     * 提示词模板编码（sys_ai_prompt_template.template_code）
     */
    private String templateName;

    /**
     * 业务类型，仅用于审计检索
     */
    private String bizType;

    /**
     * 业务ID，仅用于审计检索
     */
    private Long bizId;

    /**
     * 节点输入，JSON 文本；作为提示词变量 inputJson 传入
     */
    private String inputJson;
}
