package com.his.ai.rag.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.ai.config.AiProperties;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 远程 embedding 实现
 */
@Slf4j
@Component
public class RemoteEmbeddingProvider implements EmbeddingProvider {

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RemoteEmbeddingProvider(AiProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String name() {
        return "remote";
    }

    @Override
    public float[] embed(String text) {
        AiProperties.Rag rag = properties.getRag();
        String base = TextUtil.hasText(rag.getEmbedBaseUrl()) ? rag.getEmbedBaseUrl() : properties.getBaseUrl();
        String key = TextUtil.hasText(rag.getEmbedApiKey()) ? rag.getEmbedApiKey() : properties.getApiKey();
        String model = TextUtil.hasText(rag.getEmbedModel()) ? rag.getEmbedModel() : properties.getModel();
        if (!TextUtil.hasText(base) || !TextUtil.hasText(key) || !TextUtil.hasText(model)) {
            throw new IllegalStateException(
                    "remote embedding 未配置（ai.rag.embedding-base-url / embedding-api-key / embedding-model，或回落 ai.base-url/api-key/model）");
        }
        String normalized = base.replaceAll("/+$", "");
        // 与 RestClientLlmClientImpl.chatPath 同口径：base 已以 /v1 结尾时不再重复拼，否则 /v1/v1/embeddings 必 404
        String url = normalized.endsWith("/v1") ? normalized + "/embeddings" : normalized + "/v1/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(new EmbeddingRequest(model, text), headers);

        try {
            String body = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode embedding = data.get(0).path("embedding");
                float[] vec = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    vec[i] = (float) embedding.get(i).asDouble();
                }
                return vec;
            }
            throw new IllegalStateException("embedding 服务返回为空（data 为空）");
        } catch (RestClientException ex) {
            throw new IllegalStateException("embedding 调用失败：" + ex.getMessage());
        } catch (Exception ex) {
            throw new IllegalStateException("embedding 解析失败：" + ex.getMessage());
        }
    }

    /**
     * OpenAI 兼容 embeddings 请求体。
     */
    private record EmbeddingRequest(String model, String input) {
    }
}
