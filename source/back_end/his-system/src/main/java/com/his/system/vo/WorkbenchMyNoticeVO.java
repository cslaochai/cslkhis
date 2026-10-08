package com.his.system.vo;

import com.his.system.service.MessageMetricSupport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工作台卡片 myNotice 的出参（我未读的通知型站内信）。
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
