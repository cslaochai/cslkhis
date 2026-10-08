package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者补充留言。
 */
@Data
@Schema(description = "患者补充留言入参")
public class ServiceTicketAppendDTO {

    @NotNull(message = "工单ID不能为空")
    @Schema(description = "工单ID")
    private Long id;

    @NotBlank(message = "补充内容不能为空")
    @Schema(description = "补充内容")
    private String content;
}
