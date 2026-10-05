package com.his.patient.support;

/**
 * 住院<b>文书</b>枚举文案（病历文书 + 护理文书 + 修改日志）。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 文书状态回落成"已归档"、护理级别回落成"三级护理"，等于把没发生的事记成发生了 ——
 * 和检验「未判定 ≠ 正常」、医嘱「未知状态不能显示成已完成」是同一条原则。
 */
public final class InpatientRecordLabels {

    /**
     * 护理文书字段中文名（日志里的 fieldLabel 用；码 = 库列名）
     */
    private static final java.util.Map<String, String> NURSING_FIELD_LABELS = java.util.Map.ofEntries(
            java.util.Map.entry("nursing_type", "文书类型"),
            java.util.Map.entry("measure_time", "测量/记录时间"),
            java.util.Map.entry("shift", "班次"),
            java.util.Map.entry("temperature", "体温"),
            java.util.Map.entry("pulse", "脉搏"),
            java.util.Map.entry("respiration", "呼吸"),
            java.util.Map.entry("systolic_pressure", "收缩压"),
            java.util.Map.entry("diastolic_pressure", "舒张压"),
            java.util.Map.entry("spo2", "血氧饱和度"),
            java.util.Map.entry("stool_count", "大便次数"),
            java.util.Map.entry("urine_volume", "尿量"),
            java.util.Map.entry("intake_volume", "入量"),
            java.util.Map.entry("output_volume", "出量"),
            java.util.Map.entry("nursing_level", "护理级别"),
            java.util.Map.entry("nursing_content", "护理记录正文"),
            java.util.Map.entry("remark", "备注")
    );

    private InpatientRecordLabels() {
    }

    /**
     * 住院病历文书类型：1-入院记录 2-首次病程 3-日常病程 4-术前小结 5-手术记录 6-术后首次病程 7-出院记录 8-死亡记录
     * 9-会诊记录（P4.1：**由会诊完成时系统回写**，不允许手工新增）
     * 10-转科记录（P4.2：**由转科接收时系统回写**，不允许手工新增）
     * 11-输血记录（P4.4：**由输血闭环完成时系统回写**，不允许手工新增）
     *
     * <p>刻意<b>不提供"设置类型"以外的推断</b>：一份文书属于哪种类型由书写者选定，
     * 不允许服务端按"有没有诊断"去猜 —— 猜错会让质控统计整批失真。
     */
    public static String recordTypeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "入院记录";
            case 2 -> "首次病程";
            case 3 -> "日常病程";
            case 4 -> "术前小结";
            case 5 -> "手术记录";
            case 6 -> "术后首次病程";
            case 7 -> "出院记录";
            case 8 -> "死亡记录";
            case 9 -> "会诊记录";
            case 10 -> "转科记录";
            case 11 -> "输血记录";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 文书状态：1-草稿 2-已提交 3-已归档（沿用病案首页 summary_status 的同一套口径）
     */
    public static String recordStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已归档";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 护理文书类型：1-三测单 2-护理记录单 3-生命体征监测
     */
    public static String nursingTypeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "三测单";
            case 2 -> "护理记录单";
            case 3 -> "生命体征监测";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 护理级别：1-特级护理 2-一级护理 3-二级护理 4-三级护理
     */
    public static String nursingLevelText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "特级护理";
            case 2 -> "一级护理";
            case 3 -> "二级护理";
            case 4 -> "三级护理";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 班次：1-白班 2-小夜班 3-大夜班
     */
    public static String shiftText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "白班";
            case 2 -> "小夜班";
            case 3 -> "大夜班";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 日志单据类型：1-住院病历文书 2-护理文书
     */
    public static String docTypeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "住院病历文书";
            case 2 -> "护理文书";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 性别：文书上的 gender 是建档时从患者基本信息快照过来的，口径同主档
     * （1-男 2-女 9-未知），直接复用 {@link PatientGenderText}。
     * 原先这里没写 3，新增的「未知」会被渲染成「未知(3)」——同一码值在两个词典里说法不一致。
     */
    public static String genderText(Integer code) {
        return PatientGenderText.of(code);
    }

    /**
     * 年龄单位：1-岁 2-月 3-天
     */
    public static String ageUnitText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "岁";
            case 2 -> "月";
            case 3 -> "天";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 病历文书状态：已归档后禁止修改
     */
    public static boolean isArchived(Integer status) {
        return status != null && status == 3;
    }

    /**
     * 病历文书状态：草稿（可随意改）
     */
    public static boolean isDraft(Integer status) {
        return status != null && status == 1;
    }

    /**
     * 护理文书变更字段的中文名。未知列名**原样返回，不猜** —— 猜错一个列名，
     * 飞检时会变成"日志里写了一个不存在的字段"。
     */
    public static String nursingFieldLabel(String code) {
        if (code == null) {
            return "—";
        }
        return NURSING_FIELD_LABELS.getOrDefault(code, code);
    }

    /**
     * 只有「入院记录 / 出院记录 / 死亡记录」要求诊断要素必填。
     * <p>日常病程不一定带新诊断，把诊断设成病程的必填项会逼医生写假诊断，
     * 反而污染首页主要诊断的取数。
     */
    public static boolean requiresDiagnosis(Integer recordType) {
        return recordType != null && (recordType == 1 || recordType == 7 || recordType == 8);
    }

    /**
     * 是否为"病程类"文书（正文写在 courseNote）
     */
    public static boolean isCourseRecord(Integer recordType) {
        return recordType != null && (recordType == 2 || recordType == 3 || recordType == 6);
    }

    /**
     * 是否为「会诊记录」（P4.1 由会诊完成时系统回写，走自己的结构化要素清单）
     */
    public static boolean isConsultRecord(Integer recordType) {
        return recordType != null && recordType == 9;
    }

    /**
     * 是否为「转科记录」（P4.2 由转科接收时系统回写，走自己的结构化要素清单）
     */
    public static boolean isTransferRecord(Integer recordType) {
        return recordType != null && recordType == 10;
    }

    /**
     * 是否为「手术记录」（record_type=5）。
     *
     * <p>与会诊（9）/转科（10）<b>不同</b>：5 这个码值 P2 就有了，医生本来就能手写，
     * 所以它<b>不属于"系统专用文书"</b>（{@link #isSystemWritten} 里不含 5），
     * 手工新增不会被拒。P4.3 只是让手术闭环完成时**也**回写一份。
     *
     * <p>但它需要自己的结构化要素清单：手术记录的要素就是"术前诊断 + 手术经过 + 来源申请单"，
     * 套用 26 项通用清单（绝大多数项对手术记录没有意义）会把结构化率凭空拉低。
     */
    public static boolean isOperationRecord(Integer recordType) {
        return recordType != null && recordType == 5;
    }

    /**
     * 是否为「输血记录」（record_type=11，P4.4 新增码值）。
     *
     * <p>与 5-手术记录<b>不同</b>：5 这个码值 P2 就存在、医生本来就能手写；
     * 11 是本闭环新引入的，因此它是"系统专用"文书（见 {@link #isSystemWritten}），
     * 手工新增会被拒 —— 一条没有血袋、没有配血、没有双人核对的"输血记录"就是假病历，
     * 而输血恰恰是飞检必查、追溯要求最高的一类记录。
     */
    public static boolean isTransfusionRecord(Integer recordType) {
        return recordType != null && recordType == 11;
    }

    /**
     * 是否为「系统回写」文书（会诊记录 / 转科记录 / 输血记录）。
     * <p>这些文书不由医生书写，因此：不允许手工新增、不计入"医生该写的文书"，
     * 结构化率走各自的精简要素清单。
     */
    public static boolean isSystemWritten(Integer recordType) {
        return isConsultRecord(recordType) || isTransferRecord(recordType)
                || isTransfusionRecord(recordType);
    }
}
