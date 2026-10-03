package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.emr.service.EmrService;
import com.his.emr.vo.MyMedicalRecordVO;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端门诊病历（自 EmrController 患者端方法迁入）。
 */
@Tag(name = "患者端-门诊病历")
@RestController
@RequestMapping("/miniapp/emr")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappEmrController {

    private final EmrService emrService;
    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "我的病历列表（只含已提交/已归档）")
    @GetMapping("/myRecords")
    public Result<List<MyMedicalRecordVO>> myRecords(@RequestParam Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(emrService.myRecords(patientId));
    }

    @Operation(summary = "病历详情")
    @GetMapping("/myRecordDetail")
    public Result<MyMedicalRecordVO> myRecordDetail(@RequestParam Long patientId, @RequestParam Long recordId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(emrService.myRecordDetail(patientId, recordId));
    }
}
