package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.DrugInboundCancelDTO;
import com.his.pharmacy.dto.DrugInboundCreateDTO;
import com.his.pharmacy.dto.DrugInboundIdDTO;
import com.his.pharmacy.dto.DrugInboundQueryPageDTO;
import com.his.pharmacy.vo.DrugInboundVO;

/**
 * 药品入库单服务
 */
public interface DrugInboundService {

    /** 分页查询 */
    PageResult<DrugInboundVO> page(DrugInboundQueryPageDTO queryDTO);

    /** 详情（含明细） */
    DrugInboundVO getDetailById(Long inboundId);

    /** 生成入库单（采购订单审批通过后调用；不做重复生成校验以外的业务判断） */
    DrugInboundVO createInbound(DrugInboundCreateDTO dto);

    /** 审核（1→2） */
    void audit(DrugInboundIdDTO dto);

    /** 入库（2→3）：按明细建/加药品批次并写库存流水 */
    DrugInboundVO stockIn(DrugInboundIdDTO dto);

    /** 取消（1/2→4） */
    void cancel(DrugInboundCancelDTO dto);

    /** 删除（已入库不可删） */
    void deleteById(Long inboundId);
}
