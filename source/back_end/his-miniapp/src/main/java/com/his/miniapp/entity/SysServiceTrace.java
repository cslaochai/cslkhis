package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服页自助行为埋点。
 *
 * <p><b>单独建表的理由</b>：AI 客服有没有用，只能靠「自助解决率」和「转人工前的最后一个动作」
 * 来证明。{@code sys_ai_call_log} 记的是模型调用，不是患者在客服页干了什么 ——
 * 而且它现在 129 条里 112 条是「密钥未配置」，拿它算自助率只能算出 0%。
 * 埋点与模型调用是两件事，混在一张表里两个指标都会失真。
 *
 * <p>{@code sessionId} 把同一次进入客服页的动作串起来，才能回答
 * 「这个人是在第几步放弃的」。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_service_trace")
public class SysServiceTrace extends BaseEntity {

    /** 用户ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /** 就诊人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 会话标识（同一次进入客服页） */
    private String sessionId;

    /** 事件类型（visit/card/search/view/helpful/useless/transfer/message） */
    private String eventType;

    /** 事件对象（卡片名、搜索词、常见问题ID） */
    private String eventKey;

    /** 关联常见问题ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long faqId;

    /** 关联业务ID（留言ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long refId;

    /** 搜索命中条数（event_type=search 时） */
    private Integer hitCount;
}
