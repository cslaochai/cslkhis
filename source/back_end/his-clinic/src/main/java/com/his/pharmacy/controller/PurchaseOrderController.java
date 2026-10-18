package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.PurchaseInboundDTO;
import com.his.pharmacy.dto.PurchaseOrderAuditDTO;
import com.his.pharmacy.dto.PurchaseOrderPageQueryDTO;
import com.his.pharmacy.dto.PurchaseOrderUpsertDTO;
import com.his.pharmacy.service.PurchaseOrderService;
import com.his.pharmacy.vo.DrugInboundVO;
import com.his.pharmacy.vo.PurchaseOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品采购订单控制器：向供应商下单、审批与转入库单的接口入口。
 */
@Tag(name = "药品采购")
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:purchase:list')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    /**
     * 采购订单分页：按订单号、供应商、审批状态、是否已转入库单与下单日期区间翻页。
     * 用于药房「采购订单」页查询卡。
     */
    @Operation(summary = "分页查询采购订单")
    @PostMapping("/listPage")
    public Result<PageResult<PurchaseOrderVO>> listPage(@Valid @RequestBody PurchaseOrderPageQueryDTO queryDTO) {
        return Result.success(purchaseOrderService.page(queryDTO));
    }

    /**
     * 采购订单详情：主单连同全部药品明细行一起返回。
     * 用于采购订单详情弹框，也是编辑弹框与「转入库单」前的取数口。
     */
    @Operation(summary = "获取采购订单详情（含明细）")
    @GetMapping("/getDetailById")
    public Result<PurchaseOrderVO> getDetailById(@RequestParam Long orderId) {
        return Result.success(purchaseOrderService.getDetailById(orderId));
    }

    /**
     * 新增或修改采购订单：主单与明细一次覆盖保存，改单等于重新提交（审批状态回到待审批）。
     * 用于采购订单页的新增/编辑弹框；已审批通过或已转入库单的订单改不动，只能先驳回或取消入库单。
     */
    @PreAuthorize("hasAuthority('pharmacy:purchase:add')")
    @Operation(summary = "新增/修改采购订单（含明细）")
    @PostMapping("/upsert")
    public Result<PurchaseOrderVO> upsert(@Valid @RequestBody PurchaseOrderUpsertDTO upsertDTO) {
        Long orderId = purchaseOrderService.upsert(upsertDTO);
        return Result.success(purchaseOrderService.getDetailById(orderId));
    }

    /**
     * 审批采购订单：待审批的单子判通过或驳回，驳回必须填原因，本步只改审批状态不动库存。
     * 用于采购订单页的审批动作；只有审批通过的单子才能转入库单。
     */
    @PreAuthorize("hasAuthority('pharmacy:purchase:edit')")
    @Operation(summary = "审批采购订单（1-通过 2-驳回）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody PurchaseOrderAuditDTO auditDTO) {
        purchaseOrderService.audit(auditDTO);
        return Result.success();
    }

    /**
     * 由采购订单转生成入库单：照订单明细复制出待审核的入库行，一张订单只允许一张未取消的入库单。
     * 用于采购订单页审批通过后的转入库动作；本接口不碰任何库位库存，真正建批次入库在入库单审核后的入库环节。
     */
    @PreAuthorize("hasAuthority('pharmacy:purchase:add')")
    @Operation(summary = "由采购订单生成入库单（审批通过后；本接口不动库存）")
    @PostMapping("/generateInbound")
    public Result<DrugInboundVO> generateInbound(@Valid @RequestBody PurchaseInboundDTO inboundDTO) {
        return Result.success(purchaseOrderService.generateInbound(inboundDTO.getOrderId()));
    }

    /**
     * 删除采购订单：主单与明细一并移除，已生成未取消入库单的订单删不掉。
     * 用于采购订单页清理误建或作废的单子，避免留下没人认领的采购台账。
     */
    @PreAuthorize("hasAuthority('pharmacy:purchase:delete')")
    @Operation(summary = "删除采购订单")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long orderId) {
        purchaseOrderService.deleteById(orderId);
        return Result.success();
    }
}
