package com.his.workflow.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.workflow.dto.FlowDispatchDTO;
import com.his.workflow.dto.FlowStartDTO;
import com.his.workflow.dto.FlowTaskHandleDTO;
import com.his.workflow.dto.FlowTaskPageQueryDTO;
import com.his.workflow.service.FlowRuntimeService;
import com.his.workflow.vo.FlowDispatchResultVO;
import com.his.workflow.vo.FlowHistoryVO;
import com.his.workflow.vo.FlowInstanceVO;
import com.his.workflow.vo.FlowTaskVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程运行时：启动/分发、待办处理、轨迹查询。
 */
@RestController
@RequestMapping("/workflow/runtime")
@RequiredArgsConstructor
public class FlowRuntimeController {

    private final FlowRuntimeService flowRuntimeService;

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('workflow:task:handle')")
    public Result<FlowInstanceVO> start(@RequestBody @Valid FlowStartDTO dto) {
        return Result.success(flowRuntimeService.start(dto));
    }

    /**
     * 统一入口：患者在小程序里说一句话，先过 AI 意图识别，再走不同流程
     */
    @PostMapping("/dispatch")
    @PreAuthorize("isAuthenticated()")
    public Result<FlowDispatchResultVO> dispatch(@RequestBody @Valid FlowDispatchDTO dto) {
        return Result.success(flowRuntimeService.dispatch(dto));
    }

    @GetMapping("/listMyTasks")
    @PreAuthorize("hasAuthority('workflow:task:handle')")
    public Result<PageResult<FlowTaskVO>> listMyTasks(@Valid FlowTaskPageQueryDTO query) {
        return Result.success(flowRuntimeService.listMyTasks(query));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('workflow:task:handle')")
    public Result<Void> approve(@RequestBody @Valid FlowTaskHandleDTO dto) {
        flowRuntimeService.approve(dto);
        return Result.success();
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('workflow:task:handle')")
    public Result<Void> reject(@RequestBody @Valid FlowTaskHandleDTO dto) {
        flowRuntimeService.reject(dto);
        return Result.success();
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAuthority('workflow:task:handle')")
    public Result<Void> withdraw(@RequestParam Long instanceId) {
        flowRuntimeService.withdraw(instanceId);
        return Result.success();
    }

    @GetMapping("/getInstance")
    @PreAuthorize("isAuthenticated()")
    public Result<FlowInstanceVO> getInstance(@RequestParam Long id) {
        return Result.success(flowRuntimeService.getInstance(id));
    }

    @GetMapping("/listHistory")
    @PreAuthorize("isAuthenticated()")
    public Result<List<FlowHistoryVO>> listHistory(@RequestParam Long instanceId) {
        return Result.success(flowRuntimeService.listHistory(instanceId));
    }

    @GetMapping("/listByBusinessId")
    @PreAuthorize("isAuthenticated()")
    public Result<List<FlowInstanceVO>> listByBusinessId(@RequestParam String businessId) {
        return Result.success(flowRuntimeService.listByBusinessId(businessId));
    }
}
