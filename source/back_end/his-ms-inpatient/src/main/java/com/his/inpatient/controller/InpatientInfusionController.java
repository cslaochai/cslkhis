package com.his.inpatient.controller;

import com.his.common.base.Result;
import com.his.inpatient.dto.InfusionActionDTO;
import com.his.inpatient.service.InpatientInfusionService;
import com.his.inpatient.vo.InfusionRoundVO;
import com.his.inpatient.vo.InpatientOrderExecVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 输液执行闭环（G14）：开始（滴速）→ 巡视 → 结束（不良反应）。
 */
@RestController
@RequestMapping("/patient/inpatient/infusion")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:nurse:list')")
public class InpatientInfusionController {

    private final InpatientInfusionService inpatientInfusionService;

    /**
     * 开始输注：把已执行的静脉类医嘱执行行置于输液中并记录滴速。
     * 用于住院医嘱执行工作台的输液闭环弹框（护士站与住院医嘱页共用）。
     */
    @PreAuthorize("hasAuthority('ipd:nurse:edit')")
    @Operation(summary = "开始输注（执行行需已执行且为静脉类；记录滴速）")
    @PostMapping("/start")
    public Result<InpatientOrderExecVO> start(@Valid @RequestBody InfusionActionDTO dto) {
        return Result.success(inpatientInfusionService.start(dto));
    }

    /**
     * 追加一条巡视记录：滴速、剩余量与备注。
     * 用于输注进行中在输液闭环弹框里逐次登记床旁巡查。
     */
    @PreAuthorize("hasAuthority('ipd:nurse:edit')")
    @Operation(summary = "巡视（开始后、结束前；滴速/余量/备注）")
    @PostMapping("/round")
    public Result<InfusionRoundVO> round(@Valid @RequestBody InfusionActionDTO dto) {
        return Result.success(inpatientInfusionService.round(dto));
    }

    /**
     * 结束输注并收口本袋：登记不良反应标记，标记有反应时必须填写描述。
     * 用于输液闭环弹框的结束动作，结束后该执行行不再允许巡视。
     */
    @PreAuthorize("hasAuthority('ipd:nurse:edit')")
    @Operation(summary = "结束输注（adverseFlag=1 时描述必填）")
    @PostMapping("/finish")
    public Result<InpatientOrderExecVO> finish(@Valid @RequestBody InfusionActionDTO dto) {
        return Result.success(inpatientInfusionService.finish(dto));
    }

    /**
     * 查一条执行行的全部巡视记录，按时间升序。
     * 用于输液闭环弹框展示本袋输注的巡查轨迹。
     */
    @Operation(summary = "某执行行的巡视记录（时间升序）")
    @GetMapping("/rounds")
    public Result<List<InfusionRoundVO>> rounds(@RequestParam Long execId) {
        return Result.success(inpatientInfusionService.rounds(execId));
    }
}
