package com.his.system.support;

/**
 * 码值翻人话的小工具（字段级留痕专用）。
 *
 * <p>日志里写 {@code gender: 1 → 2} 没用，审计员看不懂；写性别: 男 → 女才叫审计。
 * 所以所有码值字段在落库前都要过一遍这里。
 *
 * <p><b>认不出的码值一律原样输出，绝不回落成列表第一项</b> ——
 * 那会把"未知/异常"渲染成某个合法值，日志看着漂亮，实际是伪造的（前后端同一个坑，
 * 见 MEMORY「枚举双口径：未知值被渲染成某个合法值」）。
 */
public final class CodeText {

    private CodeText() {
    }

    /**
     * 1-based 码值：1 → 第一个文本，2 → 第二个……
     *
     * @param v     字段原值（Integer 码值）
     * @param texts 从 1 开始依次对应的中文文本
     */
    public static String of(Object v, String... texts) {
        if (v instanceof Integer i && i >= 1 && i <= texts.length) {
            return texts[i - 1];
        }
        return v == null ? null : String.valueOf(v);
    }

    /**
     * 0 开头的启用状态口径（0-停用/禁用，1-启用）。
     *
     * <p>单独一个方法是因为 {@link #of} 是 1-based，硬把"停用"塞在第一个位置会让
     * {@code status=1} 渲染成"停用" —— 这种错位在日志里看不出来，查的时候才发现全反了。
     */
    public static String enable(Object v) {
        if (Integer.valueOf(1).equals(v)) {
            return "启用";
        }
        if (Integer.valueOf(0).equals(v)) {
            return "停用";
        }
        return v == null ? null : String.valueOf(v);
    }
}
