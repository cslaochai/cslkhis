package com.his.pharmacy.spi;

import com.his.pharmacy.service.PharmacyService;
import com.his.spi.dto.StockDeductResultDTO;
import com.his.spi.service.StockFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 药房库存端口实现：委托内部 {@link PharmacyService} 的 FEFO 扣减/退药回补/锁库/解锁，
 * 批次选择、流水落账与成本计算全部留在药房域内部。
 */
@Service
@RequiredArgsConstructor
public class StockFacadeServiceImpl implements StockFacadeService {

    private final PharmacyService pharmacyService;

    @Override
    public StockDeductResultDTO deductStockFefo(Long drugId, BigDecimal qty, String bizType, Long bizId,
                                                String bizNo, String operatorName) {
        com.his.pharmacy.dto.StockDeductResultDTO internal =
                pharmacyService.deductStockFefo(drugId, qty, bizType, bizId, bizNo, operatorName);
        StockDeductResultDTO result = new StockDeductResultDTO();
        if (internal != null) {
            result.setQuantityBefore(internal.getQuantityBefore());
            result.setQuantityAfter(internal.getQuantityAfter());
        }
        return result;
    }

    @Override
    public void restoreStock(Long drugId, BigDecimal units, String bizType, Long bizId,
                             String bizNo, String operatorName) {
        pharmacyService.restoreStock(drugId, units, bizType, bizId, bizNo, operatorName);
    }

    @Override
    public void lockStockByDrug(Long drugId, BigDecimal qty) {
        pharmacyService.lockStockByDrug(drugId, qty);
    }

    @Override
    public void unlockStockByDrug(Long drugId, BigDecimal qty) {
        pharmacyService.unlockStockByDrug(drugId, qty);
    }
}
