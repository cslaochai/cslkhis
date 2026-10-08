package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientTagBatchUpsertDTO;
import com.his.patient.dto.PatientTagDelDTO;
import com.his.patient.dto.PatientTagQueryDTO;
import com.his.patient.dto.PatientTagUpsertDTO;
import com.his.patient.service.BizPatientTagRelationService;
import com.his.system.vo.SysPatientTagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者标签关联控制器
 */
@Tag(name = "患者标签关联")
@RestController
@RequestMapping("/patient/tag")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'patient:list')")
public class PatientTagRelationController {

    private final BizPatientTagRelationService bizPatientTagRelationService;

    @Operation(summary = "查询患者的标签列表")
    @GetMapping("/getByPatientId")
    public Result<List<SysPatientTagVO>> getByPatientId(@Valid PatientTagQueryDTO queryDTO) {
        return Result.success(bizPatientTagRelationService.listTagsByPatientId(queryDTO.getPatientId()));
    }

    @PreAuthorize("hasAuthority('patient:tag:add')")
    @Operation(summary = "给患者添加标签")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody PatientTagUpsertDTO tagDTO) {
        bizPatientTagRelationService.addTag(tagDTO);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:tag:delete')")
    @Operation(summary = "移除患者的标签")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody PatientTagDelDTO delDTO) {
        bizPatientTagRelationService.deleteTag(delDTO);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('patient:tag:add')")
    @Operation(summary = "批量给患者添加标签")
    @PostMapping("/batchAdd")
    public Result<Void> batchAdd(@Valid @RequestBody PatientTagBatchUpsertDTO batchDTO) {
        bizPatientTagRelationService.batchAdd(batchDTO);
        return Result.success();
    }
}
