package com.his.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 可信时间戳 TSA 服务注册。
 *
 * <p>本期只有一行：本地内置 TSA（{@code tsa_code=LOCAL}）。
 * 密钥保护与 {@link SysSignCert} 完全同套（PBKDF2+AES-GCM），
 * 私钥密文列名也对齐（protected_private_key / key_salt / key_iterations），
 * 便于以后把两套密钥托管合并成一个密钥库。
 *
 * <p>本表**不提供业务删除**：TSA 密钥换了，用它签过的历史令牌就验不了
 * （除非保留旧公钥）。停用走 {@code tsa_status=0}，行保留。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tsa_server")
public class SysTsaServer extends BaseEntity {

    /**
     * TSA 服务编码（唯一，本期固定 LOCAL）
     */
    private String tsaCode;

    /**
     * TSA 服务名称（页面展示）
     */
    private String tsaName;

    /**
     * TSA 公钥（PEM）
     */
    private String publicPem;

    /**
     * 公钥指纹（SHA-256 十六进制）
     */
    private String keyFingerprint;

    /**
     * 私钥密文（PBKDF2+AES-GCM，Base64(iv||cipher)）
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
     * 状态（0-停用 1-启用，his_enable_status）
     */
    private Integer tsaStatus;
}
