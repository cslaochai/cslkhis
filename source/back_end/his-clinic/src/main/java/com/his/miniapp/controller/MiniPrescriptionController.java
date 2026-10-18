package com.his.miniapp.controller;

import com.his.emr.service.EmrPatientFacadeService;
import com.his.common.base.Result;
import com.his.emr.vo.MyPrescriptionVO;
import com.his.patient.service.PatientFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者端处方
 */
@Tag(name = "患者端-处方")
@RestController
@RequestMapping("/miniapp/prescription")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniPrescriptionController {

    private final EmrPatientFacadeService emrPatientFacadeService;
    private final PatientFacadeService patientFacadeService;

    /**
     * 我的处方：就诊人已开出的处方及其药味明细，只允许查绑定关系内的就诊人。
     * 用于小程序「处方取药」页的处方清单与取药核对。
     */
    @Operation(summary = "我的处方（含药味明细，只看自己绑定的就诊人）")
    @GetMapping("/myList")
    public Result<List<MyPrescriptionVO>> myList(@RequestParam Long patientId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的处方");
        }
        return Result.success(emrPatientFacadeService.myPrescriptions(patientId));
    }
}
