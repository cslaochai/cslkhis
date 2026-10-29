package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.util.TextUtil;
import com.his.platform.entity.SysUser;
import com.his.platform.mapper.SysUserMapper;
import com.his.platform.service.UserIdentityCache;
import com.his.platform.support.UserIdentityCacheData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 用户身份缓存：先读 Redis，miss 返回 null 由调用方查库并回填。
 *
 * <p>与 {@link RolePermissionCacheImpl} 同口径：
 * <ul>
 *   <li>TTL 10 分钟兜底：失效点遗漏时最多 10 分钟自愈；</li>
 *   <li>Redis 不可用（his-system 单独跑 / Redis 宕机）时降级直查库，不阻断认证；</li>
 *   <li>失效失败不阻断业务，等待 TTL 兜底。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdentityCacheImpl implements UserIdentityCache {

    /**
     * TTL 兜底：失效点遗漏时最多 10 分钟后自愈（与 his:perm:role:* 同口径）。
     */
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ObjectMapper objectMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * Redis 为编译期强依赖（spring-data-redis 自动配置）；运行期 Redis 宕机由下方 try/catch 降级直查库。
     */
    private final StringRedisTemplate redis;

    @Override
    public UserIdentityCacheData get(String username) {
        if (!TextUtil.hasText(username)) {
            return null;
        }
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + username);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, UserIdentityCacheData.class);
        } catch (Exception e) {
            log.warn("用户身份缓存读取失败,降级直查库(username={})", username, e);
            return null;
        }
    }

    @Override
    public void put(String username, UserIdentityCacheData data) {
        if (!TextUtil.hasText(username) || data == null) {
            return;
        }
        try {
            redis.opsForValue().set(KEY_PREFIX + username, objectMapper.writeValueAsString(data), TTL);
        } catch (Exception e) {
            log.warn("用户身份缓存回填失败(username={})", username, e);
        }
    }

    @Override
    public void invalidate(String username) {
        if (!TextUtil.hasText(username)) {
            return;
        }
        try {
            redis.delete(KEY_PREFIX + username);
        } catch (Exception e) {
            log.warn("用户身份缓存失效失败(username={}),等待 TTL 兜底", username, e);
        }
    }

    @Override
    public void invalidateByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            return;
        }
        List<SysUser> users = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmpId, employeeId));
        for (SysUser user : users) {
            if (TextUtil.hasText(user.getUserName())) {
                invalidate(user.getUserName());
            }
        }
    }

    @Override
    public void invalidateAll() {
        try {
            Set<String> keys = redis.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception e) {
            log.warn("用户身份缓存全量失效失败,等待 TTL 兜底", e);
        }
    }

    @Override
    public void invalidateByRoleId(Long roleId) {
        if (roleId == null) {
            return;
        }
        List<String> userNames = sysUserMapper.selectUserNamesByRoleId(roleId);
        if (userNames == null || userNames.isEmpty()) {
            return;
        }
        for (String userName : userNames) {
            if (TextUtil.hasText(userName)) {
                invalidate(userName);
            }
        }
    }
}
