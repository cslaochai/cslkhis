package com.his.ai.service;

import com.his.ai.vo.VoiceTranscribeResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音口述转写（G-14，非 LLM 能力）。
 * <p>
 * 音频 → 文本的确定性转换，不走 {@code AiExecutionService}（那是 LLM 结构化输出的通道）；
 * 审计独立落行，转写原文不落库（纪律 6）。失败如实抛错 —— 语音没有规则兜底文本，造文本即造假病历。
 */
public interface SpeechTranscribeService {

    /**
     * 转写一段口述录音。
     *
     * @param audioFile       录音文件（浏览器 MediaRecorder 产出，webm/opus 或 mp4 等）
     * @param durationSeconds 客户端计时口径的录音时长（秒，仅作展示与审计元数据，不参与判定）
     */
    VoiceTranscribeResultVO transcribe(MultipartFile audioFile, Integer durationSeconds);
}
