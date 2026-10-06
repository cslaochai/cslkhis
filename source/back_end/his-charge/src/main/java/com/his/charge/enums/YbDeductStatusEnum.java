package com.his.charge.enums;

/**
 * 医保扣款状态码（与 sql/163 字典 his_yb_deduct_status 逐字对齐）。
 *
 * <p>字典给前端翻译名字；本枚举是服务端状态机的唯一事实来源 —— 两边以 163 为准。
 * 码值→文案的两个出口之一（另一个为 {@code DictCacheService.getDicDataLabel}），
 * 替代原 {@code support/YbDeductStatus} 的常量壳类（后者违反「support 类不得承载码值映射」）。
 */
public enum YbDeductStatusEnum {

    /**
     * 待确认：收到通知尚未申诉或确认，唯一可编辑/可作废的状态
     */
    PENDING_CONFIRM(1, "待确认"),

    /**
     * 申诉中：已提交材料，等医保局回复
     */
    APPEALING(2, "申诉中"),

    /**
     * 申诉成功：医保局撤销扣款，无需缴回
     */
    APPEAL_SUCCESS(3, "申诉成功"),

    /**
     * 维持扣款待缴：申诉驳回或直接确认，待向医保基金缴回
     */
    WAIT_PAY(4, "维持扣款待缴"),

    /**
     * 已缴回：资金退回医保基金，闭环
     */
    PAID_BACK(5, "已缴回"),

    /**
     * 已作废：误录/重复录入
     */
    CANCELLED(6, "已作废");

    private final int code;

    private final String label;

    YbDeductStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 按 code 取枚举项（模板方法名，全仓统一用 fromCode）。
     */
    public static YbDeductStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (YbDeductStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 按 code 取中文标签 —— 读取侧展示文案。
     *
     * <p>null 渲染成「—」；未知 code 返回<b>空串</b>，不伪装成某一项
     * （否则将来新增一种状态时读取侧会静默把它当成某个合法态）。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        YbDeductStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        YbDeductStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** 与库里 Integer 列比较：null 视为不匹配 */
    public boolean matches(Integer v) {
        return v != null && v == code;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
