package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DutyPostQueryPageDTO;
import com.his.system.dto.DutyPostUpsertDTO;
import com.his.system.service.DutyPostService;
import com.his.system.vo.DutyPostSelectListVO;
import com.his.system.vo.DutyPostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 值守点位（把「位」从「人」里剥出来：位先定义存在，排班只是把人写进位里）。
 */
@Tag(name = "值守点位")
@RestController
@RequestMapping("/system/dutyPost")
@RequiredArgsConstructor
public class DutyPostController {

    private final DutyPostService dutyPostService;

    @Operation(summary = "点位下拉（只要求登录；值班排班页取数）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<DutyPostSelectListVO>> selectList(@RequestParam(required = false) Integer dutyScope,
                                                         @RequestParam(required = false) Integer orgType,
                                                         @RequestParam(required = false) Long orgId) {
        return Result.success(dutyPostService.selectListVO(dutyScope, orgType, orgId));
    }

    @Operation(summary = "分页查询点位")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DutyPostVO>> listPage(@Valid @RequestBody DutyPostQueryPageDTO queryDTO) {
        return Result.success(dutyPostService.pageVO(queryDTO));
    }

    @Operation(summary = "新增/修改点位（必须挂在启用班次上，编码全局唯一）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/dutyPostUpsert")
    public Result<Long> dutyPostUpsert(@Valid @RequestBody DutyPostUpsertDTO upsertDTO) {
        Long id = dutyPostService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", id);
    }

    @Operation(summary = "删除点位（物理删）")
    @PreAuthorize("hasAuthority('org:duty:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyPostService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
