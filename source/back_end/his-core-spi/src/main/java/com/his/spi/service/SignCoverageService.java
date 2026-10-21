package com.his.spi.service;

import com.his.common.enums.SignBizTypeEnum;

/**
 * 「这类对象一共有多少、签了多少」的统计扩展点。
 */
public interface SignCoverageService {

    /**
     * 声明本实现负责统计的业务对象类型，一个实现只认领一档。
     * 用于签名概览汇总覆盖率时把各业务模块的统计按类型对齐。
     */
    SignBizTypeEnum bizType();

    /**
     * 出该对象类型的签名账：可签总数、已签数、待签数、已作废数。
     * 用于签名概览卡片区逐类型覆盖率一行的取数；分母为 0 时由展示侧显示占位而非 0%。
     */
    SignCoverage coverage();

    record SignCoverage(int bizType, String bizTypeText,
                        long total, long signed, long pendingSign, long invalidated) {
    }
}
