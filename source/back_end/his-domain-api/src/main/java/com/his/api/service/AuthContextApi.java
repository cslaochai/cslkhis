package com.his.api.service;

import com.his.api.entity.CurrentUser;

/**
 * 当前登录用户上下文端口（his-system 实现）。
 *
 * <p>miniapp 通过本端口取登录态，不直接依赖 his-system 的 {@code UserUtils}。
 */
public interface AuthContextApi {

    /**
     * 取当前登录用户（无登录态时由实现方决定抛异常或返回 null，语义与 {@code UserUtils.getCurrentUser()} 一致）。
     */
    CurrentUser getCurrentUser();
}
