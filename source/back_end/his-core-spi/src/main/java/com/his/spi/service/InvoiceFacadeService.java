package com.his.spi.service;

import com.his.spi.vo.PatientInvoiceVO;

import java.util.List;

/**
 * 电子票据患者查询端口（实现落在 his-ms-charge）。
 *
 * <p>只读：开票/作废仍是收费员动作（{@code InvoiceService}），患者只能查看自己名下已开具的票据。</p>
 */
public interface InvoiceFacadeService {

    /**
     * 我的票据（按开票时间倒序，只出本页渲染的字段）。
     */
    List<PatientInvoiceVO> myInvoices(Long patientId, int pageNum, int pageSize);

    /**
     * 票据详情（按票据ID，调用方负责归属校验）。
     */
    PatientInvoiceVO invoiceDetail(Long invoiceId);
}
