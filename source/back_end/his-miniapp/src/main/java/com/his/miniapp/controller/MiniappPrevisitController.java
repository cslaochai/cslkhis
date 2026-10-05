package com.his.miniapp.controller;

import com.his.appoint.service.QueueService;
import com.his.common.base.Result;
import com.his.emr.dto.PrevisitSubmitDTO;
import com.his.emr.service.PrevisitRecordService;
import com.his.emr.vo.PrevisitDetailVO;
import com.his.emr.vo.PrevisitQuestionnaireVO;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者端预问诊（G-05）：挂号后、就诊前采集病史，报告写回医生站。
 * 题目结构由后端量表下发，前端不写死；归属校验按登录态绑定关系收窄。
 */
@Tag(name = "患者端-预问诊")
@RestController
@RequestMapping("/miniapp/previsit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappPrevisitController {

    private final PrevisitRecordService previsitRecordService;

    private final QueueService queueService;

    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "预问诊量表（主症状 + 通用问 + 主症状追问组）")
    @GetMapping("/questionnaire")
    public Result<PrevisitQuestionnaireVO> questionnaire() {
        return Result.success(previsitRecordService.questionnaire());
    }

    @Operation(summary = "提交问卷（按挂号 upsert，重复提交覆盖更新）")
    @PostMapping("/submit")
    public Result<PrevisitDetailVO> submit(@RequestBody @Valid PrevisitSubmitDTO dto) {
        // 归属从挂号记录反查，不看前端传的谁 —— 改 registId 就能替他人填病史
        Long ownerPatientId = queueService.patientIdOfRegist(dto.getRegistId());
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientGuardianService.canAccessPatient(ownerPatientId)) {
            return Result.error("无权为该就诊人填写预问诊");
        }
        return Result.success("提交成功", previsitRecordService.submit(dto));
    }

    @Operation(summary = "按挂号查已提交的问卷（回显）")
    @GetMapping("/getByRegist")
    public Result<PrevisitDetailVO> getByRegist(@RequestParam Long registId) {
        Long ownerPatientId = queueService.patientIdOfRegist(registId);
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientGuardianService.canAccessPatient(ownerPatientId)) {
            return Result.error("无权查看该就诊人的预问诊");
        }
        return Result.success(previsitRecordService.getByRegist(registId));
    }
}
