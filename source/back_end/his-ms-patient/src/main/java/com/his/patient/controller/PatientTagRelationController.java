package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientTagBatchUpsertDTO;
import com.his.patient.dto.PatientTagDelDTO;
import com.his.patient.dto.PatientTagQueryDTO;
import com.his.patient.dto.PatientTagUpsertDTO;
import com.his.patient.service.BizPatientTagRelationService;
import com.his.patient.vo.SysPatientTagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者标签关联控制器：把标签定义挂到患者身上、以及按患者回读已挂标签的接口入口。
 */
@Tag(name = "患者标签关联")
@RestController
@RequestMapping("/patient/tag")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'patient:list')")
public class PatientTagRelationController {

    private final BizPatientTagRelationService bizPatientTagRelationService;

    /**
     * 回读一名患者当前挂着的标签。
     * 用于医生工作站「患者标签管理」弹窗的当前标签区，以及患者档案列表逐行补标签列。
     */
    @Operation(summary = "查询患者的标签列表")
    @GetMapping("/getByPatientId")
    public Result<List<SysPatientTagVO>> getByPatientId(@Valid PatientTagQueryDTO queryDTO) {
        return Result.success(bizPatientTagRelationService.listTagsByPatientId(queryDTO.getPatientId()));
    }

    /**
     * 给一名患者挂一个标签，已挂过同一标签时按业务错误拒绝。
     * 用于医生工作站「患者标签管理」弹窗的添加标签区。
     */
    @PreAuthorize("hasAuthority('patient:tag:add')")
    @Operation(summary = "给患者添加标签")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody PatientTagUpsertDTO tagDTO) {
        bizPatientTagRelationService.addTag(tagDTO);
        return Result.success();
    }

    /**
     * 摘掉患者身上的一个标签，只断关联、不动标签定义。
     * 用于医生工作站「患者标签管理」弹窗移除单个标签。
     */
    @PreAuthorize("hasAuthority('patient:tag:delete')")
    @Operation(summary = "移除患者的标签")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody PatientTagDelDTO delDTO) {
        bizPatientTagRelationService.deleteTag(delDTO);
        return Result.success();
    }

    /**
     * 一次给一名患者挂上多个标签，整批在一个请求内落库，已挂过的标签直接跳过。
     * 供批量打标环节使用：一组标签整体提交，不逐条往返。
     */
    @PreAuthorize("hasAuthority('patient:tag:add')")
    @Operation(summary = "批量给患者添加标签")
    @PostMapping("/batchAdd")
    public Result<Void> batchAdd(@Valid @RequestBody PatientTagBatchUpsertDTO batchDTO) {
        bizPatientTagRelationService.batchAdd(batchDTO);
        return Result.success();
    }
}
