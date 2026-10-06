package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.MaintainCreateDTO;
import com.his.system.dto.MaintainQueryPageDTO;
import com.his.system.service.EquipmentService;
import com.his.system.vo.MaintainVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 设备维保控制器。登记成功回写档案最近维保日期；录错可删（剩余记录重算）。
 */
@Tag(name = "设备维保")
@RestController
@RequestMapping("/equipment/maintain")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:equipment:list')")
public class EquipmentMaintainController {

    private final EquipmentService equipmentService;

    @Operation(summary = "维保记录分页")
    @PostMapping("/listPage")
    public Result<PageResult<MaintainVO>> listPage(@Valid @RequestBody MaintainQueryPageDTO dto) {
        var page = equipmentService.maintainListPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @PreAuthorize("hasAuthority('asset:equipment:add')")
    @Operation(summary = "维保登记")
    @PostMapping("/create")
    public Result<MaintainVO> create(@Valid @RequestBody MaintainCreateDTO dto) {
        return Result.success("维保登记成功", equipmentService.maintainCreate(dto));
    }

    @PreAuthorize("hasAuthority('asset:equipment:delete')")
    @Operation(summary = "维保记录删除（录错可删，删除后重算档案最近维保日期）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        equipmentService.maintainDelete(id);
        return Result.success("删除成功", null);
    }
}
