package com.his.patient.support;

/**
 * 住院枚举文案
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 把未知值显示成「医嘱离院」或「正常」，等于替医保局把违规洗成合规——这和检验「未判定 ≠ 正常」是同一个坑。
 */
public final class InpatientLabels {

    private InpatientLabels() {
    }

    /**
     * 床位状态：0-维修 1-空闲 2-占用 3-锁定
     */
    public static String bedStatusText(Integer code) {
        if (code == null) {
            return "未知";
        }
        return switch (code) {
            case 0 -> "维修";
            case 1 -> "空闲";
            case 2 -> "占用";
            case 3 -> "锁定";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 入院状态：0-已出院 1-在院
     */
    public static String admissionStatusText(Integer code) {
        if (code == null) {
            return "未知";
        }
        return switch (code) {
            case 0 -> "已出院";
            case 1 -> "在院";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 病案首页状态：1-草稿 2-已提交 3-已归档
     */
    public static String summaryStatusText(Integer code) {
        if (code == null) {
            return "未生成";
        }
        return switch (code) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已归档";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 入院途径：1-门诊 2-急诊 3-转院 4-其他。<b>null 表示既有数据未填，不是"门诊"。</b>
     */
    public static String admitWayText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "门诊";
            case 2 -> "急诊";
            case 3 -> "转院";
            case 4 -> "其他";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 离院方式：1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他
     */
    public static String dischargeWayText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "医嘱离院";
            case 2 -> "医嘱转院";
            case 3 -> "医嘱转社区";
            case 4 -> "非医嘱离院";
            case 5 -> "死亡";
            case 9 -> "其他";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 诊断类型：1-主要诊断 2-其他诊断
     */
    public static String diagTypeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "主要诊断";
            case 2 -> "其他诊断";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 入院病情：1-有 2-临床未确定 3-情况不明 4-无
     */
    public static String admitConditionText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "有";
            case 2 -> "临床未确定";
            case 3 -> "情况不明";
            case 4 -> "无";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 离院方式合法值校验
     */
    public static boolean isValidDischargeWay(Integer code) {
        return code != null && (code == 1 || code == 2 || code == 3 || code == 4 || code == 5 || code == 9);
    }

    /**
     * 入院途径合法值校验
     */
    public static boolean isValidAdmitWay(Integer code) {
        return code != null && code >= 1 && code <= 4;
    }

    /**
     * 性别：住院证上的 gender 是建档时从患者基本信息原样快照过来的，
     * 所以口径必须跟主档一致 —— <b>1-男 2-女 9-未知</b>，直接复用 {@link PatientGenderText}。
     *
     * <p>这里原先写的是「0-女 1-男」，与主档口径相反。后果不是"显示成另一种性别"那么轻：
     * 女性患者（gender=2）会落进 default 分支被渲染成「未知(2)」，脏值 0 反而被说成「女」。
     * 现有数据里没有 gender=2 的住院证（能开住院证的女患者恰好没有），所以一直没暴露 ——
     * 一旦有女患者开住院证就会显形。
     */
    public static String genderText(Integer code) {
        return PatientGenderText.of(code);
    }

    /**
     * 住院证状态：1-待收治 2-已收治 3-已作废 4-已过期
     * <p>注意「已过期」是**查询时按 valid_until 实时算**出来的展示态，库里不写这个值
     * （同"危急值超时"的口径：时限到了不等于状态变了，不能把时间流逝伪装成一次业务动作）。
     */
    public static String orderStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "待收治";
            case 2 -> "已收治";
            case 3 -> "已作废";
            case 4 -> "已过期";
            default -> "未知(" + code + ")";
        };
    }
}
