package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.RecordQcFlowService;
import com.his.emr.vo.RecordQcFlowActionVO;
import com.his.emr.vo.RecordQcFlowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病历三级质控流转控制器
 * <p>
 * 状态机全部收口在 RecordQcFlowServiceImpl，本类不做状态判断。
 */
@Tag(name = "病历三级质控流转")
@RestController
@RequestMapping("/recordQcFlow")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('qc:recordQc:list')")
public class RecordQcFlowController {

    private final RecordQcFlowService flowService;

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "发起三级质控流转（同一病历同时只允许一条在途）")
    @PostMapping("/start")
    public Result<RecordQcFlowVO> start(@Valid @RequestBody RecordQcFlowStartDTO dto) {
        return Result.success("已发起，进入科级待审", flowService.start(dto));
    }

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "当前级审核通过（科级/病案室；医务处请走 finalApprove）")
    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody RecordQcFlowOpinionDTO dto) {
        flowService.approve(dto);
        return Result.success("审核通过", null);
    }

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "当前级退回整改（缺陷明细 + 整改要求必填）")
    @PostMapping("/return")
    public Result<Void> returnForRework(@Valid @RequestBody RecordQcFlowReturnDTO dto) {
        flowService.returnForRework(dto);
        return Result.success("已退回整改", null);
    }

    @PreAuthorize("hasAuthority('qc:recordQc:add')")
    @Operation(summary = "科室整改提交（回到退回发生级待审）")
    @PostMapping("/resubmit")
    public Result<Void> resubmit(@Valid @RequestBody RecordQcFlowOpinionDTO dto) {
        flowService.resubmit(dto);
        return Result.success("整改已提交", null);
    }

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "医务处终审（定级必填，终态）")
    @PostMapping("/finalApprove")
    public Result<Void> finalApprove(@Valid @RequestBody RecordQcFlowFinalDTO dto) {
        flowService.finalApprove(dto);
        return Result.success("终审通过", null);
    }

    @Operation(summary = "流转单分页")
    @PostMapping("/listPage")
    public Result<PageResult<RecordQcFlowVO>> listPage(@Valid @RequestBody RecordQcFlowQueryPageDTO queryDTO) {
        return Result.success(flowService.page(queryDTO));
    }

    @Operation(summary = "流转单详情")
    @GetMapping("/getDetailById")
    public Result<RecordQcFlowVO> getDetailById(@RequestParam Long id) {
        return Result.success(flowService.getDetailById(id));
    }

    @Operation(summary = "流转时间线（动作升序）")
    @GetMapping("/listActions")
    public Result<List<RecordQcFlowActionVO>> listActions(@RequestParam Long flowId) {
        return Result.success(flowService.listActions(flowId));
    }
}
