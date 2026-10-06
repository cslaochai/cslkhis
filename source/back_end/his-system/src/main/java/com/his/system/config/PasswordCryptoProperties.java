package com.his.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录口令传输加密配置（yml 的 {@code his.security.sm2.*} 段）。
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.security.sm2")
public class PasswordCryptoProperties {

    /**
     * 16 进制 SM2 私钥（64 个 hex 字符）。留空则启动时临时生成一对。
     *
     * <p><b>生产必须显式配置</b>：留空时每次启动都是新密钥，多实例部署会因为实例间密钥不同
     * 而随机解密失败（LB 打到哪个实例全看运气），症状是"登录时好时坏"。
     */
    private String privateKey = "";

    /**
     * 16 进制 SM2 公钥（130 个 hex 字符，含 04 前缀）。留空则从私钥推导，一般为免手抄才配。
     */
    private String publicKey = "";
}
