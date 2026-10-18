package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.service.StocktakeService;
import com.his.pharmacy.vo.StocktakeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药房盘点（第 1 期：账面快照 → 实盘录入 → 差异 → 复核过账）
 */
@Tag(name = "药房盘点")
@RestController
@RequestMapping("/pharmacy/stocktake")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeService stocktakeService;

    /**
     * 盘点单分页：按盘点单号、主题、状态与建单日期区间翻页，行上带批次数与差异汇总。
     * 用于药房「药房盘点」页查询卡。
     */
    @Operation(summary = "盘点单分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:list')")
    public Result<PageResult<StocktakeVO>> listPage(@Valid @RequestBody StocktakePageQueryDTO query) {
        return Result.success(stocktakeService.listPage(query));
    }

    /**
     * 盘点单详情：主单 + 逐批次明细（账面数、实盘数、差异、是否已过账）+ 本次过账产生的库存流水。
     * 用于盘点详情弹框，也是复核人核对差异的证据页。
     */
    @Operation(summary = "盘点单详情（含明细与本次过账流水）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:list')")
    public Result<StocktakeVO> getDetailById(@RequestParam Long id) {
        return Result.success(stocktakeService.getDetailById(id));
    }

    /**
     * 建盘点单或改主题与范围：新建按范围抓一次账面快照，改范围会重抓快照并清空已录的实盘数。
     * 用于药房盘点页的新增/编辑弹框；范围只按药品类型与关键字圈定，药库与药房两层库位的批次一起入盘。
     */
    @Operation(summary = "建单（抓账面快照）/ 改主题与范围")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:add')")
    public Result<StocktakeVO> upsert(@Valid @RequestBody StocktakeUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "盘点单已生成" : "盘点单已保存", stocktakeService.upsert(dto));
    }

    /**
     * 录入实盘数：逐条算出差异数量与差异金额，只在盘点中状态可写，可反复保存且不产生库存流水。
     * 用于盘点详情弹框的实盘录入表格。
     */
    @Operation(summary = "录入实盘数（可反复保存）")
    @PostMapping("/saveCount")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:add')")
    public Result<StocktakeVO> saveCount(@Valid @RequestBody StocktakeCountUpsertDTO dto) {
        return Result.success("实盘数已保存", stocktakeService.saveCount(dto));
    }

    /**
     * 提交盘点：批次没盘完直接拒绝（漏盘等于把账实不符永久留在账上），无差异当场关单，有差异转待复核。
     * 用于盘点详情页录完实盘后的送审动作。
     */
    @Operation(summary = "提交（无差异直接关单，有差异转待复核）")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:edit')")
    public Result<StocktakeVO> submit(@Valid @RequestBody StocktakeIdDTO dto) {
        return Result.success("盘点单已提交", stocktakeService.submit(dto));
    }

    /**
     * 复核盘点单：通过则逐条差异改批次余额并落盘盈/盘亏流水（批次在自己的库位上过账），任一条被闸门挡下整单回滚；不通过退回盘点中重录、实盘数原样保留。
     * 用于药房盘点页的复核过账，是盘点唯一动库存的一步。
     */
    @Operation(summary = "复核（通过则差异过账落库存流水；不通过退回盘点中）")
    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:edit')")
    public Result<StocktakeVO> audit(@Valid @RequestBody StocktakeAuditDTO dto) {
        return Result.success(Boolean.TRUE.equals(dto.getPass()) ? "差异已过账" : "已退回重录",
                stocktakeService.audit(dto));
    }

    /**
     * 删除盘点单：仅「盘点中」可删，主单与明细一并物理清除。
     * 用于盘点页废弃重开的单子；待复核请复核人退回，已过账与已关单是留档凭证删不掉。
     */
    @Operation(summary = "删除盘点单（仅盘点中）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('pharmacy:stocktake:delete')")
    public Result<Void> deleteById(@RequestParam Long id) {
        stocktakeService.deleteById(id);
        return Result.success("盘点单已删除", null);
    }
}
