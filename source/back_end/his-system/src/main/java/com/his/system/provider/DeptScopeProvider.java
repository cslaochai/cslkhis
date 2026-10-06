package com.his.system.provider;

import java.util.Set;

/**
 * 科室权限
 */
public interface DeptScopeProvider {

    /**
     * 取该员工被授权的科室 ID 集合。
     *
     * <p><b>为什么要按角色收</b>：员工岗位现在是岗位维度（角色 × 科室），
     * 整表 distinct 会把「药剂师·药房」那条也塞进医生身份的授权集合里 ——
     * 岗位切了、数据范围没切，等于切了个寂寞。收口范围必须是**当前角色下的那些岗位科室**。
     *
     * @param employeeId    员工主键（不是用户主键）
     * @param primaryDeptId 主科室 ID，作为「授权为空」时的兜底；可为 null
     * @param roleCode      当前岗位角色编码；为空（老 token / 未走过滤器）时退回整表口径
     * @return 受限时为科室 ID 集合；{@code null} 表示**不受限（全院）**。
     * 永不返回 null 之外的"空表示不收口"这种模糊值。
     */
    Set<Long> deptIdsOfEmployee(Long employeeId, Long primaryDeptId, String roleCode);

    /**
     * 该角色是否**不受**科室范围限制（角色的数据范围取「全部数据」）。
     *
     * <p>放在同一个 SPI 里而不是另开一个接口：这个判断和「取授权科室」是同一个决策的两半，
     * 拆开会让调用方漏判一半。缺省返回 false（保守：按受限处理）。
     *
     * @param roleCode 当前岗位的角色编码
     */
    default boolean isUnrestrictedRole(String roleCode) {
        return false;
    }

    /**
     * 当前用户是否**被限定**在部分科室。
     *
     * @return true = 需要按 {@link #allowedDeptIds()} 收口；false = 不受限（全院 / 未登录 / 未装配）
     */
    boolean isScoped();

    /**
     * 当前用户被授权的科室集合。
     *
     * @return null = 不受限（看全院）；非 null = 只能看这些科室
     */
    Set<Long> allowedDeptIds();

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
     * @throws com.his.common.exception.BusinessException 指定了无权访问的科室
     */
    Long resolveDeptId(Long requestedDeptId);

    /**
     * 判断某个科室是否在当前用户范围内（用于逐条过滤，不抛异常）。
     *
     * @param deptId 待判断的科室ID
     */
    boolean canAccessDept(Long deptId);
}
