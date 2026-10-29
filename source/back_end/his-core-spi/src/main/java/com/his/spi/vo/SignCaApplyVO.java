package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * CA 签发申请单（线下人工送 CA 的凭证）。
 *
 * <p>管理员在 HIS 生成密钥对后拿到本单据，把其中的 Subject DN 与公钥 PEM 线下送
 * CA 认证中心人工签发；CA 返回证书 PEM 后再用申请单上的 certId 回填。
 * 私钥不在本单据内，也不离开 HIS。
 */
@Data
public class SignCaApplyVO implements Serializable {

    /**
     * 证书ID（回填证书时用这个定位那一行）
     */
    private Long certId;

    /**
     * 证书编号
     */
    private String certNo;

    /**
     * 签名人姓名
     */
    private String empName;

    /**
     * CA 签发申请单主体 DN（CN=姓名, O=机构, OU=科室）
     */
    private String subjectDn;

    /**
     * 公钥 PEM（送 CA 签发的就是这个）
     */
    private String publicKeyPem;

    /**
     * 公钥指纹（每 4 位一组，供人工核对）
     */
    private String keyFingerprintGroups;

    /**
     * 建议有效期天数
     */
    private Integer validDays;

    /**
     * 证书状态文案
     */
    private String certStatusText;

    /**
     * 操作提示（线下流程说明）
     */
    private String guide;
}
