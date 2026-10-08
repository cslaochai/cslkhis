package com.his.operation.controller;

import com.his.common.base.Result;
import com.his.operation.dto.CountItemInputUpsertDTO;
import com.his.operation.dto.CountPhaseDTO;
import com.his.operation.dto.OperationCountUpsertDTO;
import com.his.operation.service.OperationCountService;
import com.his.operation.vo.OperationCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 手术器械/敷料清点端点
 */
@Tag(name = "手术器械清点")
@RestController
@RequestMapping("/patient/inpatient/operationCount")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:operationCount:list')")
public class OperationCountController {

    private final OperationCountService operationCountService;

    @Operation(summary = "某台手术的清点单（含明细；没有则 data 为 null）")
    @GetMapping("/getByApply")
    public Result<OperationCountVO> getByApply(@RequestParam Long applyId) {
        return Result.success(operationCountService.getByApply(applyId));
    }

    @Operation(summary = "清点单详情")
    @GetMapping("/getDetailById")
    public Result<OperationCountVO> getDetailById(@RequestParam Long countId) {
        return Result.success(operationCountService.getDetailById(countId));
    }

    @PreAuthorize("hasAuthority('ipd:operationCount:add')")
    @Operation(summary = "建立清点单（含清点清单），返回清点单号")
    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid OperationCountUpsertDTO dto) {
        return Result.success("清点单已建立（请依次完成三次核对）", operationCountService.create(dto));
    }

    @PreAuthorize("hasAuthority('ipd:operationCount:add')")
    @Operation(summary = "追加一行清点明细（开始清点后不再允许加行）")
    @PostMapping("/addItem")
    public Result<Void> addItem(@RequestParam Long countId, @RequestBody @Valid CountItemInputUpsertDTO dto) {
        operationCountService.addItem(countId, dto);
        return Result.success("清点明细已追加", null);
    }

    @PreAuthorize("hasAuthority('ipd:operationCount:edit')")
    @Operation(summary = "登记某一阶段的清点数量（1-术前 2-关体前 3-关体后；必须逐项给全）")
    @PostMapping("/countPhase")
    public Result<Void> countPhase(@Valid @RequestBody CountPhaseDTO dto) {
        operationCountService.countPhase(dto);
        return Result.success("本次清点已登记", null);
    }
}
