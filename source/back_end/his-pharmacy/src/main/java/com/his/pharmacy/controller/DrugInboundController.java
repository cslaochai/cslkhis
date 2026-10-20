package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.DrugInboundCancelDTO;
import com.his.pharmacy.dto.DrugInboundIdDTO;
import com.his.pharmacy.dto.DrugInboundPageQueryDTO;
import com.his.pharmacy.service.DrugInboundService;
import com.his.pharmacy.vo.DrugInboundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品入库单控制器
 */
@Tag(name = "药品入库")
@RestController
@RequestMapping("/drugInbound")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:inbound:list')")
public class DrugInboundController {

    private final DrugInboundService drugInboundService;

    /**
     * 药品入库单分页，可按单号/药品关键字与单据状态筛。
     * 用于药库「药品入库单」页的查询卡与入库单表格。
     */
    @Operation(summary = "分页查询入库单")
    @PostMapping("/listPage")
    public Result<PageResult<DrugInboundVO>> listPage(@Valid @RequestBody DrugInboundPageQueryDTO queryDTO) {
        return Result.success(drugInboundService.page(queryDTO));
    }

    /**
     * 取单张入库单的头部信息与全部到货明细（批号、效期、进货价、数量）。
     * 用于入库单列表行点开的详情弹框，也是审核与入库前的核对依据。
     */
    @Operation(summary = "获取入库单详情（含明细）")
    @GetMapping("/getDetailById")
    public Result<DrugInboundVO> getDetailById(@RequestParam Long inboundId) {
        return Result.success(drugInboundService.getDetailById(inboundId));
    }

    /**
     * 审核入库单：待审核推进为已审核并记录审核人与审核时间。
     * 用于药品入库单页行内审核，审核通过后这张单才允许入库。
     */
    @PreAuthorize("hasAuthority('pharmacy:inbound:edit')")
    @Operation(summary = "审核入库单（待审核 → 已审核）")
    @PostMapping("/audit")
    public Result<DrugInboundVO> audit(@Valid @RequestBody DrugInboundIdDTO idDTO) {
        drugInboundService.audit(idDTO);
        return Result.success(drugInboundService.getDetailById(idDTO.getInboundId()));
    }

    /**
     * 把已审核的入库单按明细建/加药品批次并写库存流水，单据推进为已入库。
     * 用于药品入库单页的收货确认；本单批次直接落药房库位（当前口径不经药库下拨），发药与退药回库都在药房这一层。
     */
    @PreAuthorize("hasAuthority('pharmacy:inbound:edit')")
    @Operation(summary = "入库（已审核 → 已入库，按明细建/加药品批次并写库存流水）")
    @PostMapping("/stockIn")
    public Result<DrugInboundVO> stockIn(@Valid @RequestBody DrugInboundIdDTO idDTO) {
        return Result.success(drugInboundService.stockIn(idDTO));
    }

    /**
     * 取消入库单：待审核或已审核的单推进为已取消并记录取消人与原因，明细整单作废。
     * 用于药品入库单页行内取消；已经入库的单据要冲销得走退货链路，不能在这里取消。
     */
    @PreAuthorize("hasAuthority('pharmacy:inbound:delete')")
    @Operation(summary = "取消入库单（待审核/已审核 → 已取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody DrugInboundCancelDTO cancelDTO) {
        drugInboundService.cancel(cancelDTO);
        return Result.success();
    }

    /**
     * 删除入库单连同其明细，已入库的单据不允许删除。
     * 用于清理录错的待审核单据；一旦入过库，批次与库存流水已经生成，只能靠红字冲销不能删凭证。
     */
    @PreAuthorize("hasAuthority('pharmacy:inbound:delete')")
    @Operation(summary = "删除入库单（已入库不可删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long inboundId) {
        drugInboundService.deleteById(inboundId);
        return Result.success();
    }
}
