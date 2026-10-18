package com.his.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.charge.dto.RefundApplySubmitDTO;
import com.his.charge.entity.BizRefundApply;
import com.his.charge.vo.BizRefundApplyVO;
import com.his.charge.vo.RefundableLineVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 退费申请服务接口
 */
public interface RefundApplyService extends IService<BizRefundApply> {

    /**
     * 查询退费申请列表（出参带退费流水字段：执行成功后钱是以什么方式退回去的）
     *
     * @param keyword 退费申请号 / 原收费单号 / 患者姓名模糊匹配，可为空
     */
    PageResult<BizRefundApplyVO> selectRefundApplyPage(Long patientId, Integer applyStatus,
                                                       String keyword, int pageNum, int pageSize);

    /**
     * 获取退费申请详情（含退费流水字段）
     */
    BizRefundApplyVO getRefundApplyDetail(Long applyId);

    /**
     * 提交退费申请
     */
    BizRefundApplyVO submitRefundApply(RefundApplySubmitDTO submitDTO);

    /**
     * 审核退费申请
     */
    boolean auditRefundApply(Long applyId, boolean approved, Long auditorId, String auditorName, String remark);

    /**
     * 作废退费申请（1-待审核 / 2-审核通过 → 5-已作废，原因必填）。
     *
     * <p>没有它，一条"审核通过但执行不了"的申请会把那张收费单永久锁死：
     * 判重拦 (1,2) 两态、审核只认 1、执行又落不到明细边界，四个条件凑在一起就是死局。
     * 作废不动收费单、不动钱，只把申请单从"活着"里摘出去，之后可以重新发起一笔金额对的申请。
     */
    boolean discardRefundApply(Long applyId, String reason);

    /**
     * 执行退费
     */
    boolean executeRefund(Long applyId, String refundBy);

    /**
     * 退费候选行：某张账单按退费类型还能退的记账行（含剩余数量与来源单号）。
     *
     * <p>口径与 {@code SettlementBillService#refundableLines} 一致，这里只是给退费申请
     * 域开一个 HTTP 出口：发起申请前按处方号/申请单号圈选、按行改退费数量。
     */
    List<RefundableLineVO> refundableLines(Long billId, Integer refundType);
}
