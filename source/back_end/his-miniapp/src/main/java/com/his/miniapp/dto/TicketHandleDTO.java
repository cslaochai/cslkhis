package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 院内处理工单：受理 / 回复 / 办结 / 关闭 / 内部备注。
 *
 * <p><b>操作人一律服务端取登录人</b>，DTO 里不收 operator ——
 * 收了就意味着前端能伪造"张三处理的"。
 */
@Data
@Schema(description = "院内处理工单入参")
public class TicketHandleDTO {

    @NotBlank(message = "工单ID不能为空")
    @Schema(description = "工单ID（字符串）")
    private String id;

    @Schema(description = "动作：accept-受理 reply-回复 finish-办结 close-关闭 note-内部备注", example = "accept")
    private String action;

    @Schema(description = "内容（回复正文 / 处理结果 / 关闭原因；受理可为空）")
    private String content;

    @Schema(description = "是否患者可见（仅 reply/note 用；note 默认 0 内部备注）")
    private Integer visibleToPatient;
}
