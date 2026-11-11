package com.his.app.controller;

import com.his.app.service.MiniInvoiceService;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.spi.vo.PatientInvoiceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端-电子票据（查看已开具票据；开票与作废仍是收费员动作）。
 */
@Tag(name = "患者端-电子票据")
@RestController
@RequestMapping("/miniapp/invoice")
@RequiredArgsConstructor
public class MiniInvoiceController {

    private final MiniInvoiceService miniInvoiceService;

    @Operation(summary = "我的票据")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/list")
    public Result<PageResult<PatientInvoiceVO>> list(@RequestParam Long patientId,
                                                     @RequestParam int pageNum,
                                                     @RequestParam int pageSize) {
        return Result.success(miniInvoiceService.myInvoices(patientId, pageNum, pageSize));
    }

    @Operation(summary = "票据详情")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/detail")
    public Result<PatientInvoiceVO> detail(@RequestParam Long patientId, @RequestParam Long invoiceId) {
        return Result.success(miniInvoiceService.invoiceDetail(patientId, invoiceId));
    }
}
