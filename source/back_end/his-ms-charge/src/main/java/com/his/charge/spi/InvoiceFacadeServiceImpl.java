package com.his.charge.spi;

import com.his.charge.service.InvoiceService;
import com.his.charge.vo.BizInvoiceVO;
import com.his.common.base.PageResult;
import com.his.common.enums.InvoiceStatusEnum;
import com.his.spi.service.InvoiceFacadeService;
import com.his.spi.vo.PatientInvoiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 电子票据患者查询端口实现：只读复用 {@link InvoiceService} 的分页与详情，不碰开票/作废。
 *
 * <p>依赖图已核对：InvoiceServiceImpl 只依赖 SettlementBillService / BizPaymentTxnMapper /
 * RedisSequenceService，SettlementBillServiceImpl 不回指 InvoiceService，无 Bean 环。</p>
 */
@Service
@RequiredArgsConstructor
public class InvoiceFacadeServiceImpl implements InvoiceFacadeService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InvoiceService invoiceService;

    @Override
    public List<PatientInvoiceVO> myInvoices(Long patientId, int pageNum, int pageSize) {
        if (patientId == null) {
            return List.of();
        }
        PageResult<BizInvoiceVO> page = invoiceService.selectInvoicePage(patientId, null, null, null, pageNum, pageSize);
        return page.getRecords().stream().map(this::toVo).toList();
    }

    @Override
    public PatientInvoiceVO invoiceDetail(Long invoiceId) {
        if (invoiceId == null) {
            return null;
        }
        BizInvoiceVO vo = invoiceService.getInvoiceDetail(invoiceId);
        return vo == null ? null : toVo(vo);
    }

    private PatientInvoiceVO toVo(BizInvoiceVO vo) {
        PatientInvoiceVO result = new PatientInvoiceVO();
        result.setInvoiceId(vo.getId());
        result.setPatientId(vo.getPatientId());
        result.setInvoiceNo(vo.getInvoiceNo());
        result.setInvoiceType(vo.getInvoiceType());
        result.setBillNo(vo.getBillNo());
        result.setTotalAmount(vo.getTotalAmount());
        result.setInvoiceStatus(vo.getInvoiceStatus());
        result.setInvoiceStatusText(InvoiceStatusEnum.getLabel(vo.getInvoiceStatus()));
        result.setInvoiceTime(vo.getInvoiceTime() == null ? null : TIME.format(vo.getInvoiceTime()));
        result.setElectronicUrl(vo.getElectronicUrl());
        return result;
    }
}
