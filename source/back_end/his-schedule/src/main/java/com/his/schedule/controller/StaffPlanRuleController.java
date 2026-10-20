package com.his.schedule.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.schedule.dto.StaffPlanRulePageQueryDTO;
import com.his.schedule.dto.StaffPlanRuleUpsertDTO;
import com.his.schedule.service.StaffPlanRuleService;
import com.his.schedule.vo.StaffPlanRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 人力配置标准（一个单元 × 一个班次 × 一个岗位类别该配多少人）。
 *
 * <p>权限独立于排班（staff:planRule:* 而非 staff:schedule:*）：能排班的岗位（护士长）不应能改排班的分母。
 */
@Tag(name = "人力配置标准")
@RestController
@RequestMapping("/system/staffPlanRule")
@RequiredArgsConstructor
public class StaffPlanRuleController {

    private final StaffPlanRuleService staffPlanRuleService;

    /**
     * 人力配置标准分页：按单元类型、单元归属、岗位类别与状态收口。
     * 用于排班域「人力配置标准」页的查询卡与表格；在岗人数是否达标的核对就以这批标准作口径。
     */
    @Operation(summary = "分页查询人力标准")
    @PreAuthorize("hasAuthority('staff:planRule:list')")
    @PostMapping("/listPage")
    public Result<PageResult<StaffPlanRuleVO>> listPage(@Valid @RequestBody StaffPlanRulePageQueryDTO queryDTO) {
        return Result.success(staffPlanRuleService.pageVO(queryDTO));
    }

    /**
     * 新增或修改人力标准：入参带主键走修改、不带走新增，返回落库主键；同一单元同一班次同一岗位类别只留一条。
     * 用于「人力配置标准」页新增/修改弹框保存。
     */
    @Operation(summary = "新增/修改人力标准（同单元同班次同岗位只允许一条）")
    @PreAuthorize("hasAuthority('staff:planRule:add')")
    @PostMapping("/staffPlanRuleUpsert")
    public Result<Long> staffPlanRuleUpsert(@Valid @RequestBody StaffPlanRuleUpsertDTO upsertDTO) {
        Long id = staffPlanRuleService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", id);
    }

    /**
     * 删除一条人力标准：不留软删痕迹，配置行直接移除。
     * 用于「人力配置标准」页列表行的删除操作。
     */
    @Operation(summary = "删除人力标准（物理删）")
    @PreAuthorize("hasAuthority('staff:planRule:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        staffPlanRuleService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
