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
 * 血库储血台账控制器：血袋入库、库存异动、交叉配血全流程与出入库流水的接口入口。
 */
@Tag(name = "血库管理")
@RestController
@RequestMapping("/medicaltech/bloodBank")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:bloodBank:list')")
public class BloodController {

    private final BloodService bloodService;

    /**
     * 血袋入库登记：写入库存行并记一笔入库流水。
     * 用于血库储血台账页「入库」弹框确认后落账。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "血袋入库登记")
    @PostMapping("/inbound")
    public Result<BloodVO.InventoryVO> inbound(@Valid @RequestBody BloodDTO.Inbound dto) {
        return Result.success("血袋已入库", bloodService.toInvVo(bloodService.inbound(dto)));
    }

    /**
     * 库存台账分页：按袋号、血型、血液成分、状态与效期预警天数筛血袋。
     * 用于血库储血台账页库存查询卡。
     */
    @Operation(summary = "库存台账分页")
    @PostMapping("/inventoryListPage")
    public Result<PageResult<BloodVO.InventoryVO>> inventoryListPage(@Valid @RequestBody BloodDTO.InventoryQuery query) {
        return Result.success(bloodService.inventoryPage(query));
    }

    /**
     * 库存统计：在库、预留、效期预警总量与分血型分布。
     * 用于血库储血台账页顶部统计卡，随入库与发血操作后刷新。
     */
    @Operation(summary = "库存统计（在库/预留/效期预警/分血型）")
    @GetMapping("/inventoryStats")
    public Result<BloodVO.StatsVO> inventoryStats() {
        return Result.success(bloodService.inventoryStats());
    }

    /**
     * 预留血袋：把在库血袋置为已预留并记流水。
     * 用于库存台账行的「预留」操作。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "预留血袋")
    @PostMapping("/reserve")
    public Result<Void> reserve(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.reserve(dto);
        return Result.success("血袋已预留", null);
    }

    /**
     * 取消预留：把已预留的血袋释回在库。
     * 用于库存台账行的「取消预留」操作。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:delete')")
    @Operation(summary = "取消预留")
    @PostMapping("/cancelReserve")
    public Result<Void> cancelReserve(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.cancelReserve(dto);
        return Result.success("预留已取消", null);
    }

    /**
     * 发血：血袋出库交付临床使用，只认配血复核相合产生的预留，且必须带用血申请单号。
     * 用于库存台账行的「发血」操作。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "发血（必须已预留，且带用血申请单号）")
    @PostMapping("/issue")
    public Result<Void> issue(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.issue(dto);
        return Result.success("发血完成", null);
    }

    /**
     * 血袋报废：置为报废态并记流水，报废原因必填。
     * 用于库存台账行的「报废」操作（效期到期、血袋破损等）。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:delete')")
    @Operation(summary = "报废（必填原因）")
    @PostMapping("/scrap")
    public Result<Void> scrap(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.scrap(dto);
        return Result.success("血袋已报废", null);
    }

    /**
     * 血袋退回：把在库或已预留的血袋置为已退回并记出库人与时刻，退回原因必填。
     * 用于库存台账行的「退回」操作。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "退回（必填原因）")
    @PostMapping("/returnBag")
    public Result<Void> returnBag(@Valid @RequestBody BloodDTO.BagAction dto) {
        bloodService.returnBag(dto);
        return Result.success("血袋已退回", null);
    }

    /**
     * 新建配血单：为一名用血患者生成一张待配血单据。
     * 用于血库储血台账页「交叉配血」的「新建配血单」弹框提交。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:add')")
    @Operation(summary = "新建配血单（待配血）")
    @PostMapping("/crossmatchCreate")
    public Result<BloodVO.CrossmatchVO> crossmatchCreate(@Valid @RequestBody BloodDTO.CrossmatchCreate dto) {
        return Result.success("配血单已创建", bloodService.crossmatchCreate(dto));
    }

    /**
     * 配血单分页：按单号、袋号、患者、状态与配血结果筛配血单。
     * 用于血库储血台账页「交叉配血」的查询卡。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "配血单分页")
    @PostMapping("/crossmatchListPage")
    public Result<PageResult<BloodVO.CrossmatchVO>> crossmatchListPage(@Valid @RequestBody BloodDTO.CrossmatchQuery query) {
        return Result.success(bloodService.crossmatchPage(query));
    }

    /**
     * 执行配血：登记配血方法与结果，单据由待配血转入待复核。
     * 用于配血单行的「执行配血」弹框提交，是双人复核的第一步。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "执行配血")
    @PostMapping("/crossmatchExecute")
    public Result<Void> crossmatchExecute(@Valid @RequestBody BloodDTO.CrossmatchExecute dto) {
        bloodService.crossmatchExecute(dto);
        return Result.success("配血已执行，待复核", null);
    }

    /**
     * 配血复核：复核人不得是配血人，结果相合时自动预留名下血袋。
     * 用于配血单行的「复核」操作，是发血的准入闸门。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:edit')")
    @Operation(summary = "配血复核（复核人不得是配血人；相合自动预留血袋）")
    @PostMapping("/crossmatchVerify")
    public Result<Void> crossmatchVerify(@Valid @RequestBody BloodDTO.CrossmatchVerify dto) {
        bloodService.crossmatchVerify(dto);
        return Result.success("复核通过", null);
    }

    /**
     * 作废配血单：已复核的单据不可作废。
     * 用于配血单行的「作废」操作。
     */
    @PreAuthorize("hasAuthority('medtech:bloodBank:delete')")
    @Operation(summary = "作废配血单（已复核不可作废）")
    @PostMapping("/crossmatchVoid")
    public Result<Void> crossmatchVoid(@Valid @RequestBody BloodDTO.CrossmatchVoid dto) {
        bloodService.crossmatchVoid(dto.getMatchId());
        return Result.success("配血单已作废", null);
    }

    /**
     * 出入库流水分页：逐笔给出血袋状态迁移、操作人与时刻。
     * 用于血库储血台账页「出入库流水」的查询卡，流水只增不改。
     */
    @Operation(summary = "出入库流水分页")
    @PostMapping("/logListPage")
    public Result<PageResult<BloodVO.StockLogVO>> logListPage(@Valid @RequestBody BloodDTO.LogQuery query) {
        return Result.success(bloodService.logPage(query));
    }
}
