package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 口令爆破嫌疑账号聚合行（{@code SysLoginLogMapper#selectFailAccounts} 的返回行）。
 *
 * <p>对应 SQL：{@code sys_login_log} 按 {@code user_name} 的 group by ——
 * 失败次数 {@code COUNT(*)} 与最近一次失败时间 {@code MAX(login_time)}。
 *
 * <p>只在<b>登录失败</b>（{@code login_status = 1}）且时间窗内统计：只记成功登录的话
 * 这一栏永远是空的，等保三级要的「重要安全事件」就看不见了。
 */
@Data
public class LoginFailAccountVO implements Serializable {

    /**
     * 登录账号
     */
    private String userName;

    /**
     * 窗口内登录失败次数
     */
    private Long failCount;

    /**
     * 最近一次失败时间
     */
    private LocalDateTime lastFailTime;
}
