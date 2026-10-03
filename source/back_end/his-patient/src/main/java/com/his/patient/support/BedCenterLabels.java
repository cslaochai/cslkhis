package com.his.patient.support;

/**
 * 床位服务中心的码值文案集中出口。
 *
 * <p><b>未知码值一律渲染成未知(码值)，绝不回落到某个合法值</b>：
 * 把看不懂的状态说成「等待中」或「普通」，下游看到的是一段被洗过的干净数据，
 * 而真相是这条记录的字段值已经越界了 —— 这不是文案问题，是把数据质量问题藏起来。
 *
 * <p>读取侧禁止直接 switch 默认回落到合法值，必须走这里。
 */
public final class BedCenterLabels {

    private BedCenterLabels() {
    }

    /** 等待状态：0-等待中 1-已安排床位 2-已收治 3-已取消 */
    public static String waitStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "等待中";
            case 1 -> "已安排床位";
            case 2 -> "已收治入院";
            case 3 -> "已取消";
            default -> "未知(" + status + ")";
        };
    }

    /** 优先级：1-普通 2-急 3-危重 */
    public static String priorityText(Integer priority) {
        return switch (priority == null ? -1 : priority) {
            case 1 -> "普通";
            case 2 -> "急";
            case 3 -> "危重";
            default -> "未知(" + priority + ")";
        };
    }

    /** 床位类型：与床位的床位类型同口径 */
    public static String bedTypeText(String type) {
        if (type == null || type.isBlank()) {
            return "未分类";
        }
        return switch (type) {
            case "normal" -> "普通";
            case "ICU" -> "重症";
            case "VIP" -> "特需";
            default -> "未知(" + type + ")";
        };
    }

    /** 性别限制：0-不限 1-限男床 2-限女床 */
    public static String genderLimitText(Integer limit) {
        return switch (limit == null ? -1 : limit) {
            case 0 -> "不限";
            case 1 -> "限男床";
            case 2 -> "限女床";
            default -> "未知(" + limit + ")";
        };
    }

    /** 调配类型：1-本科室预留 2-跨科调配 3-急诊占床 */
    public static String allocTypeText(Integer type) {
        return switch (type == null ? -1 : type) {
            case 1 -> "本科室预留";
            case 2 -> "跨科调配";
            case 3 -> "急诊占床";
            default -> "未知(" + type + ")";
        };
    }

    /** 调配状态：1-已预留 2-已转入院 3-已释放 4-已作废 */
    public static String allocStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 1 -> "已预留";
            case 2 -> "已转入院";
            case 3 -> "已释放";
            case 4 -> "已作废";
            default -> "未知(" + status + ")";
        };
    }

    public static boolean isValidPriority(Integer priority) {
        return priority != null && priority >= 1 && priority <= 3;
    }

    public static boolean isValidWaitStatus(Integer status) {
        return status != null && status >= 0 && status <= 3;
    }

    public static boolean isValidGenderLimit(Integer limit) {
        return limit != null && limit >= 0 && limit <= 2;
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
