package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.StaffPlanRuleQueryPageDTO;
import com.his.system.dto.StaffPlanRuleUpsertDTO;
import com.his.system.service.StaffPlanRuleService;
import com.his.system.vo.StaffPlanRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 人力配置标准（一个单元 × 一个班次 × 一个岗位类别该配多少人）。
 */
@Tag(name = "人力配置标准")
@RestController
@RequestMapping("/system/staffPlanRule")
@RequiredArgsConstructor
public class StaffPlanRuleController {

    private final StaffPlanRuleService staffPlanRuleService;

    @Operation(summary = "分页查询人力标准")
    @PreAuthorize("hasAuthority('org:schedule:list')")
    @PostMapping("/listPage")
    public Result<PageResult<StaffPlanRuleVO>> listPage(@Valid @RequestBody StaffPlanRuleQueryPageDTO queryDTO) {
        return Result.success(staffPlanRuleService.pageVO(queryDTO));
    }

    @Operation(summary = "新增/修改人力标准（同单元同班次同岗位只允许一条）")
    @PreAuthorize("hasAuthority('org:schedule:add')")
    @PostMapping("/staffPlanRuleUpsert")
    public Result<Long> staffPlanRuleUpsert(@Valid @RequestBody StaffPlanRuleUpsertDTO upsertDTO) {
        Long id = staffPlanRuleService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", id);
    }

    @Operation(summary = "删除人力标准（物理删）")
    @PreAuthorize("hasAuthority('org:schedule:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        staffPlanRuleService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
