package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.StocktakeAuditDTO;
import com.his.pharmacy.dto.StocktakeCountUpsertDTO;
import com.his.pharmacy.dto.StocktakeIdDTO;
import com.his.pharmacy.dto.StocktakeQueryPageDTO;
import com.his.pharmacy.dto.StocktakeUpsertDTO;
import com.his.pharmacy.service.StocktakeService;
import com.his.pharmacy.vo.StocktakeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 药房盘点（第 1 期：账面快照 → 实盘录入 → 差异 → 复核过账）
 * <p>约定：查询一律 GET，写操作一律 POST，路径驼峰；鉴权只标方法（AGENTS §4，类级会静默覆盖）。
 */
@Tag(name = "药房盘点")
@RestController
@RequestMapping("/pharmacy/stocktake")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeService stocktakeService;

    @Operation(summary = "盘点单分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:list')")
    public Result<PageResult<StocktakeVO>> listPage(@RequestBody StocktakeQueryPageDTO query) {
        return Result.success(stocktakeService.listPage(query));
    }

    @Operation(summary = "盘点单详情（含明细与本次过账流水）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:list')")
    public Result<StocktakeVO> getDetailById(@RequestParam Long id) {
        return Result.success(stocktakeService.getDetailById(id));
    }

    @Operation(summary = "建单（抓账面快照）/ 改主题与范围")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:add')")
    public Result<StocktakeVO> upsert(@Valid @RequestBody StocktakeUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "盘点单已生成" : "盘点单已保存", stocktakeService.upsert(dto));
    }

    @Operation(summary = "录入实盘数（可反复保存）")
    @PostMapping("/saveCount")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:add')")
    public Result<StocktakeVO> saveCount(@Valid @RequestBody StocktakeCountUpsertDTO dto) {
        return Result.success("实盘数已保存", stocktakeService.saveCount(dto));
    }

    @Operation(summary = "提交（无差异直接关单，有差异转待复核）")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:edit')")
    public Result<StocktakeVO> submit(@Valid @RequestBody StocktakeIdDTO dto) {
        return Result.success("盘点单已提交", stocktakeService.submit(dto));
    }

    @Operation(summary = "复核（通过则差异过账落库存流水；不通过退回盘点中）")
    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:edit')")
    public Result<StocktakeVO> audit(@Valid @RequestBody StocktakeAuditDTO dto) {
        return Result.success(Boolean.TRUE.equals(dto.getPass()) ? "差异已过账" : "已退回重录",
                stocktakeService.audit(dto));
    }

    @Operation(summary = "删除盘点单（仅盘点中）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:delete')")
    public Result<Void> deleteById(@RequestParam Long id) {
        stocktakeService.deleteById(id);
        return Result.success("盘点单已删除", null);
    }
}
