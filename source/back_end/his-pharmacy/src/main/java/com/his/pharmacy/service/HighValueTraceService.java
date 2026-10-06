package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.ConsumableTraceQueryPageDTO;
import com.his.pharmacy.dto.HighValueUseDTO;
import com.his.pharmacy.vo.BizConsumableTraceVO;
import com.his.pharmacy.vo.ConsumableTraceDetailVO;
import com.his.pharmacy.vo.UdiScanVO;

/**
 * 高值耗材 UDI 扫码溯源服务（L11）。
 * 口径：一物一行台账；使用登记扣批次 1 件（流水 type=7）并尝试计费（独立事务、失败留痕可补记）；
 * 作废仅限未计费记录，且把 1 件退回批次（流水 type=3）。
 */
public interface HighValueTraceService {

    /**
     * UDI 扫码解析：拆 DI/序列号/批号/有效期，按 DI 命中耗材字典并给出有货批次候选
     */
    UdiScanVO scanUdi(String udiCode);

    /**
     * 使用登记（关联患者 + 扣批次 + 计费尝试）
     */
    BizConsumableTraceVO traceUse(HighValueUseDTO dto, String operatorName);

    /**
     * 溯源台账分页（正/反向追溯）
     */
    PageResult<BizConsumableTraceVO> selectTracePage(ConsumableTraceQueryPageDTO queryDTO);

    /**
     * 溯源详情（字典→入库批次→使用患者→计费全链）
     */
    ConsumableTraceDetailVO getTraceDetailById(Long traceId);

    /**
     * 作废（退货）：置作废并把 1 件加回批次；已计费拒绝
     */
    BizConsumableTraceVO traceVoid(Long traceId, String reason, String operatorName);

    /**
     * 计费补记（未计费/计费失败的记录重走计费）
     */
    BizConsumableTraceVO traceRecharge(Long traceId, String operatorName);
}
