package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.DrugTransferActionDTO;
import com.his.pharmacy.dto.DrugTransferQueryPageDTO;
import com.his.pharmacy.dto.DrugTransferUpsertDTO;
import com.his.pharmacy.vo.DrugTransferVO;

/**
 * 药品调拨服务（sql/154 ②级：药库 ↔ 药房）
 *
 * <p>单据流：建单（按批次抓快照，校验库位与可用量）【待发出】→（确认发出，扣发出库位并落 type=7 流水）
 * 【待接收】→（确认接收，落到接收库位同批号批次并落 type=8 流水）【已完成】。
 * 未发出前可作废【已作废】，也可直接删除。
 *
 * <p>两个设计口径值得先说清楚：
 * <ul>
 *   <li><b>发出与接收分两步</b>：中间那段是「在途」，货既不在药房也不在药库。一步搬完省事，
 *       但表达不出「车上那箱此刻盘不到」，两边库管员也无从各自追责。</li>
 *   <li><b>在途不许作废</b>：药已经离开原库位，作废只会让它凭空消失。要收回来就开一张反向调拨。</li>
 * </ul>
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
