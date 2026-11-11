package com.his.app.controller;

import com.his.app.dto.MiniSurveySubmitDTO;
import com.his.app.service.MiniSurveyService;
import com.his.common.base.Result;
import com.his.spi.vo.PatientSurveyPaperVO;
import com.his.spi.vo.PatientSurveyTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端-满意度评价（出院/就诊后待填问卷与提交）。
 */
@Tag(name = "患者端-满意度评价")
@RestController
@RequestMapping("/miniapp/survey")
@RequiredArgsConstructor
public class MiniSurveyController {

    private final MiniSurveyService miniSurveyService;

    @Operation(summary = "我的待填问卷")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/pending")
    public Result<List<PatientSurveyTaskVO>> pending(@RequestParam Long patientId) {
        return Result.success(miniSurveyService.pending(patientId));
    }

    @Operation(summary = "问卷卷面（题目与已填意见）")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/paper")
    public Result<PatientSurveyPaperVO> paper(@RequestParam Long patientId, @RequestParam Long dispatchId) {
        return Result.success(miniSurveyService.paper(patientId, dispatchId));
    }

    @Operation(summary = "提交答卷")
    @PreAuthorize("hasAuthority('PATIENT')")
    @PostMapping("/submit")
    public Result<Long> submit(@Valid @RequestBody MiniSurveySubmitDTO dto) {
        return Result.success(miniSurveyService.submit(dto));
    }
}
