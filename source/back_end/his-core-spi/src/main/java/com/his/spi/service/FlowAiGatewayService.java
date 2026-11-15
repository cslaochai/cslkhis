package com.his.spi.service;

import com.his.spi.dto.FlowAiNodeRequestDTO;
import com.his.spi.dto.FlowIntentRouteRequestDTO;
import com.his.spi.vo.FlowAiNodeResultVO;
import com.his.spi.vo.FlowIntentRouteResultVO;

/**
 * 工作流 → AI 的端口：AI 节点执行与意图路由。
 * <p>实现方在 his-ms-ai，工作流模块只依赖本接口，不反向依赖 AI 模块。
 */
public interface FlowAiGatewayService {

    /**
     * 执行一个 AI 节点（提示词模板 + 变量 → 结构化 JSON）。
     */
    FlowAiNodeResultVO executeNode(FlowAiNodeRequestDTO request);

    /**
     * 意图路由：从候选流程中选一条。
     */
    FlowIntentRouteResultVO routeIntent(FlowIntentRouteRequestDTO request);
}
