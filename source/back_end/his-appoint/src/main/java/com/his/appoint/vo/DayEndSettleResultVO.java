package com.his.appoint.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 日终结转结果。
 *
 * <p>条数用 {@code long} 而不是 {@code int}：BIGINT 出参走 LONG，且这个 VO 只做统计展示，
 * 不会参与前端精确计算（真要精确前端也用字符串接）。
 */
@Data
@Schema(description = "日终结转结果")
public class DayEndSettleResultVO {

    @Schema(description = "本次结转覆盖的起始就诊日")
    private LocalDate fromDate;

    @Schema(description = "本次结转覆盖的截止就诊日")
    private LocalDate toDate;

    @Schema(description = "覆盖天数")
    private int days;

    @Schema(description = "未签到 → 爽约 的挂号数")
    private long noShowCount;

    @Schema(description = "已签到/已接诊未就诊 → 未就诊 的挂号数")
    private long unvisitedCount;

    @Schema(description = "队列收「已失效」的行数")
    private long queueExpiredCount;

    @Schema(description = "队列行跟随挂号终态对齐（退号/过号没同步队列）的行数")
    private long queueAlignedCount;

    @Schema(description = "未就诊里「已接诊但从未结诊」的条数，需人工核对是否漏结诊")
    private long stuckConsultingCount;

    @Schema(description = "是否只试算（未落库）")
    private boolean dryRun;

    /**
     * 消息内容
     */
    @Schema(description = "结论文案")
    private String message;
}
