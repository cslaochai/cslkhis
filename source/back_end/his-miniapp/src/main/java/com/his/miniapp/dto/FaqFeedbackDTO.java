package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 常见问题「有用 / 没帮助」反馈。
 */
@Data
@Schema(name = "FaqFeedbackDTO", description = "常见问题有用反馈")
public class FaqFeedbackDTO {

    @NotNull(message = "faqId不能为空")
    @Schema(description = "常见问题ID")
    private Long faqId;

    /**
     * 1-有帮助 0-没帮助
     */
    @Schema(description = "1-有帮助 0-没帮助")
    private Integer helpful;
}
