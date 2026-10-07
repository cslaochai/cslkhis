package com.his.medicaltech.service;

import com.his.system.provider.WorkbenchMetricProvider;

/**
 * 工作台卡片 {@code weekVisitTrend} 的取数提供方：近 7 天挂号趋势（全院口径）。
 *
 * <p>本接口只是<b>域标记</b>，不重复声明 {@code widgetCode()} / {@code summary()} ——
 * 两个方法都从 {@link WorkbenchMetricProvider} 继承。
 *
 * <p><b>{@code summary()} 为什么仍是 {@code Map<String, Object> 而不是 VO</b>：
 * 父接口 {@code WorkbenchMetricProvider} 在 his-system，聚合方
 * {@code WorkbenchServiceImpl} 把返回值原样塞进 {@code WorkbenchDataVO.data}
 * （类型就是 {@code Map<String, Object>}），前端再按 {@code data.xxx} 取键。
 * 改这个签名要动 his-system 与前端，属于跨模块契约变更，不在本模块范围内。
 *
 * <p>本模块内部已不出现裸 Map 数据契约：取数走
 * {@code WorkbenchMetricMapper} 的有类型行 VO，只在 SPI 边界上把 VO 转回 Map，
 * 键名与前端 {@code workbench-widgets.js} 的 METRIC_SPECS 一一对应。
 */
public interface WeekVisitTrendMetricProvider extends WorkbenchMetricProvider {
}
