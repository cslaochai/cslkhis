package com.his.system.service;

import com.his.system.provider.WorkbenchMetricProvider;

/**
 * 工作台卡片 {@code myNotice} 的取数提供方：我未读的通知型站内信。
 *
 * <p>本接口只是<b>域标记</b>，不重复声明 {@code widgetCode()} / {@code summary()} ——
 * 两个方法都从 {@link WorkbenchMetricProvider} 继承（重复声明只会多一处要同步的签名）。
 *
 * <p><b>{@code summary()} 为什么仍是 {@code Map<String, Object>} 而不是 VO</b>：
 * 同 {@link MyTodoMetricProvider} 的说明 —— 父接口是跨模块 SPI（his-medicaltech 等模块
 * 另有 5 个子接口实现它），{@code WorkbenchServiceImpl} 把返回值原样塞进
 * {@code WorkbenchDataVO.data}，前端按 {@code data.xxx} 取键。
 *
 * <p>与 {@link MyTodoMetricProvider} 分两张卡、不合并成一个数字：通知型
 * {@code handle_status} 为 NULL、靠 {@code read_status} 闭环，两类混在一起计数会让数字失去意义。
 */
public interface MyNoticeMetricProvider extends WorkbenchMetricProvider {
}
