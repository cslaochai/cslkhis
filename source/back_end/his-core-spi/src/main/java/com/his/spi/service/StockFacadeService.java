package com.his.spi.service;

import com.his.spi.dto.StockDeductResultDTO;

import java.math.BigDecimal;

/**
 * 药房库存端口：发药 FEFO 扣减、退药回补、开方锁库/撤方解锁。
 *
 * <p>跨模块只按「药品」下达指令，批次怎么选、流水怎么落、成本怎么算是药房域内部的事，
 * 调用方不复制任何库存口径。
 */
public interface StockFacadeService {

    /**
     * FEFO 扣减药房库存（先过期先出、跨批次扣减，逐批落流水）；
     * 总可用量不足时抛业务异常，整单回滚。
     *
     * @param drugId      药品ID
     * @param qty         扣减数量
     * @param bizType     来源业务类型（库存流水反查用）
     * @param bizId       来源业务单ID
     * @param bizNo       来源业务单号
     * @param operatorName 操作人
     * @return 该药品全部批次合计的扣减前后数量
     */
    StockDeductResultDTO deductStockFefo(Long drugId, BigDecimal qty, String bizType, Long bizId,
                                         String bizNo, String operatorName);

    /**
     * 退药回补库存：并入该药品现有数量最大的批次并落流水；
     * 该药品已无任何批次时以来源单号建新批次承接。
     *
     * @param drugId      药品ID
     * @param units       回补数量
     * @param bizType     来源业务类型
     * @param bizId       来源业务单ID
     * @param bizNo       来源业务单号
     * @param operatorName 操作人
     */
    void restoreStock(Long drugId, BigDecimal units, String bizType, Long bizId,
                      String bizNo, String operatorName);

    /**
     * 处方保存锁定库存：按药品跨批次（FEFO 顺序）把可用量转成锁定量；
     * 该药品一个批次都没有时直接放过，硬闸门在发药扣库。
     *
     * @param drugId 药品ID
     * @param qty    锁定数量
     */
    void lockStockByDrug(Long drugId, BigDecimal qty);

    /**
     * 删处方/撤方解锁：按药品跨批次把锁定量退回可用量，只退实际锁着的部分。
     *
     * @param drugId 药品ID
     * @param qty    解锁数量
     */
    void unlockStockByDrug(Long drugId, BigDecimal qty);
}
