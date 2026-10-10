package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 院内处理工单
 */
@Data
@Schema(description = "院内处理工单入参")
public class TicketHandleUpsertDTO {

    @NotNull(message = "工单ID不能为空")
    @Schema(description = "工单ID")
    private Long id;

    @Schema(description = "动作：accept-受理 reply-回复 finish-办结 close-关闭 note-内部备注", example = "accept")
    private String action;

    @Schema(description = "内容（回复正文 / 处理结果 / 关闭原因；受理可为空）")
    private String content;

    @Schema(description = "是否患者可见（仅 reply/note 用；note 默认 0 内部备注）")
    private Integer visibleToPatient;
}
