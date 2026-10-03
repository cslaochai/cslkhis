package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者档案合并入参（P5.1 EMPI）
 *
 * <p>一次只并一份（不是批量）：合并是"把一个人从档案库里抹掉"的动作，
 * 批量接口会让人一次点掉一批而没有逐个确认的机会。审计表也因此天然是一行一档。
 */
@Data
public class PatientMergeDTO {

    /** 主档患者ID（保留的这个） */
    @NotNull(message = "主档与被并档案都必须指定")
    private Long masterId;

    /** 被并入的患者ID（合并后在册状态失效） */
    @NotNull(message = "主档与被并档案都必须指定")
    private Long mergedId;

    /** 合并理由（必填；级别不同要求的长度不同，见 PatientMatchLevelEnum.minReasonLength） */
    private String reason;

    /**
     * 匹配置信级别（前端可传，但**服务端会自己重算并以服务端为准**）。
     * <p>留着这个字段只是为了在两边不一致时能发现"前端在撒谎"并记进日志。
     */
    private Integer matchType;

    /**
     * 是否把被并档"有值而主档为空"的关键字段补全到主档。
     * <p>默认 false：合并的默认语义是"知道它们是一个人"，不是"顺手改主档内容"。
     * 要补字段必须显式选，且补了哪些会写进审计快照。
     */
    private Boolean fillBlank;
}
