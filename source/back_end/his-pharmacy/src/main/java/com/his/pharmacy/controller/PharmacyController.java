package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.BizDrugStockUpsertDTO;
import com.his.pharmacy.dto.DrugStockChangeDTO;
import com.his.pharmacy.dto.DrugStockLogQueryPageDTO;
import com.his.pharmacy.dto.DrugStockQueryDTO;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.vo.BizDrugStockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药房管理控制器
 */
@Tag(name = "药房管理")
@RestController
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:stock:list', 'asset:supplies:list')")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @Operation(summary = "分页查询库存列表")
    @PostMapping("/stockListPage")
    public Result<PageResult<BizDrugStockVO>> stockListPage(@Valid @RequestBody DrugStockQueryDTO queryDTO) {
        return Result.success(pharmacyService.selectStockPage(
                queryDTO.getDrugName(), queryDTO.getStockStatus(), queryDTO.getStockRoom(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 批次候选（调拨/退货建单时点批次用）
     * <p>三个碰得到它的页面（库存/调拨/供应商退货）任一有权限即可，不新建独立权限码：
     * 同一份参照数据挂三把锁，非管理岗一进页面就 403 空掉下拉（AGENTS §4）。
     */
    @Operation(summary = "库存批次候选列表")
    @PostMapping("/stockBatchCandidates")
    @PreAuthorize("hasAnyAuthority('pharmacy:stock:list', 'pharmacy:drugTransfer:list', 'pharmacy:supplierReturn:list')")
    public Result<List<BizDrugStockVO>> stockBatchCandidates(@Valid @RequestBody DrugStockQueryDTO queryDTO) {
        boolean onlyWithSupplier = Boolean.TRUE.equals(queryDTO.getOnlyWithSupplier());
        return Result.success(pharmacyService.selectBatchCandidates(
                queryDTO.getStockRoom(), queryDTO.getDrugName(), onlyWithSupplier));
    }

    @Operation(summary = "获取库存详情")
    @GetMapping("/getStockById")
    public Result<BizDrugStockVO> getStockInfo(@RequestParam Long stockId) {
        return Result.success(pharmacyService.getStockDetailById(stockId));
    }

    @PreAuthorize("hasAuthority('pharmacy:stock:add')")
    @Operation(summary = "新增库存")
    @PostMapping("/stockUpsert")
    public Result<Void> addStock(@Valid @RequestBody BizDrugStockUpsertDTO upsertDTO) {
        boolean success = pharmacyService.addStock(upsertDTO);
        return success ? Result.success() : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('pharmacy:stock:edit')")
    @Operation(summary = "入库")
    @PostMapping("/stockInbound")
    public Result<Void> inboundStock(@Valid @RequestBody DrugStockChangeDTO changeDTO) {
        boolean success = pharmacyService.inboundStock(changeDTO.getStockId(), changeDTO.getQuantity());
        return success ? Result.success() : Result.error("入库失败");
    }

    @PreAuthorize("hasAuthority('pharmacy:stock:edit')")
    @Operation(summary = "出库")
    @PostMapping("/stockOutbound")
    public Result<Void> outboundStock(@Valid @RequestBody DrugStockChangeDTO changeDTO) {
        boolean success = pharmacyService.outboundStock(changeDTO.getStockId(), changeDTO.getQuantity());
        return success ? Result.success() : Result.error("出库失败");
    }

    @PreAuthorize("hasAuthority('pharmacy:stock:edit')")
    @Operation(summary = "查询库存预警列表")
    @PostMapping("/stockWarningList")
    public Result<List<BizDrugStockVO>> stockWarning(@Valid @RequestBody DrugStockQueryDTO queryDTO) {
        return Result.success(pharmacyService.selectStockWarningList(queryDTO.getStockStatus()));
    }

    @Operation(summary = "分页查询库存出入库流水")
    @PostMapping("/stockLogListPage")
    public Result<PageResult<BizDrugStockLogVO>> stockLogListPage(@Valid @RequestBody DrugStockLogQueryPageDTO queryDTO) {
        return Result.success(pharmacyService.selectStockLogPage(
                queryDTO.getDrugName(), queryDTO.getChangeType(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }
}
