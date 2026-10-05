package com.his.ai.service.impl;

import com.his.ai.service.AiExecutionService;
import com.his.ai.service.AiAuditService;
import com.his.ai.service.LlmClient;
import com.his.ai.config.AiConfigProvider;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.AiChatRequestDTO;
import com.his.ai.dto.AiMessageDTO;
import com.his.ai.dto.LlmResultDTO;
import com.his.ai.entity.SysAiCallLog;
import com.his.ai.support.*;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI 能力统一执行器 —— 全系统所有模型调用的<b>唯一入口</b>。
 * <p>
 * 把「工程纪律」集中在这一个类里，而不是散落到每个能力实现中：
 * <ol>
 *   <li>能力开关 → 熔断 → 渲染提示词 → 调用模型 → 解析 JSON → 自修正重试 → 审计 → 降级</li>
 *   <li>任何异常都不向外抛，统一返回 {@link Optional#empty()} 表示「已降级」</li>
 *   <li><b>不决定降级后的业务行为</b> —— 那属于各能力的业务语义。例如 ICD 降级后
 *       要回落到关键词规则，而处方审核降级后是「放行」（绝不能因为 AI 不可用拦住开方）</li>
 * </ol>
 * 调用方只需关心「拿到结果」和「没拿到结果」两种情况。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiExecutionServiceImpl implements AiExecutionService {

    private static final String SELF_CORRECT_HINT =
            "上一次输出无法解析为 JSON：%s。请只输出合法 JSON 对象，不要任何解释文字，不要用 markdown 代码块包裹。";

    private static final String OPERATOR_FALLBACK = "system";

    private final AiConfigProvider configProvider;

    private final PromptTemplate promptTemplate;

    private final LlmClient llmClient;

    private final StructuredOutputParser outputParser;

    private final AiDegradeGuard degradeGuard;

    private final AiAuditService auditService;

    /**
     * 执行一次 AI 能力调用。
     *
     * @param call       调用描述
     * @param resultType 期望的结构化输出类型
     * @return 解析成功的结果；任何环节失败均返回 {@link Optional#empty()}
     */
    public <T> Optional<T> call(AiCallDTO call, Class<T> resultType) {
        String capabilityKey = call.getCapabilityKey();
        String operator = currentOperator();

        if (!configProvider.isCapabilityEnabled(capabilityKey)) {
            record(call, operator, null, AiCallStatus.DEGRADED, 0,
                    configProvider.capabilityDisabledReason(capabilityKey));
            degradeGuard.recordDegrade(capabilityKey);
            return Optional.empty();
        }

        if (!degradeGuard.isAvailable(capabilityKey)) {
            record(call, operator, null, AiCallStatus.CIRCUIT_OPEN, 0, "熔断中，本次跳过调用");
            degradeGuard.recordDegrade(capabilityKey);
            return Optional.empty();
        }

        RenderedPrompt prompt;
        try {
            prompt = promptTemplate.render(call.getTemplateName(), call.getVariables());
        } catch (Exception ex) {
            degradeGuard.recordFailure(capabilityKey);
            degradeGuard.recordDegrade(capabilityKey);
            record(call, operator, null, AiCallStatus.FAILED, 0, "提示词渲染失败：" + ex.getMessage());
            log.error("[AI] {} 提示词渲染失败", capabilityKey, ex);
            return Optional.empty();
        }

        List<AiMessageDTO> messages = new ArrayList<>();
        messages.add(AiMessageDTO.system(prompt.getSystemText()));
        messages.add(AiMessageDTO.user(prompt.getUserText()));

        int timeoutMs = configProvider.timeoutOf(capabilityKey);
        String model = configProvider.modelOf(capabilityKey, call.isUseLiteModel());
        long start = System.currentTimeMillis();

        try {
            LlmResultDTO result = llmClient.complete(buildRequest(call, model, messages), timeoutMs);
            String content = result.getContent();

            T parsed;
            try {
                parsed = outputParser.parse(content, resultType, capabilityKey);
            } catch (LlmException parseError) {
                log.warn("[AI] {} 首次输出解析失败，携带错误信息重试一次：{}", capabilityKey, parseError.getMessage());
                List<AiMessageDTO> retryMessages = new ArrayList<>(messages);
                retryMessages.add(AiMessageDTO.assistant(content));
                retryMessages.add(AiMessageDTO.user(String.format(SELF_CORRECT_HINT, parseError.getMessage())));
                LlmResultDTO retryResult = llmClient.complete(buildRequest(call, model, retryMessages), timeoutMs);
                parsed = outputParser.parse(retryResult.getContent(), resultType, capabilityKey);
                content = retryResult.getContent();
                result = retryResult;
            }

            int latency = (int) (System.currentTimeMillis() - start);
            degradeGuard.recordSuccess(capabilityKey);

            SysAiCallLog entity = baseLog(call, operator, prompt.getVersion());
            entity.setStatus(AiCallStatus.SUCCESS.getCode());
            entity.setLatencyMs(latency);
            entity.setModel(result.getModel());
            entity.setPromptTokens(result.getPromptTokens());
            entity.setCompletionTokens(result.getCompletionTokens());
            entity.setOutputDigest(AiAuditDigestSupport.buildDigest(parsed));
            auditService.record(entity);

            return Optional.ofNullable(parsed);
        } catch (LlmException ex) {
            degradeGuard.recordFailure(capabilityKey);
            degradeGuard.recordDegrade(capabilityKey);
            degradeGuard.recordFailureReason(capabilityKey, ex.getMessage());
            int latency = (int) (System.currentTimeMillis() - start);
            record(call, operator, prompt.getVersion(), model,
                    ex.isTimeout() ? AiCallStatus.TIMEOUT : AiCallStatus.FAILED, latency, ex.getMessage());
            log.warn("[AI] {} 调用未成功，已降级：{}", capabilityKey, ex.getMessage());
            return Optional.empty();
        } catch (Exception ex) {
            degradeGuard.recordFailure(capabilityKey);
            degradeGuard.recordDegrade(capabilityKey);
            degradeGuard.recordFailureReason(capabilityKey,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
            int latency = (int) (System.currentTimeMillis() - start);
            record(call, operator, prompt.getVersion(), model, AiCallStatus.FAILED, latency,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
            log.error("[AI] {} 出现未预期异常，已降级", capabilityKey, ex);
            return Optional.empty();
        }
    }

    private AiChatRequestDTO buildRequest(AiCallDTO call, String model, List<AiMessageDTO> messages) {
        return AiChatRequestDTO.builder()
                .capabilityKey(call.getCapabilityKey())
                .model(model)
                .messages(messages)
                .temperature(call.getTemperature())
                .jsonMode(true)
                .maxTokens(call.getMaxTokens())
                .build();
    }

    private SysAiCallLog baseLog(AiCallDTO call, String operator, String promptVersion) {
        SysAiCallLog entity = new SysAiCallLog();
        entity.setCapabilityKey(call.getCapabilityKey());
        entity.setBizType(call.getBizType());
        entity.setBizId(call.getBizId());
        entity.setProvider(configProvider.get().getProvider());
        entity.setPromptVersion(promptVersion);
        entity.setInputDigest(AiMaskUtils.digest(call.getInputDigest()));
        entity.setOperator(operator);
        return entity;
    }

    private void record(AiCallDTO call, String operator, String promptVersion,
                        AiCallStatus status, int latencyMs, String errorMsg) {
        record(call, operator, promptVersion, null, status, latencyMs, errorMsg);
    }

    /** 已进入模型调用阶段的失败要带上 model —— 超时/失败行没有 model 就说不清是哪个模型挂的 */
    private void record(AiCallDTO call, String operator, String promptVersion, String model,
                        AiCallStatus status, int latencyMs, String errorMsg) {
        SysAiCallLog entity = baseLog(call, operator, promptVersion);
        entity.setStatus(status.getCode());
        entity.setLatencyMs(latencyMs);
        entity.setModel(model);
        entity.setErrorMsg(AiMaskUtils.digest(errorMsg, 480));
        auditService.record(entity);
    }

    /**
     * 取当前登录账号。异步线程（如未来的 @Async 质控任务）取不到 SecurityContext，
     * 会回落为 system —— 所以异步化时必须在投递前把 operator 捕获下来传进去。
     */
    private String currentOperator() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user != null) {
                if (StringUtils.hasText(user.getUsername())) {
                    return user.getUsername();
                }
                if (StringUtils.hasText(user.getRealName())) {
                    return user.getRealName();
                }
            }
        } catch (Exception ignored) {
            // 非请求线程，忽略
        }
        return OPERATOR_FALLBACK;
    }

    /**
     * 生成降级原因说明，供能力层回传前端。
     * <p>
     * 「AI 不可用」对医生没有价值，「密钥未配置」「已触发熔断」「服务不可达」才有价值 ——
     * 前者只能让人重试，后者能让人去修配置或找运维。
     * 所以这里按「配置问题 → 熔断 → 具体失败原因」三段递进地往下找原因。
     */
    public String degradeReasonOf(String capabilityKey) {
        String disabled = configProvider.capabilityDisabledReason(capabilityKey);
        if (StringUtils.hasText(disabled)) {
            return disabled;
        }
        if (degradeGuard.isOpen(capabilityKey)) {
            return "该能力连续调用失败已触发熔断，正在冷却，本次直接降级";
        }
        String lastFailure = degradeGuard.lastFailureReason(capabilityKey);
        if (StringUtils.hasText(lastFailure)) {
            return "模型调用未成功：" + lastFailure;
        }
        return "模型调用未成功（超时、限流，或返回内容无法解析为约定 JSON）";
    }
}
