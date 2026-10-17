package com.his.system.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录日志服务：登录成功 / 失败 / 登出的统一落库入口。
 */
public interface SysLoginLogService {

    /**
     * 记录一条登录日志（只知道登录名，用户身份需回查）。
     *
     * @param userName 登录名（可为空，如登录名本身缺失的失败场景）
     * @param request  当前请求，允许为 null（无请求上下文时 IP/UA 记空）
     * @param success  是否成功
     * @param msg      失败原因或备注
     */
    void record(String userName, HttpServletRequest request, boolean success, String msg);

    /**
     * 记录一条登录日志（身份已明确，省一次回查）。
     *
     * @param userName 登录名
     * @param userId   用户主键（已查到）
     * @param realName 用户姓名（已查到）
     * @param request  当前请求
     * @param success  是否成功
     * @param msg      失败原因或备注
     */
    void record(String userName, Long userId, String realName, HttpServletRequest request,
                boolean success, String msg);
}
