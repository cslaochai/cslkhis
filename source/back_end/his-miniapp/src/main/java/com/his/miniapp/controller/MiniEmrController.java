package com.his.miniapp.controller;

import com.his.spi.service.EmrPatientFacadeService;
import com.his.common.base.Result;
import com.his.spi.vo.MyMedicalRecordVO;
import com.his.spi.service.PatientFacadeService;
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
 * 患者端门诊病历
 */
@Tag(name = "患者端-门诊病历")
@RestController
@RequestMapping("/miniapp/emr")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniEmrController {

    private final EmrPatientFacadeService emrPatientFacadeService;
    private final PatientFacadeService patientFacadeService;

    /**
     * 我的病历列表：就诊人已提交或已归档的门诊病历，按就诊时间倒序。
     * 用于小程序「门诊病历」页的病历清单。
     */
    @Operation(summary = "我的病历列表（只含已提交/已归档）")
    @GetMapping("/myRecords")
    public Result<List<MyMedicalRecordVO>> myRecords(@RequestParam Long patientId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(emrPatientFacadeService.myRecords(patientId));
    }

    /**
     * 病历详情：按病历 ID 取单份门诊病历全文，越权即拒。
     * 用于小程序「门诊病历」页点开某条病历后的正文展示。
     */
    @Operation(summary = "病历详情")
    @GetMapping("/myRecordDetail")
    public Result<MyMedicalRecordVO> myRecordDetail(@RequestParam Long patientId, @RequestParam Long recordId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(emrPatientFacadeService.myRecordDetail(patientId, recordId));
    }
}
