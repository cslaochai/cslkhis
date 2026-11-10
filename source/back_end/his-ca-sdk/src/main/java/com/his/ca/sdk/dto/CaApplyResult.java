package com.his.ca.sdk.dto;

/**
 * 证书申请结果
 */
public class CaApplyResult {
    /** 申请单号 */
    private String applyNo;
    /** 申请状态：PENDING / APPROVED / REJECTED / ISSUED / REVOKED */
    private String status;
    /** 证书序列号（签发后才有） */
    private String certSerial;
    /** 证书 PEM（签发后才有） */
    private String certPem;
    /** 是否幂等重放 */
    private boolean idempotentReplay;

    public String getApplyNo() { return applyNo; }
    public void setApplyNo(String applyNo) { this.applyNo = applyNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCertSerial() { return certSerial; }
    public void setCertSerial(String certSerial) { this.certSerial = certSerial; }
    public String getCertPem() { return certPem; }
    public void setCertPem(String certPem) { this.certPem = certPem; }
    public boolean isIdempotentReplay() { return idempotentReplay; }
    public void setIdempotentReplay(boolean idempotentReplay) { this.idempotentReplay = idempotentReplay; }
}
