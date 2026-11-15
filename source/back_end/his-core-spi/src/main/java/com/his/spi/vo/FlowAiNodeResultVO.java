package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 节点执行结果。
 */
@Data
public class FlowAiNodeResultVO implements Serializable {

    /**
     * 是否拿到模型结果（false 时调用方按节点降级策略处理）
     */
    private boolean succeeded;

    /**
     * 模型输出的 JSON 文本，原样带回；失败时为空
     */
    private String payload;

    /**
     * 是否降级（能力开关关闭/熔断/解析失败）
     */
    private boolean degraded;

    /**
     * 降级原因，直接回显给流程实例变量
     */
    private String degradeReason;
}
