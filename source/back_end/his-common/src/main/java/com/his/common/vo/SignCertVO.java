package com.his.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签名证书展示对象。
 */
@Data
public class SignCertVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 证书编号
     */
    private String certNo;

    /**
     * 签名人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    /**
     * 签名人姓名
     */
    private String empName;

    /**
     * BIGINT 出参一律转字符串：雪花 ID 超过 JS 安全整数范围，Number 化会静默改值
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 密钥算法
     */
    private String keyAlgo;
    /**
     * 摘要算法
     */
    private String digestAlgo;
    /**
     * 签名算法
     */
    private String signAlgo;

    /**
     * 公钥指纹
     */
    private String keyFingerprint;

    /**
     * 指纹分组展示（每 4 位一组，便于人工核对）
     */
    private String keyFingerprintGroups;

    /**
     * 签发方式（1-人工签发 2-系统自动签发）
     */
    private Integer issuedMode;
    private String issuedModeText;

    /**
     * 证书状态（1-有效 2-已吊销）
     */
    private Integer certStatus;
    private String certStatusText;

    /**
     * 生效时间
     */
    private LocalDateTime validFrom;
    /**
     * 失效时间
     */
    private LocalDateTime validTo;

    /**
     * 是否已过期（有效期过了但状态还是"有效"时要单独提示 —— 两个事实别混）
     */
    private Boolean expired;

    /**
     * 吊销原因
     */
    private String revokeReason;
    /**
     * 吊销时间
     */
    private LocalDateTime revokeTime;
    /**
     * 吊销操作人姓名
     */
    private String revokeByName;

    /**
     * 最近一次使用时间
     */
    private LocalDateTime lastUsedTime;
    /**
     * 累计签名次数
     */
    private Integer signCount;

    /**
     * 公钥 PEM（详情接口回填）
     */
    private String publicKey;

    /**
     * 是否可吊销
     */
    private Boolean canRevoke;

    private String actionHint;
}
