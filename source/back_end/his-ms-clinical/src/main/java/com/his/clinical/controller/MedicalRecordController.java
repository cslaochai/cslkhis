package com.his.clinical.controller;

import com.his.common.base.Result;
import com.his.clinical.dto.MedicalRecordSaveDTO;
import com.his.clinical.service.EmrService;
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

    /**
     * 暂存门诊病历：病历连同处方覆盖式写入，状态仍是草稿，可反复保存。
     * 用于医生工作站的「临时保存」，返回病历 ID 供前端挂后续开单。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存病历（临时保存）")
    @PostMapping("/recordSave")
    public Result<String> recordSave(@RequestBody @Valid MedicalRecordSaveDTO recordSaveDTO) {
        Long recordId = emrService.saveMedicalRecord(recordSaveDTO, false);
        return Result.success("保存成功", String.valueOf(recordId));
    }

    /**
     * 结诊提交病历：病历转已提交并进入待审方，同时收口本次挂号与叫号队列、生成收费单与患者引导单。
     * 用于医生工作站的「结诊」，是一次的原子动作，失败整体回滚。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "结诊提交病历")
    @PostMapping("/recordSubmitDirect")
    public Result<String> recordSubmitDirect(@RequestBody @Valid MedicalRecordSaveDTO recordSaveDTO) {
        Long recordId = emrService.saveMedicalRecord(recordSaveDTO, true);
        return Result.success("结诊成功", String.valueOf(recordId));
    }
}
