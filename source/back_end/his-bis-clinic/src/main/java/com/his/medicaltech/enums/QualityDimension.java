package com.his.medicaltech.enums;

import com.his.common.util.TextUtil;

/**
 * 数据质量五维度（P5.3）。
 */
public enum QualityDimension {

    COMPLETENESS("完整性", "该记的没记 —— 必填要素、必备文书、必备关联记录存在空缺"),
    CONSISTENCY("一致性", "同一件事在两处对不上 —— 跨表字段、主表与明细、文本与结构化"),
    TIMELINESS("及时性", "该按时做的没按时做 —— 超时限未完成、超期未归档、超时未处置"),
    UNIQUENESS("唯一性", "本该唯一的不唯一 —— 重复档案、重复文书、重复结果行"),
    VALIDITY("有效性", "记了但不可信 —— 码值越界、格式非法、日期倒挂、金额非法");

    private final String text;
    private final String description;

    QualityDimension(String text, String description) {
        this.text = text;
        this.description = description;
    }

    /**
     * 未知维度码返回 null，由调用方决定如何显式报错，不做静默回落
     */
    public static QualityDimension parse(String code) {
        if (!TextUtil.hasText(code)) {
            return null;
        }
        for (QualityDimension d : values()) {
            if (d.name().equalsIgnoreCase(code.trim()) || d.text.equals(code.trim())) {
                return d;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }
}
