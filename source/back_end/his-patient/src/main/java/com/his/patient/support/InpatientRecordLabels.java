package com.his.patient.support;

import java.util.Map;

/**
 * 住院<b>文书</b>域纯判定 / 字段映射工具（病历文书 + 护理文书 + 修改日志）。
 *
 * <p><b>码值 → 文案的映射已下沉到对应枚举</b>（{@code labelOf} 展示用、{@code labelOrUnknown} 异常 / 审计用），
 * 本类只保留文书类型判定、状态判定、护理文书字段中文名映射等与码值无关的纯逻辑。
 */
public final class InpatientRecordLabels {

    /**
     * 护理文书字段中文名（日志里的 fieldLabel 用；码 = 库列名）
     */
    private static final Map<String, String> NURSING_FIELD_LABELS = Map.ofEntries(
            Map.entry("nursing_type", "文书类型"),
            Map.entry("measure_time", "测量/记录时间"),
            Map.entry("shift", "班次"),
            Map.entry("temperature", "体温"),
            Map.entry("pulse", "脉搏"),
            Map.entry("respiration", "呼吸"),
            Map.entry("systolic_pressure", "收缩压"),
            Map.entry("diastolic_pressure", "舒张压"),
            Map.entry("spo2", "血氧饱和度"),
            Map.entry("stool_count", "大便次数"),
            Map.entry("urine_volume", "尿量"),
            Map.entry("intake_volume", "入量"),
            Map.entry("output_volume", "出量"),
            Map.entry("nursing_level", "护理级别"),
            Map.entry("nursing_content", "护理记录正文"),
            Map.entry("remark", "备注")
    );

    private InpatientRecordLabels() {
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
