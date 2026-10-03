package com.his.appoint.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 叫号回执 VO。
 *
 * <p>为什么必须有它：{@code /queue/callNext} 原先返回 {@code Result<Void>}，
 * 前端只能「叫完号 → 等 500ms → 重新拉列表 → 从 queueStatus=3 的行里猜刚落座的是谁」。
 * 500ms 窗口里分诊台再动一次队列（插队/退号/呼叫），猜到的行和实际接到的行就不是同一个人，
 * 屏幕显示与病历挂的诊次会不一致。叫号是「谁被叫进来」这个事实的唯一定义点，
 * 必须由服务端把结果原样回给前端。
 *
 * <p>继承 {@link BizQueueListVO}：接诊动作要落地成「当前患者」，前端 selectPatient(row)
 * 需要的字段（patientId / registId / visitType / revisitRecordId ...）与列表行完全一致，
 * 继承可以保证两份形状不会再漂移。
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
    private String previousQueueId;

    /**
     * 本次叫号是否为「回诊」—— 把被自动收口（叫下一位时置 4、病历并未结诊）的
     * 本人患者重新叫回就诊中。回执带这个标记，前端才能如实显示「已回诊」而不是「已呼叫」。
     */
    private Boolean reconsult;
}
