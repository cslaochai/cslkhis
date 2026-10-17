package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.dto.PatientMedicationHistoryUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientMedicationHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者既往用药史控制器
 */
@Tag(name = "患者既往用药史")
@RestController
@RequestMapping("/patient/medication")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientMedicationHistoryController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的既往用药史列表")
    @PostMapping("/list")
    public Result<List<PatientMedicationHistoryVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getMedications());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改既往用药史")
    @PostMapping("/medicationUpsert")
    public Result<PatientMedicationHistoryVO> medicationUpsert(@RequestBody @Valid PatientMedicationHistoryUpsertDTO dto) {
        return Result.success(patientHealthProfileService.saveMedication(dto));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除既往用药史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deleteMedication(id);
        return Result.success();
    }
}
