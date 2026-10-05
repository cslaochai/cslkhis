package com.his.security;

import com.his.security.entity.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 — 获取当前登录用户信息
 */
public class UserUtils {

    public static CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser) {
            return (CurrentUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 当前登录用户的**员工ID**（签名人、站内信的接收人一律用这个）。
     *
     * <p>不用 {@code userId}：用户主键指向系统账号，员工主键才对应员工。
     * admin 恰好两者都是 1，所以用错在 admin 上完全看不出来 —— 换个账号就"签名人不是我"/
     * "未读数恒为 0"，而且**零报错**。拿不到员工ID 时才回落 userId。
     */
    public static Long getCurrentEmployeeId() {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
    }

    /** 当前登录用户的姓名（员工姓名优先，回落系统账号姓名） */
    public static String getCurrentEmployeeName() {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            return null;
        }
        if (user.getEmployeeName() != null && !user.getEmployeeName().isBlank()) {
            return user.getEmployeeName();
        }
        return user.getRealName();
    }
}
