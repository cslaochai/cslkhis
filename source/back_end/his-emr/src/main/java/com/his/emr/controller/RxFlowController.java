package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.RxFlowActionDTO;
import com.his.emr.dto.RxFlowPageQueryDTO;
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
 * 处方流转单
 */
@Slf4j
@Tag(name = "处方流转")
@RestController
@RequestMapping("/rxflow")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class RxFlowController {

    private final RxFlowService rxFlowService;

    /**
     * 创建流转单：把一张已开好的处方流转到院外取药机构，生成待取的流转凭据。
     * 供医生站处方走院外取药口子时的开单环节写入。
     */
    @Operation(summary = "创建流转单（处方 → 院外机构）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    public Result<RxFlowListVO> upsert(@RequestBody @Valid RxFlowUpsertDTO dto) {
        return Result.success(rxFlowService.createFlow(dto));
    }

    /**
     * 取药完成回写：流转单从待取置为已取药，终态不可逆。
     * 供院外取药口子患者完成取药后的状态回写。
     */
    @Operation(summary = "取药完成回写（外联口子打印：1→2）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestBody @Valid RxFlowActionDTO dto) {
        rxFlowService.finish(dto);
        return Result.success(null);
    }

    /**
     * 取消流转：未取药的流转单置为已取消，终态不可逆。
     * 供处方作废或患者改回院内取药时撤销流转的环节。
     */
    @Operation(summary = "取消流转（1→3）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    public Result<Void> cancel(@RequestBody @Valid RxFlowActionDTO dto) {
        rxFlowService.cancel(dto);
        return Result.success(null);
    }

    /**
     * 流转单分页：按患者、处方与流转状态收口。
     * 供流转台账核对院外取药进度、追查哪些处方还挂在外面时取数。
     */
    @Operation(summary = "流转单分页")
    @PostMapping("/listPage")
    public Result<PageResult<RxFlowListVO>> listPage(@Valid @RequestBody RxFlowPageQueryDTO dto) {
        return Result.success(rxFlowService.listPage(dto));
    }
}
