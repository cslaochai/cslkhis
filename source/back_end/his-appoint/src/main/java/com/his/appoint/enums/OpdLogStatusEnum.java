package com.his.appoint.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 门诊日志的「就诊状态」——用户的筛选口径，不等于队列状态。
 *
 * <p>为什么需要它：门诊日志的基表是<b>挂号</b>（一次就诊 = 一条挂号），
 * 而 {@link QueueStatusEnum} 只描述「已入队之后」的状态。没入队的挂号在队列枚举里
 * 根本没有对应码值，所以：
 * <ul>
 *   <li>码 0/1 由「挂号状态 + 有没有收费单」推出来，不落库；</li>
 *   <li>码 2~7 <b>故意与 {@link QueueStatusEnum} 对齐</b>（7 = 队列已失效 → 就诊状态未就诊），
 *       避免同一语义出现两套数字；</li>
 *   <li>码 8「爽约」只在挂号侧存在（没签到就没有队列行），没有队列对应值。</li>
 * </ul>
 *
 * <p><b>推导规则（以挂号状态为主干，队列状态只在已入队时优先，实现在 OpdLogMapper.xml）：</b>
 * <pre>
 *   已入队(queue_status ∈ 2..7) → 用队列状态（7 已失效在就诊状态里读作「未就诊」）
 *   已入队但状态不在枚举内     → NULL（前端显示「未知(n)」，见下）
 *   未入队 → 挂号状态 5 已退号 / 6 已过号 / 4 已就诊 / 3 就诊中
 *                     / 2 待签到 / 1 → 已开收费单=待签到，否则=未缴费
 *                     / 7 爽约   / 8 未就诊
 * </pre>
 *
 * <p>踩过的坑：一开始只看「有没有队列行」，结果把库里 <b>164 条 regist_status=5（已退号、
 * 退号未收费、没有队列行）</b> 全标成了「待签到」——把已退号说成待签到，
 * 比显示未知更有害。凡是「一个状态要由两套数据推出来」的地方，都要先拿数据核一遍。</p>
 *
 * <p>另一类坑（已修）：候诊队列.queue_status 的<b>列默认值是 1</b>，而 1 在
 * {@link QueueStatusEnum} 里没有定义，历史脚本走默认值造出过 9 行脏数据。本枚举不认领这种行——
 * 列表里原样返回 {@code queueStatus=1} 且 {@code logStatus=null}，由前端渲染成「未知(1)」，
 * 绝不回落到「待签到」这种合法文案。列默认值已在 sql/59 改成 2（候诊中）。</p>
 */
@Getter
public enum OpdLogStatusEnum {

    UNPAID(0, "未缴费"),
    WAIT_CHECK_IN(1, "待签到"),
    WAITING(QueueStatusEnum.WAITING.getCode(), "候诊中"),
    CONSULTING(QueueStatusEnum.CONSULTING.getCode(), "就诊中"),
    COMPLETED(QueueStatusEnum.COMPLETED.getCode(), "已就诊"),
    CANCELLED(QueueStatusEnum.CANCELLED.getCode(), "已退号"),
    OVERDUE(QueueStatusEnum.OVERDUE.getCode(), "已过号"),
    /**
     * 未就诊：到院签到过，但当天没被接诊（日终结转收的口）。
     * 码与 {@link QueueStatusEnum#EXPIRED}（已失效）相同 —— 队列行的「已失效」
     * 在就诊状态口径里就是「这个人没看上」。
     */
    UNVISITED(QueueStatusEnum.EXPIRED.getCode(), "未就诊"),
    /**
     * 爽约：挂了号但当天没到院（没有队列行）
     */
    NO_SHOW(8, "爽约");

    private final int code;
    private final String label;

    OpdLogStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 由队列状态推导就诊状态。
     *
     * <p>队列状态不在枚举内（如历史脏数据 1）返回 null，交给前端显示未知。
     */
    public static OpdLogStatusEnum ofQueueStatus(Integer queueStatus) {
        if (queueStatus == null) {
            return null;
        }
        if (queueStatus == QueueStatusEnum.EXPIRED.getCode()) {
            return UNVISITED;
        }
        for (OpdLogStatusEnum status : values()) {
            if (status != UNVISITED && status != NO_SHOW
                    && status != UNPAID && status != WAIT_CHECK_IN
                    && status.code == queueStatus) {
                return status;
            }
        }
        return null;
    }

    /**
     * 由挂号状态推导就诊状态（仅在「没有队列行」时使用）
     */
    public static OpdLogStatusEnum ofRegistStatus(Integer registStatus) {
        if (registStatus == null) {
            return null;
        }
        return switch (registStatus) {
            case 1 -> null;                 // 已挂号：还要看有没有收费单，由 SQL 判
            case 2 -> WAIT_CHECK_IN;        // 已签到却没队列行：脏数据，按「待签到」显示最接近事实
            case 3 -> CONSULTING;
            case 4 -> COMPLETED;
            case 5 -> CANCELLED;
            case 6 -> OVERDUE;
            case 7 -> NO_SHOW;
            case 8 -> UNVISITED;
            default -> null;
        };
    }

    public static OpdLogStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OpdLogStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案
     *（脏数据行由前端依据 {@code queueStatus}/logStatus 原值渲染「未知(n)」）。
     */
    public static String getText(Integer code) {
        OpdLogStatusEnum status = fromCode(code);
        return status != null ? status.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        OpdLogStatusEnum status = fromCode(code);
        return status != null ? status.label : "未知(" + code + ")";
    }

    /**
     * 入队后的状态码，用于拼 SQL 的 {@code queue_status IN (...)}。
     *
     * <p>包含 7（已失效）；不含 8（爽约没有队列行）。
     */
    public static List<Integer> queuedCodes() {
        return List.of(
                QueueStatusEnum.WAITING.getCode(),
                QueueStatusEnum.CONSULTING.getCode(),
                QueueStatusEnum.COMPLETED.getCode(),
                QueueStatusEnum.CANCELLED.getCode(),
                QueueStatusEnum.OVERDUE.getCode(),
                QueueStatusEnum.EXPIRED.getCode());
    }

    public static Set<Integer> allCodes() {
        return Arrays.stream(values()).map(OpdLogStatusEnum::getCode).collect(Collectors.toSet());
    }
}
