package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.dto.PatientSurgeryHistoryUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientSurgeryHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者手术外伤史控制器
 */
@Tag(name = "患者手术外伤史")
@RestController
@RequestMapping("/patient/surgeryHistory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientSurgeryHistoryController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的手术外伤史列表")
    @PostMapping("/list")
    public Result<List<PatientSurgeryHistoryVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getSurgeryHistories());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改手术外伤史")
    @PostMapping("/surgeryHistoryUpsert")
    public Result<PatientSurgeryHistoryVO> surgeryHistoryUpsert(@RequestBody @Valid PatientSurgeryHistoryUpsertDTO dto) {
        return Result.success(patientHealthProfileService.saveSurgeryHistory(dto));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除手术外伤史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deleteSurgeryHistory(id);
        return Result.success();
    }
}
