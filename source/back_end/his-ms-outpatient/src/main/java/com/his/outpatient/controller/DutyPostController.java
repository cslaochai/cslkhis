package com.his.outpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.outpatient.dto.DutyPostPageQueryDTO;
import com.his.outpatient.dto.DutyPostUpsertDTO;
import com.his.outpatient.service.DutyPostService;
import com.his.outpatient.vo.DutyPostSelectListVO;
import com.his.outpatient.vo.DutyPostVO;
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

    /**
     * 点位下拉候选：按值守范围与归属组织过滤出可用的值守点位。
     * 用于总值班排班登记弹框选「排的是哪个位」。
     */
    @Operation(summary = "点位下拉（只要求登录；值班排班页取数）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<DutyPostSelectListVO>> selectList(@RequestParam(required = false) Integer dutyScope,
                                                         @RequestParam(required = false) Integer orgType,
                                                         @RequestParam(required = false) Long orgId) {
        return Result.success(dutyPostService.selectListVO(dutyScope, orgType, orgId));
    }

    /**
     * 值守点位分页：按名称、值守范围、归属组织筛选已定义的点位。
     * 用于「值守点位」页查询卡与台账表格。
     */
    @Operation(summary = "分页查询点位")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DutyPostVO>> listPage(@Valid @RequestBody DutyPostPageQueryDTO queryDTO) {
        return Result.success(dutyPostService.pageVO(queryDTO));
    }

    /**
     * 登记或修改值守点位：点位必须挂在启用班次上，编码全局唯一。
     * 用于「值守点位」页新增/编辑弹框保存。
     */
    @Operation(summary = "新增/修改点位（必须挂在启用班次上，编码全局唯一）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/dutyPostUpsert")
    public Result<Long> dutyPostUpsert(@Valid @RequestBody DutyPostUpsertDTO upsertDTO) {
        Long id = dutyPostService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", id);
    }

    /**
     * 删除值守点位：已被排班占用的点位由服务层拦下，不允许删位留空班。
     * 用于「值守点位」页操作列删除。
     */
    @Operation(summary = "删除点位（物理删）")
    @PreAuthorize("hasAuthority('org:duty:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyPostService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
