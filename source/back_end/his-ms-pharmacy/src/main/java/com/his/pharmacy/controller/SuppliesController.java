package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.service.HighValueTraceService;
import com.his.pharmacy.service.SuppliesService;
import com.his.pharmacy.vo.*;
import com.his.common.entity.CurrentUser;
import com.his.common.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物资耗材控制器
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
    public Result<PageResult<SysConsumableVO>> consumableListPage(@Valid @RequestBody ConsumablePageQueryDTO queryDTO) {
        return Result.success(suppliesService.selectConsumablePage(
                queryDTO.getKeyword(), queryDTO.getCategory(), queryDTO.getStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 按ID取一条耗材字典档案（编码名称、类别规格单位、零售价、是否高值与 UDI 标识）。
     * 供耗材字典行点开或改单前取全量字段，列表页本身只吃分页结果。
     */
    @Operation(summary = "获取耗材详情")
    @GetMapping("/getConsumableById")
    public Result<SysConsumableVO> getConsumableById(@RequestParam Long id) {
        return Result.success(suppliesService.getConsumableById(id));
    }

    /**
     * 新增或修改耗材字典条目（无ID为新增，编码全表唯一）。
     * 用于耗材字典页的新增/修改弹框保存。
     */
    @PreAuthorize("hasAuthority('asset:supplies:add')")
    @Operation(summary = "耗材字典新增/修改")
    @PostMapping("/consumableUpsert")
    public Result<Void> consumableUpsert(@Valid @RequestBody ConsumableUpsertDTO dto) {
        boolean success = suppliesService.consumableUpsert(dto);
        return success ? Result.success() : Result.error("保存失败");
    }

    /**
     * 启用状态的耗材下拉候选，停用的耗材不出候选。
     * 用于建批入库、科室领用等弹框选耗材，也用于器械包模板组成行选耗材。
     */
    @Operation(summary = "启用耗材下拉")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/consumable/selectList")
    public Result<List<ConsumableSelectListVO>> consumableSelectList() {
        return Result.success(suppliesService.selectEnabledConsumables());
    }

    // 批次库存

    @Operation(summary = "库存分页")
    @PostMapping("/stockListPage")
    public Result<PageResult<BizConsumableStockVO>> stockListPage(@Valid @RequestBody ConsumableStockPageQueryDTO queryDTO) {
        return Result.success(suppliesService.selectStockPage(
                queryDTO.getKeyword(), queryDTO.getCategory(), queryDTO.getStockStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 按批次主键取一个耗材批次库存的实时档案（批号效期、数量、成本、存放位置与所属耗材信息）。
     * 供补货/出库等动作前按批次ID取数核对，列表页本身只吃分页结果。
     */
    @Operation(summary = "获取库存详情")
    @GetMapping("/getStockById")
    public Result<BizConsumableStockVO> getStockById(@RequestParam Long stockId) {
        return Result.success(suppliesService.getStockDetailById(stockId));
    }

    /**
     * 为某耗材新建一个批次库存并记首次入库（同耗材同批号已存在则拒绝，只能走补货）。
     * 用于物资耗材页的建批入库弹框，到货拿到批号效期与进货价后录第一手库存。
     */
    @PreAuthorize("hasAuthority('asset:supplies:add')")
    @Operation(summary = "建批入库")
    @PostMapping("/stockUpsert")
    public Result<Void> stockUpsert(@Valid @RequestBody ConsumableStockUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        boolean success = suppliesService.addStock(dto.getConsumableId(), dto.getBatchNo(),
                dto.getProductionDate(), dto.getExpiryDate(), dto.getQuantity(), dto.getCostPrice(),
                dto.getLocation(), dto.getSupplier(), operatorUser.getRealName());
        return success ? Result.success() : Result.error("入库失败");
    }

    /**
     * 往已有批次上追加数量并记入库流水，不新建批次。
     * 用于物资耗材页批次库存行内的补货入库，同批号再次到货时接着加。
     */
    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "补货入库")
    @PostMapping("/stockInbound")
    public Result<Void> stockInbound(@Valid @RequestBody ConsumableStockChangeDTO changeDTO) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        boolean success = suppliesService.inboundStock(changeDTO.getStockId(), changeDTO.getQuantity(), operatorUser.getRealName());
        return success ? Result.success() : Result.error("入库失败");
    }

    /**
     * 从指定批次手工减数量并记出库流水，用于院内非领用的消耗（报损、报废、院外调走等）。
     * 用于物资耗材页批次库存行内的其他出库；科室领用得走领用接口才会留台账。
     */
    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "其他出库")
    @PostMapping("/stockOutbound")
    public Result<Void> stockOutbound(@Valid @RequestBody ConsumableStockChangeDTO changeDTO) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        boolean success = suppliesService.outboundStock(changeDTO.getStockId(), changeDTO.getQuantity(), operatorUser.getRealName());
        return success ? Result.success() : Result.error("出库失败");
    }

    // 科室领用

    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "科室领用（扣库存 FEFO）")
    @PostMapping("/consume")
    public Result<Void> consume(@Valid @RequestBody ConsumableConsumeDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        boolean success = suppliesService.consume(dto.getConsumableId(), dto.getQuantity(),
                dto.getDeptId(), dto.getPurpose(), operatorUser.getRealName());
        return success ? Result.success("领用成功", null) : Result.error("领用失败");
    }

    /**
     * 科室领用台账分页，可按耗材关键字与领用科室筛。
     * 用于物资耗材页「科室领用台账」页签，月度科室耗材消耗归集靠这张表。
     */
    @PreAuthorize("hasAuthority('asset:supplies:edit')")
    @Operation(summary = "领用台账分页")
    @PostMapping("/consumeListPage")
    public Result<PageResult<BizConsumableConsumeVO>> consumeListPage(@Valid @RequestBody ConsumableConsumePageQueryDTO queryDTO) {
        return Result.success(suppliesService.selectConsumePage(
                queryDTO.getKeyword(), queryDTO.getDeptId(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    // 流水

    @Operation(summary = "出入库流水分页")
    @PostMapping("/stockLogListPage")
    public Result<PageResult<BizConsumableStockLogVO>> stockLogListPage(@Valid @RequestBody ConsumableStockLogPageQueryDTO queryDTO) {
        return Result.success(suppliesService.selectStockLogPage(
                queryDTO.getKeyword(), queryDTO.getChangeType(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    // 高值耗材 UDI 扫码溯源（L11）

    @Operation(summary = "UDI 扫码解析（拆DI/序列号/批号/有效期 + 字典命中 + 有货批次候选）")
    @PostMapping("/udiScan")
    public Result<UdiScanVO> udiScan(@Valid @RequestBody UdiScanDTO dto) {
        return Result.success(highValueTraceService.scanUdi(dto.getUdiCode()));
    }

    /**
     * 登记一枚高值耗材用到哪位患者身上：绑定批次与患者、从批次扣一件并尝试记账。
     * 用于物资耗材页「高值扫码登记」页签在扫码解析出候选批次后的确认动作。
     */
    @Operation(summary = "高值耗材使用登记（关联患者+扣批次1件+计费尝试）")
    @PostMapping("/traceUse")
    public Result<BizConsumableTraceVO> traceUse(@Valid @RequestBody HighValueUseDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(highValueTraceService.traceUse(dto, operatorUser.getRealName()));
    }

    /**
     * 高值耗材使用溯源台账分页，可按患者正查用了哪些耗材，也可按耗材与 UDI 关键字反查落到了谁身上。
     * 用于物资耗材页「溯源台账」页签。
     */
    @Operation(summary = "溯源台账分页（正/反向追溯）")
    @PostMapping("/traceListPage")
    public Result<PageResult<BizConsumableTraceVO>> traceListPage(@Valid @RequestBody ConsumableTracePageQueryDTO queryDTO) {
        return Result.success(highValueTraceService.selectTracePage(queryDTO));
    }

    /**
     * 按溯源记录ID摊开全链：耗材字典条目、入库批次、用到哪位患者、计费结果。
     * 用于溯源台账行点开的详情弹框，回答「这一件从哪来、用到谁身上、收没收到钱」。
     */
    @Operation(summary = "溯源详情（字典→入库批次→使用患者→计费 全链）")
    @GetMapping("/getTraceDetailById")
    public Result<ConsumableTraceDetailVO> getTraceDetailById(@RequestParam Long traceId) {
        return Result.success(highValueTraceService.getTraceDetailById(traceId));
    }

    /**
     * 撤销一条高值耗材使用登记并记原因，那一件退回原批次；已经计费的记录不许作废。
     * 用于溯源台账行内的作废登记，比如发错型号、患者拒用把耗材收回库房。
     */
    @Operation(summary = "溯源记录作废（退货回库；已计费拒绝）")
    @PostMapping("/traceVoid")
    public Result<BizConsumableTraceVO> traceVoid(@Valid @RequestBody TraceVoidDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(highValueTraceService.traceVoid(dto.getTraceId(), dto.getReason(), operatorUser.getRealName()));
    }

    /**
     * 对未计费或计费失败的高值耗材使用记录重走一次记账，沿用登记时快照的耗材与患者。
     * 用于溯源台账行内的计费失败补记：药用了、库存扣了，费用漏收时靠这一步补上。
     */
    @Operation(summary = "计费失败补记（按台账快照重走计费）")
    @PostMapping("/traceRecharge")
    public Result<BizConsumableTraceVO> traceRecharge(@Valid @RequestBody TraceRechargeDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(highValueTraceService.traceRecharge(dto.getTraceId(), operatorUser.getRealName()));
    }

}
