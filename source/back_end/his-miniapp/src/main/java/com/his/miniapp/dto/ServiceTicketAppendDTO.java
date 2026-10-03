package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者补充留言。
 *
 * <p><b>已办结的工单上补充 = 患者认为没解决</b>，服务端自动把单重开（status 回到处理中），
 * 不让患者面对一个已经"办结"却还能说话的死单。
 */
@Data
@Schema(description = "患者补充留言入参")
public class ServiceTicketAppendDTO {

    @NotBlank(message = "工单ID不能为空")
    @Schema(description = "工单ID（字符串）")
    private String id;

    @NotBlank(message = "补充内容不能为空")
    @Schema(description = "补充内容")
    private String content;
}
