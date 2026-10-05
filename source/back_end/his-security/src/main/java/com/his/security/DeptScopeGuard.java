package com.his.security;

import com.his.common.exception.BusinessException;

import java.util.Set;

public final class DeptScopeGuard {

    private DeptScopeGuard() {
    }

    /**
     * 由 Spring 启动时注入（{@code DeptScopeProviderImpl}），未装配时为 null → 视为不受限
     */
    private static DeptScopeProvider provider;

    /**
     * 供 Spring 配置类调用一次。刻意不用 {@code @Autowired} 字段注入：
     * 这是静态门面，注入时机必须可控且在容器就绪后。
     */
    public static void setProvider(DeptScopeProvider scopeProvider) {
        provider = scopeProvider;
    }

    /**
     * 当前用户是否**被限定**在部分科室。
     *
     * @return true = 需要按 {@link #allowedDeptIds()} 收口；false = 不受限（全院/未登录/未装配）
     */
    public static boolean isScoped() {
        return allowedDeptIds() != null;
    }

    /**
     * 当前用户被授权的科室集合。
     *
     * @return null = 不受限（看全院）；非 null = 只能看这些科室
     */
    public static Set<Long> allowedDeptIds() {
        if (provider == null) {
            return null;
        }
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return null;
        }
        // 角色 data_scope=1（全部数据）直接放开 —— 否则 admin/院领导会被锁在自己被授权的几个科室上
        if (provider.isUnrestrictedRole(user.getCurrentRole())) {
            return null;
        }
        return provider.deptIdsOfEmployee(user.getEmployeeId(), user.getDeptId(), user.getCurrentRole());
    }

    /**
     * 校验并归一化调用方传来的 deptId —— 数据权限的**执行点**。
     *
     * <p>三种结果：
     * <ul>
     *   <li>回报传入的 deptId：在授权范围内，按它过滤；</li>
     *   <li>报 null：调用方没指定科室，且当前用户不受限 → 不加科室条件（看全院）；</li>
     *   <li><b>抛异常</b>：指定了越权科室 → 明确拒绝，**不静默改写查询条件**。</li>
     * </ul>
     *
     * <p>为什么越权要报错而不是静默回落：静默改写会让用户以为"这个科室真的没数据"，
     * 而静默放行则是漏洞本身。报错是唯一能让越权暴露出来的选择。
     *
     * @param requestedDeptId 调用方传入的科室ID，可为 null
     * @return 归一化后的科室ID；null 表示"不限科室"
     * @throws BusinessException 指定了无权访问的科室
     */
    public static Long resolveDeptId(Long requestedDeptId) {
        Set<Long> allowed = allowedDeptIds();
        if (allowed == null) {
            // 不受限 → 传什么用什么
            return requestedDeptId;
        }
        if (requestedDeptId == null) {
            // 受限但没指定 → 返回 null，由调用方决定"用 allowedDeptIds() 收口"还是"只取主科室"。
            // 这里刻意不自动返回主科室：调用语义（列表要看全部授权科室 / 单条要看主科室）由业务定。
            return null;
        }
        if (!allowed.contains(requestedDeptId)) {
            throw new BusinessException("无权查看该科室的数据（科室ID " + requestedDeptId + "）");
        }
        return requestedDeptId;
    }

    /**
     * 判断某个科室是否在当前用户范围内（用于逐条过滤，不抛异常）。
     */
    public static boolean canAccessDept(Long deptId) {
        Set<Long> allowed = allowedDeptIds();
        if (allowed == null) {
            return true;
        }
        return deptId != null && allowed.contains(deptId);
    }
}
