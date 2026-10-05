package com.his.ai.controller;

import com.his.ai.dto.EmrDraftDTO;
import com.his.ai.dto.EmrExtractDTO;
import com.his.ai.service.EmrDraftCapability;
import com.his.ai.service.EmrExtractCapability;
import com.his.ai.service.SpeechTranscribeService;
import com.his.ai.vo.EmrDraftResultVO;
import com.his.ai.vo.EmrExtractResultVO;
import com.his.ai.vo.VoiceTranscribeResultVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 病历文本辅助接口（P1-3）。
 * <p>
 * 两个动作刻意都<b>不写库</b>：
 * <ul>
 *   <li>{@code extract} 的产出是「待医生逐字段采纳的候选值」；</li>
 *   <li>{@code draft} 的产出是「必须医生确认的草稿」。</li>
 * </ul>
 * 采纳动作在前端完成（点「填入」写进表单），最终由病历保存流程统一落库 ——
 * 这样"AI 有没有改过病历"这个问题的答案永远是"没有"。
 */
@Tag(name = "AI 能力-病历文本")
@RestController
@RequestMapping("/ai/emrText")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class AiEmrTextController {

    private final EmrExtractCapability emrExtractCapability;

    private final EmrDraftCapability emrDraftCapability;

    private final SpeechTranscribeService speechTranscribeService;

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "病历文本结构化抽取（字段标签切分 + 模型搬运，带原文依据校验，不写库）")
    @PostMapping("/extract")
    public Result<EmrExtractResultVO> extract(@RequestBody @Valid EmrExtractDTO extractDTO) {
        return Result.success(emrExtractCapability.execute(extractDTO));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "病历草拟（只生成现病史草稿，不生成诊断与处理意见，不写库）")
    @PostMapping("/draft")
    public Result<EmrDraftResultVO> draft(@RequestBody @Valid EmrDraftDTO draftDTO) {
        return Result.success(emrDraftCapability.execute(draftDTO));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "语音口述转写（音频→文本，不写库；失败如实报错不造文本）")
    @PostMapping("/transcribe")
    public Result<VoiceTranscribeResultVO> transcribe(
            @RequestParam("file") MultipartFile audioFile,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds) {
        return Result.success(speechTranscribeService.transcribe(audioFile, durationSeconds));
    }
}
