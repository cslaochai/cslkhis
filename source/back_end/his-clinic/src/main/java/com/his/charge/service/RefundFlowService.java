package com.his.charge.service;


import com.his.charge.dto.RefundFlowQueryPageDTO;
import com.his.charge.vo.BizRefundFlowDetailVO;
import com.his.charge.vo.BizRefundFlowVO;
import com.his.common.base.PageResult;

/**
 * 退费流水台账（只读，数据源为四层支付流水支付资金流水 direction=2）。
 */
public interface RefundFlowService {

    /**
     * 分页查询退费流水（投影自支付资金流水 direction=2）
     */
    PageResult<BizRefundFlowVO> selectFlowPage(RefundFlowQueryPageDTO query);

    /**
     * 退费流水详情（含逐条退费明细，明细来自账单行）
     */
    BizRefundFlowDetailVO getFlowDetail(Long refundId);
}
