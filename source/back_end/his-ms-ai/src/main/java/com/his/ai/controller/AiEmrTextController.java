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
 * 病历文本辅助接口
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

    /**
     * 把一段自由文本拆成病历各字段的候选值，逐条标注来源与原文依据，被丢弃的条数一并返回。
     * 用于门诊医生工作站与住院病历书写页的文本抽取，医生逐字段点「填入」才进表单，本接口不写库。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "病历文本结构化抽取（字段标签切分 + 模型搬运，带原文依据校验，不写库）")
    @PostMapping("/extract")
    public Result<EmrExtractResultVO> extract(@RequestBody @Valid EmrExtractDTO extractDTO) {
        return Result.success(emrExtractCapability.execute(extractDTO));
    }

    /**
     * 依据已填的主诉、既往史等生成现病史草稿，不产出诊断与处理意见。
     * 用于门诊医生工作站的「AI 草拟现病史」弹框，医生确认后自行粘进病历，本接口不写库。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "病历草拟（只生成现病史草稿，不生成诊断与处理意见，不写库）")
    @PostMapping("/draft")
    public Result<EmrDraftResultVO> draft(@RequestBody @Valid EmrDraftDTO draftDTO) {
        return Result.success(emrDraftCapability.execute(draftDTO));
    }

    /**
     * 把医生口述录音转写成文本，识别不到内容就如实返回空，绝不代拟文本。
     * 用于门诊医生工作站「语音口述现病史」弹框的录音转文字环节，转写原文不落库。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "语音口述转写（音频→文本，不写库；失败如实报错不造文本）")
    @PostMapping("/transcribe")
    public Result<VoiceTranscribeResultVO> transcribe(
            @RequestParam("file") MultipartFile audioFile,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds) {
        return Result.success(speechTranscribeService.transcribe(audioFile, durationSeconds));
    }
}
