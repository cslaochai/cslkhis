package com.his.ai.service;

import com.his.ai.dto.AiChatRequestDTO;
import com.his.ai.dto.LlmResultDTO;
import com.his.ai.exception.LlmException;

/**
 * 大模型接入层。
 */
public interface LlmClient {

    /**
     * 发起一次单轮调用。
     * <p>
     * 实现约定：
     * <ul>
     *   <li>单轮为主，不做自主循环 —— 循环由上层业务代码控制，见方案 §3.0</li>
     *   <li>失败与超时统一抛 {@link LlmException}，不返回 null</li>
     *   <li>不做降级判断，降级由调用方（AiExecutionService）负责</li>
     * </ul>
     *
     * @param request   请求
     * @param timeoutMs 本次调用的超时（毫秒），由能力级配置决定
     * @return 调用结果
     * @throws LlmException 调用失败或超时
     */
    LlmResultDTO complete(AiChatRequestDTO request, int timeoutMs);
}
