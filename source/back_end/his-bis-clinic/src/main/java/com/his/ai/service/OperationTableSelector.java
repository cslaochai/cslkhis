package com.his.ai.service;

/**
 * 运营问数「按需加载」选表器。
 *
 * <p>把「问题涉及哪些数据表」从生成段拆出来，当前实现为语义命中 + 模型兜底
 * （{@code SemanticOperationTableSelectorImpl}）；未来意图路由、Agent 工作流接入时
 * 换一个实现即可，生成段不需要感知路由方式。
 */
public interface OperationTableSelector {

    /**
     * 根据问题选出相关表集合与命中语义。
     *
     * @return 选表结果；tables 为空表示未选出任何表（由调用方兜底全量）
     */
    OperationTableSelection select(String question);
}
