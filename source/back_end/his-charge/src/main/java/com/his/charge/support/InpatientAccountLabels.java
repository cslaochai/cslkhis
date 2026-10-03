package com.his.charge.support;

import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.enums.SettlementModeEnum;

/**
 * 住院账务（预交金 / 日清单 / 结算）的码值文案。
 *
 * <p>文案一律由后端给，前端不自己判码值 —— 前端判一次、后端判一次就会有第三套口径。
 *
 * <p><b>支付方式与结算方式必须从枚举读，不许在这里再抄一份 switch</b>：
 * 本类原先自己写了一套"4-银行卡、5-转账"，而字典权威
 * {@link PaymentMethodEnum} 是"4-医保个账、5-院内余额、6-银行卡、7-转账"，
 * 结果预交金列表把刷医保卡显示成刷银行卡（裸数字抄第二份必然漂移，与 G12/G14 同一条病）。
 *
 * <p><b>未知码值渲染成「未知(码值)」，不回落到合法值</b>：把 99 显示成"充值"，
 * 等于替数据撒谎（与「未判定 ≠ 正常」同一条原则）。
 */
public final class InpatientAccountLabels {

    private InpatientAccountLabels() {
    }

    /**
     * 流水类型文案
     */
    public static String prepayTypeText(Integer type) {
        if (type == null) {
            return "—";
        }
        return switch (type) {
            case 1 -> "充值";
            case 2 -> "退款";
            default -> "未知(" + type + ")";
        };
    }

    /**
     * 支付方式文案（字典 {@code his_pay_method} 权威在 {@link PaymentMethodEnum}）
     */
    public static String payMethodText(Integer method) {
        if (method == null) {
            return "—";
        }
        PaymentMethodEnum e = PaymentMethodEnum.getByCode(method);
        return e == null ? "未知(" + method + ")" : e.getDesc();
    }

    /**
     * 结算状态文案（派生值：账单应缴与已收比出来的，不是库里的一列）
     */
    public static String settleStatusText(Integer status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case 1 -> "已结清";
            case 2 -> "欠费";
            default -> "未知(" + status + ")";
        };
    }

    /**
     * 结算方式文案（字典 {@code his_settlement_mode} 权威在 {@link SettlementModeEnum}）
     */
    public static String settleModeText(Integer mode) {
        if (mode == null) {
            return "—";
        }
        SettlementModeEnum e = SettlementModeEnum.getByCode(mode);
        return e == null ? "未知(" + mode + ")" : e.getDesc();
    }

    /**
     * 收费明细项目类型文案（复用既有枚举，不另立一套）
     */
    public static String itemTypeText(Integer itemType) {
        PaymentItemTypeEnum e = PaymentItemTypeEnum.getByCode(itemType);
        if (e != null) {
            return e.getDesc();
        }
        return itemType == null ? "—" : "未知(" + itemType + ")";
    }
}
