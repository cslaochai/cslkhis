package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.PurchaseOrderAuditDTO;
import com.his.pharmacy.dto.PurchaseOrderQueryPageDTO;
import com.his.pharmacy.dto.PurchaseOrderUpsertDTO;
import com.his.pharmacy.vo.DrugInboundVO;
import com.his.pharmacy.vo.PurchaseOrderVO;

/**
 * 采购订单服务
 */
public interface PurchaseOrderService {

    /** 分页查询 */
    PageResult<PurchaseOrderVO> page(PurchaseOrderQueryPageDTO queryDTO);

    /** 详情（含明细） */
    PurchaseOrderVO getDetailById(Long orderId);

    /** 新增/修改（返回主键） */
    Long upsert(PurchaseOrderUpsertDTO dto);

    /** 审批（通过/驳回） */
    void audit(PurchaseOrderAuditDTO dto);

    /**
     * 审批通过后生成入库单（一张采购单只能有一张未取消的入库单）
     *
     * ⚠ 这里**不动库存**：采购只决定「买什么」，到货验收由入库单记录；
     *   真正的入库（建/加药品批次 + 写库存流水）发生在 /drugInbound/stockIn 上。
     */
    DrugInboundVO generateInbound(Long orderId);

    /** 删除（已入库不可删） */
    void deleteById(Long orderId);
}
