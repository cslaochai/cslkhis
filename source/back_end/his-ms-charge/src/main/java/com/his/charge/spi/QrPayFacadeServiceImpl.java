package com.his.charge.spi;

import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.service.QrPayService;
import com.his.charge.vo.QrPayCreateVO;
import com.his.spi.service.QrPayFacadeService;
import com.his.spi.vo.QrPayResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 聚合支付出码端口实现：复用收费台同一入口（QrPayService.create），
 * 出码的去重、节流、建单与码串缓存口径与收费台扫码收款完全一致。
 */
@Service
@RequiredArgsConstructor
public class QrPayFacadeServiceImpl implements QrPayFacadeService {

    private final QrPayService qrPayService;

    @Override
    public QrPayResultVO createQrCode(Long billId) {
        QrPayCreateDTO dto = new QrPayCreateDTO();
        dto.setBillId(billId);
        QrPayCreateVO created = qrPayService.create(dto);
        QrPayResultVO vo = new QrPayResultVO();
        vo.setPayNo(created.getPayNo());
        vo.setBillNo(created.getBillNo());
        vo.setAmount(created.getAmount());
        vo.setQrImage(created.getQrImage());
        vo.setExpireTime(created.getExpireTime());
        vo.setServerTime(created.getServerTime());
        return vo;
    }
}
