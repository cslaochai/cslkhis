package com.his.common.entity;

import com.his.common.util.SignCryptoUtil;

/**
 * 被签对象在"签名这一刻"的投影。
 */
public record SignSubject(
        Long bizId,
        String bizNo,
        Long patientId,
        String patientName,
        Long deptId,
        String deptName,
        Integer bizStatus,
        String bizStatusText,
        String canonicalContent) {

    /**
     * 内容摘要（SHA-256 十六进制小写）
     */
    public String digest() {
        return SignCryptoUtil.sha256Hex(canonicalContent);
    }

    /**
     * 把"上一环签名摘要"拼进本次被签内容，形成签名链。
     *
     * <p>为什么需要：医嘱双签（开立医生 + 校对护士）如果两次都只签医嘱内容本身，
     * 那么"校对之后医生又改了医嘱"这件事，两次签名都会验过 —— 因为它们各自绑的还是同一份内容。
     * 把第一环摘要写进第二环，就形成"改任何一环，后面每一环的验签都会断"。
     */
    public String contentWithPrev(String prevDigest) {
        return canonicalContent + "prevDigest=" + (prevDigest == null ? "" : prevDigest) + "\n";
    }

    /**
     * 便捷：带链的摘要
     */
    public String digestWithPrev(String prevDigest) {
        return SignCryptoUtil.sha256Hex(contentWithPrev(prevDigest));
    }
}
