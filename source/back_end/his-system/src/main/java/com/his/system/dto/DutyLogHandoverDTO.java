package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交班入参：把本班的遗留事项交给下一班。
 *
 * <p>{@code handoverEmpId} 留空 = 交给「下一班的总值班」（白班→同日夜班，夜班→次日白班）。
 * 这个默认值不是图省事：<b>交接不清最常见的形态就是"没想好交给谁"</b>，
 * 让人必须显式选一个人，结果往往是随手选一个不相干的人，或者干脆不交。
 */
@Data
public class DutyLogHandoverDTO {

    @NotNull(message = "日志ID不能为空")
    private Long id;

    /**
     * 接班人（留空 = 下一班总值班）
     */
    private Long handoverEmpId;

    /**
     * 交班说明（留给接班人的话；不是必填，但交接时说清背景能省对方半小时）
     */
    private String handleResult;
}
