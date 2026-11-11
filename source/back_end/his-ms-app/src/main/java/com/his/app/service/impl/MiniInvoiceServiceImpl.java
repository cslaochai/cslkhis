package com.his.app.service.impl;

import com.his.app.service.MiniInvoiceService;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.spi.service.InvoiceFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.PatientInvoiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniInvoiceServiceImpl implements MiniInvoiceService {

    private final InvoiceFacadeService invoiceFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public PageResult<PatientInvoiceVO> myInvoices(Long patientId, int pageNum, int pageSize) {
        requireAccessible(patientId);
        List<PatientInvoiceVO> records = invoiceFacadeService.myInvoices(patientId, pageNum, pageSize)
                .stream().filter(v -> Objects.equals(patientId, v.getPatientId())).toList();
        return PageResult.of(records.size(), pageNum, pageSize, 1, records);
    }

    @Override
    public PatientInvoiceVO invoiceDetail(Long patientId, Long invoiceId) {
        requireAccessible(patientId);
        PatientInvoiceVO vo = invoiceFacadeService.invoiceDetail(invoiceId);
        if (vo == null) {
            throw new BusinessException("票据不存在或已删除");
        }
        if (!Objects.equals(patientId, vo.getPatientId())) {
            throw new BusinessException("无权查看该票据");
        }
        return vo;
    }

    private void requireAccessible(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("就诊人不能为空");
        }
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查看该就诊人的票据");
        }
    }
}
