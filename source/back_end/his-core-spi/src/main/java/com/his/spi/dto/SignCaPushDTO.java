package com.his.spi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CA 认证中心向 HIS 推送申请单状态变更的报文。
 *
 * <p>HIS 不再主动查询 CA（无查询权限），申请单从 PENDING 到 APPROVED/REJECTED/CANCELLED，
 * 以及最终 ISSUED 都由 CA 推送；同一状态重复推送按幂等重放处理，不允许状态回退或跳变。</p>
 */
@Data
public class SignCaPushDTO {

    /**
     * 推送事件号（CA 侧生成；同一次状态变更重试时保持不变，便于排重）
     */
    private String eventId;

    /**
     * CA 申请单号
     */
    @NotBlank(message = "applyNo 不能为空")
    private String applyNo;

    /**
     * HIS 侧幂等请求号（CA 提交时携带，便于按 applyNo 找不到时反查）
     */
    private String externalRequestId;

    /**
     * 目标状态：APPROVED / REJECTED / ISSUED / CANCELLED
     */
    @NotBlank(message = "status 不能为空")
    private String status;

    /**
     * 状态文案（CA 侧展示口径）
     */
    private String statusText;

    /**
     * 审核人姓名
     */
    private String reviewedByName;

    /**
     * 审核意见 / 驳回原因
     */
    private String reviewComment;

    /**
     * CA 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    /**
     * CA 签发时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedAt;

    /**
     * 已签发时随报文下发的证书信息（其他状态为空）
     */
    @Valid
    private Certificate certificate;

    /**
     * CA 签发证书：PEM 与有效期均由 CA 组装，HIS 只校验公钥后回填。
     */
    @Data
    public static class Certificate {

        private String serialHex;
        private String subjectDn;
        private String issuerDn;
        private String algorithm;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime notBefore;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime notAfter;
        private String fingerprintSha256;
        private String certPem;
        private String chainPem;
    }
}
