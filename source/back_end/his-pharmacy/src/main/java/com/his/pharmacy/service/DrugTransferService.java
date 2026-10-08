package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.DrugTransferActionDTO;
import com.his.pharmacy.dto.DrugTransferQueryPageDTO;
import com.his.pharmacy.dto.DrugTransferUpsertDTO;
import com.his.pharmacy.vo.DrugTransferVO;

/**
 * 药品调拨服务（sql/154 ②级：药库 ↔ 药房）
 */
public interface DrugTransferService {

    PageResult<DrugTransferVO> listPage(DrugTransferQueryPageDTO query);

    /** 详情（含明细与本单落下的两行流水） */
    DrugTransferVO getDetailById(Long id);

    /** 建单 / 改明细（仅待发出，整单替换明细） */
    DrugTransferVO upsert(DrugTransferUpsertDTO dto);

    /** 确认发出：按明细逐批扣减发出库位库存（type=7） */
    DrugTransferVO confirmOut(DrugTransferActionDTO dto);

    /** 确认接收：按明细逐批落到接收库位（type=8），全部接收后转已完成 */
    DrugTransferVO confirmIn(DrugTransferActionDTO dto);

    /** 作废（仅待发出；已发出属在途，只能反向调拨） */
    DrugTransferVO cancel(DrugTransferActionDTO dto);

    /** 删除（仅待发出/已作废；物理删，单号唯一键不含 del_flag） */
    void deleteById(Long id);
}
