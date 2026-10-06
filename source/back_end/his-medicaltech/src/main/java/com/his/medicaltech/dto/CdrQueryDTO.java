package com.his.medicaltech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * CDR 患者全景时间轴查询条件。
 *
 * <p>为什么患者ID是字符串：库里的主键是雪花算法生成的 BIGINT（19 位），
 * 用 JSON number 传到前端会被 JS 的 Number 截断（末几位变 0），
 * 于是"查 A 的患者，打开了 B 的档案"。所以 ID 一律字符串进出。
 */
@Data
@Schema(description = "CDR 查询条件")
public class CdrQueryDTO {

    /**
     * 患者ID
     */
    @Schema(description = "患者ID（主档或影子档案都可以，服务端按 EMPI 口径归并）")
    private String patientId;

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
