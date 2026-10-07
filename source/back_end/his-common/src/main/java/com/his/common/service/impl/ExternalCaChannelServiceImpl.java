package com.his.common.service.impl;

import com.his.common.config.SignProperties;
import com.his.common.service.ExternalCaChannelService;
import com.his.common.util.SignCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 外部 CA（证书颁发机构）通道 —— M8 留口子。
 *
 * <p><b>背景</b>：G6/G6b 的签名证书全部是院内自签 RSA（信任根是自己的
 * {@code KeyPairFactory}），形态对、法律效力没到位。换真 CA（如省级卫健委
 * 统一发证、或商业 CA 的 Ukey/移动签名）不是改签名模型，而是换"谁给公钥背书"
 * —— PKCS#10 常规流程是<b>密钥对本地生成、CA 只签公钥</b>，
 * 私钥加密托管、签名链、验签断言全都不动。
 *
 * <p><b>形态</b>：{@code his.sign.ca-mode=internal}（默认）时本通道不参与，行为与 G6/G6b 一致；
 * 配 {@code external} 时 {@link #available()} 为 true，证书签发会走到这里，
 * 把"向 CA 提交 CSR"这一步<b>原样打印到控制台</b>（Subject、公钥指纹、有效期）后返回 null 中断签发。
 * 采购的 CA 厂商确定后，替换 {@link #issueCert} 内部那段外发实现（对接厂商 SDK/REST）即可，签发链路不动。
 *
 * <p><b>两条硬约束</b>：
 * <ol>
 *   <li>外部 CA 模式下<b>不许静默回退自签</b> —— 拿不到证书就中断签发，
 *       宁可让签名功能暂不可用，也不能发一张"看起来是真 CA"的院内证书。</li>
 *   <li>CA 签发失败不允许把已生成的私钥以任何形式留痕（私钥还没用就该丢）。</li>
 * </ol>
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
        return "外部CA控制台打印桩（M8留口子，未接入真CA）";
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
                StringUtils.hasText(request.publicKeyPem()) ? SignCryptoUtil.fingerprint(request.publicKeyPem()) : "无");
        log.info("[M8真CA口子] 申请有效期 : {} 天", request.validDays());
        log.info("[M8真CA口子] （真实接入=在本方法内替换为对接 CA 厂商 SDK/REST 的外发与回执解析）");
        // 桩不产证书：返回 null，调用方据此中断签发（绝不静默回退院内自签冒充真 CA）
        return null;
    }
}
