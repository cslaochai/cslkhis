package com.his.app.service;

import com.his.common.base.PageResult;
import com.his.spi.vo.PatientInvoiceVO;

/**
 * 患者端电子票据（只读）。
 */
public interface MiniInvoiceService {

    /**
     * 我的票据
     */
    PageResult<PatientInvoiceVO> myInvoices(Long patientId, int pageNum, int pageSize);

    /**
     * 票据详情（只允许看本人票据）
     */
    PatientInvoiceVO invoiceDetail(Long patientId, Long invoiceId);
}
