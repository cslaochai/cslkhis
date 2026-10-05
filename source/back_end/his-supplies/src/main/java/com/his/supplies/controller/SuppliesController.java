package com.his.supplies.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.security.UserUtils;
import com.his.supplies.dto.*;
import com.his.supplies.service.HighValueTraceService;
import com.his.supplies.service.SuppliesService;
import com.his.supplies.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物资耗材控制器
 * 耗材域独立于药品域：字典 + 批次库存 + 出入库流水 + 科室领用（领用即扣库存）。
 */
@Tag(name = "物资耗材管理")
@RestController
@RequestMapping("/supplies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:supplies:list')")
public class SuppliesController {

    private final SuppliesService suppliesService;
    private final HighValueTraceService highValueTraceService;

    // 耗材字典

    @Operation(summary = "耗材字典分页")
    @PostMapping("/consumableListPage")
    public Result<PageResult<SysConsumableVO>> consumableListPage(@RequestBody ConsumableQueryPageDTO queryDTO) {
        return Result.success(suppliesService.selectConsumablePage(
                queryDTO.getKeyword(), queryDTO.getCategory(), queryDTO.getStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "获取耗材详情")
    @GetMapping("/getConsumableById")
    public Result<SysConsumableVO> getConsumableById(@RequestParam Long id) {
        return Result.success(suppliesService.getConsumableById(id));
    }

    @PreAuthorize("hasAuthority('asset:supplies:add')")
    @Operation(summary = "耗材字典新增/修改")
    @PostMapping("/consumableUpsert")
    public Result<Void> consumableUpsert(@Valid @RequestBody ConsumableUpsertDTO dto) {
        boolean success = suppliesService.consumableUpsert(dto);
        return success ? Result.success() : Result.error("保存失败");
    }

    @Operation(summary = "启用耗材下拉")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/consumable/selectList")
    public Result<List<ConsumableSelectListVO>> consumableSelectList() {
        return Result.success(suppliesService.selectEnabledConsumables());
    }

    // 批次库存

    @Operation(summary = "库存分页")
    @PostMapping("/stockListPage")
    public Result<PageResult<BizConsumableStockVO>> stockListPage(@RequestBody ConsumableStockQueryPageDTO queryDTO) {
        return Result.success(suppliesService.selectStockPage(
                queryDTO.getKeyword(), queryDTO.getCategory(), queryDTO.getStockStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "获取库存详情")
    @GetMapping("/getStockById")
    public Result<BizConsumableStockVO> getStockById(@RequestParam Long stockId) {
        return Result.success(suppliesService.getStockDetailById(stockId));
    }

    @PreAuthorize("hasAuthority('asset:supplies:add')")
    @Operation(summary = "建批入库")
    @PostMapping("/stockUpsert")
    public Result<Void> stockUpsert(@Valid @RequestBody ConsumableStockUpsertDTO dto) {
        boolean success = suppliesService.addStock(dto.getConsumableId(), dto.getBatchNo(),
                dto.getProductionDate(), dto.getExpiryDate(), dto.getQuantity(), dto.getCostPrice(),
                dto.getLocation(), dto.getSupplier(), operatorName());
        return success ? Result.success() : Result.error("入库失败");
    }

    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "补货入库")
    @PostMapping("/stockInbound")
    public Result<Void> stockInbound(@RequestBody ConsumableStockChangeDTO changeDTO) {
        boolean success = suppliesService.inboundStock(changeDTO.getStockId(), changeDTO.getQuantity(), operatorName());
        return success ? Result.success() : Result.error("入库失败");
    }

    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "其他出库")
    @PostMapping("/stockOutbound")
    public Result<Void> stockOutbound(@RequestBody ConsumableStockChangeDTO changeDTO) {
        boolean success = suppliesService.outboundStock(changeDTO.getStockId(), changeDTO.getQuantity(), operatorName());
        return success ? Result.success() : Result.error("出库失败");
    }

    // 科室领用

    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "科室领用（扣库存 FEFO）")
    @PostMapping("/consume")
    public Result<Void> consume(@Valid @RequestBody ConsumableConsumeDTO dto) {
        boolean success = suppliesService.consume(dto.getConsumableId(), dto.getQuantity(),
                dto.getDeptId(), dto.getPurpose(), operatorName());
        return success ? Result.success("领用成功", null) : Result.error("领用失败");
    }

    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "领用台账分页")
    @PostMapping("/consumeListPage")
    public Result<PageResult<BizConsumableConsumeVO>> consumeListPage(@RequestBody ConsumableConsumeQueryPageDTO queryDTO) {
        return Result.success(suppliesService.selectConsumePage(
                queryDTO.getKeyword(), queryDTO.getDeptId(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    // 流水

    @Operation(summary = "出入库流水分页")
    @PostMapping("/stockLogListPage")
    public Result<PageResult<BizConsumableStockLogVO>> stockLogListPage(@RequestBody ConsumableStockLogQueryPageDTO queryDTO) {
        return Result.success(suppliesService.selectStockLogPage(
                queryDTO.getKeyword(), queryDTO.getChangeType(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    // 高值耗材 UDI 扫码溯源（L11）

    @Operation(summary = "UDI 扫码解析（拆DI/序列号/批号/有效期 + 字典命中 + 有货批次候选）")
    @PostMapping("/udiScan")
    public Result<UdiScanVO> udiScan(@Valid @RequestBody UdiScanDTO dto) {
        return Result.success(highValueTraceService.scanUdi(dto.getUdiCode()));
    }

    @Operation(summary = "高值耗材使用登记（关联患者+扣批次1件+计费尝试）")
    @PostMapping("/traceUse")
    public Result<BizConsumableTraceVO> traceUse(@Valid @RequestBody HighValueUseDTO dto) {
        return Result.success(highValueTraceService.traceUse(dto, operatorName()));
    }

    @Operation(summary = "溯源台账分页（正/反向追溯）")
    @PostMapping("/traceListPage")
    public Result<PageResult<BizConsumableTraceVO>> traceListPage(@RequestBody ConsumableTraceQueryPageDTO queryDTO) {
        return Result.success(highValueTraceService.selectTracePage(queryDTO));
    }

    @Operation(summary = "溯源详情（字典→入库批次→使用患者→计费 全链）")
    @GetMapping("/getTraceDetailById")
    public Result<ConsumableTraceDetailVO> getTraceDetailById(@RequestParam Long traceId) {
        return Result.success(highValueTraceService.getTraceDetailById(traceId));
    }

    @Operation(summary = "溯源记录作废（退货回库；已计费拒绝）")
    @PostMapping("/traceVoid")
    public Result<BizConsumableTraceVO> traceVoid(@Valid @RequestBody TraceVoidDTO dto) {
        return Result.success(highValueTraceService.traceVoid(dto.getTraceId(), dto.getReason(), operatorName()));
    }

    @Operation(summary = "计费失败补记（按台账快照重走计费）")
    @PostMapping("/traceRecharge")
    public Result<BizConsumableTraceVO> traceRecharge(@Valid @RequestBody TraceRechargeDTO dto) {
        return Result.success(highValueTraceService.traceRecharge(dto.getTraceId(), operatorName()));
    }

    private String operatorName() {
        String name = UserUtils.getCurrentEmployeeName();
        return (name != null && !name.isBlank()) ? name : "系统操作";
    }
}
