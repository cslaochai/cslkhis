package com.his.ca.sdk.dto;

/**
 * 证书申请请求
 */
public class CaApplyRequest {
    /** 外部业务单号（幂等用，重复提交返回同一结果） */
    private String externalRequestId;
    /** 申请人姓名 */
    private String applicantName;
    /** 申请人身份证号 */
    private String applicantIdCard;
    /** 申请人手机号 */
    private String applicantPhone;
    /** 证书模板编码（如 DOCTOR_SIGN） */
    private String templateCode;
    /** 申请人类别：INSTITUTION / PERSON */
    private String applicantType;
    /** 公司统一社会信用代码 */
    private String companyUscc;
    /** 备注 */
    private String remark;

    public String getExternalRequestId() { return externalRequestId; }
    public void setExternalRequestId(String externalRequestId) { this.externalRequestId = externalRequestId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApplicantIdCard() { return applicantIdCard; }
    public void setApplicantIdCard(String applicantIdCard) { this.applicantIdCard = applicantIdCard; }
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getApplicantType() { return applicantType; }
    public void setApplicantType(String applicantType) { this.applicantType = applicantType; }
    public String getCompanyUscc() { return companyUscc; }
    public void setCompanyUscc(String companyUscc) { this.companyUscc = companyUscc; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
