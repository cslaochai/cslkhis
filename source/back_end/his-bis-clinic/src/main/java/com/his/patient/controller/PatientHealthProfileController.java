package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientHealthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者健康档案聚合控制器
 */
@Tag(name = "患者健康档案")
@RestController
@RequestMapping("/patient/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientHealthProfileController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者健康档案（六组）")
    @GetMapping("/getDetail")
    public Result<PatientHealthProfileVO> getDetail(@RequestParam Long patientId) {
        return Result.success(patientHealthProfileService.getProfile(patientId));
    }
}
