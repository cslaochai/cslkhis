package com.his.medicaltech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * CDR 患者全景时间轴查询条件。
 */
@Data
@Schema(description = "CDR 查询条件")
public class CdrQueryDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID（主档或影子档案都可以，服务端按 EMPI 口径归并）")
    private Long patientId;

    /**
     * 开始日期
     */
    @Schema(description = "起始日期 yyyy-MM-dd（按就诊开始时间过滤，可空）")
    private String startDate;

    /**
     * 结束日期
     */
    @Schema(description = "截止日期 yyyy-MM-dd（可空）")
    private String endDate;

    @Schema(description = "只看某类事件（事件类型码，可空=全部）")
    private String eventType;
}
