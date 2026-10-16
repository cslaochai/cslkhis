package com.his.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.ai.config.AiConfigProvider;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.OpenAiAsrRequestDTO;
import com.his.ai.dto.OpenAiChatResponseDTO;
import com.his.ai.entity.SysAiCallLog;
import com.his.ai.enums.AiCallStatusEnum;
import com.his.ai.service.AiAuditService;
import com.his.ai.service.SpeechTranscribeService;
import com.his.ai.support.AiAuditDigestSupport;
import com.his.ai.support.AiMaskUtils;
import com.his.ai.vo.VoiceTranscribeResultVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音口述转写实现（DashScope qwen3-asr-flash，OpenAI 兼容同步调用）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechTranscribeServiceImpl implements SpeechTranscribeService {

    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Map<String, String> CONTENT_TYPE_TO_FORMAT = Map.of(
            "audio/webm", "webm",
            "audio/ogg", "ogg",
            "audio/opus", "opus",
            "audio/mpeg", "mp3",
            "audio/mp4", "mp4",
            "audio/wav", "wav",
            "audio/x-wav", "wav");

    private final AiConfigProvider aiConfigProvider;

    private final AiAuditService aiAuditService;

    /**
     * 按「地址 + 超时」缓存连接工厂，与 LlmClient 同口径
     */
    private final Map<String, RestClient> clientCache = new ConcurrentHashMap<>();

    @Override
    public VoiceTranscribeResultVO transcribe(MultipartFile audioFile, Integer durationSeconds) {
        String capabilityKey = AiCapabilityKeys.VOICE_TRANSCRIBE;
        long start = System.currentTimeMillis();
        try {
            if (!aiConfigProvider.isCapabilityEnabled(capabilityKey)) {
                throw new BusinessException(aiConfigProvider.capabilityDisabledReason(capabilityKey));
            }
            if (audioFile == null || audioFile.isEmpty()) {
                throw new BusinessException("请先录制或上传音频");
            }
            String format = resolveFormat(audioFile.getContentType());
            int maxBytes = aiConfigProvider.get().getAsr().getMaxAudioMb() * 1024 * 1024;
            if (audioFile.getSize() > maxBytes) {
                throw new BusinessException("音频超过 " + aiConfigProvider.get().getAsr().getMaxAudioMb() + "MB 上限，请缩短口述时长");
            }

            byte[] bytes = audioFile.getBytes();
            String model = aiConfigProvider.asrModel();
            String content = callAsr(bytes, format, model, aiConfigProvider.timeoutOf(capabilityKey));
            if (!TextUtil.hasText(content)) {
                throw new BusinessException("未能从音频中识别出语音内容");
            }

            int latency = (int) (System.currentTimeMillis() - start);
            recordAudit(capabilityKey, AiCallStatusEnum.SUCCESS, latency, model, null,
                    audioMeta(audioFile, format, durationSeconds), "text=" + AiAuditDigestSupport.sha256Short(content));

            VoiceTranscribeResultVO vo = new VoiceTranscribeResultVO();
            vo.setText(content.trim());
            vo.setDurationSeconds(durationSeconds);
            vo.setModel(model);
            vo.setElapsedMs(latency);
            return vo;
        } catch (BusinessException ex) {
            recordAudit(capabilityKey, AiCallStatusEnum.FAILED,
                    (int) (System.currentTimeMillis() - start), aiConfigProvider.asrModel(), ex.getMessage(),
                    audioFile == null ? "audioFile=empty" : audioMeta(audioFile, null, durationSeconds), null);
            throw ex;
        } catch (IOException ex) {
            String reason = "读取音频失败：" + ex.getMessage();
            recordAudit(capabilityKey, AiCallStatusEnum.FAILED,
                    (int) (System.currentTimeMillis() - start), aiConfigProvider.asrModel(), reason, null, null);
            throw new BusinessException(reason);
        }
    }

    private String callAsr(byte[] bytes, String format, String model, int timeoutMs) {
        AiConfigProvider cfg = aiConfigProvider;
        if (!TextUtil.hasText(cfg.get().getBaseUrl())) {
            throw new BusinessException("AI 服务地址未配置（application.yml: ai.base-url）");
        }
        if (!TextUtil.hasText(cfg.asrApiKey())) {
            throw new BusinessException("语音转写密钥未配置（环境变量 " + AiConfigProvider.ENV_API_KEY + "）");
        }
        String baseUrl = cfg.get().getBaseUrl().trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String path = baseUrl.endsWith("/v1") ? "/chat/completions" : "/v1/chat/completions";

        OpenAiAsrRequestDTO body = OpenAiAsrRequestDTO.builder()
                .model(model)
                .messages(List.of(OpenAiAsrRequestDTO.Message.builder()
                        .role("user")
                        // qwen3-asr 的 user content 只允许 audio part：裸 base64 会被当 URL 解析报
                        // 「provided URL does not appear to be valid」，混入 text part 则整个输入被拒
                        .content(List.of(OpenAiAsrRequestDTO.ContentPart.builder()
                                .type("input_audio")
                                .inputAudio(OpenAiAsrRequestDTO.InputAudio.builder()
                                        .data("data:audio/" + format + ";base64,"
                                                + Base64.getEncoder().encodeToString(bytes))
                                        .format(format)
                                        .build())
                                .build()))
                        .build()))
                .build();

        try {
            RestClient client = clientOf(baseUrl, timeoutMs);
            // retrieve() 会正确读错误流：非 2xx 抛 RestClientResponseException 且携带上游响应体，
            // exchange + SimpleClientHttpRequestFactory 会把它包成 I/O error 丢掉 400 的真实原因
            String raw = client.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.asrApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            OpenAiChatResponseDTO response = RESPONSE_MAPPER.readValue(raw, OpenAiChatResponseDTO.class);
            return response.firstContent();
        } catch (JsonProcessingException ex) {
            throw new BusinessException("语音服务返回解析失败：" + ex.getOriginalMessage());
        } catch (RestClientResponseException ex) {
            String detail = ex.getResponseBodyAsString();
            if (TextUtil.hasText(detail) && detail.length() > 300) {
                detail = detail.substring(0, 300) + "...";
            }
            throw new BusinessException("语音服务返回 " + ex.getStatusCode().value()
                    + (TextUtil.hasText(detail) ? "：" + detail : ""));
        } catch (RestClientException ex) {
            throw new BusinessException("语音服务不可达：" + ex.getMessage());
        }
    }

    private String resolveFormat(String contentType) {
        String type = contentType == null ? "" : contentType.toLowerCase().split(";")[0].trim();
        String format = CONTENT_TYPE_TO_FORMAT.get(type);
        if (format == null) {
            throw new BusinessException("不支持的音频格式（" + type + "），请使用浏览器录音（webm/mp4）或 wav/mp3 文件");
        }
        return format;
    }

    private String audioMeta(MultipartFile audioFile, String format, Integer durationSeconds) {
        String type = audioFile.getContentType() == null ? "unknown" : audioFile.getContentType();
        return "audioSize=" + audioFile.getSize() + "B"
                + ",contentType=" + type
                + (format == null ? "" : ",format=" + format)
                + (durationSeconds == null ? "" : ",duration=" + durationSeconds + "s");
    }

    private void recordAudit(String capabilityKey, AiCallStatusEnum status, int latencyMs, String model,
                             String errorMsg, String inputMeta, String outputDigest) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String operator = operatorUser.getRealName();
        try {
            SysAiCallLog entity = new SysAiCallLog();
            entity.setCapabilityKey(capabilityKey);
            entity.setBizType("emr_voice");
            entity.setProvider(aiConfigProvider.get().getProvider());
            entity.setStatus(status.getCode());
            entity.setLatencyMs(latencyMs);
            entity.setModel(model);
            if (TextUtil.hasText(inputMeta)) {
                entity.setInputDigest(AiMaskUtils.digest(inputMeta));
            }
            if (TextUtil.hasText(outputDigest)) {
                entity.setOutputDigest(outputDigest);
            }
            if (TextUtil.hasText(errorMsg)) {
                entity.setErrorMsg(AiMaskUtils.digest(errorMsg, 480));
            }
            entity.setOperator(operator);
            aiAuditService.record(entity);
        } catch (Exception ex) {
            // 审计失败不能反过来打断转写主流程
            log.warn("[AI] {} 审计落库失败：{}", capabilityKey, ex.getMessage());
        }
    }

    private RestClient clientOf(String baseUrl, int timeoutMs) {
        String key = baseUrl + "@" + timeoutMs;
        return clientCache.computeIfAbsent(key, ignored -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Math.min(timeoutMs, 5_000));
            factory.setReadTimeout(timeoutMs);
            return RestClient.builder()
                    .baseUrl(baseUrl)
                    .requestFactory(factory)
                    .build();
        });
    }
}
