package com.his.system.vo;

import com.his.system.service.MessageMetricSupport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工作台卡片 {@code myNotice} 的出参（我未读的通知型站内信）。
 *
 * <p>对应 {@code MyNoticeMetricProviderImpl#summary}：未读总数 + 前 8 条摘要。
 * 没有 {@code urgentTotal} —— 通知型不分加急档，{@code handle_status} 为 NULL、
 * 靠 {@code read_status} 闭环，与待办卡不是同一口径。
 *
 * <p><b>类本身是有类型的，但 SPI 边界上仍要转回 Map</b>：同
 * {@link WorkbenchMyTodoVO} 的类注释 —— {@code WorkbenchMetricProvider#summary}
 * 的返回类型不能改，{@code WorkbenchServiceImpl} 原样塞进 {@code WorkbenchDataVO.data}，
 * 前端按 {@code data.xxx} 取键。
 */
@Data
public class WorkbenchMyNoticeVO implements Serializable {

    /**
     * 未读总数
     */
    private Long total;

    /**
     * 前 8 条摘要（按 加急 → 警告 → 普通，再按发送时间倒序）
     */
    private List<MessageMetricSupport.Item> items;
}
