package com.his.report.support;

/**
 * 数据质量五维度（P5.3）。
 *
 * <p>这不是随便凑的五个词，而是《电子病历系统应用水平分级评价》与三甲评审里
 * 对"数据质量"的固定拆法。把它们分开的意义在于：**同一份数据在不同维度下的
 * 整改动作完全不同** ——
 * <ul>
 *   <li>完整性缺 → 补录入（临床责任）</li>
 *   <li>一致性错 → 查双写/同步逻辑（工程责任）</li>
 *   <li>及时性慢 → 查流程卡点（管理责任）</li>
 *   <li>唯一性重 → 查归并与幂等（工程责任）</li>
 *   <li>有效性假 → 查码表与校验（工程责任）</li>
 * </ul>
 * 混在一起报"有 100 个问题"，等于没报。
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

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    /** 未知维度码返回 null，由调用方决定如何显式报错，不做静默回落 */
    public static QualityDimension parse(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (QualityDimension d : values()) {
            if (d.name().equalsIgnoreCase(code.trim()) || d.text.equals(code.trim())) {
                return d;
            }
        }
        return null;
    }
}
