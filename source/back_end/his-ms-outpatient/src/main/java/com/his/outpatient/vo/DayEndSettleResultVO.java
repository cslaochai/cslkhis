package com.his.outpatient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 日终结转结果。
 */
@Data
@Schema(description = "日终结转结果")
public class DayEndSettleResultVO {

    /**
     * 本次结转覆盖的起始就诊日
     */
    @Schema(description = "本次结转覆盖的起始就诊日")
    private LocalDate fromDate;

    /**
     * 本次结转覆盖的截止就诊日
     */
    @Schema(description = "本次结转覆盖的截止就诊日")
    private LocalDate toDate;

    /**
     * 覆盖天数（起止就诊日之间的日历日数）
     */
    @Schema(description = "覆盖天数")
    private int days;

    /**
     * 未签到 → 爽约 的挂号数
     */
    @Schema(description = "未签到 → 爽约 的挂号数")
    private long noShowCount;

    /**
     * 已签到/已接诊未就诊 → 未就诊 的挂号数
     */
    @Schema(description = "已签到/已接诊未就诊 → 未就诊 的挂号数")
    private long unvisitedCount;

    /**
     * 队列收「已失效」的行数
     */
    @Schema(description = "队列收「已失效」的行数")
    private long queueExpiredCount;

    /**
     * 队列行跟随挂号终态对齐（退号/过号没同步队列）的行数
     */
    @Schema(description = "队列行跟随挂号终态对齐（退号/过号没同步队列）的行数")
    private long queueAlignedCount;

    /**
     * 未就诊里「已接诊但从未结诊」的条数，需人工核对是否漏结诊
     */
    @Schema(description = "未就诊里「已接诊但从未结诊」的条数，需人工核对是否漏结诊")
    private long stuckConsultingCount;

    /**
     * 是否只试算（未落库）
     */
    @Schema(description = "是否只试算（未落库）")
    private boolean dryRun;

    /**
     * 消息内容
     */
    @Schema(description = "结论文案")
    private String message;
}
