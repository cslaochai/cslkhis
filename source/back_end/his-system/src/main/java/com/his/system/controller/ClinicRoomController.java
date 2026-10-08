package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.ClinicRoomQueryDTO;
import com.his.system.dto.ClinicRoomUpsertDTO;
import com.his.system.service.SysClinicRoomService;
import com.his.system.vo.ClinicRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 诊室管理控制器
 */
@Tag(name = "诊室管理")
@RestController
@RequestMapping("/system/clinicRoom")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ClinicRoomController {

    private final SysClinicRoomService sysClinicRoomService;

    @Operation(summary = "分页查询诊室列表")
    @PostMapping("/listPage")
    public Result<PageResult<ClinicRoomVO>> listPage(@Valid @RequestBody ClinicRoomQueryDTO queryDTO) {
        return Result.success(sysClinicRoomService.listPage(queryDTO));
    }

    @Operation(summary = "查询诊室列表（不分页）")
    @PostMapping("/list")
    public Result<List<ClinicRoomVO>> list(@Valid @RequestBody ClinicRoomQueryDTO queryDTO) {
        return Result.success(sysClinicRoomService.listAll(queryDTO));
    }

    @Operation(summary = "获取诊室详情")
    @GetMapping("/getById")
    public Result<ClinicRoomVO> getInfo(@RequestParam Long roomId) {
        return Result.success(sysClinicRoomService.getInfo(roomId));
    }

    @Operation(summary = "新增或修改诊室")
    @PreAuthorize("hasAuthority('org:clinicRoom:add')")
    @PostMapping("/clinicRoomUpsert")
    public Result<Void> clinicRoomUpsert(@Valid @RequestBody ClinicRoomUpsertDTO upsertDTO) {
        return Result.success(sysClinicRoomService.upsert(upsertDTO), null);
    }

    @Operation(summary = "删除诊室")
    @PreAuthorize("hasAuthority('org:clinicRoom:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long roomId) {
        sysClinicRoomService.delete(roomId);
        return Result.success("删除成功", null);
    }
}
