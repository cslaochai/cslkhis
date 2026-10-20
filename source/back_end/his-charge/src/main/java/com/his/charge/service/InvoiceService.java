package com.his.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.charge.dto.InvoiceIssueDTO;
import com.his.charge.entity.BizInvoice;
import com.his.charge.vo.BizInvoiceVO;
import com.his.common.base.PageResult;

/**
 * 发票服务（L4 票据）。
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
