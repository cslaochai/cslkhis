package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.emr.service.PrescriptionService;
import com.his.emr.vo.MyPrescriptionVO;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端处方（自 PrescriptionController.myList 迁入）。
 */
@Tag(name = "患者端-处方")
@RestController
@RequestMapping("/miniapp/prescription")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappPrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "我的处方（含药味明细，只看自己绑定的就诊人）")
    @GetMapping("/myList")
    public Result<List<MyPrescriptionVO>> myList(@RequestParam Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的处方");
        }
        return Result.success(prescriptionService.myPrescriptions(patientId));
    }
}
