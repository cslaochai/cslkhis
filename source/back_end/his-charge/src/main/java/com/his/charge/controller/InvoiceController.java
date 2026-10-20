package com.his.charge.controller;


import com.his.charge.dto.InvoiceIssueDTO;
import com.his.charge.dto.InvoicePageQueryDTO;
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
 * 收费票据控制器：账单出票、打印与作废换开的接口入口，落收费四层里的票据对账层。
 */
@Tag(name = "发票管理")
@RestController
@RequestMapping("/charge/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * 发票台账分页：按患者、账单、票据状态或关键字捞已开出的票。
     * 用于「发票管理」页查询卡，财务核对票面金额与实收是否相符时从这里进。
     */
    @Operation(summary = "分页查询发票")
    @PreAuthorize("hasAuthority('finance:invoice:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizInvoiceVO>> listPage(@Valid @RequestBody InvoicePageQueryDTO queryDTO) {
        return Result.success(invoiceService.selectInvoicePage(queryDTO.getPatientId(), queryDTO.getBillId(),
                queryDTO.getInvoiceStatus(), queryDTO.getKeyword(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 单张发票的票面信息取数，只读展示不回表单。
     * 用于「发票管理」页行点开的发票详情弹框。
     */
    @Operation(summary = "获取发票详情")
    @PreAuthorize("hasAuthority('finance:invoice:list')")
    @GetMapping("/getById")
    public Result<BizInvoiceVO> getById(@RequestParam Long id) {
        return Result.success(invoiceService.getInvoiceDetail(id));
    }

    /**
     * 按账单出票：票面金额取账单净实收，一张账单同时候只留一张有效票。
     * 用于收费台收款把账单收齐后的「出票」动作，也是作废重开时接红冲链的唯一写入口。
     */
    @Operation(summary = "按账单出票（一票对一账单，票面=净实收；作废后重开自动接红冲链）")
    @PreAuthorize("hasAuthority('finance:invoice:add')")
    @PostMapping("/issue")
    public Result<BizInvoiceVO> issue(@Valid @RequestBody InvoiceIssueDTO dto) {
        return Result.success(invoiceService.issueByBill(dto));
    }

    /**
     * 把一张已开具的票标记为已打印并记打印时点，作废或红冲过的票拒绝。
     * 用于「发票管理」台账行内的打印按钮，财务据此区分开出与已交付患者的票。
     */
    @PreAuthorize("hasAuthority('finance:invoice:export')")
    @Operation(summary = "打印发票")
    @PostMapping("/printInvoice")
    public Result<Void> printInvoice(@RequestParam Long id) {
        boolean success = invoiceService.printInvoice(id);
        return success ? Result.success("打印成功", null) : Result.error("打印失败");
    }

    /**
     * 作废发票并留原因：票面作废后这张账单才允许重开，新票自动接在原票的红冲链上。
     * 用于「发票管理」台账行内的作废动作，也是收错钱、票面与实收不符时的换开起点。
     */
    @PreAuthorize("hasAuthority('finance:invoice:delete')")
    @Operation(summary = "作废发票")
    @PostMapping("/voidInvoice")
    public Result<Void> voidInvoice(@Valid @RequestBody InvoiceVoidDTO actionDTO) {
        boolean success = invoiceService.voidInvoice(actionDTO.getId(), actionDTO.getReason());
        return success ? Result.success("作废成功", null) : Result.error("作废失败");
    }
}
