package com.his.miniapp.controller;

import com.his.api.service.AppointApi;
import com.his.common.base.Result;
import com.his.api.dto.PrevisitSubmitDTO;
import com.his.api.service.EmrPatientApi;
import com.his.api.vo.PrevisitDetailVO;
import com.his.api.vo.PrevisitQuestionnaireVO;
import com.his.api.service.PatientApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端预问诊
 */
@Tag(name = "患者端-预问诊")
@RestController
@RequestMapping("/miniapp/previsit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniPrevisitController {

    private final EmrPatientApi emrPatientApi;

    private final AppointApi appointApi;

    private final PatientApi patientApi;

    @Operation(summary = "预问诊量表（主症状 + 通用问 + 主症状追问组）")
    @GetMapping("/questionnaire")
    public Result<PrevisitQuestionnaireVO> questionnaire() {
        return Result.success(emrPatientApi.questionnaire());
    }

    @Operation(summary = "提交问卷（按挂号 upsert，重复提交覆盖更新）")
    @PostMapping("/submit")
    public Result<PrevisitDetailVO> submit(@RequestBody @Valid PrevisitSubmitDTO dto) {
        // 归属从挂号记录反查，不看前端传的谁 —— 改 registId 就能替他人填病史
        Long ownerPatientId = appointApi.patientIdOfRegist(dto.getRegistId());
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientApi.canAccessPatient(ownerPatientId)) {
            return Result.error("无权为该就诊人填写预问诊");
        }
        return Result.success("提交成功", emrPatientApi.submit(dto));
    }

    @Operation(summary = "按挂号查已提交的问卷（回显）")
    @GetMapping("/getByRegist")
    public Result<PrevisitDetailVO> getByRegist(@RequestParam Long registId) {
        Long ownerPatientId = appointApi.patientIdOfRegist(registId);
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientApi.canAccessPatient(ownerPatientId)) {
            return Result.error("无权查看该就诊人的预问诊");
        }
        return Result.success(emrPatientApi.getByRegist(registId));
    }
}
