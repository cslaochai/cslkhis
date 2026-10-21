package com.his.spi.service;

import com.his.spi.dto.PendingDispensingCreateDTO;

import java.util.List;

/**
 * 待发药记录端口（事实宿主：药房域 biz_drug_dispensing）：
 * 缴费推进生成待发药、退费取消待发药、退费前未退药闸门。
 *
 * <p>已发药的记录本端口一律不动：回库是药师实物验收的退药动作，退费只退钱。
 */
public interface DispensingFacadeService {

    /**
     * 为一条已缴清的处方明细生成待发药记录；同一条明细已有记录则幂等返回，不叠加。
     *
     * @param dto 开药快照（处方/患者/药品字段）
     */
    void createPendingDispensing(PendingDispensingCreateDTO dto);

    /**
     * 退费取消：仅把该明细名下「待发药(PENDING)」的记录置为已取消，已发药记录不动。
     *
     * @param prescriptionDetailId 处方明细ID
     * @param reason               取消原因（落备注，截断由实现侧负责）
     */
    void cancelPendingDispensingByDetailId(Long prescriptionDetailId, String reason);

    /**
     * 退费前置闸门：这些处方明细里只要还有「已发药、未办退药」的记录就抛业务异常。
     * 查不到未退药记录则放行。
     *
     * @param prescriptionDetailIds 处方明细ID清单
     * @param scene                 场景文字（进报错文案）
     */
    void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene);
}
