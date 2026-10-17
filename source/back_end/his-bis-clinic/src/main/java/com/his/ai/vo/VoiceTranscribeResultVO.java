package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语音口述转写结果。转写失败不走本 VO —— 直接报错（语音没有「纯规则」兜底文本，造文本即造假病历）。
 */
@Data
@Schema(description = "语音口述转写结果")
public class VoiceTranscribeResultVO {

    @Schema(description = "转写文本（医生可编辑，编辑后自行喂病历草拟）")
    private String text;

    @Schema(description = "音频时长（秒，向上取整；仅作展示，不参与任何判定）")
    private Integer durationSeconds;

    @Schema(description = "实际使用的 ASR 模型")
    private String model;

    @Schema(description = "转写耗时（毫秒）")
    private Integer elapsedMs;
}
