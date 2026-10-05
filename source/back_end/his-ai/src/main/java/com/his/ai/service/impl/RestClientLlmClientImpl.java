package com.his.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.ai.config.AiConfigProvider;
import com.his.ai.config.AiProperties;
import com.his.ai.dto.AiChatRequestDTO;
import com.his.ai.dto.LlmResultDTO;
import com.his.ai.dto.OpenAiChatRequestDTO;
import com.his.ai.dto.OpenAiChatResponseDTO;
import com.his.ai.exception.LlmException;
import com.his.ai.service.LlmClient;
import com.his.ai.service.RestClientLlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 Spring Boot 3.2 自带 {@code RestClient} 的模型客户端。
 * <p>
 * 为什么不用 Spring AI / Spring AI Alibaba：
 * <ol>
 *   <li>两者都要求 Spring Boot 3.4+/3.5+，本项目是 3.2.5，引入即启动失败；</li>
 *   <li>Spring AI Alibaba 的核心增量是 Graph 多智能体编排，而本方案明确定调不做 Agent 编排；</li>
 *   <li>裸调 OpenAI 兼容协议可以一套代码切换 DeepSeek / 通义 / 内网 vLLM，不被 SDK 绑定。</li>
 * </ol>
 * 详见 docs/AI能力施工手册.md §2.1。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestClientLlmClientImpl implements LlmClient, RestClientLlmClient {

    private static final String CHAT_PATH = "/v1/chat/completions";
    private static final int MAX_CONNECT_TIMEOUT_MS = 5_000;
    private static final int MAX_ERROR_BODY_LENGTH = 500;
    // 手动解析替代按 Content-Type 转换；与 Spring Boot 自动配置的 ObjectMapper 同口径，未知字段不报错
    // （推理模型会在 message 里多带 reasoning_content，FAIL_ON_UNKNOWN_PROPERTIES 默认开会把正常响应拒掉）
    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final AiConfigProvider configProvider;

    /**
     * 按「地址 + 超时」缓存，避免每次调用都重建连接工厂
     */
    private final Map<String, RestClient> clientCache = new ConcurrentHashMap<>();

    private static String abbreviate(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String value = text.replaceAll("\\s+", " ").trim();
        return value.length() <= MAX_ERROR_BODY_LENGTH
                ? value
                : value.substring(0, MAX_ERROR_BODY_LENGTH) + "...";
    }

    @Override
    public LlmResultDTO complete(AiChatRequestDTO request, int timeoutMs) {
        AiProperties properties = configProvider.get();
        guardConfig(properties);

        String model = StringUtils.hasText(request.getModel()) ? request.getModel() : properties.getModel();
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl());
        int effectiveTimeout = timeoutMs > 0 ? timeoutMs : properties.getTimeoutMs();

        OpenAiChatRequestDTO body = buildBody(request, model);
        RestClient client = clientOf(baseUrl, effectiveTimeout);

        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        LlmException lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long start = System.currentTimeMillis();
            try {
                // 不依赖响应 Content-Type：部分兼容端点偶发把 JSON 标成 application/octet-stream，
                // 按 content-type 走 Jackson 转换器会直接抛 UnknownContentType；这里取原始字节按 UTF-8 自行解析
                String raw = client.post()
                        .uri(chatPath(baseUrl))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(body)
                        .exchange((req, resp) -> {
                            byte[] bytes = resp.getBody().readAllBytes();
                            if (resp.getStatusCode().isError()) {
                                throw new RestClientResponseException("HTTP " + resp.getStatusCode().value(),
                                        resp.getStatusCode(), resp.getStatusText(), null, bytes, StandardCharsets.UTF_8);
                            }
                            return new String(bytes, StandardCharsets.UTF_8);
                        });

                OpenAiChatResponseDTO response = RESPONSE_MAPPER.readValue(raw, OpenAiChatResponseDTO.class);
                String content = response.firstContent();
                if (!StringUtils.hasText(content)) {
                    throw new LlmException("模型返回内容为空");
                }

                LlmResultDTO result = new LlmResultDTO();
                result.setContent(content);
                result.setModel(StringUtils.hasText(response.getModel()) ? response.getModel() : model);
                result.setLatencyMs((int) (System.currentTimeMillis() - start));
                result.setAttempts(attempt);
                if (response.getUsage() != null) {
                    result.setPromptTokens(response.getUsage().getPromptTokens());
                    result.setCompletionTokens(response.getUsage().getCompletionTokens());
                }
                if (attempt > 1) {
                    log.info("[AI] {} 第 {} 次尝试成功，耗时 {}ms",
                            request.getCapabilityKey(), attempt, result.getLatencyMs());
                }
                return result;
            } catch (LlmException ex) {
                // 语义性问题（如内容为空），重试无意义
                lastError = ex;
                break;
            } catch (JsonProcessingException ex) {
                lastError = new LlmException("模型返回解析失败：" + ex.getOriginalMessage(), ex);
                break;
            } catch (RestClientException ex) {
                lastError = toLlmException(ex);
                log.warn("[AI] {} 第 {}/{} 次调用失败: {}",
                        request.getCapabilityKey(), attempt, maxAttempts, lastError.getMessage());
                if (!shouldRetry(ex) || attempt == maxAttempts) {
                    break;
                }
            }
        }
        throw lastError != null ? lastError : new LlmException("模型调用失败");
    }

    private void guardConfig(AiProperties properties) {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new LlmException("AI 服务地址未配置（application.yml: ai.base-url）");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new LlmException("AI 访问密钥未配置（环境变量 " + AiConfigProvider.ENV_API_KEY + "）");
        }
        if (!StringUtils.hasText(properties.getModel())) {
            throw new LlmException("AI 模型未配置（application.yml: ai.model）");
        }
    }

    private OpenAiChatRequestDTO buildBody(AiChatRequestDTO request, String model) {
        OpenAiChatRequestDTO body = new OpenAiChatRequestDTO();
        body.setModel(model);
        body.setMessages(request.getMessages());
        body.setTemperature(request.getTemperature());
        body.setMaxTokens(request.getMaxTokens());
        body.setStream(Boolean.FALSE);
        if (request.isJsonMode()) {
            body.setResponseFormat(OpenAiChatRequestDTO.ResponseFormat.jsonObject());
        }
        return body;
    }

    private RestClient clientOf(String baseUrl, int timeoutMs) {
        String key = baseUrl + "@" + timeoutMs;
        return clientCache.computeIfAbsent(key, ignored -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Math.min(timeoutMs, MAX_CONNECT_TIMEOUT_MS));
            factory.setReadTimeout(timeoutMs);
            return RestClient.builder()
                    .baseUrl(baseUrl)
                    .requestFactory(factory)
                    .build();
        });
    }

    /**
     * 仅对「可能瞬时恢复」的错误重试：超时、429、5xx、连接层异常。
     * 401/403/400 这类重试没有意义，直接失败。
     */
    private boolean shouldRetry(RestClientException ex) {
        if (LlmException.isTimeout(ex)) {
            return true;
        }
        if (ex instanceof RestClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            return status == 429 || status >= 500;
        }
        return true;
    }

    private LlmException toLlmException(RestClientException ex) {
        if (ex instanceof RestClientResponseException responseException) {
            String detail = abbreviate(responseException.getResponseBodyAsString());
            return new LlmException("模型服务返回 " + responseException.getStatusCode().value()
                    + (StringUtils.hasText(detail) ? "：" + detail : ""), ex);
        }
        return new LlmException("模型服务不可达：" + ex.getMessage(), ex);
    }

    /**
     * 兼容两种常见写法：填 https://api.deepseek.com 或直接填 .../v1
     */
    private String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String chatPath(String normalizedBaseUrl) {
        return normalizedBaseUrl.endsWith("/v1") ? "/chat/completions" : CHAT_PATH;
    }
}
