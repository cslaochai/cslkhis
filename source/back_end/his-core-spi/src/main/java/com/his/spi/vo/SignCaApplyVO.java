package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * CA 签发申请结果：在线申请（csssl RA）的申请单状态，或线下人工送 CA 的申请单凭证。
 *
 * <p>在线模式下，密钥对生成后 HIS 直接向 CA 提交申请（externalRequestId = certNo），
 * 本对象带回 CA 申请单号与状态；CA 签发后 HIS 自动下载证书并回填生效。
 * 未接入或提交失败时退回线下流程：管理员拿 Subject DN 与公钥 PEM 线下送 CA，
 * 再按 certId 回填证书。私钥始终留在 HIS。</p>
 */
@Data
public class SignCaApplyVO implements Serializable {

    /**
     * 证书ID（刷新状态 / 回填证书时用这个定位那一行）
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
     * 证书状态（0-待CA签发 1-有效 2-已吊销）
     */
    private Integer certStatus;

    /**
     * 证书状态文案
     */
    private String certStatusText;

    /**
     * 是否已发起 CA 在线申请
     */
    private Boolean online;

    /**
     * CA 在线申请单号（未提交成功时为空）
     */
    private String applyNo;

    /**
     * CA 申请单状态（PENDING/APPROVED/ISSUED/REJECTED/CANCELLED/FAILED）
     */
    private String applyStatus;

    /**
     * CA 申请单状态文案
     */
    private String applyStatusText;

    /**
     * CA 最近一次状态说明 / 提交失败原因
     */
    private String applyMessage;

    /**
     * 本次使用的 CA 模板编码
     */
    private String templateCode;

    /**
     * 操作提示（在线等待签发 / 提交失败可重试 / 线下流程说明）
     */
    private String guide;
}

