package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.ClinicRoomQueryDTO;
import com.his.system.dto.ClinicRoomUpsertDTO;
import com.his.system.service.SysClinicRoomService;
import com.his.system.vo.ClinicRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 诊室管理控制器
 */
@Tag(name = "诊室管理")
@RestController
@RequestMapping("/system/clinicRoom")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ClinicRoomController {

    private final SysClinicRoomService clinicRoomService;

    @Operation(summary = "分页查询诊室列表")
    @PostMapping("/listPage")
    public Result<PageResult<ClinicRoomVO>> listPage(@RequestBody ClinicRoomQueryDTO queryDTO) {
        return Result.success(clinicRoomService.listPage(queryDTO));
    }

    @Operation(summary = "查询诊室列表（不分页）")
    @PostMapping("/list")
    public Result<List<ClinicRoomVO>> list(@RequestBody ClinicRoomQueryDTO queryDTO) {
        return Result.success(clinicRoomService.listAll(queryDTO));
    }

    @Operation(summary = "获取诊室详情")
    @GetMapping("/getById")
    public Result<ClinicRoomVO> getInfo(@RequestParam Long roomId) {
        return Result.success(clinicRoomService.getInfo(roomId));
    }

    @Operation(summary = "新增或修改诊室")
    @PreAuthorize("hasAuthority('org:clinicRoom:add')")
    @PostMapping("/clinicRoomUpsert")
    public Result<Void> clinicRoomUpsert(@RequestBody ClinicRoomUpsertDTO upsertDTO) {
        return Result.success(clinicRoomService.upsert(upsertDTO), null);
    }

    @Operation(summary = "删除诊室")
    @PreAuthorize("hasAuthority('org:clinicRoom:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long roomId) {
        clinicRoomService.delete(roomId);
        return Result.success("删除成功", null);
    }
}
