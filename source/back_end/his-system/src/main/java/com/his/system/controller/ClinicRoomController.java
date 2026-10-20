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

/** 诊室管理控制器：门诊诊室基础档案查询与增删改的接口入口。 */
@Tag(name = "诊室管理")
@RestController
@RequestMapping("/system/clinicRoom")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ClinicRoomController {

    private final SysClinicRoomService sysClinicRoomService;

    /**
     * 诊室分页查询。
     * 用于系统管理「诊室管理」页表格取数。
     */
    @Operation(summary = "分页查询诊室列表")
    @PostMapping("/listPage")
    public Result<PageResult<ClinicRoomVO>> listPage(@Valid @RequestBody ClinicRoomQueryDTO queryDTO) {
        return Result.success(sysClinicRoomService.listPage(queryDTO));
    }

    /**
     * 诊室全量清单（不分页）。
     * 用于门诊排班、排班模板等编辑弹框的诊室下拉候选。
     */
    @Operation(summary = "查询诊室列表（不分页）")
    @PostMapping("/list")
    public Result<List<ClinicRoomVO>> list(@Valid @RequestBody ClinicRoomQueryDTO queryDTO) {
        return Result.success(sysClinicRoomService.listAll(queryDTO));
    }

    /**
     * 取单条诊室详情。
     * 用于诊室编辑弹框回显——表单整对象提交，回显必须给全量字段。
     */
    @Operation(summary = "获取诊室详情")
    @GetMapping("/getById")
    public Result<ClinicRoomVO> getInfo(@RequestParam Long roomId) {
        return Result.success(sysClinicRoomService.getInfo(roomId));
    }

    /**
     * 新增或修改诊室（合一入口，按 id 有无分支）。
     * 用于系统管理「诊室管理」页诊室弹框保存。
     */
    @Operation(summary = "新增或修改诊室")
    @PreAuthorize("hasAuthority('org:clinicRoom:add')")
    @PostMapping("/clinicRoomUpsert")
    public Result<Void> clinicRoomUpsert(@Valid @RequestBody ClinicRoomUpsertDTO upsertDTO) {
        return Result.success(sysClinicRoomService.upsert(upsertDTO), null);
    }

    /**
     * 删除诊室。
     * 用于系统管理「诊室管理」页的行删除动作。
     */
    @Operation(summary = "删除诊室")
    @PreAuthorize("hasAuthority('org:clinicRoom:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long roomId) {
        sysClinicRoomService.delete(roomId);
        return Result.success("删除成功", null);
    }
}
