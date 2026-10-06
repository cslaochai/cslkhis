package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.entity.SysLoginLog;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysLoginLogMapper;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.SysLoginLogService;
import com.his.system.utils.RequestInfoUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登录日志落库实现。
 *
 * <p>IP / 浏览器 / 操作系统的解析统一走 {@link RequestInfoUtils}，与操作日志同源同口径。
 * <b>写库的文本一律先截到列宽</b>：UA 这类字段超长会报 {@code Data too long}，
 * 结果是"记一条登录失败日志"升级成 500，用户连失败原因都看不到。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysUserMapper userMapper;

    @Override
    public void record(String userName, HttpServletRequest request, boolean success, String msg) {
        Long userId = null;
        String realName = null;
        if (StringUtils.hasText(userName)) {
            SysUser user = findUser(userName);
            if (user != null) {
                userId = user.getId();
                realName = user.getRealName();
            }
        }
        record(userName, userId, realName, request, success, msg);
    }

    @Override
    public void record(String userName, Long userId, String realName, HttpServletRequest request,
                       boolean success, String msg) {
        try {
            String ip = RequestInfoUtils.clientIp(request);
            String ua = request == null ? null : request.getHeader("User-Agent");
            SysLoginLog row = new SysLoginLog();
            row.setUserName(cut(userName, 64));
            row.setUserId(userId);
            row.setRealName(cut(realName, 64));
            row.setLoginIp(cut(ip, 50));
            row.setLoginLocation(ip == null ? null : (RequestInfoUtils.isPrivateIp(ip) ? "内网" : "外网"));
            row.setBrowser(cut(RequestInfoUtils.parseBrowser(ua), 100));
            row.setOs(cut(RequestInfoUtils.parseOs(ua), 100));
            row.setUserAgent(cut(ua, 500));
            row.setLoginStatus(success ? 0 : 1);
            row.setMsg(cut(msg, 200));
            row.setLoginTime(LocalDateTime.now());
            row.setCreateBy(cut(userName, 64));
            loginLogMapper.insert(row);
        } catch (Exception e) {
            log.error("登录日志写入失败 userName={} success={}", userName, success, e);
        }
    }

    private SysUser findUser(String userName) {
        try {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUserName, userName).last("LIMIT 1");
            return userMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("登录日志回填用户信息失败 userName={}", userName, e);
            return null;
        }
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
