package com.his.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 随访话术草拟入参（医护端，新建/编辑随访任务时点「AI 拟话术」）
 */
@Data
public class FollowupComposeDTO {

    /** 患者ID（用于读慢病档案做上下文） */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /** 随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访） */
    @NotNull(message = "随访类型不能为空")
    private Integer followupType;

    /** 诊断（新任务尚未保存时由前端携带，可为空） */
    private String diagnosis;
}
