package com.his.system.service.impl;

import com.his.common.util.TextUtil;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.RolePermissionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 角色 → 权限码集合的 Redis 读缓存。
 *
 * <p>为什么缓存:每个请求过 JwtAuthenticationFilter 都要取一次「当前角色的权限码集合」,
 * 不缓存 = 每请求一条 SQL,且 100 个并发请求是 100 次一模一样的查询。缓存后 SQL 归零,
 * 只有角色授权变更 / 菜单权限码变更时重查一次。
 *
 * <p>缓存键是 <b>角色编码</b> 不是用户:权限挂在角色上(角色菜单关联),用户只是持角色引用,
 * 上万用户共用同一个角色的同一份缓存 —— 缓存条目数 = 角色数,与用户量无关。
 *
 * <p>一致性模型(刻意选的,写清楚免得被当 bug):
 * <ul>
 *   <li>写路径主动失效:角色授权保存 / 角色删除 / 菜单权限码变更处调用 {@link #invalidate};
 *       分布式部署时也只需在各实例入口处失效(Redis 集中读,天然一致)。</li>
 *   <li>TTL 10 分钟兜底:任何失效点遗漏,最多 10 分钟后自动一致 —— 权限变更多活几分钟
 *       在安全上可接受,所以这是「最终一致」,不追求强一致。</li>
 *   <li>Redis 故障降级:直接查库,鉴权绝不因缓存层故障而瘫痪(宁可慢,不可瞎)。</li>
 * </ul>
 *
 * <p>序列化用逗号拼接(权限码是字母数字冒号,不含逗号),不引 JSON,空集合存空串。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionCacheImpl implements RolePermissionCache {
    /**
     * TTL 兜底:失效点遗漏时最多 10 分钟后自愈
     */
    private static final Duration TTL = Duration.ofMinutes(10);

    private final SysUserMapper sysUserMapper;

    /**
     * 可选注入:Redis 不可用时(his-system 单独跑、Redis 宕机)降级直查库
     */
    private final ObjectProvider<StringRedisTemplate> redisProvider;

    /**
     * 取角色权限码集合:先读缓存,miss 查库并回填。
     * 返回空集合是合法结果(该角色确实没配任何权限码),调用方按「真没权限」处理。
     */
    public List<String> permissionsOfRole(String roleCode) {
        if (!TextUtil.hasText(roleCode)) {
            return Collections.emptyList();
        }
        String key = KEY_PREFIX + roleCode;
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis != null) {
            try {
                String cached = redis.opsForValue().get(key);
                if (cached != null) {
                    return cached.isEmpty() ? Collections.emptyList() : Arrays.asList(cached.split(","));
                }
            } catch (Exception e) {
                log.warn("权限缓存读取失败,降级直查库(role={})", roleCode, e);
            }
        }

        List<String> permissions = sysUserMapper.selectPermissionsByRoleCode(roleCode);
        List<String> result = permissions == null ? Collections.emptyList() : permissions;

        if (redis != null) {
            try {
                redis.opsForValue().set(key, String.join(",", result), TTL);
            } catch (Exception e) {
                log.warn("权限缓存回填失败(role={})", roleCode, e);
            }
        }
        return result;
    }

    /**
     * 失效单个角色的权限缓存(角色授权保存 / 角色删除后调用)
     */
    public void invalidate(String roleCode) {
        if (!TextUtil.hasText(roleCode)) {
            return;
        }
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) {
            return;
        }
        try {
            redis.delete(KEY_PREFIX + roleCode);
        } catch (Exception e) {
            // 失效失败不阻断业务:10 分钟 TTL 兜底
            log.warn("权限缓存失效失败(role={}),等待 TTL 兜底", roleCode, e);
        }
    }

    /**
     * 失效全部角色权限缓存(菜单权限码变更后调用;角色数级,量小 keys 可接受)
     */
    public void invalidateAll() {
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) {
            return;
        }
        try {
            Set<String> keys = redis.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception e) {
            log.warn("权限缓存全量失效失败,等待 TTL 兜底", e);
        }
    }
}
