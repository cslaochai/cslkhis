package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.RxFlowActionDTO;
import com.his.emr.dto.RxFlowQueryPageDTO;
import com.his.emr.dto.RxFlowUpsertDTO;
import com.his.emr.service.RxFlowService;
import com.his.emr.vo.RxFlowListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处方流转单（M2，院外取药口子·打印桩形态）。
 *
 * <p>状态机与防重闸门见 {@code RxFlowService}。
 */
@Slf4j
@Tag(name = "处方流转")
@RestController
@RequestMapping("/rxflow")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class RxFlowController {

    private final RxFlowService rxFlowService;

    @Operation(summary = "创建流转单（处方 → 院外机构）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    public Result<RxFlowListVO> upsert(@RequestBody @Valid RxFlowUpsertDTO dto) {
        return Result.success(rxFlowService.createFlow(dto));
    }

    @Operation(summary = "取药完成回写（外联口子打印桩：1→2）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestBody @Valid RxFlowActionDTO dto) {
        rxFlowService.finish(dto);
        return Result.success(null);
    }

    @Operation(summary = "取消流转（1→3）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    public Result<Void> cancel(@RequestBody @Valid RxFlowActionDTO dto) {
        rxFlowService.cancel(dto);
        return Result.success(null);
    }

    @Operation(summary = "流转单分页")
    @PostMapping("/listPage")
    public Result<PageResult<RxFlowListVO>> listPage(@Valid @RequestBody RxFlowQueryPageDTO dto) {
        return Result.success(rxFlowService.listPage(dto));
    }
}
