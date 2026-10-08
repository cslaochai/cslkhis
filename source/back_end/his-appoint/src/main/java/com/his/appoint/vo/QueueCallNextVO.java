package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 叫号回执 VO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueueCallNextVO extends BizQueueListVO {

    /**
     * 本次叫号顺带结束的上一位「就诊中」患者姓名；没有则为 null。
     * <p>同一医生同一时刻只允许一条就诊中，叫下一位前会先把上一条置为已就诊。
     * 这个字段用于前端如实告知医生「上一位已被结束」，而不是静默改状态。
     */
    private String previousPatientName;

    /**
     * 被顺带结束的上一条队列ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long previousQueueId;

    /**
     * 本次叫号是否为「回诊」—— 把被自动收口（叫下一位时置 4、病历并未结诊）的
     * 本人患者重新叫回就诊中。回执带这个标记，前端才能如实显示「已回诊」而不是「已呼叫」。
     */
    private Boolean reconsult;
}
