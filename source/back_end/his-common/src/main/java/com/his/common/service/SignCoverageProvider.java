package com.his.common.service;

import com.his.common.enums.SignBizTypeEnum;

/**
 * 「这类对象一共有多少、签了多少」的统计扩展点。
 */
public interface SignCoverageProvider {

    SignBizTypeEnum bizType();

    /**
     * 统计口径：合计是**当前仍然有效**的对象数（未删除）；
     * {@code pendingSign} 与 {@code invalidated} 必须分开 ——
     * "从来没签"和"签名被作废"在管理上要做的事不一样。
     */
    SignCoverage coverage();


    record SignCoverage(int bizType, String bizTypeText,
                        long total, long signed, long pendingSign, long invalidated) {
    }
}
