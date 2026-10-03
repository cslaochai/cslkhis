package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.ShiftQueryPageDTO;
import com.his.system.dto.ShiftUpsertDTO;
import com.his.system.service.ShiftService;
import com.his.system.vo.ShiftSelectListVO;
import com.his.system.vo.ShiftVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班次字典控制器（排班/周模板「标准班次」下拉的数据源）
 */
@Tag(name = "班次字典")
@RestController
@RequestMapping("/shift")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "班次列表（下拉取数，只要求登录；deptId 传了=该科室适用+全院通用；useScope 传了=只列该册班次）")
    @GetMapping("/selectList")
    public Result<List<ShiftSelectListVO>> selectList(@RequestParam(required = false) Long deptId,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) Integer useScope) {
        return Result.success(shiftService.selectListVO(deptId, status, useScope));
    }

    @PreAuthorize("hasAuthority('org:schedule:list')")
    @Operation(summary = "班次分页查询（关键词=名称模糊；排序 start_time + id 二级键）")
    @PostMapping("/listPage")
    public Result<PageResult<ShiftVO>> listPage(@RequestBody ShiftQueryPageDTO dto) {
        return Result.success(shiftService.pageVO(dto));
    }

    @PreAuthorize("hasAuthority('org:schedule:add')")
    @Operation(summary = "新增/修改班次（时长按起止时间重算，同名同科室防重）")
    @PostMapping("/shiftUpsert")
    public Result<Void> shiftUpsert(@Valid @RequestBody ShiftUpsertDTO dto) {
        shiftService.upsertShift(dto);
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", null);
    }

    @PreAuthorize("hasAuthority('org:schedule:delete')")
    @Operation(summary = "删除班次（逻辑删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        return shiftService.deleteShift(id) ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @Operation(summary = "班次改名（维护界面：只改名称，时间/科室/类型不可改）")
    @PostMapping("/rename")
    public Result<Void> rename(@RequestParam Long id, @RequestParam String shiftName) {
        String error = shiftService.renameShift(id, shiftName);
        return error == null ? Result.success("改名成功", null) : Result.error(error);
    }

    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @Operation(summary = "班次启用/停用（维护界面：只改状态，名称/时间/科室/类型不可改）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        String error = shiftService.updateStatus(id, status);
        return error == null ? Result.success("状态更新成功", null) : Result.error(error);
    }
}
