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

    /**
     * 操作日志总量
     */
    private Long operTotal;
    /** 操作日志今日条数 */
    private Long operToday;
    /** 操作日志今日失败条数（执行结果为失败的请求） */
    private Long operFailToday;
    /** 操作日志近 7 天条数 */
    private Long oper7d;

    /**
     * 登录日志总量
     */
    private Long loginTotal;
    /** 登录日志今日条数 */
    private Long loginToday;
    /** 登录日志今日失败次数（口令爆破与异地登录排查入口） */
    private Long loginFailToday;
    /** 登录日志近 7 天条数 */
    private Long login7d;

    /**
     * 审计日志总量
     */
    private Long auditTotal;
    /** 审计日志今日条数 */
    private Long auditToday;
    /** 审计日志今日失败条数（审计动作未执行成功的记录） */
    private Long auditFailToday;
    /** 审计日志近 7 天条数 */
    private Long audit7d;

    /**
     * 字段级变更总量（sql/159 的第四本账）
     */
    private Long fieldChangeTotal;
    /** 字段级变更今日条数 */
    private Long fieldChangeToday;
    /** 字段级变更近 7 天条数 */
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
