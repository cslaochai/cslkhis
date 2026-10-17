package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientFamilyHistoryUpsertDTO;
import com.his.patient.dto.PatientHistoryQueryDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientFamilyHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者家族史控制器
 */
@Tag(name = "患者家族史")
@RestController
@RequestMapping("/patient/familyHistory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientFamilyHistoryController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的家族史列表")
    @PostMapping("/list")
    public Result<List<PatientFamilyHistoryVO>> list(@Valid @RequestBody PatientHistoryQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getFamilyHistories());
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改家族史")
    @PostMapping("/familyHistoryUpsert")
    public Result<PatientFamilyHistoryVO> familyHistoryUpsert(@RequestBody @Valid PatientFamilyHistoryUpsertDTO dto) {
        return Result.success(patientHealthProfileService.saveFamilyHistory(dto));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除家族史")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        patientHealthProfileService.deleteFamilyHistory(id);
        return Result.success();
    }
}
