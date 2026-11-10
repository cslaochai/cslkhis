package com.his.ca.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * CA 推送事件 DTO：申请单状态变更推送的报文体。
 *
 * <p>外部系统（HIS）收到推送后反序列化为这个对象，配合 {@link com.his.ca.sdk.CaPushVerifier} 验签。</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaPushEvent {

    /** 推送事件唯一 ID（UUID） */
    private String eventId;

    /** 申请单号 */
    private String applyNo;

    /** 外部业务请求 ID（提交时外部系统自己带的） */
    private String externalRequestId;

    /** 申请单状态：SUBMITTED / APPROVED / REJECTED / ISSUED / REVOKED */
    private String status;

    /** 状态中文描述 */
    private String statusText;

    /** 审批人姓名 */
    private String reviewedByName;

    /** 审批意见 */
    private String reviewComment;

    /** 审批时间（yyyy-MM-dd HH:mm:ss） */
    private String reviewedAt;

    /** 签发时间（yyyy-MM-dd HH:mm:ss） */
    private String issuedAt;

    /** 签发成功时携带的证书信息 */
    private CertificateInfo certificate;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getApplyNo() { return applyNo; }
    public void setApplyNo(String applyNo) { this.applyNo = applyNo; }

    public String getExternalRequestId() { return externalRequestId; }
    public void setExternalRequestId(String externalRequestId) { this.externalRequestId = externalRequestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getIssuedAt() { return issuedAt; }
    public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }

    public CertificateInfo getCertificate() { return certificate; }
    public void setCertificate(CertificateInfo certificate) { this.certificate = certificate; }

    /**
     * 签发证书信息（仅 status=ISSUED 时出现）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CertificateInfo {
        private String serialHex;
        private String subjectDn;
        private String issuerDn;
        private String algorithm;
        private String notBefore;
        private String notAfter;
        private String certPem;
        private String chainPem;

        public String getSerialHex() { return serialHex; }
        public void setSerialHex(String serialHex) { this.serialHex = serialHex; }

        public String getSubjectDn() { return subjectDn; }
        public void setSubjectDn(String subjectDn) { this.subjectDn = subjectDn; }

        public String getIssuerDn() { return issuerDn; }
        public void setIssuerDn(String issuerDn) { this.issuerDn = issuerDn; }

        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

        public String getNotBefore() { return notBefore; }
        public void setNotBefore(String notBefore) { this.notBefore = notBefore; }

        public String getNotAfter() { return notAfter; }
        public void setNotAfter(String notAfter) { this.notAfter = notAfter; }

        public String getCertPem() { return certPem; }
        public void setCertPem(String certPem) { this.certPem = certPem; }

        public String getChainPem() { return chainPem; }
        public void setChainPem(String chainPem) { this.chainPem = chainPem; }
    }
}
