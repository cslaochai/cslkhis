package com.his.spi.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知窄端口：只保留跨模块投递与追发判重所需的方法，
 * 不含收件箱分页、SSE 推送、未读数统计等模块内方法。
 */
public interface SysMessageService {

    /**
     * 发送站内信（通知型默认值：severity=info，无 payload，无 handle_status）。
     * 待办型消息请用 {@link #sendSystemMessage(Long, String, String, String, String, Long, String, String, Integer)}
     * 显式带上紧急度与处理状态。
     */
    boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                              String bizType, Long bizId);

    /**
     * 发送站内信（完整参数）。
     *
     * @param severity     紧急度：info/warning/urgent（urgent 参与列表 SQL 置顶排序，见 sql/70）
     * @param payload      结构化负载 JSON 字符串（patientName/prescriptionNo/bedNo/opinion 等），可为 null
     * @param handleStatus 处理状态：0-待处理（待办型）；null-通知型（用 read_status 闭环）
     */
    boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                              String bizType, Long bizId, String severity, String payload, Integer handleStatus);

    /**
     * 查某条业务对象当前发过的全部消息收件人（员工ID，去重）。
     * 危急值超时升级等「追发通知」场景用它找原收件人 —— 收件箱里可能有
     * 开单医生也可能有兜底接收人的消息，升级时一个都不能漏。
     */
    List<Long> receiverIdsOfBiz(String bizType, Long bizId);

    /**
     * 某业务对象自指定时刻起已发的消息条数。
     *
     * <p>定时与手动补跑共用同一个催办入口时用它保证「每张单每天最多催一次」的可重入。
     */
    long countSentSince(String bizType, Long bizId, LocalDateTime since);

    /**
     * 同一业务单对同一收件人在时窗内是否发过指定紧急度的消息。
     *
     * <p>催办类动作用它判重：永久判重会让「催过一次就永远不再催」，
     * 而挂住的等床/转诊单恰恰会越挂越久。
     *
     * @param severity 紧急度（催办传 urgent，只跟催办类判重，别把通知型算进来）
     * @param hours    时窗（小时）
     */
    boolean sentWithinHours(String bizType, Long bizId, Long receiverId, String severity, int hours);

    /**
     * 把某业务对象名下「待处理」的待办消息批量置为目标处理状态。
     *
     * <p>业务侧动作闭环收件箱用（会诊取消、危急值处置完成），
     * 联动失败不回滚业务单据，由调用方按日志处理。
     *
     * @param targetStatus 目标处理状态（1-已处理 2-已关闭）
     */
    void closeTodoByBiz(String bizType, Long bizId, Integer targetStatus);
}
