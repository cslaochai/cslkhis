package com.his.miniapp.controller;

import com.his.spi.service.RegistFacadeService;
import com.his.common.base.Result;
import com.his.spi.dto.PrevisitSubmitDTO;
import com.his.spi.service.EmrPatientFacadeService;
import com.his.spi.vo.PrevisitDetailVO;
import com.his.spi.vo.PrevisitQuestionnaireVO;
import com.his.spi.service.PatientFacadeService;
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

    private final EmrPatientFacadeService emrPatientFacadeService;

    private final RegistFacadeService registFacadeService;

    private final PatientFacadeService patientFacadeService;

    /**
     * 预问诊量表：下发主症状、通用问题与主症状追问组组成的题目结构。
     * 用于小程序「预问诊」页进入时渲染待填问卷。
     */
    @Operation(summary = "预问诊量表（主症状 + 通用问 + 主症状追问组）")
    @GetMapping("/questionnaire")
    public Result<PrevisitQuestionnaireVO> questionnaire() {
        return Result.success(emrPatientFacadeService.questionnaire());
    }

    /**
     * 提交预问诊问卷：按挂号维度保存本次填写内容，重复提交覆盖更新；归属从挂号记录反查后再校验。
     * 用于小程序「预问诊」页填完问卷后的提交动作，医生接诊前可读到该结果。
     */
    @Operation(summary = "提交问卷（按挂号 upsert，重复提交覆盖更新）")
    @PostMapping("/submit")
    public Result<PrevisitDetailVO> submit(@RequestBody @Valid PrevisitSubmitDTO dto) {
        // 归属从挂号记录反查，不看前端传的谁 —— 改 registId 就能替他人填病史
        Long ownerPatientId = registFacadeService.patientIdOfRegist(dto.getRegistId());
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientFacadeService.canAccessPatient(ownerPatientId)) {
            return Result.error("无权为该就诊人填写预问诊");
        }
        return Result.success("提交成功", emrPatientFacadeService.submit(dto));
    }

    /**
     * 按挂号查已提交的问卷：取该次挂号填写过的预问诊内容，未填时为空。
     * 用于小程序「预问诊」页进入时与量表并行拉取，把已填内容回显以便修改。
     */
    @Operation(summary = "按挂号查已提交的问卷（回显）")
    @GetMapping("/getByRegist")
    public Result<PrevisitDetailVO> getByRegist(@RequestParam Long registId) {
        Long ownerPatientId = registFacadeService.patientIdOfRegist(registId);
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientFacadeService.canAccessPatient(ownerPatientId)) {
            return Result.error("无权查看该就诊人的预问诊");
        }
        return Result.success(emrPatientFacadeService.getByRegist(registId));
    }
}
