package com.his.emr.controller;

import com.his.common.base.Result;
import com.his.emr.dto.MedicalRecordSaveDTO;
import com.his.emr.service.EmrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生工作站 - 病历保存控制器（临时保存 / 结诊）
 */
@Tag(name = "医生工作站-病历保存")
@RestController
@RequestMapping("/medicalRecord")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class MedicalRecordController {

    private final EmrService emrService;

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存病历（临时保存）")
    @PostMapping("/recordSave")
    public Result<String> recordSave(@RequestBody @Valid MedicalRecordSaveDTO recordSaveDTO) {
        Long recordId = emrService.saveMedicalRecord(recordSaveDTO, false);
        return Result.success("保存成功", String.valueOf(recordId));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "结诊提交病历")
    @PostMapping("/recordSubmitDirect")
    public Result<String> recordSubmitDirect(@RequestBody @Valid MedicalRecordSaveDTO recordSaveDTO) {
        Long recordId = emrService.saveMedicalRecord(recordSaveDTO, true);
        return Result.success("结诊成功", String.valueOf(recordId));
    }
}
