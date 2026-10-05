package com.his.emr.support;

import com.his.common.exception.BusinessException;

/**
 * 质控对象来自哪张表。
 *
 * <p>质控检查记录的记录ID 原先是一个"看运气才知道 JOIN 哪张表"的裸 ID：
 * 门诊病历、住院文书、病案归档表的 ID 混在一列里（实测库里三种都有）。
 * 所以新增记录来源明确来源，并把旧的默认值定为 OUTPATIENT ——
 * 历史上真正可用的那几行确实指向门诊病历。
 */
public enum QcRecordSource {

    /**
     * 门诊病历
     */
    OUTPATIENT("OUTPATIENT", "门诊病历", "biz_medical_record"),

    /**
     * 住院文书（住院病历文书）
     */
    INPATIENT("INPATIENT", "住院文书", "biz_inpatient_record");

    private final String code;

    private final String text;

    private final String tableName;

    QcRecordSource(String code, String text, String tableName) {
        this.code = code;
        this.text = text;
        this.tableName = tableName;
    }

    /**
     * 解析来源码。空值按门诊病历处理（旧数据没有 record_source 列）。
     *
     * <p><b>错码值抛 {@link BusinessException}，不抛 {@code IllegalArgumentException}</b>：
     * 后者会被全局异常处理器兜成 500「系统内部错误」——前端只能弹一句无信息量的提示，
     * 排查时也看不到到底传了什么。参数错就是参数错，必须回 400 + 可选值。
     */
    public static QcRecordSource parse(String code) {
        if (code == null || code.isBlank()) {
            return OUTPATIENT;
        }
        String value = code.trim().toUpperCase();
        for (QcRecordSource source : values()) {
            if (source.code.equals(value)) {
                return source;
            }
        }
        throw new BusinessException("未知的病历来源：" + code + "（可选 OUTPATIENT-门诊病历 / INPATIENT-住院文书）");
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public String getTableName() {
        return tableName;
    }
}
