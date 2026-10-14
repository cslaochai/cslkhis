package com.mis.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 执行结算结果（前端按 ok / error / bill 判定）
 */
@Data
public class SettleResultVO implements Serializable {

    private boolean ok;
    private String error;
    private BillVO bill;

    public static SettleResultVO ok(BillVO bill) {
        SettleResultVO v = new SettleResultVO();
        v.ok = true;
        v.bill = bill;
        return v;
    }

    public static SettleResultVO fail(String error) {
        SettleResultVO v = new SettleResultVO();
        v.ok = false;
        v.error = error;
        return v;
    }
}
