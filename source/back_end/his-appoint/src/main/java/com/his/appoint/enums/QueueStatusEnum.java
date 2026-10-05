package com.his.appoint.enums;

import lombok.Getter;

/**
 * 排队状态枚举 —— 候诊队列.queue_status 的<b>唯一权威码值</b>。
 *
 * <p>字典 {@code his_queue_status}、前端 {@code lib/statusColor.ts} 的 QUEUE_STATUS、
 * 门诊日志的 {@link OpdLogStatusEnum} 都必须与这里逐字一致。
 * 历史上字典那套（1候诊中/2已叫号/4已过号/5已就诊/6已退号）整套错位，
 * 全靠「没有任何报表读它」才没炸 —— 现已由 sql/59 按本枚举对齐，改这里的码值要同步改字典与前端。
 *
 * <p>为什么单列一个 7「已失效」而不是复用 6「已过号」：
 * 6 的语义是「叫了没来」，而日终结转要收的是「昨天签到了但一整天没人给他看上」——
 * 患者从没被叫过，标成过号是冤枉他，且会让「过号率」这类统计失真。
 */
@Getter
public enum QueueStatusEnum {

    /**
     * 已入队候诊中（签到入队后的初始态）
     */
    WAITING(2, "候诊中"),
    /**
     * 医生正在接诊
     */
    CONSULTING(3, "就诊中"),
    /**
     * 接诊结束
     */
    COMPLETED(4, "已就诊"),
    /**
     * 退号
     */
    CANCELLED(5, "已退号"),
    /**
     * 叫号未到
     */
    OVERDUE(6, "已过号"),
    /**
     * 跨日结转：签到入队后当天未接诊，队列作废（与「过号」区分，见类注释）
     */
    EXPIRED(7, "已失效");

    private final int code;
    private final String label;

    QueueStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QueueStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QueueStatusEnum status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 文案；不在枚举内返回 null（调用方自己决定兜底文案，不要回落成某个合法文案）
     */
    public static String getText(Integer code) {
        QueueStatusEnum s = fromCode(code);
        return s == null ? null : s.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        QueueStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 是否「还在队列里、可以被叫号」的状态
     */
    public static boolean isActive(Integer code) {
        return code != null && (code == WAITING.code || code == CONSULTING.code);
    }
}
