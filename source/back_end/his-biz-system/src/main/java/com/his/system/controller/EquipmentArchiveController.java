package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.EquipmentQueryPageDTO;
import com.his.system.service.EquipmentService;
import com.his.system.vo.EquipmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 设备档案控制器（台账只读；档案增删走资产口径，本域聚焦维保计量）。
 */
@Tag(name = "设备档案")
@RestController
@RequestMapping("/equipment/equipment")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:equipment:list')")
public class EquipmentArchiveController {

    private final EquipmentService equipmentService;

    @Operation(summary = "设备台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<EquipmentVO>> listPage(@Valid @RequestBody EquipmentQueryPageDTO dto) {
        var page = equipmentService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "设备详情（含最近维保/计量记录）")
    @GetMapping("/getDetailById")
    public Result<EquipmentVO> getDetailById(@RequestParam Long equipmentId) {
        return Result.success(equipmentService.getDetailById(equipmentId));
    }
}
