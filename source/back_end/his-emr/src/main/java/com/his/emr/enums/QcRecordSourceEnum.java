package com.his.emr.enums;

import com.his.common.exception.BusinessException;
import lombok.Getter;

/**
 * 质控对象来自哪张表。
 *
 * <p>质控检查记录的记录ID 原先是一个"看运气才知道 JOIN 哪张表"的裸 ID：
 * 门诊病历、住院文书、病案归档表的 ID 混在一列里（实测库里三种都有）。
 * 所以新增记录来源明确来源，并把旧的默认值定为 OUTPATIENT ——
 * 历史上真正可用的那几行确实指向门诊病历。
 */
@Getter
public enum QcRecordSourceEnum {

    /**
     * 门诊病历
     */
    OUTPATIENT("OUTPATIENT", "门诊病历", "biz_medical_record"),

    /**
     * 住院文书（住院病历文书）
     */
    INPATIENT("INPATIENT", "住院文书", "biz_inpatient_record");

    private final String code;

    private final String label;

    private final String tableName;

    QcRecordSourceEnum(String code, String label, String tableName) {
        this.code = code;
        this.label = label;
        this.tableName = tableName;
    }

    /**
     * 解析来源码。空值按门诊病历处理（旧数据没有 record_source 列）。
     *
     * <p><b>错码值抛 {@link BusinessException}，不抛 {@code IllegalArgumentException}</b>：
     * 后者会被全局异常处理器兜成 500「系统内部错误」——前端只能弹一句无信息量的提示，
     * 排查时也看不到到底传了什么。参数错就是参数错，必须回 400 + 可选值。
     */
    public static QcRecordSourceEnum parse(String code) {
        if (code == null || code.isBlank()) {
            return OUTPATIENT;
        }
        String value = code.trim().toUpperCase();
        for (QcRecordSourceEnum source : values()) {
            if (source.code.equals(value)) {
                return source;
            }
        }
        throw new BusinessException("未知的病历来源：" + code + "（可选 OUTPATIENT-门诊病历 / INPATIENT-住院文书）");
    }

    /**
     * 纯解析：不兜默认值、不抛异常。null / 空白 / 不在枚举内（脏数据）返回 null。
     */
    public static QcRecordSourceEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String value = code.trim().toUpperCase();
        for (QcRecordSourceEnum source : values()) {
            if (source.code.equals(value)) {
                return source;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案（record_source 列文案唯一出口）。
     * null / 空白 / 不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(String code) {
        QcRecordSourceEnum source = fromCode(code);
        return source == null ? "" : source.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null / 空白 / 不在枚举内返回「未知(原始码)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(String code) {
        QcRecordSourceEnum source = fromCode(code);
        return source == null ? (code == null ? "未知" : "未知(" + code + ")") : source.label;
    }
}
