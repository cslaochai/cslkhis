package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.InfusionActionDTO;
import com.his.patient.service.InpatientInfusionService;
import com.his.patient.vo.InpatientOrderExecVO;
import com.his.patient.vo.InfusionRoundVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 输液执行闭环（G14）：开始（滴速）→ 巡视 → 结束（不良反应）。
 */
@RestController
@RequestMapping("/patient/inpatient/infusion")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:nurse:list')")
public class InpatientInfusionController {

    private final InpatientInfusionService infusionService;

    @PreAuthorize("hasAuthority('ipd:nurse:edit')")
    @Operation(summary = "开始输注（执行行需已执行且为静脉类；记录滴速）")
    @PostMapping("/start")
    public Result<InpatientOrderExecVO> start(@Valid @RequestBody InfusionActionDTO dto) {
        return Result.success(infusionService.start(dto));
    }

    @PreAuthorize("hasAuthority('ipd:nurse:edit')")
    @Operation(summary = "巡视（开始后、结束前；滴速/余量/备注）")
    @PostMapping("/round")
    public Result<InfusionRoundVO> round(@Valid @RequestBody InfusionActionDTO dto) {
        return Result.success(infusionService.round(dto));
    }

    @PreAuthorize("hasAuthority('ipd:nurse:edit')")
    @Operation(summary = "结束输注（adverseFlag=1 时描述必填）")
    @PostMapping("/finish")
    public Result<InpatientOrderExecVO> finish(@Valid @RequestBody InfusionActionDTO dto) {
        return Result.success(infusionService.finish(dto));
    }

    @Operation(summary = "某执行行的巡视记录（时间升序）")
    @GetMapping("/rounds")
    public Result<List<InfusionRoundVO>> rounds(@RequestParam Long execId) {
        return Result.success(infusionService.rounds(execId));
    }
}
