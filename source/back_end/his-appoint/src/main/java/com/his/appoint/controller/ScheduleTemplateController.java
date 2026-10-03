package com.his.appoint.controller;

import com.his.appoint.dto.ScheduleTemplateGenerateDTO;
import com.his.appoint.dto.ScheduleTemplateQueryPageDTO;
import com.his.appoint.dto.ScheduleTemplateUpsertDTO;
import com.his.appoint.service.ScheduleTemplateService;
import com.his.appoint.vo.ScheduleTemplatePreviewVO;
import com.his.appoint.vo.ScheduleTemplateVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "排班模板管理")
@RestController
@RequestMapping("/scheduleTemplate")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('org:schedule:list')")
public class ScheduleTemplateController {

    private final ScheduleTemplateService scheduleTemplateService;

    @Operation(summary = "模板列表（GET，可按科室/岗位类别/星期几/状态过滤）")
    @GetMapping("/list")
    public Result<List<ScheduleTemplateVO>> list(@RequestParam(required = false) Long deptId,
                                                 @RequestParam(required = false) Integer staffType,
                                                 @RequestParam(required = false) Integer weekDay,
                                                 @RequestParam(required = false) Integer status) {
        return Result.success(scheduleTemplateService.listVO(deptId, staffType, weekDay, status));
    }

    @Operation(summary = "模板分页查询（关键词=科室/医生/诊室/备注模糊；排序 星期几+班次+开始时间+id 二级键）")
    @PostMapping("/listPage")
    public Result<PageResult<ScheduleTemplateVO>> listPage(@RequestBody ScheduleTemplateQueryPageDTO dto) {
        return Result.success(scheduleTemplateService.pageVO(dto));
    }

    @PreAuthorize("hasAuthority('org:schedule:add')")
    @Operation(summary = "新增/修改模板（合一）")
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody ScheduleTemplateUpsertDTO dto) {
        scheduleTemplateService.upsertTemplate(dto);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('org:schedule:delete')")
    @Operation(summary = "删除模板")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        boolean success = scheduleTemplateService.deleteTemplate(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @Operation(summary = "模板启停")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean success = scheduleTemplateService.updateStatus(id, status);
        return success ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "按模板生成预览（dryRun 不落库）")
    @GetMapping("/preview")
    public Result<ScheduleTemplatePreviewVO> preview(@RequestParam(required = false) Integer weekOffset,
                                                     @RequestParam(required = false) Long deptId,
                                                     @RequestParam(required = false) Integer staffType) {
        return Result.success(scheduleTemplateService.previewForWeek(weekOffset, deptId, staffType));
    }

    @PreAuthorize("hasAuthority('org:schedule:add')")
    @Operation(summary = "按模板生成目标周排班（返回结论文案）")
    @PostMapping("/generate")
    public Result<Void> generate(@RequestBody ScheduleTemplateGenerateDTO dto) {
        String message = scheduleTemplateService.generateForWeek(dto.getWeekOffset(), dto.getDeptId(),
                dto.getStaffType());
        // 返回值即结论：文案放 message（铁律 12）
        return Result.success(message, null);
    }
}
