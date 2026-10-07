package com.his.medicaltech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * CDR 患者全景时间轴查询条件。
 *
 * <p><b>ID 为什么入参用 Long、出参用 String</b>：库里的主键是雪花算法生成的 BIGINT（19 位），
 * 出参用 JSON number 会被 JS 的 Number 截断（末几位变 0），于是「查 A 的患者，打开了 B 的档案」，
 * 所以<b>出参</b>一律加 {@code ToStringSerializer} 走字符串。
 * 但<b>入参</b>用 String 没有任何收益 —— Jackson 本来就能把 {@code "1857..."} 解析成 Long，
 * 换成 String 只会让「传了个坏 ID」从 400 变成 500，故障更难查。
 */
@Data
@Schema(description = "CDR 查询条件")
public class CdrQueryDTO {

    /**
     * 患者ID
     */
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
