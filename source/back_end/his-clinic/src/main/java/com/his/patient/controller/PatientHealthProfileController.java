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
 * 患者健康档案聚合控制器：过敏史、既往病史、手术外伤史、家族史、用药史、联系人六组档案的读接口入口。
 */
@Tag(name = "患者健康档案")
@RestController
@RequestMapping("/patient/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientHealthProfileController {

    private final PatientHealthProfileService patientHealthProfileService;

    /**
     * 一次带回一名患者健康档案的六组明细、主档文本投影与「只有文本没有明细」的分叉标记。
     * 用于「健康档案」页的唯一数据源。
     */
    @Operation(summary = "查询患者健康档案（六组）")
    @GetMapping("/getDetail")
    public Result<PatientHealthProfileVO> getDetail(@RequestParam Long patientId) {
        return Result.success(patientHealthProfileService.getProfile(patientId));
    }
}
