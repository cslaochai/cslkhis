package com.his.drgdip.settle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.drgdip.settle.dto.MedpaySettleDTO;
import com.his.drgdip.settle.entity.BizCaseAppeal;
import com.his.drgdip.settle.vo.MedpaySettleVO;

/**
 * 按病种付费结算服务（P4 结算层）。
 * <p>出院结算账单按 medpay.mode 路由：DRG=权重×费率，DIP=分值（基准×系数）×点值；
 * 应付/盈亏参数未下发放 NULL，不编造。特例单议：1-草稿→2-已提交→3-通过/4-驳回。
 */
public interface MedpaySettleService {

    /**
     * 按结算账单计算医保应付与盈亏并落库（幂等覆盖，按 settle_bill_id+pay_mode+version）。
     */
    MedpaySettleVO.SettleRow settleByBill(Long billId);

    /**
     * 批量结算（billIds 为空取最近 50 张出院结算账单；非出院/未关联首页的账单跳过并计数）
     */
    MedpaySettleVO.BatchVO settleBatch(MedpaySettleDTO.SettleBatch dto);

    /**
     * 结算结果分页
     */
    IPage<MedpaySettleVO.SettleRow> resultPage(MedpaySettleDTO.SettleQuery dto);

    /**
     * 当前医保付费参数（主方式 / DRG 版本 / DIP 版本 / DRG 费率 / DIP 点值）
     */
    MedpaySettleVO.ModeVO mode();

    /**
     * 创建特例单议草稿（每结算一条活跃申请，状态 1-草稿）
     */
    BizCaseAppeal createAppeal(MedpaySettleDTO.AppealCreate dto);

    /**
     * 提交特例单议（1-草稿 → 2-已提交）
     */
    void submitAppeal(Long id);

    /**
     * 审核特例单议（2-已提交 → 3-审核通过 / 4-审核驳回）
     */
    void reviewAppeal(MedpaySettleDTO.AppealReview dto);

    /**
     * 撤回草稿（1-草稿 → 5-已撤回）
     */
    void withdrawAppeal(Long id);

    /**
     * 特例单议分页
     */
    IPage<MedpaySettleVO.AppealRow> appealPage(MedpaySettleDTO.AppealQuery dto);
}
