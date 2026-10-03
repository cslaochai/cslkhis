package com.his.charge.support;

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
public enum AuditResultState {

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

    AuditResultState(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 按 code 取中文标签 —— <b>读取侧必须走这里，不要自己拼</b>。
     *
     * <p>两条硬约束：</p>
     * <ol>
     *   <li>code=3 必须渲染成「不适用」，不能渲染成「通过」；</li>
     *   <li>未知 code 必须渲染成「未知」，不能兜底成「通过」——
     *       否则将来新增一种状态时，读取侧会静默把它当成通过。</li>
     * </ol>
     */
    public static String labelOf(Integer code) {
        if (code == null) {
            return "未评估";
        }
        for (AuditResultState state : values()) {
            if (state.code == code) {
                return state.label;
            }
        }
        return "未知(" + code + ")";
    }

    public static AuditResultState of(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditResultState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
