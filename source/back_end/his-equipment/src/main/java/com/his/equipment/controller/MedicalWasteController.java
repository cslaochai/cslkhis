package com.his.equipment.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.equipment.dto.WasteDTO;
import com.his.equipment.service.WasteService;
import com.his.equipment.vo.WasteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医疗废物登记控制器。登记 → 交接 → 处置三态闭环；已交接后禁删。
 */
@Tag(name = "医疗废物登记")
@RestController
@RequestMapping("/waste/waste")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:waste:list')")
public class MedicalWasteController {

    private final WasteService wasteService;

    @PreAuthorize("hasAuthority('asset:waste:add')")
    @Operation(summary = "医废登记")
    @PostMapping("/create")
    public Result<WasteVO> create(@Valid @RequestBody WasteDTO.Create dto) {
        return Result.success("医废登记成功", wasteService.create(dto));
    }

    @PreAuthorize("hasAuthority('asset:waste:edit')")
    @Operation(summary = "医废交接（1→2）")
    @PostMapping("/handover")
    public Result<WasteVO> handover(@Valid @RequestBody WasteDTO.Handover dto) {
        return Result.success("交接成功", wasteService.handover(dto));
    }

    @PreAuthorize("hasAuthority('asset:waste:edit')")
    @Operation(summary = "处置确认（2→3）")
    @PostMapping("/dispose")
    public Result<WasteVO> dispose(@Valid @RequestBody WasteDTO.Dispose dto) {
        return Result.success("处置确认成功", wasteService.dispose(dto));
    }

    @PreAuthorize("hasAuthority('asset:waste:delete')")
    @Operation(summary = "医废登记删除（仅已登记状态可删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        wasteService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "医废登记分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<WasteVO>> listPage(@Valid @RequestBody WasteDTO.QueryPage dto) {
        var page = wasteService.listPage(dto == null ? new WasteDTO.QueryPage() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}
