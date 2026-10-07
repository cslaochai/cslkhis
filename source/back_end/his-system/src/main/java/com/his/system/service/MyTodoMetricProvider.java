package com.his.system.service;

import com.his.system.provider.WorkbenchMetricProvider;

/**
 * 工作台卡片 {@code myTodo} 的取数提供方：我当前未办结的站内信（待办型）。
 *
 * <p>本接口只是<b>域标记</b>，不重复声明 {@code widgetCode()} / {@code summary()} ——
 * 两个方法都从 {@link WorkbenchMetricProvider} 继承（重复声明只会多一处要同步的签名）。
 *
 * <p><b>{@code summary()} 为什么仍是 {@code Map<String, Object>} 而不是 VO</b>：
 * 父接口 {@code WorkbenchMetricProvider} 是跨模块 SPI，his-medicaltech 等模块另有 5 个
 * 子接口实现它；聚合方 {@code WorkbenchServiceImpl} 把返回值原样塞进
 * {@code WorkbenchDataVO.data}（类型就是 {@code Map<String, Object>}），
 * 前端再按 {@code data.xxx} 取键。改这个签名要同时动 his-system、his-medicaltech 与前端，
 * 属于跨模块契约变更。
 *
 * <p>本模块内部已不出现裸 Map 数据契约：出参装进
 * {@link com.his.system.vo.WorkbenchMyTodoVO}，只在 SPI 边界上把 VO 转回 Map，
 * 键名与前端 {@code workbench-widgets.js} 的 {@code myTodo} 指标项一一对应。
 */
public interface MyTodoMetricProvider extends WorkbenchMetricProvider {
}
