package com.his.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端随访反馈提交入参
 */
@Data
public class FollowupReplyDTO {

    /** 随访任务ID */
    @NotNull(message = "随访任务ID不能为空")
    private Long taskId;

    /** 就诊人ID（只允许给自己绑定的就诊人反馈） */
    @NotNull(message = "就诊人ID不能为空")
    private Long patientId;

    /** 反馈内容 */
    @NotBlank(message = "反馈内容不能为空")
    private String replyText;
}
