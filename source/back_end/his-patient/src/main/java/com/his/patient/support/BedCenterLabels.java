package com.his.patient.support;

/**
 * 床位服务中心的纯计算 / 工具方法。
 *
 * <p><b>历史：</b>原本这里堆积了「码值 → 文案」的静态方法（等待状态 / 优先级 / 床位类型 / 性别限制 /
 * 调配类型 / 调配状态）以及对应的 {@code isValid} 范围校验，这些都是本应定义为枚举的常量字典。
 * 现已全部迁到 {@code com.his.patient.enums} 下的枚举
 * （{@code BedWaitStatusEnum} / {@code BedPriorityEnum} / {@code BedTypeEnum} /
 * {@code BedGenderLimitEnum} / {@code BedAllocTypeEnum} / {@code BedAllocateStatusEnum}），
 * 文案走各枚举的 {@code labelOf}，校验走 {@code labelOf(x) != null}。
 *
 * <p>本类只保留无法归并到枚举的纯计算：等待时长的人读文案。
 */
public final class BedCenterLabels {

    private BedCenterLabels() {
    }

    /**
     * 等待时长的人读文案。
     *
     * <p>只表达"等了多久"，不做任何判断 —— 是否超时由 {@code expired} 单独给，
     * 不要在这里把"等了 8 天"直接说成"已超时"（阈值是可配的，写死在文案里改配置就失效）。
     */
    public static String waitDurationText(long hours) {
        if (hours < 1) {
            return "不足 1 小时";
        }
        if (hours < 24) {
            return hours + " 小时";
        }
        long days = hours / 24;
        long rest = hours % 24;
        return rest == 0 ? days + " 天" : days + " 天 " + rest + " 小时";
    }
}
