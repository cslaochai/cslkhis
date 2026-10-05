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

    // 雪花ID 必须序列化成字符串：前端 res.data 会存下来当作下次保存的 recordId 回传，
    // 走 JSON number 会被 JS double 舍入（2101516032853229570 → ...229600），
    // 下次 getById(舍入后的 id) 必然查不到 → 抛「病历不存在」，病历越存越断。
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
