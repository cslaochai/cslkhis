package com.his.security;

import java.util.Set;

/**
 * 「当前登录用户能看哪些科室」的提供方（SPI）—— 科室数据权限的**唯一收口点**。
 *
 * <p><b>为什么需要它</b>：本仓库的鉴权现状是 {@code SecurityConfig} 只有
 * {@code anyRequest().authenticated()}、全仓零 {@code @PreAuthorize}，permissions 只管前端菜单。
 * 于是任何登录用户只要把 {@code deptId} 参数一改/一删，就能看全院所有科室的挂号、排班、号源 ——
 * 而权限数据（员工岗位的 112 行授权）其实一直存在，只是**没有任何后端消费方**。
 *
 * <p><b>为什么放在 his-security（叶子模块）</b>：按既有范式（见 {@link RolePermissionProvider}），
 * his-system 依赖 his-security，反向依赖会成环。所以接口留在 security、实现由 his-system 提供
 * （见 {@code DeptScopeProviderImpl}），业务模块经 Spring 注入消费。
 *
 * <p><b>范围口径</b>（与老王 2026-09-21 定案一致）：
 * <ol>
 *   <li>取员工岗位里该员工**在当前角色下**被授权的科室集合（岗位按角色过滤）；</li>
 *   <li>集合为空时**回落到主科室**（{@code CurrentUser.deptId} / 员工档案上的主科室）——
 *       否则新员工没配授权就变成"什么都看不见"，比不收口更难用；</li>
 *   <li>调用方显式传的 deptId **必须在授权集合内**，越权直接拒绝（不静默改写条件）。</li>
 * </ol>
 *
 * <p><b>注意别把它当成"必须收口"</b>：返回 {@code null} 与返回空集语义完全不同 ——
 * {@code null} = 不受限（看全院），空集 = 受限但一个科室都没有。调用方必须区分。
 */
public interface DeptScopeProvider {

    /**
     * 取该员工被授权的科室 ID 集合。
     *
     * <p><b>为什么要按角色收</b>：员工岗位现在是岗位维度（角色 × 科室），
     * 整表 distinct 会把「药剂师·药房」那条也塞进医生身份的授权集合里 ——
     * 岗位切了、数据范围没切，等于切了个寂寞。收口范围必须是**当前角色下的那些岗位科室**。
     *
     * @param employeeId 员工主键（不是用户主键）
     * @param primaryDeptId 主科室 ID，作为「授权为空」时的兜底；可为 null
     * @param roleCode 当前岗位角色编码；为空（老 token / 未走过滤器）时退回整表口径
     * @return 受限时为科室 ID 集合；{@code null} 表示**不受限（全院）**。
     *         永不返回 null 之外的"空表示不收口"这种模糊值。
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
}
