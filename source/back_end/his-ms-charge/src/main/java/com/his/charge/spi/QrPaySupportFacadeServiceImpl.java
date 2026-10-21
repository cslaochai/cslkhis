package com.his.charge.spi;

import com.his.charge.support.AggregatedQrSupport;
import com.his.charge.support.PayQrCodeCache;
import com.his.spi.service.QrPaySupportFacadeService;
import com.his.spi.vo.AggPaidInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 收款台扫码支撑端口实现：聚合码出码、模拟收款与码串缓存的跨模块出口。
 */
@Service
@RequiredArgsConstructor
public class QrPaySupportFacadeServiceImpl implements QrPaySupportFacadeService {

    private final AggregatedQrSupport aggregatedQrSupport;

    private final PayQrCodeCache payQrCodeCache;

    @Override
    public String buildQrContent(String payNo) {
        return aggregatedQrSupport.buildQrContent(payNo);
    }

    @Override
    public boolean isMockEnabled() {
        return aggregatedQrSupport.isMockEnabled();
    }

    @Override
    public void markMockPaid(String payNo, Integer channel, BigDecimal amount) {
        aggregatedQrSupport.markMockPaid(payNo, channel, amount);
    }

    @Override
    public AggPaidInfoVO queryMockPaid(String payNo) {
        AggregatedQrSupport.AggPaidInfo info = aggregatedQrSupport.queryMockPaid(payNo);
        if (info == null) {
            return null;
        }
        AggPaidInfoVO vo = new AggPaidInfoVO();
        vo.setChannel(info.channel());
        vo.setAmount(info.amount());
        return vo;
    }

    @Override
    public String getQrCode(String payNo) {
        return payQrCodeCache.get(payNo);
    }

    @Override
    public void putQrCode(String payNo, String qrCode) {
        payQrCodeCache.put(payNo, qrCode);
    }

    @Override
    public void evictQrCode(String payNo) {
        payQrCodeCache.evict(payNo);
    }
}
