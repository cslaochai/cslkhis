package com.his.charge.service;


import com.his.charge.dto.RefundFlowQueryPageDTO;
import com.his.charge.vo.BizRefundFlowDetailVO;
import com.his.charge.vo.BizRefundFlowVO;
import com.his.common.base.PageResult;

/**
 * 退费流水台账（只读，数据源为四层支付流水支付资金流水 direction=2）。
 *
 * <p>M7 独立的旧退费单/旧退费明细双写台账已并入 L3 支付流水：
 * 一笔真退出去的钱在支付资金流水里就是一行 {@code direction=2} 的记录（含患者快照、
 * 原账单号、退费方式、渠道流水号、操作人、来源申请号等），本服务只是把它投影成前端台账所需的
 * {@link BizRefundFlowVO} 形状，不另存任何表。
 *
 * <p>冲正入口（退费申请执行 / 收费处直退 / 退号联动）统一走四层 {@code PaymentService.refund} 与
 * 结算账单作废链，那一头已经把 {@code direction=2} 的流水写进支付资金流水，这里只负责读。
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
