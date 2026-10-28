package com.his.clinical.controller;

import com.his.clinical.dto.MedicalRecordSaveDTO;
import com.his.clinical.service.EmrService;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医生工作站 - 病历保存控制器（临时保存 / 结诊）
 */
@Tag(name = "医生工作站-病历保存")
@RestController
@RequestMapping("/medicalRecord")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'opd:emergencyDoctor:list')")
public class MedicalRecordController {

    private final EmrService emrService;

    /**
     * 暂存门诊病历：病历连同处方覆盖式写入，状态仍是草稿，可反复保存。
     * 用于医生工作站的「临时保存」，返回病历 ID 供前端挂后续开单。
     * 急诊医生工作站复用本接口（接诊后保存病历），凭 opd:emergencyDoctor:edit 放行。
     */
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:add', 'opd:emergencyDoctor:edit')")
    @Operation(summary = "保存病历（临时保存）")
    @PostMapping("/recordSave")
    public Result<String> recordSave(@RequestBody @Valid MedicalRecordSaveDTO recordSaveDTO) {
        Long recordId = emrService.saveMedicalRecord(recordSaveDTO, false);
        return Result.success("保存成功", String.valueOf(recordId));
    }

    /**
     * 结诊提交病历：病历转已提交并进入待审方，同时收口本次挂号与叫号队列、生成收费单与患者引导单。
     * 用于医生工作站的「结诊」，是一次的原子动作，失败整体回滚。
     * 急诊医生工作站复用本接口（接诊后保存并结诊），凭 opd:emergencyDoctor:edit 放行。
     */
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:add', 'opd:emergencyDoctor:edit')")
    @Operation(summary = "结诊提交病历")
    @PostMapping("/recordSubmitDirect")
    public Result<String> recordSubmitDirect(@RequestBody @Valid MedicalRecordSaveDTO recordSaveDTO) {
        Long recordId = emrService.saveMedicalRecord(recordSaveDTO, true);
        return Result.success("结诊成功", String.valueOf(recordId));
    }

    /**
     * 医生撤回结诊：已提交 → 草稿，病历重新开放编辑。
     * 仅本人当日已提交(2)、未缴费、未执行的病历允许撤回；已归档(3) 拒绝。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "撤回结诊（已提交→草稿，本人当日未缴费未执行）")
    @PostMapping("/recordRevoke")
    public Result<Void> recordRevoke(@RequestParam Long recordId,
                                     @RequestParam(required = false) Long queueId,
                                     @RequestParam(required = false) String reason) {
        emrService.revokeRecord(recordId, queueId, reason);
        return Result.success("已撤回结诊，病历回到草稿", null);
    }
}
