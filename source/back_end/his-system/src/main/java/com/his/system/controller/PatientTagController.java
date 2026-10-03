package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.SysPatientTagQueryDTO;
import com.his.system.dto.SysPatientTagUpsertDTO;
import com.his.system.service.PatientTagService;
import com.his.system.vo.SysPatientTagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

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

    @Operation(summary = "查询患者标签列表")
    @PostMapping("/listPage")
    public Result<PageResult<SysPatientTagVO>> listPage(@RequestBody SysPatientTagQueryDTO queryDTO) {
        return Result.success(patientTagService.queryTagPage(queryDTO));
    }

    @Operation(summary = "查询患者标签列表")
    @PostMapping("/list")
    public Result<List<SysPatientTagVO>> list(@RequestBody SysPatientTagQueryDTO queryDTO) {
        return Result.success(patientTagService.queryTagList(queryDTO));
    }

    @Operation(summary = "获取患者标签详情")
    @GetMapping("/getById")
    public Result<SysPatientTagVO> getInfo(@RequestParam Long tagId) {
        return Result.success(patientTagService.getTagInfo(tagId));
    }

    @PreAuthorize("hasAuthority('patient:tag:add')")
    @Operation(summary = "新增或修改患者标签")
    @PostMapping("/patientTagUpsert")
    public Result<Void> patientTagUpsert(@RequestBody SysPatientTagUpsertDTO upsertDTO) {
        return Result.success(patientTagService.upsertTag(upsertDTO), null);
    }

    @PreAuthorize("hasAuthority('patient:tag:delete')")
    @Operation(summary = "删除患者标签")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long tagId) {
        patientTagService.removeTag(tagId);
        return Result.success("删除成功", null);
    }
}
