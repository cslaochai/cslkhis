package com.his.patient.support;

import com.his.common.enums.YesOrNoEnum;
import com.his.patient.enums.ConsultCategoryEnum;

/**
 * 住院会诊纯计算 / 常量 / 类别默认工具（P4.1）。
 *
 * <p><b>码值 → 文案的映射已下沉到对应枚举</b>（{@code labelOf} 展示用、{@code labelOrUnknown} 异常 / 审计用）：
 * 状态走 {@code ConsultationStatusEnum}、范围走 {@code ConsultScopeEnum}、紧急走 {@code ConsultUrgentEnum}、
 * 类别走 {@code ConsultCategoryEnum}。本类只保留跨文件引用的别名常量、响应时限计算与类别默认值。
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
     * 会诊类别文案。null（存量数据未填）→ 一律按"普通科间会诊"显示；
     * 非 null 但不在枚举内（脏数据）→ 返回空串，由数据治理修复，不伪装。
     */
    public static String categoryText(Integer code) {
        if (code == null) {
            return CATEGORY_NORMAL_TEXT;
        }
        return ConsultCategoryEnum.labelOf(code);
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
