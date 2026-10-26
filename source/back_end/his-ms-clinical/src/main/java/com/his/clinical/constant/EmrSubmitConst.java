package com.his.clinical.constant;

/**
 * 门诊结诊接口的约定常量：前后端都要按同一个口径认，不能各写各的字面量。
 */
public interface EmrSubmitConst {

    /**
     * 响应体 code：结诊被定稿质控的否决项拦下，等医生决策（HTTP 状态仍是 200，本次什么都没落库）。
     *
     * <p>前端认这个码才知道「这不是错误，是要弹框让医生选『去修改』还是『仍要结诊』」——
     * 带 forceSubmit=true 重发同一条结诊就放行。
     */
    int QC_CONFIRM_CODE = 409;
}
