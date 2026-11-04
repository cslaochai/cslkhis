package com.his.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 医院身份配置（his.organization / his.organization-credit-code）。
 *
 * <p>两项都不随部署环境变化，连同环境变量占位一起在 application.yml 声明一次，dev / pro 不重复。
 * 机构名写入签名证书申请单主体 DN 的 O，必须与 CA 侧租户登记的「证书主体O」一致；
 * 统一社会信用代码是医院作为法人和其他组织的唯一身份标识（GB 32100，18 位）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his")
public class HospitalProperties {

    /**
     * 机构名（医院名称）：签名证书主体 DN 的 O
     */
    private String organization;

    /**
     * 医院统一社会信用代码（18 位）
     */
    private String organizationCreditCode;
}
