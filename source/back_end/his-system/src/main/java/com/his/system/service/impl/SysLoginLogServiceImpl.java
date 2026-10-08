package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.util.TextUtil;
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

import java.time.LocalDateTime;

/**
 * 登录日志落库实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    private final SysLoginLogMapper sysLoginLogMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public void record(String userName, HttpServletRequest request, boolean success, String msg) {
        Long userId = null;
        String realName = null;
        if (TextUtil.hasText(userName)) {
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
            row.setUserName(TextUtil.cut(userName, 64));
            row.setUserId(userId);
            row.setRealName(TextUtil.cut(realName, 64));
            row.setLoginIp(TextUtil.cut(ip, 50));
            row.setLoginLocation(ip == null ? null : (RequestInfoUtils.isPrivateIp(ip) ? "内网" : "外网"));
            row.setBrowser(TextUtil.cut(RequestInfoUtils.parseBrowser(ua), 100));
            row.setOs(TextUtil.cut(RequestInfoUtils.parseOs(ua), 100));
            row.setUserAgent(TextUtil.cut(ua, 500));
            row.setLoginStatus(success ? 0 : 1);
            row.setMsg(TextUtil.cut(msg, 200));
            row.setLoginTime(LocalDateTime.now());
            row.setCreateBy(TextUtil.cut(userName, 64));
            sysLoginLogMapper.insert(row);
        } catch (Exception e) {
            log.error("登录日志写入失败 userName={} success={}", userName, success, e);
        }
    }

    private SysUser findUser(String userName) {
        try {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUserName, userName).last("LIMIT 1");
            return sysUserMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("登录日志回填用户信息失败 userName={}", userName, e);
            return null;
        }
    }

}
