package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志审计首页统计（三本账各自的总量 / 今日 / 失败 / 近7天 + 口令爆破嫌疑账号）。
 */
@Data
public class LogStatVO {

    /** 操作日志总量 */
    private Long operTotal;
    private Long operToday;
    private Long operFailToday;
    private Long oper7d;

    /** 登录日志总量 */
    private Long loginTotal;
    private Long loginToday;
    private Long loginFailToday;
    private Long login7d;

    /** 审计日志总量 */
    private Long auditTotal;
    private Long auditToday;
    private Long auditFailToday;
    private Long audit7d;

    /** 字段级变更总量（sql/159 的第四本账） */
    private Long fieldChangeTotal;
    private Long fieldChangeToday;
    private Long fieldChange7d;

    /**
     * 口令爆破嫌疑账号：近24小时登录失败 ≥ 5 次。
     * 等保三级要看的是"重要安全事件"，这一栏就是它 —— 只记成功登录的话这栏永远是空的。
     */
    private List<RiskAccount> riskyAccounts;

    @Data
    public static class RiskAccount {
        private String userName;

        private Long failCount;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastFailTime;
    }
}
