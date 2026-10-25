package com.his.outpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.outpatient.dto.NurseScheduleTemplateDTO;
import com.his.outpatient.service.NurseScheduleTemplateService;
import com.his.outpatient.vo.NurseScheduleTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 护理排班模板：班次实用排班模式（APN / 8-8 / 小组排班）的脚手架规则、长期周模板、以及一键套用到周。
 * 与病区护理周排班（{@code /nursing/schedule}）是同一套数据范围与权限域（nursing:schedule:*）。
 */
@Tag(name = "护理排班模板")
@RestController
@RequestMapping("/nursing/scheduleTemplate")
@RequiredArgsConstructor
public class NurseScheduleTemplateController {

    private final NurseScheduleTemplateService templateService;

    /**
     * 首页四张统计卡（我的模板 / 已用模板 / 已删除模板 / 权限组模板）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "统计卡（我的模板/已用模板/已删除模板/权限组模板）")
    @GetMapping("/stats")
    public Result<NurseScheduleTemplateVO.Stats> stats() {
        return Result.success(templateService.stats());
    }

    /**
     * 模板分页（delFlag=0）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "模板分页（不含已删除）")
    @PostMapping("/pageTemplates")
    public Result<PageResult<NurseScheduleTemplateVO.Template>> pageTemplates(@Valid @RequestBody NurseScheduleTemplateDTO.PageQuery dto) {
        return Result.success(templateService.pageTemplates(dto));
    }

    /**
     * 模板详情（含明细行）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "模板详情（含明细行）")
    @GetMapping("/detail")
    public Result<NurseScheduleTemplateVO.Template> detail(@RequestParam Long id) {
        return Result.success(templateService.detail(id));
    }

    /**
     * 新增/修改模板（合一，主表 + 明细同事务替换）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "新增/修改模板（主表+明细同事务替换）")
    @PostMapping("/upsert")
    public Result<Void> upsert(@Valid @RequestBody NurseScheduleTemplateDTO.Upsert dto) {
        templateService.upsert(dto);
        return Result.success(dto.getId() == null ? "模板已创建" : "模板已更新", null);
    }

    /**
     * 逻辑删除模板（置 delFlag=1，联动明细），并校验数据范围。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:delete')")
    @Operation(summary = "删除模板（逻辑删，联动明细）")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success("模板已删除", templateService.delete(id));
    }

    /**
     * 启停模板（status 0-停用 1-启用）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "启停模板（0-停用 1-启用）")
    @PostMapping("/updateStatus")
    public Result<Boolean> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        return Result.success("模板状态已更新", templateService.updateStatus(id, status));
    }

    /**
     * 班次实用排班模式规则列表（含停用行）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "排班模式规则列表（含停用行）")
    @GetMapping("/listRules")
    public Result<List<NurseScheduleTemplateVO.Rule>> listRules() {
        return Result.success(templateService.listRules());
    }

    /**
     * 新增/修改排班模式规则。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "新增/修改排班模式规则")
    @PostMapping("/upsertRule")
    public Result<Void> upsertRule(@Valid @RequestBody NurseScheduleTemplateDTO.RuleUpsert dto) {
        templateService.upsertRule(dto);
        return Result.success(dto.getId() == null ? "规则已创建" : "规则已更新", null);
    }

    /**
     * 逻辑删除排班模式规则。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "删除排班模式规则（逻辑删）")
    @DeleteMapping("/deleteRule")
    public Result<Boolean> deleteRule(@RequestParam Long id) {
        return Result.success("规则已删除", templateService.deleteRule(id));
    }

    /**
     * 套用模板到目标周，生成真实护士周排班（来源=TEMPLATE），并写适用记录。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "套用模板到周（生成护士周排班，来源=TEMPLATE）")
    @PostMapping("/applyToWeek")
    public Result<NurseScheduleTemplateVO.Apply> applyToWeek(@Valid @RequestBody NurseScheduleTemplateDTO.Apply dto) {
        return Result.success("套用完成", templateService.applyToWeek(dto));
    }

    /**
     * 适用记录分页（按套用时间倒序），对应「适用记录」页签。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "适用记录分页（按套用时间倒序）")
    @PostMapping("/pageApplies")
    public Result<PageResult<NurseScheduleTemplateVO.Apply>> pageApplies(@Valid @RequestBody NurseScheduleTemplateDTO.ApplyPageQuery dto) {
        return Result.success(templateService.pageApplies(dto));
    }
}
