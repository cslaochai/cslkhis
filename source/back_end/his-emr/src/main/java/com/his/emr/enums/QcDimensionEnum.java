package com.his.emr.enums;

/**
 * 病历形式质控的三个维度。
 *
 * <p><b>维度即质控类型</b>：编码与质控检查记录的质控类型同值
 * （1=完整性 2=规范性 3=逻辑性），明细表质控问题明细.dimension 也用它。
 * 不再另立一套"维度编码"—— 两套编码最终一定会对不上，然后前端的维度筛选就静默失效。
 *
 * <p>AI 内涵质控（qc_type=4）一次覆盖三个维度，不属于本枚举；
 * 它由 his-ai 的 {@code EmrQcCapability} 负责，两者并存而不是互相替代：
 * 规则负责"确定的、可解释的、降级也能跑"的部分，模型负责"规则写不完"的部分。
 */
public enum QcDimensionEnum {

    /**
     * 完整性：该有的要素有没有
     */
    COMPLETENESS(1, "完整性", "必填要素是否落实到病历原文"),

    /**
     * 规范性：写得合不合规范
     */
    REGULARITY(2, "规范性", "书写格式与用词是否合规"),

    /**
     * 逻辑性：内容之间自不自洽
     */
    LOGIC(3, "逻辑性", "内容与事实、时间、性别年龄之间是否矛盾");

    private final int code;

    private final String text;

    private final String description;

    QcDimensionEnum(int code, String text, String description) {
        this.code = code;
        this.text = text;
        this.description = description;
    }

    /**
     * 按质控类型取维度；不认识的值返回 null 由调用方决定怎么处理
     */
    public static QcDimensionEnum ofCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QcDimensionEnum dimension : values()) {
            if (dimension.code == code) {
                return dimension;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        QcDimensionEnum dimension = ofCode(code);
        return dimension == null ? "" : dimension.text;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        QcDimensionEnum item = code == null ? null : ofCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }
}
