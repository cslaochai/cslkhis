package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.SupplierReturnActionDTO;
import com.his.pharmacy.dto.SupplierReturnQueryPageDTO;
import com.his.pharmacy.dto.SupplierReturnUpsertDTO;
import com.his.pharmacy.vo.SupplierReturnVO;

/**
 * 药品供应商退货服务（sql/154 ③级：药离开医院）
 *
 * <p>单据流：建单（校验批次归属该供应商）【待退货】→（确认退货，扣批次库存并落 type=9 流水）【已退货】；
 * 未确认前可作废【已作废】或删除。
 *
 * <p>与调拨的差别只有一处但很关键：<b>没有接收方</b>。所以退货是一步动作，
 * 也所以批次必须挂着 supplier_id —— 不知道退给谁，这张单就没有对手方，退款无从主张。
 */
public interface SupplierReturnService {

    PageResult<SupplierReturnVO> listPage(SupplierReturnQueryPageDTO query);

    /** 详情（含明细与退货出库流水） */
    SupplierReturnVO getDetailById(Long id);

    /** 建单 / 改明细（仅待退货，整单替换明细） */
    SupplierReturnVO upsert(SupplierReturnUpsertDTO dto);

    /** 确认退货：按明细逐批扣减库存（type=9 退货出库） */
    SupplierReturnVO confirmReturn(SupplierReturnActionDTO dto);

    /** 作废（仅待退货；确认后药已出库，只能反向入库补回） */
    SupplierReturnVO cancel(SupplierReturnActionDTO dto);

    /** 删除（仅待退货/已作废；物理删，单号唯一键不含 del_flag） */
    void deleteById(Long id);
}
