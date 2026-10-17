package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 口令爆破嫌疑账号聚合行（SysLoginLogMapper#selectFailAccounts 的返回行）。
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
