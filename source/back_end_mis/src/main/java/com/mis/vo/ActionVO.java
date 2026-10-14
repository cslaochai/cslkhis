package com.mis.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用动作结果（前端按 ok / matched 判定）
 */
@Data
public class ActionVO implements Serializable {

    private boolean ok;
    private String error;
    private Integer matched;

    public static ActionVO ok() {
        ActionVO v = new ActionVO();
        v.ok = true;
        return v;
    }

    public static ActionVO fail(String error) {
        ActionVO v = new ActionVO();
        v.ok = false;
        v.error = error;
        return v;
    }

    public static ActionVO matched(int n) {
        ActionVO v = new ActionVO();
        v.ok = true;
        v.matched = n;
        return v;
    }
}
