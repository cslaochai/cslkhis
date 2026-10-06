package com.his.common.enums;

/**
 * 结算账单状态（字典 {@code his_bill_status}，落在结算账单的账单状态列）。
 *
 * <p>已支付与否不由收费员点一下按钮就翻状态，而是 SUM(成功收款流水) 与应缴额比出来的：
 * 没收够是 PARTIAL，收够才 PAID。VOID 只解锁记账行、不动资金；已付账单要退走 REFUNDED（配退款流水）。
 */
public enum BillStatusEnum {

    UNPAID(1, "待支付"),
    PARTIAL_PAID(2, "部分支付"),
    PAID(3, "已支付"),
    VOIDED(4, "已作废"),
    REFUNDED(5, "已退费");

    private final Integer code;
    private final String desc;

    BillStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BillStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BillStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String descOf(Integer code) {
        BillStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 已收回的钱还能不能再收（作废/退完的单一率拒收，防止重复收同一笔钱）
     */
    public boolean payable() {
        return this == UNPAID || this == PARTIAL_PAID;
    }
}
