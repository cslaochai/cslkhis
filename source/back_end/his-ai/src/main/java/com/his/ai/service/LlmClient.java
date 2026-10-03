package com.his.ai.service;

import com.his.ai.dto.AiChatRequestDTO;
import com.his.ai.dto.LlmResultDTO;
import com.his.ai.support.LlmException;

/**
 * 大模型接入层。
 * <p>
 * 抽成接口的目的：把「用不用 Spring AI / 换不换厂商」这个决策隔离在实现类里。
 * 将来引入 Spring AI 时，只需新增一个实现类替换当前的
 * {@link RestClientLlmClient}，业务代码（各 capability）零改动。
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
