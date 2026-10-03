package com.his.equipment.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.equipment.dto.EquipmentDTO;
import com.his.equipment.service.EquipmentService;
import com.his.equipment.vo.MeteringVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 设备计量（强检/校准）控制器。valid_until 过期即台账亮红。
 */
@Tag(name = "设备计量")
@RestController
@RequestMapping("/equipment/metering")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:equipment:list')")
public class EquipmentMeteringController {

    private final EquipmentService equipmentService;

    @Operation(summary = "计量记录分页")
    @PostMapping("/listPage")
    public Result<PageResult<MeteringVO>> listPage(@Valid @RequestBody EquipmentDTO.MeteringQueryPage dto) {
        var page = equipmentService.meteringListPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @PreAuthorize("hasAuthority('asset:equipment:add')")
    @Operation(summary = "计量登记")
    @PostMapping("/create")
    public Result<MeteringVO> create(@Valid @RequestBody EquipmentDTO.MeteringCreate dto) {
        return Result.success("计量登记成功", equipmentService.meteringCreate(dto));
    }

    @PreAuthorize("hasAuthority('asset:equipment:delete')")
    @Operation(summary = "计量记录删除（录错可删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        equipmentService.meteringDelete(id);
        return Result.success("删除成功", null);
    }
}
