package com.his.common.service;

import com.his.common.enums.SignBizType;

/**
 * 「这类对象一共有多少、签了多少」的统计扩展点。
 *
 * <p>放在业务侧实现的原因和 {@link SignableContentProvider} 一样：
 * 签名概览里的"覆盖率"必须数业务表，而 his-common 不认识任何业务表。
 */
public interface SignCoverageProvider {

    SignBizType bizType();

    /**
     * 统计口径：合计是**当前仍然有效**的对象数（未删除）；
     * {@code pendingSign} 与 {@code invalidated} 必须分开 ——
     * "从来没签"和"签名被作废"在管理上要做的事不一样。
     */
    SignCoverage coverage();

    /**
     * @param bizType     对象类型
     * @param bizTypeText 对象类型文案
     * @param total       对象总数
     * @param signed      已签名（锚点状态为"已签名"）
     * @param pendingSign 未签名
     * @param invalidated 签名已失效
     */
    record SignCoverage(int bizType, String bizTypeText,
                        long total, long signed, long pendingSign, long invalidated) {
    }
}
