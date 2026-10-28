package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.BizDrugStockUpsertDTO;
import com.his.pharmacy.dto.DrugStockChangeDTO;
import com.his.pharmacy.dto.DrugStockLogPageQueryDTO;
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
 * 药房库存控制器：药品批次库存的查询、手工建批、零星出入库与库存流水的接口入口。
 */
@Tag(name = "药房管理")
@RestController
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:stock:list', 'asset:supplies:list', 'opd:emergencyDoctor:list')")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    /**
     * 批次库存分页：按药名、库存状态、库位过滤，库位不传即药库与药房两层一起看。
     * 用于药房库存管理页的查询卡与顶部统计卡取数，门诊医生站选药时也用这一口径拿库存。
     */
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

    /**
     * 单条批次库存详情：按批次主键取该批次的余额、可用量、成本、效期等全量字段。
     * 页面当前直接用列表行回显，本接口留给需要按批次补全档案的核对与追溯环节。
     */
    @Operation(summary = "获取库存详情")
    @GetMapping("/getStockById")
    public Result<BizDrugStockVO> getStockInfo(@RequestParam Long stockId) {
        return Result.success(pharmacyService.getStockDetailById(stockId));
    }

    /**
     * 手工建库存批次：同一药品同一批号在同一库位只允许一条，库位没传按旧口径默认落药房。
     * 用于药房库存管理页「新增药品」弹框补录历史批次；正式进货仍走采购入库单，不用这个口子。
     */
    @PreAuthorize("hasAuthority('pharmacy:stock:add')")
    @Operation(summary = "新增库存")
    @PostMapping("/stockUpsert")
    public Result<Void> addStock(@Valid @RequestBody BizDrugStockUpsertDTO upsertDTO) {
        boolean success = pharmacyService.addStock(upsertDTO);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 批次加量入库：在该批次所属库位上加余额与可用量，并落一条入库流水。
     * 用于药房库存管理页入库弹框做零星补录；采购到货的批量入库仍走入库单审核后的入库环节。
     */
    @PreAuthorize("hasAuthority('pharmacy:stock:edit')")
    @Operation(summary = "入库")
    @PostMapping("/stockInbound")
    public Result<Void> inboundStock(@Valid @RequestBody DrugStockChangeDTO changeDTO) {
        boolean success = pharmacyService.inboundStock(changeDTO.getStockId(), changeDTO.getQuantity());
        return success ? Result.success() : Result.error("入库失败");
    }

    /**
     * 批次减量出库：可用量够才放行，扣的是该批次所属库位的余额，并落一条其他出库流水。
     * 用于药房库存管理页出库弹框处理报损、领用这类没有临床单据来源的减少。
     */
    @PreAuthorize("hasAuthority('pharmacy:stock:edit')")
    @Operation(summary = "出库")
    @PostMapping("/stockOutbound")
    public Result<Void> outboundStock(@Valid @RequestBody DrugStockChangeDTO changeDTO) {
        boolean success = pharmacyService.outboundStock(changeDTO.getStockId(), changeDTO.getQuantity());
        return success ? Result.success() : Result.error("出库失败");
    }

    /**
     * 库存预警清单：不分页一次捞出预警及以下状态的批次，含所属库位。
     * 供缺货提醒与补货、下拨的候选环节使用。
     */
    @PreAuthorize("hasAuthority('pharmacy:stock:edit')")
    @Operation(summary = "查询库存预警列表")
    @PostMapping("/stockWarningList")
    public Result<List<BizDrugStockVO>> stockWarning(@Valid @RequestBody DrugStockQueryDTO queryDTO) {
        return Result.success(pharmacyService.selectStockWarningList(queryDTO.getStockStatus()));
    }

    /**
     * 库存流水分页：按药名模糊与变动类型过滤药品批次的每一笔进出。
     * 供库存台账核对账实与追溯某药品进出量的环节使用；流水本身不记库位，要看货在哪由批次侧带出。
     */
    @Operation(summary = "分页查询库存出入库流水")
    @PostMapping("/stockLogListPage")
    public Result<PageResult<BizDrugStockLogVO>> stockLogListPage(@Valid @RequestBody DrugStockLogPageQueryDTO queryDTO) {
        return Result.success(pharmacyService.selectStockLogPage(
                queryDTO.getDrugName(), queryDTO.getChangeType(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }
}
