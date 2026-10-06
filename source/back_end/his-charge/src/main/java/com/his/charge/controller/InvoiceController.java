package com.his.charge.controller;





import com.his.charge.dto.InvoiceIssueDTO;
import com.his.charge.dto.InvoiceQueryPageDTO;
import com.his.charge.dto.InvoiceVoidDTO;
import com.his.charge.service.InvoiceService;
import com.his.charge.vo.BizInvoiceVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 发票管理控制器（L4 票据）。
 *
 * <p>出票口子原先内嵌在 {@code /charge/processPay} 里（收完钱顺手造一张票），
 * 那是旧模型一体的产物；四层之后票据独立成一层，收讫与出票是两步，
 * 而且必须两步 —— 收银员收款与发票号段的发放是两个人、两本账。
 */
@Tag(name = "发票管理")
@RestController
@RequestMapping("/charge/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "分页查询发票")
    @PreAuthorize("hasAuthority('finance:invoice:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizInvoiceVO>> listPage(@Valid @RequestBody InvoiceQueryPageDTO queryDTO) {
        return Result.success(invoiceService.selectInvoicePage(queryDTO.getPatientId(), queryDTO.getBillId(),
                queryDTO.getInvoiceStatus(), queryDTO.getKeyword(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "获取发票详情")
    @PreAuthorize("hasAuthority('finance:invoice:list')")
    @GetMapping("/getById")
    public Result<BizInvoiceVO> getById(@RequestParam Long id) {
        return Result.success(invoiceService.getInvoiceDetail(id));
    }

    @Operation(summary = "按账单出票（一票对一账单，票面=净实收；作废后重开自动接红冲链）")
    @PreAuthorize("hasAuthority('finance:invoice:add')")
    @PostMapping("/issue")
    public Result<BizInvoiceVO> issue(@Valid @RequestBody InvoiceIssueDTO dto) {
        return Result.success(invoiceService.issueByBill(dto));
    }

    @PreAuthorize("hasAuthority('finance:invoice:export')")
    @Operation(summary = "打印发票")
    @PostMapping("/printInvoice")
    public Result<Void> printInvoice(@RequestParam Long id) {
        boolean success = invoiceService.printInvoice(id);
        return success ? Result.success("打印成功", null) : Result.error("打印失败");
    }

    @PreAuthorize("hasAuthority('finance:invoice:delete')")
    @Operation(summary = "作废发票")
    @PostMapping("/voidInvoice")
    public Result<Void> voidInvoice(@Valid @RequestBody InvoiceVoidDTO actionDTO) {
        boolean success = invoiceService.voidInvoice(actionDTO.getId(), actionDTO.getReason());
        return success ? Result.success("作废成功", null) : Result.error("作废失败");
    }
}
