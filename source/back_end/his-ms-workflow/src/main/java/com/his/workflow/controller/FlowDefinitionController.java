package com.his.workflow.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.workflow.dto.FlowDefinitionPageQueryDTO;
import com.his.workflow.dto.FlowDefinitionUpsertDTO;
import com.his.workflow.service.FlowDefinitionService;
import com.his.workflow.vo.FlowDefinitionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义管理：审批流与 AI 集成流都在这里配。
 */
@RestController
@RequestMapping("/workflow/definition")
@RequiredArgsConstructor
public class FlowDefinitionController {

    private final FlowDefinitionService flowDefinitionService;

    @GetMapping("/listPage")
    @PreAuthorize("hasAuthority('workflow:definition:config')")
    public Result<PageResult<FlowDefinitionVO>> listPage(@Valid FlowDefinitionPageQueryDTO query) {
        return Result.success(flowDefinitionService.listPage(query));
    }

    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('workflow:definition:config')")
    public Result<FlowDefinitionVO> getDetailById(@RequestParam Long id) {
        return Result.success(flowDefinitionService.getDetailById(id));
    }

    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('workflow:definition:config')")
    public Result<Long> upsert(@RequestBody @Valid FlowDefinitionUpsertDTO dto) {
        return Result.success(flowDefinitionService.upsert(dto));
    }

    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('workflow:definition:config')")
    public Result<Void> publish(@RequestParam Long id) {
        flowDefinitionService.publish(id);
        return Result.success();
    }

    @PostMapping("/deleteById")
    @PreAuthorize("hasAuthority('workflow:definition:config')")
    public Result<Void> deleteById(@RequestParam Long id) {
        flowDefinitionService.deleteById(id);
        return Result.success();
    }

    /**
     * 已发布流程下拉，供手工启动与意图候选展示
     */
    @GetMapping("/selectList")
    @PreAuthorize("isAuthenticated()")
    public Result<List<FlowDefinitionVO>> selectList() {
        return Result.success(flowDefinitionService.selectList());
    }
}
