package com.his.patient.support;

import com.his.common.enums.YesOrNoEnum;
import com.his.patient.enums.ConsultCategoryEnum;
import com.his.patient.enums.ConsultScopeEnum;
import com.his.patient.enums.ConsultationStatusEnum;

/**
 * 住院会诊枚举文案（P4.1）。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 把未知状态显示成"已完成"、把未知范围显示成"科间"，等于替临床编造一个没发生过的会诊 ——
 * 和检验「未判定 ≠ 正常」、医嘱「未知状态不能显示成已完成」是同一条原则。
 */
public final class ConsultationLabels {

    /**
     * 急会诊响应时限（分钟）。
     * <p>只作为**查询时判定**超时的依据，不落状态列 —— 与"危急值超时是查询时算的"同一口径。
     */
    public static final int URGENT_RESPONSE_MINUTES = 10;
    /**
     * 普通会诊响应时限（小时）
     */
    public static final int NORMAL_RESPONSE_HOURS = 24;
    /**
     * 类别：普通科间会诊
     */
    public static final int CATEGORY_NORMAL = ConsultCategoryEnum.NORMAL.getCode();
    /**
     * 类别：营养会诊（由营养筛查阳性发起，营养科应答）
     */
    public static final int CATEGORY_NUTRITION = ConsultCategoryEnum.NUTRITION.getCode();
    /**
     * 类别：药学会诊
     */
    public static final int CATEGORY_PHARMACY = ConsultCategoryEnum.PHARMACY.getCode();
    /**
     * 类别：其他专科会诊
     */
    public static final int CATEGORY_OTHER = ConsultCategoryEnum.OTHER.getCode();

    // 会诊类别（sql/168 §5）：码值口径在 ConsultCategoryEnum，这里只保留跨文件引用的别名常量
    /**
     * 存量行的类别列默认 1，历史数据不回填也应按普通会诊显示
     */
    private static final String CATEGORY_NORMAL_TEXT = "普通科间会诊";
    private ConsultationLabels() {
    }

    /**
     * 会诊状态：0-待应答 1-已完成 2-已取消 3-已应答（会诊中）
     */
    public static String statusText(Integer code) {
        if (code == null) {
            return "—";
        }
        String label = ConsultationStatusEnum.labelOf(code);
        return label == null ? "未知(" + code + ")" : label;
    }

    /**
     * 会诊范围：1-科内 2-科间 3-全院
     */
    public static String typeText(Integer code) {
        if (code == null) {
            return "—";
        }
        String label = ConsultScopeEnum.labelOf(code);
        return label == null ? "未知(" + code + ")" : label;
    }

    /**
     * 急会诊标志：0-普通 1-急会诊
     */
    public static String urgentText(Integer code) {
        if (code == null) {
            return "—";
        }
        if (YesOrNoEnum.YES.getCode() == code) {
            return "急会诊";
        }
        if (YesOrNoEnum.NO.getCode() == code) {
            return "普通会诊";
        }
        return "未知(" + code + ")";
    }

    /**
     * 会诊类别：1-普通科间 2-营养 3-药学 4-其他专科。
     * <p>类别只决定"这单归谁处理、在哪个工作台出现"，闭环状态机与 consult_type 完全共用。
     */
    public static String categoryText(Integer code) {
        if (code == null) {
            return CATEGORY_NORMAL_TEXT;
        }
        String label = ConsultCategoryEnum.labelOf(code);
        return label == null ? "未知(" + code + ")" : label;
    }

    /**
     * 会诊是否按时应答：急会诊 ≤10 分钟、普通 ≤24 小时；未应答按超时计。
     */
    public static boolean onTime(Integer urgent, java.time.LocalDateTime applyTime,
                                 java.time.LocalDateTime acceptTime) {
        if (applyTime == null || acceptTime == null) {
            return false;
        }
        long minutes = java.time.Duration.between(applyTime, acceptTime).toMinutes();
        if (minutes < 0) {
            return false;
        }
        return urgent != null && urgent == YesOrNoEnum.YES.getCode()
                ? minutes <= URGENT_RESPONSE_MINUTES
                : minutes <= NORMAL_RESPONSE_HOURS * 60L;
    }
}
