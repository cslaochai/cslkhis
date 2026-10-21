package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.SysPatientTagQueryDTO;
import com.his.patient.dto.SysPatientTagUpsertDTO;
import com.his.patient.service.PatientTagService;
import com.his.patient.vo.SysPatientTagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者标签管理控制器
 */
@Tag(name = "患者标签管理")
@RestController
@RequestMapping("/system/patientTag")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'patient:list', 'patient:tag:list')")
public class PatientTagController {

    private final PatientTagService patientTagService;

    /**
     * 标签定义分页查询：按标签名模糊收口。
     * 用于患者标签管理页列表，以及患者档案页查询卡的标签筛选下拉取候选。
     */
    @Operation(summary = "查询患者标签列表")
    @PostMapping("/listPage")
    public Result<PageResult<SysPatientTagVO>> listPage(@Valid @RequestBody SysPatientTagQueryDTO queryDTO) {
        return Result.success(patientTagService.queryTagPage(queryDTO));
    }

    /**
     * 标签定义全量列表（不分页，一次拿完）。
     * 用于医生工作站「患者标签」维护弹框展示全部可选标签。
     */
    @Operation(summary = "查询患者标签列表")
    @PostMapping("/list")
    public Result<List<SysPatientTagVO>> list(@Valid @RequestBody SysPatientTagQueryDTO queryDTO) {
        return Result.success(patientTagService.queryTagList(queryDTO));
    }

    /**
     * 单个标签定义详情。
     * 供按标签身份回显名称、颜色、排序等属性的取数场景。
     */
    @Operation(summary = "获取患者标签详情")
    @GetMapping("/getById")
    public Result<SysPatientTagVO> getInfo(@RequestParam Long tagId) {
        return Result.success(patientTagService.getTagInfo(tagId));
    }

    /**
     * 新增或修改标签定义：入参带主键走修改，不带走新增，返回结果文案。
     * 用于患者标签管理页新增/修改弹框保存。
     */
    @PreAuthorize("hasAuthority('patient:tag:add')")
    @Operation(summary = "新增或修改患者标签")
    @PostMapping("/patientTagUpsert")
    public Result<Void> patientTagUpsert(@Valid @RequestBody SysPatientTagUpsertDTO upsertDTO) {
        return Result.success(patientTagService.upsertTag(upsertDTO), null);
    }

    /**
     * 删除标签定义。
     * 用于患者标签管理页列表行的删除操作。
     */
    @PreAuthorize("hasAuthority('patient:tag:delete')")
    @Operation(summary = "删除患者标签")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long tagId) {
        patientTagService.removeTag(tagId);
        return Result.success("删除成功", null);
    }
}
