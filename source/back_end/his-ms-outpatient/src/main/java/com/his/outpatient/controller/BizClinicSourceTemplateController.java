package com.his.outpatient.controller;

import com.his.outpatient.dto.ScheduleTemplateGenerateDTO;
import com.his.outpatient.dto.ScheduleTemplatePageQueryDTO;
import com.his.outpatient.dto.ScheduleTemplateUpsertDTO;
import com.his.outpatient.service.BizClinicSourceTemplateService;
import com.his.outpatient.vo.ScheduleTemplatePreviewVO;
import com.his.outpatient.vo.ScheduleTemplateVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 排班模板管理控制器：门诊周期性排班模板的定义、启停、预览与生成的接口入口。 */
@Tag(name = "排班模板管理")
@RestController
@RequestMapping("/scheduleTemplate")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('outpatient:schedule:list')")
public class BizClinicSourceTemplateController {

    private final BizClinicSourceTemplateService bizScheduleTemplateService;

    /**
     * 按科室、岗位类别、星期几、状态过滤排班模板全量清单（不分页）。
     * 供按模板生成目标周排班的流程环节一次取全适用模板时使用。
     */
    @Operation(summary = "模板列表（GET，可按科室/岗位类别/星期几/状态过滤）")
    @GetMapping("/list")
    public Result<List<ScheduleTemplateVO>> list(@RequestParam(required = false) Long deptId,
                                                 @RequestParam(required = false) Integer staffType,
                                                 @RequestParam(required = false) Integer weekDay,
                                                 @RequestParam(required = false) Integer status) {
        return Result.success(bizScheduleTemplateService.listVO(deptId, staffType, weekDay, status));
    }

    /**
     * 排班模板分页查询，关键词模糊匹配科室/医生/诊室/备注。
     * 用于门诊排班页模板管理面板的表格取数。
     */
    @Operation(summary = "模板分页查询（关键词=科室/医生/诊室/备注模糊；排序 星期几+班次+开始时间+id 二级键）")
    @PostMapping("/listPage")
    public Result<PageResult<ScheduleTemplateVO>> listPage(@Valid @RequestBody ScheduleTemplatePageQueryDTO dto) {
        return Result.success(bizScheduleTemplateService.pageVO(dto));
    }

    /**
     * 新增或修改排班模板（合一入口，按 id 有无分支）。
     * 用于门诊排班页排班模板弹框保存。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "新增/修改模板（合一）")
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody ScheduleTemplateUpsertDTO dto) {
        bizScheduleTemplateService.upsertTemplate(dto);
        return Result.success();
    }

    /**
     * 删除排班模板。
     * 用于门诊排班页模板面板的模板行删除动作。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:delete')")
    @Operation(summary = "删除模板")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        boolean success = bizScheduleTemplateService.deleteTemplate(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 启用或停用排班模板，停用后不再生成排班。
     * 用于门诊排班页模板面板的模板行启停开关。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:edit')")
    @Operation(summary = "模板启停")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean success = bizScheduleTemplateService.updateStatus(id, status);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 按模板试算目标周将生成的排班结果，不落库。
     * 用于门诊排班页模板生成入口的「生成前先看一眼」预览。
     */
    @Operation(summary = "按模板生成预览（dryRun 不落库）")
    @GetMapping("/preview")
    public Result<ScheduleTemplatePreviewVO> preview(@RequestParam(required = false) Integer weekOffset,
                                                     @RequestParam(required = false) Long deptId,
                                                     @RequestParam(required = false) Integer staffType) {
        return Result.success(bizScheduleTemplateService.previewForWeek(weekOffset, deptId, staffType));
    }

    /**
     * 按排班模板生成目标周排班并落库，生成结论以提示文案返回。
     * 用于门诊排班页模板面板的一键生成下周排班动作。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "按模板生成目标周排班（返回结论文案）")
    @PostMapping("/generate")
    public Result<Void> generate(@Valid @RequestBody ScheduleTemplateGenerateDTO dto) {
        String message = bizScheduleTemplateService.generateForWeek(dto.getWeekOffset(), dto.getDeptId(),
                dto.getStaffType());
        // 返回值即结论：文案放 message（铁律 12）
        return Result.success(message, null);
    }
}
