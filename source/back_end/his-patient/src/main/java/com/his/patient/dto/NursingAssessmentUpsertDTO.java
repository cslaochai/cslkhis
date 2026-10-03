package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 护理评估单录入 DTO。
 *
 * <p>itemsJson 由前端按量表组装（项:得分），但 <b>totalScore 由后端对 items 求和重算</b>——
 * 前端传的总分只做展示参考，落库以重算为准，两边不一致直接拒绝（说明量表项有漏选）。
 */
@Data
public class NursingAssessmentUpsertDTO {

    /** 主键ID */
    private Long id;

    /** 入院ID */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /** 评估类型（1-压疮Braden 2-跌倒Morse 3-疼痛NRS） */
    @NotNull(message = "评估类型不能为空")
    @Min(value = 1, message = "评估类型取值不合法")
    @Max(value = 5, message = "评估类型取值不合法")
    private Integer assessType;

    /** 前端算的合计（后端对 items 求和复算，不一致即拒绝） */
    @NotNull(message = "总分不能为空")
    private Integer totalScore;

    /** 评分明细 JSON（数组字符串） */
    @NotBlank(message = "评分明细不能为空（总分必须能从明细推导）")
    private String itemsJson;

    /** 评估时间 */
    @NotNull(message = "评估时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessTime;

    /** 备注 */
    private String remark;
}
