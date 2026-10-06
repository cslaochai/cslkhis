package com.his.common.entity;

import com.his.common.util.SignCryptoUtil;

/**
 * 被签对象在"签名这一刻"的投影。
 *
 * <p>由业务模块的 {@link SignableContentProvider} 组装。刻意做成一份**扁平快照**而不是
 * 直接把实体交出去：his-common 不认识任何业务实体，签名层也只有这份快照可用，
 * 于是"签名层顺手改了业务字段"这种事在结构上就不可能发生。
 *
 * @param bizId            对象ID
 * @param bizNo            对象单号（快照，可为空）
 * @param patientId        患者ID（可为空）
 * @param patientName      患者姓名（快照）
 * @param deptId           对象所属科室ID
 * @param deptName         对象所属科室名称
 * @param bizStatus        对象自身的业务状态码（如病历 record_status）
 * @param bizStatusText    对象业务状态文案（由业务侧翻译好，未知码值要渲染成"未知(n)"）
 * @param canonicalContent **规范化后的被签内容全文**（由 {@code CanonicalText} 生成）
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
