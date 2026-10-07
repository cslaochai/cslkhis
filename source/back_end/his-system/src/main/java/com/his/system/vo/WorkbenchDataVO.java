package com.his.system.vo;

import lombok.Data;

import java.util.Map;

/**
 * 一张卡片的聚合取数结果。
 *
 * <p>{@code error} 非空表示该卡取数失败：前端渲染「—」而不是整屏 500，
 * 因为一次 {@code /workbench/data} 里任何一张卡的 SQL 问题都不该连带其它卡。
 *
 * <p><b>{@code data} 为什么是 {@code Map<String, Object>} 而不是嵌套 VO</b>：
 * 它是<b>真动态</b>的，键集合不由本类决定，而由「那张卡是谁算的」决定 ——
 * {@code data} 的值就是 {@code WorkbenchMetricProvider#summary} 的原样返回值，
 * 而实现方散落在 his-system / his-appoint / his-medicaltech 等模块（当前 7 个），
 * 每张卡的键名都跟前端 {@code workbench-widgets.js} 里该卡的 METRIC_SPECS 绑定
 * （如 {@code myTodo} 是 {@code total / urgentTotal / items}，
 * {@code hospitalToday} 是另外一组指标名）。卡片注册表
 * （{@code sys_workbench_widget}）可增删卡片，新增卡片就意味着新增一组键。
 *
 * <p>所以这里能做的只是<b>把每张卡自己的出参收进有类型的 VO</b>
 * （见 {@code WorkbenchMyTodoVO} / {@code WorkbenchMyNoticeVO} 等），
 * 在各自 SPI 实现里于边界处转回 Map；而不是把 {@code data} 换成一个含 N 个可空字段的
 * 巨大 VO —— 那会让「这张卡没配」与「这张卡该字段为 0」在 JSON 上再也分不开，
 * 也会把每张卡的键名锁死在前端某次改动上。
 */
@Data
public class WorkbenchDataVO {

    /**
     * 卡片编码（工作台卡片注册表.widget_code）
     */
    private String code;

    /**
     * 该卡的指标集合，键名由该卡的 {@code WorkbenchMetricProvider} 实现决定（见类注释）
     */
    private Map<String, Object> data;

    private String error;
}
