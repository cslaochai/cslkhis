package com.his.app.dto;

import com.his.common.validation.AllowedValues;
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

    @AllowedValues(values = {"accept", "reply", "finish", "close", "note"}, message = "工单动作取值不合法")
    @Schema(description = "动作：accept-受理 reply-回复 finish-办结 close-关闭 note-内部备注", example = "accept")
    private String action;

    @Schema(description = "内容（回复正文 / 处理结果 / 关闭原因；受理可为空）")
    private String content;

    @AllowedValues(values = {"0", "1"}, message = "患者可见标志取值不合法")
    @Schema(description = "是否患者可见（仅 reply/note 用；note 默认 0 内部备注）")
    private Integer visibleToPatient;
}
