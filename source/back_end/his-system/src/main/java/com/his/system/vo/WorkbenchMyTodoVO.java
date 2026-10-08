package com.his.system.vo;

import com.his.system.service.MessageMetricSupport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工作台卡片 myTodo 的出参（我当前未办结的站内信）。
 */
@Data
public class WorkbenchMyTodoVO implements Serializable {

    /**
     * 未办结总数
     */
    private Long total;

    /**
     * 其中加急（severity=urgent）条数
     */
    private Long urgentTotal;

    /**
     * 前 8 条摘要（按 加急 → 警告 → 普通，再按发送时间倒序）
     */
    private List<MessageMetricSupport.Item> items;
}
