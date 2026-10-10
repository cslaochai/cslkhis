package com.his.system.service.impl;

import com.his.system.config.SignProperties;
import com.his.system.service.ExternalCaChannelService;
import com.his.system.utils.SignCryptoUtil;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 外部 CA（证书颁发机构）通道 —— M8 留口子。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCaChannelServiceImpl implements ExternalCaChannelService {

    private final SignProperties signProperties;

    /**
     * 通道名称（状态接口展示）
     */
    public String name() {
        return "外部CA控制台打印（M8留口子，未接入真CA）";
    }

    /**
     * 是否就绪（配置为 external）。未就绪时签发直接走院内自签，不降级冒充真 CA
     */
    public boolean available() {
        return "external".equalsIgnoreCase(signProperties.getCaMode());
    }

    /**
     * 向 CA 提交证书签名请求。
     *
     * @param request Subject（CN=签名人 / O=机构）、本地生成的公钥 PEM、期望有效期
     * @return CA 签发的证书；未接入真 CA 时返回 {@code null}（调用方据此中断签发）
     */
    public IssuedCert issueCert(IssueRequest request) {
        // —— M8 留口子：这一段打印就是"向外部 CA 提交 CSR"的占位，真 CA 接入后整块替换 ——
        log.info("[M8真CA口子] ===== 模拟向外部CA提交证书签名请求 =====");
        log.info("[M8真CA口子] Subject DN : {}", request.subjectDn());
        log.info("[M8真CA口子] 公钥指纹   : {}（算法 RSA-2048，密钥对已在本地生成并等待托管）",
                TextUtil.hasText(request.publicKeyPem()) ? SignCryptoUtil.fingerprint(request.publicKeyPem()) : "无");
        log.info("[M8真CA口子] 申请有效期 : {} 天", request.validDays());
        log.info("[M8真CA口子] （真实接入=在本方法内替换为对接 CA 厂商 SDK/REST 的外发与回执解析）");
        // 不产证书：返回 null，调用方据此中断签发（绝不静默回退院内自签冒充真 CA）
        return null;
    }
}
