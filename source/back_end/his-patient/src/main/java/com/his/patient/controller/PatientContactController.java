package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientContactQueryDTO;
import com.his.patient.dto.PatientContactUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientContactVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者联系人控制器
 */
@Tag(name = "患者联系人")
@RestController
@RequestMapping("/patient/contact")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientContactController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者的联系人列表")
    @PostMapping("/list")
    public Result<List<PatientContactVO>> list(@Valid @RequestBody PatientContactQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getContacts());
    }

    @Operation(summary = "根据ID查询联系人")
    @GetMapping("/getById")
    public Result<PatientContactVO> getById(@RequestParam Long contactId) {
        return Result.success(patientHealthProfileService.getContact(contactId));
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改联系人")
    @PostMapping("/contactUpsert")
    public Result<PatientContactVO> contactUpsert(@RequestBody @Valid PatientContactUpsertDTO contactUpsertDTO) {
        return Result.success(patientHealthProfileService.saveContact(contactUpsertDTO));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除联系人")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long contactId) {
        patientHealthProfileService.deleteContact(contactId);
        return Result.success();
    }
}
