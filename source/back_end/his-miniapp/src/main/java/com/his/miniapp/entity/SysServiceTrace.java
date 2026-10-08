package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服页自助行为埋点。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_service_trace")
public class SysServiceTrace extends BaseEntity {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 就诊人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 会话标识（同一次进入客服页）
     */
    private String sessionId;

    /**
     * 事件类型（visit/card/search/view/helpful/useless/transfer/message）
     */
    private String eventType;

    /**
     * 事件对象（卡片名、搜索词、常见问题ID）
     */
    private String eventKey;

    /**
     * 关联常见问题ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long faqId;

    /**
     * 关联业务ID（留言ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long refId;

    /**
     * 搜索命中条数（event_type=search 时）
     */
    private Integer hitCount;
}
