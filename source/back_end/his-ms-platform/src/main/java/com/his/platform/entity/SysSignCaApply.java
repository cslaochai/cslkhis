package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 签名证书 CA 签发申请记录：一次向 CA 发起的申请一行，四个阶段时间刻画流程走到了哪一步。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sign_ca_apply")
public class SysSignCaApply extends BaseEntity {

    /**
     * 证书ID
     */
    private Long certId;

    /**
     * 证书编号
     */
    private String certNo;

    /**
     * 第几次申请（同一张证书内递增，重提不换证书行）
     */
    private Integer applySeq;

    /**
     * HIS 侧申请请求号（CA 幂等键，重提时带时间戳后缀）
     */
    private String externalRequestId;

    /**
     * CA 申请单号（提交成功才有值）
     */
    private String applyNo;

    /**
     * CA 证书模板编码
     */
    private String templateCode;

    /**
     * 证书主体 DN（CN=姓名, O=机构, OU=科室）
     */
    private String subjectDn;

    /**
     * CA 申请状态（PENDING-待审核 APPROVED-审核通过待签发 ISSUED-已签发 REJECTED-已驳回 CANCELLED-已撤销 FAILED-HIS 提交失败）
     */
    private String applyStatus;

    /**
     * CA 回执说明（状态文案、审核人与意见；提交失败时为失败原因）
     */
    private String caMessage;

    /**
     * 签发该证书的 CA 名称
     */
    private String caIssuer;

    /**
     * 提交 CA 时间（本行创建即提交发起，非 CA 回执时间）
     */
    private LocalDateTime submittedAt;

    /**
     * CA 审核时间
     */
    private LocalDateTime reviewedAt;

    /**
     * CA 签发时间
     */
    private LocalDateTime issuedAt;

    /**
     * 证书回填生效时间（HIS 下载或人工回填到本系统并让证书生效的时刻）
     */
    private LocalDateTime boundAt;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;
}
