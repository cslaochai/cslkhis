package com.his.system.service;

import com.his.common.enums.SignBizTypeEnum;

/**
 * 「这类对象一共有多少、签了多少」的统计扩展点。
 */
public interface SignCoverageService {

    SignBizTypeEnum bizType();

    SignCoverage coverage();

    record SignCoverage(int bizType, String bizTypeText,
                        long total, long signed, long pendingSign, long invalidated) {
    }
}
