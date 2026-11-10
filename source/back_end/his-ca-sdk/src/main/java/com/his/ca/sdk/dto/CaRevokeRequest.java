package com.his.ca.sdk.dto;

/**
 * 证书吊销请求
 */
public class CaRevokeRequest {
    /** 证书序列号 */
    private String certSerial;
    /** 操作人姓名 */
    private String operatorName;
    /** 吊销原因 */
    private String reason;

    public String getCertSerial() { return certSerial; }
    public void setCertSerial(String certSerial) { this.certSerial = certSerial; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
