package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 可信时间戳 TSA 服务注册。
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

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
