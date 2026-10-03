package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息列表查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageQueryPageDTO extends PageParam {

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
}
