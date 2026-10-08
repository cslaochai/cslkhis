package com.his.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 员工电子签名证书。
 *
 * <p>字段与表**完全对齐**（多一列 → 全表 select 直接 500）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sign_cert")
public class SysSignCert extends BaseEntity {

    /**
     * 证书编号（CERT + yyyyMMdd + 4位序号）
     */
    private String certNo;

    /**
     * 签名人员工ID（员工档案主键，不是系统用户主键）
     */
    private Long empId;

    /**
     * 签名人姓名
     */
    private String empName;

    /**
     * 所属科室ID
     */
    private Long deptId;
    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 密钥算法（RSA2048）
     */
    private String keyAlgo;

    /**
     * 摘要算法（SHA256）
     */
    private String digestAlgo;

    /**
     * 签名算法（SHA256withRSA）
     */
    private String signAlgo;

    /**
     * 公钥（PEM）
     */
    private String publicKey;

    /**
     * 公钥指纹（SHA-256 十六进制）
     */
    private String keyFingerprint;

    /**
     * 私钥密文（PBKDF2 + AES-256-GCM）
     */
    private String protectedPrivateKey;

    /**
     * 私钥派生盐（Base64）
     */
    private String keySalt;

    /**
     * 私钥派生迭代次数
     */
    private Integer keyIterations;

    /**
     * 签发方式（1-人工签发 2-系统自动签发）
     */
    private Integer issuedMode;

    /**
     * 证书状态（1-有效 2-已吊销）
     */
    private Integer certStatus;

    /**
     * 生效时间
     */
    private LocalDateTime validFrom;
    /**
     * 失效时间
     */
    private LocalDateTime validTo;

    /**
     * 吊销原因
     */
    private String revokeReason;
    /**
     * 吊销时间
     */
    private LocalDateTime revokeTime;
    /**
     * 吊销操作人员工ID
     */
    private Long revokeBy;
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
}
