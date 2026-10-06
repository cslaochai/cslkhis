package com.his.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.charge.dto.InvoiceIssueDTO;
import com.his.charge.entity.BizInvoice;
import com.his.charge.vo.BizInvoiceVO;
import com.his.common.base.PageResult;

/**
 * 发票服务（L4 票据）。
 *
 * <p><b>一票对一张结算账单</b>（发票的账单ID），票面金额 = 开票时该账单的
 * <b>净实收</b>（Σ 成功收款流水 − Σ 成功退款流水），由支付资金流水现算。
 * 旧口径把票挂在旧收费单上、票面抄合计金额（应收），
 * 于是"应收 100 只收 60"能开出一张 100 的票 —— 票据的意义正是证明钱收过了，
 * 金额取自应收等于让票为没发生的交易背书。
 *
 * <p>票号是财政序列：IV + yyyyMMdd + 5 位，一天一号，写进去就不改。
 * 票面错了只能作废（3）或红冲换开（4，新票用 {@code orig_invoice_id} 指回旧票），
 * 就地改金额等于伪造票据。
 */
public interface InvoiceService extends IService<BizInvoice> {

    /**
     * 按账单出票：一张账单只允许一张有效票；账单退了钱、票面与实收不符时，
     * 必须先作废原票再重开，不允许第二张票盖住第一张。
     */
    BizInvoiceVO issueByBill(InvoiceIssueDTO dto);

    /**
     * 查询发票列表
     *
     * @param keyword 发票号 / 账单号 / 收费单号 / 患者姓名模糊匹配，可为空
     */
    PageResult<BizInvoiceVO> selectInvoicePage(Long patientId, Long billId, Integer invoiceStatus,
                                               String keyword, int pageNum, int pageSize);

    /**
     * 获取发票详情
     */
    BizInvoiceVO getInvoiceDetail(Long invoiceId);

    /**
     * 打印发票
     */
    boolean printInvoice(Long invoiceId);

    /**
     * 作废发票（红冲换开的入口：作废后同一张账单可再开票，新票自动回指本票）
     */
    boolean voidInvoice(Long invoiceId, String reason);
}
