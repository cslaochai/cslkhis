package com.his.system.provider;

import com.his.common.exception.BusinessException;

import java.util.List;
import java.util.Set;

/**
 * 科室权限
 *
 * <p>数据权限三层模型：
 * <ol>
 *   <li><b>院内外</b>：院内员工才走本科室收口；患者端（miniapp）不带员工上下文，
 *       {@link #allowedDeptIds()} 返回 null 由其它校验兜底；</li>
 *   <li><b>角色</b>：data_scope=1（全部数据）→ {@link #allowedDeptIds()} 返回 null 不过滤；
 *       岗位科室授权 → 返回授权科室集合；</li>
 *   <li><b>场景</b>：列表/统计用 {@link #scopedDeptIds(Long)} 做 IN 收口；
 *       单据/详情/写操作用 {@link #assertDeptAccessible(Long)} 做门禁；
 *       「只看自己」类业务在业务层按登录人强控（provider 不感知）。</li>
 * </ol>
 *
 * {@link #scopedDeptIds(Long)}，拿返回集合进 SQL；当前人（人/岗位/角色）由实现类
 * 从登录态解析，业务不传身份。
 */
public interface DeptScopeService {

    /**
     * 取该员工被授权的科室 ID 集合。
     *
     * @param employeeId    员工主键（不是用户主键）
     * @param primaryDeptId 主科室 ID，作为「授权为空」时的兜底；可为 null
     * @param roleCode      当前岗位角色编码；为空（老 token / 未走过滤器）时退回整表口径
     * @return 受限时为科室 ID 集合；{@code null} 表示**不受限（全院）**。
     * 永不返回 null 之外的"空表示不收口"这种模糊值。
     */
    Set<Long> deptIdsOfEmployee(Long employeeId, Long primaryDeptId, String roleCode);

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

    /**
     * 列表/统计的科室收口：把前端传入的科室过滤参数归一成「进 SQL 的科室集合」。
     *
     * <p>判定链 = resolveDeptId（越权直接抛）→ allowedDeptIds（全院角色返回 null 不过滤）。
     * 受限用户未传科室 → 返回全部授权科室集合；授权集合为空视为配置缺失，直接拒绝
     * （IN () 是语法错误，绝不能放空集合进 SQL）。
     *
     * @param requestedDeptId 前端传入的科室过滤参数，可为 null
     * @return null = 全院角色，不加科室条件；非空 = 必须按 IN 过滤的科室集合
     * @throws com.his.common.exception.BusinessException 指定了越权科室 / 岗位未绑定科室
     */
    default List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return List.of(resolved);
        }
        Set<Long> allowed = allowedDeptIds();
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看相关数据");
        }
        return List.copyOf(allowed);
    }

    /**
     * 单据/详情/写操作的门禁：实体所属科室不在当前数据范围内则抛异常。
     *
     * @param deptId 实体上的科室ID（病区维度数据先折算成科室再传入）
     * @throws com.his.common.exception.BusinessException 科室不在当前岗位的数据范围内
     */
    default void assertDeptAccessible(Long deptId) {
        if (!canAccessDept(deptId)) {
            throw new BusinessException("该数据所属科室不在当前岗位的数据范围内");
        }
    }
}
