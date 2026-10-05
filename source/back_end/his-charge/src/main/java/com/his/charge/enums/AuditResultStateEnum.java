package com.his.charge.enums;

/**
 * 规则判定结果 —— <b>刻意是三态，不是布尔</b>。
 * <p>
 * 与检验判定那条铁律同源（「未判定 ≠ 正常」）：
 * 一条规则如果因为<b>拿不到依据</b>而没能评估，它既不是「命中」也不是「通过」。
 * 把「没评估」渲染成「通过」，会让整份合规报告系统性偏乐观，
 * 而这恰恰是医保飞检最容易抓的地方 —— 你以为系统审过了，其实它什么都没看。
 * <p>
 * 因此凡返回 {@link #NOT_APPLICABLE}，<b>必须</b>在 evidence 里写明缺的是哪一类依据。
 */
public enum AuditResultStateEnum {

    /**
     * 命中：规则判定存在问题
     */
    HIT(1, "命中"),

    /**
     * 通过：依据齐全，规则明确评估过且未发现问题
     */
    PASS(2, "通过"),

    /**
     * 不适用：缺少评估所需依据，<b>规则没有评估</b>（不得当作通过）
     */
    NOT_APPLICABLE(3, "不适用");

    private final int code;

    private final String label;

    AuditResultStateEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 按 code 取枚举项（模板方法名，全仓统一用 fromCode）。
     */
    public static AuditResultStateEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditResultStateEnum state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 按 code 取中文标签 —— <b>读取侧必须走这里，不要自己拼</b>。
     *
     * <p>两条硬约束：</p>
     * <ol>
     *   <li>code=3 必须渲染成「不适用」，不能渲染成「通过」；</li>
     *   <li>未知 code 返回<b>空串</b>，不能兜底成「通过」——
     *       否则将来新增一种状态时，读取侧会静默把它当成通过。</li>
     * </ol>
     *
     * <p>null（未评估）渲染成「未评估」而不是「不适用」：前者是"还没看"，后者是"看了，不适用"。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "未评估";
        }
        AuditResultStateEnum state = fromCode(code);
        return state == null ? "" : state.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        AuditResultStateEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** @deprecated 用 {@link #fromCode(Integer)}（模板方法名） */
    @Deprecated
    public static AuditResultStateEnum of(Integer code) {
        return fromCode(code);
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
