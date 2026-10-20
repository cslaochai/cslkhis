package com.his.pay.spi;

import com.his.pay.config.AlipayProperties;
import com.his.spi.service.AlipayChannelConfigFacadeService;
import com.his.spi.vo.AlipayChannelConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 支付宝渠道配置端口实现：把渠道凭据透出到本机服务间契约。
 *
 * <p>本类不打印任何配置内容——含应用私钥，落一次日志就等于把凭据写进日志文件。
 */
@Service
@RequiredArgsConstructor
public class AlipayChannelConfigFacadeServiceImpl implements AlipayChannelConfigFacadeService {

    private final AlipayProperties alipayProperties;

    @Override
    public AlipayChannelConfigVO getChannelConfig() {
        AlipayChannelConfigVO vo = new AlipayChannelConfigVO();
        vo.setAppId(alipayProperties.getAppId());
        vo.setGatewayUrl(alipayProperties.getGatewayUrl());
        vo.setAlipayPublicKey(alipayProperties.getAlipayPublicKey());
        vo.setPrivateKey(alipayProperties.getPrivateKey());
        vo.setEnabled(alipayProperties.isEnabled());
        return vo;
    }
}
