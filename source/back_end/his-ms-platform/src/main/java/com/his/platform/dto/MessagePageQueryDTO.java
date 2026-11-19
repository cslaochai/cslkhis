package com.his.platform.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息列表查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessagePageQueryDTO extends PageParam {

    /**
     * 已读状态：0-未读 1-已读，为空表示查询全部
     */
    private Integer readStatus;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 处理状态：0-待处理（待办 Tab 用），NULL 查全部
     */
    private Integer handleStatus;

    /**
     * 关键词（模糊匹配标题、内容）
     */
    private String keyword;

    /**
     * 只看我发起的（按 create_by_id = 当前用户），用于「我发起」页签
     */
    private Boolean mine;

    /**
     * 只看通知型（handle_status 为空：无需处置、看完即算），用于「待阅」页签
     */
    private Boolean notifyOnly;
}
