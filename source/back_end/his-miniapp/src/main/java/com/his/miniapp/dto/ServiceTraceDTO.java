package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客服页自助行为埋点上报。
 *
 * <p>一次一个事件，不批量：批量接口一旦某条失败，调用方无法知道是哪条，
 * 而埋点本来就是"丢一条无所谓、但要知道丢了什么"的东西。
 */
@Data
@Schema(name = "ServiceTraceDTO", description = "客服页行为埋点")
public class ServiceTraceDTO {

    /** 会话标识（同一次进入客服页用一个值，前端生成） */
    @Size(max = 64, message = "sessionId过长")
    private String sessionId;

    /** 事件类型（visit/card/search/view/helpful/useless/transfer/message） */
    @NotBlank(message = "eventType不能为空")
    @Size(max = 32, message = "eventType过长")
    private String eventType;

    /** 事件对象（卡片名、搜索词、常见问题ID） */
    @Size(max = 200, message = "eventKey过长")
    private String eventKey;

    /** 关联常见问题ID */
    private Long faqId;

    /** 搜索命中条数（event_type=search 时） */
    private Integer hitCount;
}
