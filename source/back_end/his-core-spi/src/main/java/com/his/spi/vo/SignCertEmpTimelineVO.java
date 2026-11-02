package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工证书操作时间线的一行：员工名下所有证书的 CA 申请流水与吊销事件合成一条履历。
 *
 * <p>CA 申请（含每次重提）是 sys_sign_ca_apply 一单一行；吊销是证书行上的
 * revoke_* 字段（吊销不落申请表，避免打乱申请状态机）。两类事件在这里统一成
 * 「一条时间线一种事件」，按事件发生时间倒序。</p>
 */
@Data
public class SignCertEmpTimelineVO implements Serializable {

    /**
     * 事件类型（CA_APPLY-CA申请单 REVOKE-吊销）
     */
    private String eventType;

    /**
     * 事件发生时间（CA 申请=提交时刻，吊销=吊销时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;

    /**
     * 证书编号（事件挂在哪张证书上）
     */
    private String certNo;

    // ---------------- CA 申请事件字段（REVOKE 事件为空） ----------------

    /**
     * 第几次申请（同一张证书内递增）
     */
    private Integer applySeq;

    /**
     * CA 申请单号（提交未成功时为空）
     */
    private String applyNo;

    /**
     * CA 申请状态码（PENDING/APPROVED/ISSUED/REJECTED/CANCELLED/FAILED）
     */
    private String applyStatus;

    /**
     * CA 申请状态文案
     */
    private String applyStatusText;

    /**
     * CA 回执说明（审核意见 / 提交失败原因）
     */
    private String caMessage;

    /**
     * 签发该证书的 CA 名称（回填后才有）
     */
    private String caIssuer;

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
     * 证书回填生效时间（这一单走完的标志）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime boundAt;

    /**
     * 发起本次申请的操作人
     */
    private String operatorName;

    // ---------------- 吊销事件字段（CA_APPLY 事件为空） ----------------

    /**
     * 吊销原因
     */
    private String revokeReason;

    /**
     * 吊销操作人姓名
     */
    private String revokeByName;
}
