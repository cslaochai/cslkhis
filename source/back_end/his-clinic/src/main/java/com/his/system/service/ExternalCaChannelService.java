package com.his.system.service;


/**
 * 外部证书机构签发通道：签名信任根来自院外 CA 时的外发能力出口。
 */
public interface ExternalCaChannelService {

    /**
     * 通道名称。
     * 供证书签发状态与外发探针结果向操作员标明当前对接的是哪家机构。
     */
    String name();

    /**
     * 外部 CA 通道当前是否启用（由签发模式配置决定）。
     * 签发前先探这一句：启用则走外发证书、绝不回退院内自签；未启用走院内自签。
     */
    boolean available();

    /**
     * 向证书机构提交证书签名请求，换回签发好的证书。
     * 人工签发证书与对接联调的外发探针都会走到这一步。
     *
     * @param request CSR 三要素（主题标识、公钥、有效期天数），完整编码由实现自建
     * @return 签发结果；未接入真机构时返回 null，调用方必须中断签发而非回退自签
     */
    IssuedCert issueCert(IssueRequest request);

    /**
     * 证书签名请求（CSR 的核心三要素；完整 PKCS#10 编码由外发实现自建）
     */
    public record IssueRequest(String subjectDn, String publicKeyPem, int validDays) {
    }

    /**
     * CA 签发结果
     */
    public record IssuedCert(String certPem, String caSerial, String caName) {
    }
}
