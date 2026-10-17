package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.DrugTransferActionDTO;
import com.his.pharmacy.dto.DrugTransferQueryPageDTO;
import com.his.pharmacy.dto.DrugTransferUpsertDTO;
import com.his.pharmacy.service.DrugTransferService;
import com.his.pharmacy.vo.DrugTransferVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品调拨（药库 ↔ 药房，sql/154 ②级）
 */
@Tag(name = "药品调拨")
@RestController
@RequestMapping("/pharmacy/drugTransfer")
@RequiredArgsConstructor
public class DrugTransferController {

    private final DrugTransferService drugTransferService;

    @Operation(summary = "调拨单分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:list')")
    public Result<PageResult<DrugTransferVO>> listPage(@Valid @RequestBody DrugTransferQueryPageDTO query) {
        return Result.success(drugTransferService.listPage(query));
    }

    @Operation(summary = "调拨单详情（含明细与两行库存流水）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:list')")
    public Result<DrugTransferVO> getDetailById(@RequestParam Long id) {
        return Result.success(drugTransferService.getDetailById(id));
    }

    @Operation(summary = "建单（按批次抓快照）/ 改明细（仅待发出）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:add')")
    public Result<DrugTransferVO> upsert(@Valid @RequestBody DrugTransferUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "调拨单已生成" : "调拨单已保存", drugTransferService.upsert(dto));
    }

    @Operation(summary = "确认发出（扣发出库位并落 7-调拨出库 流水）")
    @PostMapping("/confirmOut")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:edit')")
    public Result<DrugTransferVO> confirmOut(@Valid @RequestBody DrugTransferActionDTO dto) {
        return Result.success("已发出，等待对方库位接收", drugTransferService.confirmOut(dto));
    }

    @Operation(summary = "确认接收（落到接收库位并落 8-调拨入库 流水）")
    @PostMapping("/confirmIn")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:edit')")
    public Result<DrugTransferVO> confirmIn(@Valid @RequestBody DrugTransferActionDTO dto) {
        return Result.success("已接收，本单调拨完成", drugTransferService.confirmIn(dto));
    }

    @Operation(summary = "作废（仅待发出）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:edit')")
    public Result<DrugTransferVO> cancel(@Valid @RequestBody DrugTransferActionDTO dto) {
        return Result.success("调拨单已作废", drugTransferService.cancel(dto));
    }

    @Operation(summary = "删除调拨单（仅待发出/已作废）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('pharmacy:drugTransfer:delete')")
    public Result<Void> deleteById(@RequestParam Long id) {
        drugTransferService.deleteById(id);
        return Result.success("调拨单已删除", null);
    }
}
