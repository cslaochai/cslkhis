package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.PurchaseInboundDTO;
import com.his.pharmacy.dto.PurchaseOrderAuditDTO;
import com.his.pharmacy.dto.PurchaseOrderQueryPageDTO;
import com.his.pharmacy.dto.PurchaseOrderUpsertDTO;
import com.his.pharmacy.service.PurchaseOrderService;
import com.his.pharmacy.vo.DrugInboundVO;
import com.his.pharmacy.vo.PurchaseOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 药品采购订单控制器
 *
 * 链路：建单（含明细）→ 审批 → 入库（按明细建/加药品批次 + 写库存流水）
 * 状态机与金额口径见 {@link com.his.pharmacy.service.impl.PurchaseOrderServiceImpl} 类注释。
 */
@Tag(name = "药品采购")
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:purchase:list')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "分页查询采购订单")
    @PostMapping("/listPage")
    public Result<PageResult<PurchaseOrderVO>> listPage(@RequestBody PurchaseOrderQueryPageDTO queryDTO) {
        return Result.success(purchaseOrderService.page(queryDTO));
    }

    @Operation(summary = "获取采购订单详情（含明细）")
    @GetMapping("/getDetailById")
    public Result<PurchaseOrderVO> getDetailById(@RequestParam Long orderId) {
        return Result.success(purchaseOrderService.getDetailById(orderId));
    }

    @PreAuthorize("hasAuthority('pharmacy:purchase:add')")
    @Operation(summary = "新增/修改采购订单（含明细）")
    @PostMapping("/upsert")
    public Result<PurchaseOrderVO> upsert(@Valid @RequestBody PurchaseOrderUpsertDTO upsertDTO) {
        Long orderId = purchaseOrderService.upsert(upsertDTO);
        return Result.success(purchaseOrderService.getDetailById(orderId));
    }

    @PreAuthorize("hasAuthority('pharmacy:purchase:edit')")
    @Operation(summary = "审批采购订单（1-通过 2-驳回）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody PurchaseOrderAuditDTO auditDTO) {
        purchaseOrderService.audit(auditDTO);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('pharmacy:purchase:add')")
    @Operation(summary = "由采购订单生成入库单（审批通过后；本接口不动库存）")
    @PostMapping("/generateInbound")
    public Result<DrugInboundVO> generateInbound(@Valid @RequestBody PurchaseInboundDTO inboundDTO) {
        return Result.success(purchaseOrderService.generateInbound(inboundDTO.getOrderId()));
    }

    @PreAuthorize("hasAuthority('pharmacy:purchase:delete')")
    @Operation(summary = "删除采购订单")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long orderId) {
        purchaseOrderService.deleteById(orderId);
        return Result.success();
    }
}
