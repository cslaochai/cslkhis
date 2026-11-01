package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 签名证书的一次 CA 申请记录：证书详情里「CA 申请记录」时间线的一行。
 *
 * <p>一张证书可能向 CA 发过好几单（驳回、提交失败后重提各算一单），每单带回自己走到了哪一步、
 * 各阶段的时刻与 CA 的说明；证书行上的申请单状态只是当前挂着的那一单。</p>
 */
@Data
public class SignCaApplyRecordVO implements Serializable {

    /**
     * 第几次申请（1 起）
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
     * CA 申请状态文案（由枚举 label 渲染）
     */
    private String applyStatusText;

    /**
     * CA 回执说明（审核意见 / 提交失败原因）
     */
    private String caMessage;

    /**
     * 签发该证书的 CA 名称（证书回填后才有）
     */
    private String caIssuer;

    /**
     * 提交 CA 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

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
     * 证书回填生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime boundAt;

    /**
     * 发起本次申请的操作人
     */
    private String operatorName;
}
