package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.DrugInboundCancelDTO;
import com.his.pharmacy.dto.DrugInboundIdDTO;
import com.his.pharmacy.dto.DrugInboundQueryPageDTO;
import com.his.pharmacy.service.DrugInboundService;
import com.his.pharmacy.vo.DrugInboundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 药品入库单控制器
 *
 * 链路：采购订单审批通过 →（生成）入库单【待审核】→（审核）【已审核】→（入库）【已入库，动库存】
 * 生成入库单的入口在采购订单侧（/purchase/generateInbound），本控制器负责入库单自身的流转。
 */
@Tag(name = "药品入库")
@RestController
@RequestMapping("/drugInbound")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:inbound:list')")
public class DrugInboundController {

    private final DrugInboundService drugInboundService;

    @Operation(summary = "分页查询入库单")
    @PostMapping("/listPage")
    public Result<PageResult<DrugInboundVO>> listPage(@RequestBody DrugInboundQueryPageDTO queryDTO) {
        return Result.success(drugInboundService.page(queryDTO));
    }

    @Operation(summary = "获取入库单详情（含明细）")
    @GetMapping("/getDetailById")
    public Result<DrugInboundVO> getDetailById(@RequestParam Long inboundId) {
        return Result.success(drugInboundService.getDetailById(inboundId));
    }

    @PreAuthorize("hasAuthority('pharmacy:inbound:edit')")
    @Operation(summary = "审核入库单（待审核 → 已审核）")
    @PostMapping("/audit")
    public Result<DrugInboundVO> audit(@Valid @RequestBody DrugInboundIdDTO idDTO) {
        drugInboundService.audit(idDTO);
        return Result.success(drugInboundService.getDetailById(idDTO.getInboundId()));
    }

    @PreAuthorize("hasAuthority('pharmacy:inbound:edit')")
    @Operation(summary = "入库（已审核 → 已入库，按明细建/加药品批次并写库存流水）")
    @PostMapping("/stockIn")
    public Result<DrugInboundVO> stockIn(@Valid @RequestBody DrugInboundIdDTO idDTO) {
        return Result.success(drugInboundService.stockIn(idDTO));
    }

    @PreAuthorize("hasAuthority('pharmacy:inbound:delete')")
    @Operation(summary = "取消入库单（待审核/已审核 → 已取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody DrugInboundCancelDTO cancelDTO) {
        drugInboundService.cancel(cancelDTO);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('pharmacy:inbound:delete')")
    @Operation(summary = "删除入库单（已入库不可删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long inboundId) {
        drugInboundService.deleteById(inboundId);
        return Result.success();
    }
}
