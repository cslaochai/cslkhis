package com.his.platform.service.impl;

import com.his.common.util.TextUtil;
import com.his.platform.config.SignProperties;
import com.his.platform.service.ExternalCaChannelService;
import com.his.platform.support.CssslCaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 外部 CA（证书颁发机构）通道 —— 对接 csssl 院内 CA（RA 公钥签发）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCaChannelServiceImpl implements ExternalCaChannelService {

    private final SignProperties signProperties;

    private final CssslCaClient cssslCaClient;

    /**
     * 通道名称（状态接口展示）
     */
    public String name() {
        return "csssl院内CA（RA公钥签发）";
    }

    /**
     * 是否就绪（配置为 external）。未就绪（演示模式）时签发只提示、不落证书
     */
    public boolean available() {
        return "external".equalsIgnoreCase(signProperties.getCaMode());
    }

    /**
     * 向 csssl 提交公钥签发请求，换回 CA 签发的证书链。
     *
     * @param request Subject DN（CN=签名人 / O=机构 / OU=科室）、本地生成的公钥 PEM、期望有效期
     * @return 签发结果；csssl 不可用/未返回证书时抛异常中断签发（绝不静默回退到本地自签）
     */
    public IssuedCert issueCert(IssueRequest request) {
        log.info("[外部CA] 向 csssl 提交公钥签发：subject={} 有效天数={}",
                request.subjectDn(), request.validDays());
        CssslCaClient.IssuedCert issued = cssslCaClient.issue(
                request.subjectDn(), request.publicKeyPem(), request.validDays());
        return new IssuedCert(issued.certPem(), issued.caSerial(), issued.caIssuer());
    }
}
