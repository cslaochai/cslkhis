package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.MeteringCreateDTO;
import com.his.system.dto.MeteringPageQueryDTO;
import com.his.system.service.EquipmentService;
import com.his.system.vo.MeteringVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 计量记录分页：按设备与计量日期区间筛选强检/校准历史。
     * 供计量到期核查与设备强检台账批量核对时取数。
     */
    @Operation(summary = "计量记录分页")
    @PostMapping("/listPage")
    public Result<PageResult<MeteringVO>> listPage(@Valid @RequestBody MeteringPageQueryDTO dto) {
        var page = equipmentService.meteringListPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 登记一次强检/校准：写入有效期截止日，过期后台账该设备标记为到期未检。
     * 用于「设备管理」页计量登记弹框保存。
     */
    @PreAuthorize("hasAuthority('asset:equipment:add')")
    @Operation(summary = "计量登记")
    @PostMapping("/create")
    public Result<MeteringVO> create(@Valid @RequestBody MeteringCreateDTO dto) {
        return Result.success("计量登记成功", equipmentService.meteringCreate(dto));
    }

    /**
     * 删除录错的计量记录。
     * 用于「设备管理」设备详情里计量记录行的删除。
     */
    @PreAuthorize("hasAuthority('asset:equipment:delete')")
    @Operation(summary = "计量记录删除（录错可删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        equipmentService.meteringDelete(id);
        return Result.success("删除成功", null);
    }
}
