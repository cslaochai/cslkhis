package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者对工单的动作：撤单 / 确认解决 / 重开。
 *
 * <p>这三个动作患者都能在小程序上点，但每个动作都有前置状态
 * （见 {@link com.his.miniapp.support.ServiceTicketStatus#canCancel} 等），
 * 服务端校验，前端按钮只是"能不能点"的提示。
 */
@Data
@Schema(description = "患者工单动作入参")
public class ServiceTicketActionDTO {

    @NotBlank(message = "工单ID不能为空")
    @Schema(description = "工单ID（字符串）")
    private String id;

    @Schema(description = "动作：cancel-撤单 confirm-确认解决 reopen-不满意重开", example = "cancel")
    private String action;

    @Schema(description = "原因（撤单/重开时填写）")
    private String reason;
}
