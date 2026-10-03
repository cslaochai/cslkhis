package com.his.common.service;


public interface ExternalCaChannelService {

    /** 证书签名请求（CSR 的核心三要素；完整 PKCS#10 编码由外发实现自建） */
    public record IssueRequest(String subjectDn, String publicKeyPem, int validDays) {
    }

    /** CA 签发结果 */
    public record IssuedCert(String certPem, String caSerial, String caName) {
    }

    String name();

    boolean available();

    IssuedCert issueCert(IssueRequest request);
}
