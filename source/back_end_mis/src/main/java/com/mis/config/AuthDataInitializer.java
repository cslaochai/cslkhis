package com.mis.config;

import com.mis.entity.MssUser;
import com.mis.mapper.MssUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 默认账号播种（幂等：仅在 mss_user 为空时执行一次）。
 * 口令哈希必须在运行时用 BCrypt 生成（无法在 data.sql 里硬编码），
 * 账号/员工/岗位的对应关系由 data.sql 铺底（mss_employee / mss_employee_post）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthDataInitializer implements ApplicationRunner {

    private final MssUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long count = userMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        seed("admin", "admin123", 1L, "系统管理员");
        seed("yb001", "123456", 2L, "结算经办员");
        seed("yb002", "123456", 3L, "审核稽核员");
        log.info("[经办平台] 默认账号播种完成：admin/admin123、yb001/123456（经办员）、yb002/123456（审核员）");
    }

    private void seed(String userName, String rawPassword, Long empId, String realName) {
        MssUser user = new MssUser();
        user.setUserName(userName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setEmpId(empId);
        user.setUserType(1);
        user.setStatus(1);
        user.setLoginCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setCreateBy("system");
        userMapper.insert(user);
    }
}
