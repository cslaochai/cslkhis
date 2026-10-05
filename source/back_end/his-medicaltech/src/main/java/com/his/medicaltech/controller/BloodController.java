package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.BloodDTO;
import com.his.medicaltech.service.BloodService;
import com.his.medicaltech.vo.BloodVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 血库储血台账接口（URL 前缀 /medicaltech/bloodBank）
 */
@Tag(name = "血库管理")
@RestController
@RequestMapping("/medicaltech/bloodBank")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:bloodBank:list')")
public class BloodController {

    private final BloodService bloodService;

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "血袋入库登记")
    @PostMapping("/inbound")
    public Result<BloodVO.InventoryVO> inbound(@Valid @RequestBody BloodDTO.Inbound dto) {
        return Result.success("血袋已入库", bloodService.toInvVo(bloodService.inbound(dto)));
    }

    @Operation(summary = "库存台账分页")
    @PostMapping("/inventoryListPage")
    public Result<PageResult<BloodVO.InventoryVO>> inventoryListPage(@Valid @RequestBody BloodDTO.InventoryQuery query) {
        return Result.success(bloodService.inventoryPage(query));
    }

    @Operation(summary = "库存统计（在库/预留/效期预警/分血型）")
    @GetMapping("/inventoryStats")
    public Result<BloodVO.StatsVO> inventoryStats() {
        return Result.success(bloodService.inventoryStats());
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "预留血袋")
    @PostMapping("/reserve")
    public Result<Void> reserve(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.reserve(dto);
        return Result.success("血袋已预留", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:delete')")
    @Operation(summary = "取消预留")
    @PostMapping("/cancelReserve")
    public Result<Void> cancelReserve(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.cancelReserve(dto);
        return Result.success("预留已取消", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "发血（必须已预留，且带用血申请单号）")
    @PostMapping("/issue")
    public Result<Void> issue(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.issue(dto);
        return Result.success("发血完成", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:delete')")
    @Operation(summary = "报废（必填原因）")
    @PostMapping("/scrap")
    public Result<Void> scrap(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.scrap(dto);
        return Result.success("血袋已报废", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "退回（必填原因）")
    @PostMapping("/returnBag")
    public Result<Void> returnBag(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.returnBag(dto);
        return Result.success("血袋已退回", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:add')")
    @Operation(summary = "新建配血单（待配血）")
    @PostMapping("/crossmatchCreate")
    public Result<BloodVO.CrossmatchVO> crossmatchCreate(@Valid @RequestBody BloodDTO.CrossmatchCreate dto) {
        return Result.success("配血单已创建", bloodService.crossmatchCreate(dto));
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "配血单分页")
    @PostMapping("/crossmatchListPage")
    public Result<PageResult<BloodVO.CrossmatchVO>> crossmatchListPage(@Valid @RequestBody BloodDTO.CrossmatchQuery query) {
        return Result.success(bloodService.crossmatchPage(query));
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "执行配血")
    @PostMapping("/crossmatchExecute")
    public Result<Void> crossmatchExecute(@Valid @RequestBody BloodDTO.CrossmatchExecute dto) {
        bloodService.crossmatchExecute(dto);
        return Result.success("配血已执行，待复核", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "配血复核（复核人不得是配血人；相合自动预留血袋）")
    @PostMapping("/crossmatchVerify")
    public Result<Void> crossmatchVerify(@Valid @RequestBody BloodDTO.CrossmatchVerify dto) {
        bloodService.crossmatchVerify(dto);
        return Result.success("复核通过", null);
    }

    @PreAuthorize("hasAuthority('medtech:bloodBank:delete')")
    @Operation(summary = "作废配血单（已复核不可作废）")
    @PostMapping("/crossmatchVoid")
    public Result<Void> crossmatchVoid(@Valid @RequestBody BloodDTO.CrossmatchVoid dto) {
        bloodService.crossmatchVoid(dto.getMatchId());
        return Result.success("配血单已作废", null);
    }

    @Operation(summary = "出入库流水分页")
    @PostMapping("/logListPage")
    public Result<PageResult<BloodVO.StockLogVO>> logListPage(@Valid @RequestBody BloodDTO.LogQuery query) {
        return Result.success(bloodService.logPage(query));
    }
}
