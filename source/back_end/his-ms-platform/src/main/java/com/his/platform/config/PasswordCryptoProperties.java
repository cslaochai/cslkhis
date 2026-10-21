package com.his.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录口令传输加密配置（yml 的 his.security.sm2.* 段）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.security.sm2")
public class PasswordCryptoProperties {

    /**
     * 16 进制 SM2 私钥（64 个 hex 字符）。留空则启动时临时生成一对。
     */
    private String privateKey = "";

    /**
     * 16 进制 SM2 公钥（130 个 hex 字符，含 04 前缀）。留空则从私钥推导，一般为免手抄才配。
     */
    private String publicKey = "";
}
