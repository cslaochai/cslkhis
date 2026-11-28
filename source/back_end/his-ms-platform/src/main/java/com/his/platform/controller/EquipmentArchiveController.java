package com.his.platform.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.platform.dto.EquipmentPageQueryDTO;
import com.his.platform.dto.EquipmentRentableDTO;
import com.his.platform.dto.EquipmentStatusDTO;
import com.his.platform.dto.EquipmentUpsertDTO;
import com.his.platform.service.EquipmentService;
import com.his.platform.vo.EquipmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 设备档案控制器：设备台账的建档/修改/状态变更，以及台账与维保计量的取数入口。
 * <p>
 * 权限一律标方法（类级注解会静默罩住没写自己注解的方法）。
 */
@Tag(name = "设备档案")
@RestController
@RequestMapping("/equipment/equipment")
@RequiredArgsConstructor
public class EquipmentArchiveController {

    private final EquipmentService equipmentService;

    /**
     * 设备台账分页：按名称、编号、科室、维保与计量到期情况筛选。
     * 用于「设备管理」页查询卡与台账表格。
     */
    @Operation(summary = "设备台账分页")
    @PreAuthorize("hasAuthority('asset:equipment:list')")
    @PostMapping("/listPage")
    public Result<PageResult<EquipmentVO>> listPage(@Valid @RequestBody EquipmentPageQueryDTO dto) {
        var page = equipmentService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 设备档案详情：带最近维保记录与最近计量记录。
     * 用于「设备管理」详情面板展示，以及维保/计量记录删除后回读重算结果。
     */
    @Operation(summary = "设备详情（含最近维保/计量记录）")
    @PreAuthorize("hasAuthority('asset:equipment:list')")
    @GetMapping("/getDetailById")
    public Result<EquipmentVO> getDetailById(@RequestParam Long equipmentId) {
        return Result.success(equipmentService.getDetailById(equipmentId));
    }

    /**
     * 设备建档/修改：同一接口，id 为空走建档。
     * 用于「设备管理」页「新增设备 / 编辑」弹框保存。
     */
    @Operation(summary = "设备建档/修改")
    @PreAuthorize("hasAuthority('asset:equipment:add')")
    @PostMapping("/equipmentUpsert")
    public Result<Void> equipmentUpsert(@Valid @RequestBody EquipmentUpsertDTO dto) {
        equipmentService.equipmentUpsert(dto);
        return Result.success();
    }

    /**
     * 设备状态变更（停用/启用/维修中/报废）。
     * 用于「设备管理」页操作列的启停动作。
     */
    @Operation(summary = "设备状态变更")
    @PreAuthorize("hasAuthority('asset:equipment:add')")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody EquipmentStatusDTO dto) {
        equipmentService.updateStatus(dto);
        return Result.success();
    }

    @Operation(summary = "维护设备是否进入共享池")
    @PreAuthorize("hasAuthority('asset:deviceRental:manage')")
    @PostMapping("/updateRentable")
    public Result<Void> updateRentable(@Valid @RequestBody EquipmentRentableDTO dto) {
        equipmentService.updateRentable(dto);
        return Result.success();
    }
}
