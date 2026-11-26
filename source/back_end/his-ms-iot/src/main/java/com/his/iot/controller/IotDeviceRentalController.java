package com.his.iot.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.iot.dto.*;
import com.his.iot.service.IotDeviceRentalService;
import com.his.iot.vo.DeviceRentalCatalogVO;
import com.his.iot.vo.DeviceRentalOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备共享调配控制器：共享池目录、申请单、审批、发放、归还、验收。
 */
@Tag(name = "设备共享调配")
@RestController
@RequestMapping("/iot/deviceRental")
@RequiredArgsConstructor
public class IotDeviceRentalController {

    private final IotDeviceRentalService rentalService;

    @Operation(summary = "共享池目录（含可借数量）")
    @PreAuthorize("hasAuthority('asset:deviceRental:list')")
    @GetMapping("/catalog")
    public Result<List<DeviceRentalCatalogVO>> catalog(@RequestParam(required = false) String keyword) {
        return Result.success(rentalService.listCatalog(keyword));
    }

    @Operation(summary = "申请单分页")
    @PreAuthorize("hasAuthority('asset:deviceRental:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DeviceRentalOrderVO>> listPage(@Valid @RequestBody DeviceRentalPageQueryDTO dto) {
        var page = rentalService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "申请单详情")
    @PreAuthorize("hasAuthority('asset:deviceRental:list')")
    @GetMapping("/getDetailById")
    public Result<DeviceRentalOrderVO> getDetailById(@RequestParam Long id) {
        return Result.success(rentalService.getDetail(id));
    }

    @Operation(summary = "发起设备借用申请")
    @PreAuthorize("hasAuthority('asset:deviceRental:apply')")
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody DeviceRentalCreateDTO dto) {
        // 主键出参必须字符串化，Long 直出会被 JS 丢精度（验收时 2125795669846364200 ≠ 实际 id）
        return Result.success(String.valueOf(rentalService.create(dto)));
    }

    @Operation(summary = "审批申请")
    @PreAuthorize("hasAuthority('asset:deviceRental:approve')")
    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody DeviceRentalApproveDTO dto) {
        rentalService.approve(dto);
        return Result.success();
    }

    @Operation(summary = "设备科发放")
    @PreAuthorize("hasAuthority('asset:deviceRental:issue')")
    @PostMapping("/issue")
    public Result<Void> issue(@Valid @RequestBody DeviceRentalActionDTO dto) {
        rentalService.issue(dto);
        return Result.success();
    }

    @Operation(summary = "临床申请归还")
    @PreAuthorize("hasAuthority('asset:deviceRental:return')")
    @PostMapping("/return")
    public Result<Void> markReturn(@Valid @RequestBody DeviceRentalReturnDTO dto) {
        rentalService.markReturn(dto);
        return Result.success();
    }

    @Operation(summary = "设备科验收")
    @PreAuthorize("hasAuthority('asset:deviceRental:accept')")
    @PostMapping("/accept")
    public Result<Void> accept(@Valid @RequestBody DeviceRentalActionDTO dto) {
        rentalService.accept(dto);
        return Result.success();
    }
}
