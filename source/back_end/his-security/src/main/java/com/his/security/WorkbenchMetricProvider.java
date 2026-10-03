package com.his.security;

import java.util.Map;

/**
 * 「工作台上某一张卡的数字由谁算」的提供方（SPI）。
 *
 * <p><b>为什么需要它</b>：首页要一次画出 8~12 张卡。若前端逐卡发请求，登录后的第一屏
 * 就是 10+ 个 HTTP；若把这些统计 SQL 全塞进 {@code his-report} 的一张 DashboardMapper，
 * 就又回到「一个模块跨域查二十张表」的老路（表结构一改就静默少值，且没人说得清哪个数字归谁）。
 * 所以定这条：<b>谁的数据谁出</b> —— 挂号的卡由 his-appoint 算，药房的卡由 his-pharmacy 算，
 * 工作台只按 {@code widgetCode} 找 bean 取值。
 *
 * <p><b>为什么放在 his-security（叶子模块）</b>：按既有范式（见 {@link RolePermissionProvider}、
 * {@link DeptScopeProvider}）。业务模块都依赖 his-security，接口放这里才能被所有域实现；
 * 放 his-system 则 his-report/his-patient 要反向依赖 his-system 才能 implements，会成环。
 *
 * <p><b>实现方必须遵守三条</b>：
 * <ol>
 *   <li>{@link #widgetCode()} 与工作台卡片注册表里登记的卡片编码 <b>一字不差</b>
 *       —— 对不上的后果不是报错，是那张卡永远不出数（静默失灵）；</li>
 *   <li>以聚合数字（{@code COUNT}/{@code SUM}）为主。只有待办/通知这类"列表卡"允许带回
 *       前 N 条（N≤10，且必须自己 {@code LIMIT}），禁止把整张表捞进首页 —— 首页不是列表页，
 *       点"查看全部"走各自的列表接口；</li>
 *   <li>自己收敛数据范围（{@code CurrentUser#getDeptId()} / {@link DeptScopeGuard} /
 *       {@code CurrentUser#getEmployeeId()}），不要指望调用方传过滤条件。</li>
 * </ol>
 *
 * <p>抛异常是安全的：{@code WorkbenchService} 对每个 provider 单独 try-catch，
 * 一张卡取数失败只让那张卡显示「—」，不会把整屏打成 500。
 */
public interface WorkbenchMetricProvider {

    /** 卡片编码，对应工作台卡片注册表里登记的编码 */
    String widgetCode();

    /**
     * 取该卡当前登录人所见的数字。
     *
     * @param user 当前登录人（含 currentRole/deptId/employeeId），调用方保证非 null
     * @return 出参 Map；其中 Long 类型的 id 一律先转字符串再放（AGENTS.md §1 精度铁律），
     *         返回 null 与返回空 Map 等价（前端渲染「—」）
     */
    Map<String, Object> summary(CurrentUser user);
}
