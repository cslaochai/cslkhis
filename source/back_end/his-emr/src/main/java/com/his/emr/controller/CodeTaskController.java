package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.CodeTaskAssignUpsertDTO;
import com.his.emr.dto.CodeTaskAuditDTO;
import com.his.emr.dto.CodeTaskQueryPageDTO;
import com.his.emr.dto.CodeTaskSubmitDTO;
import com.his.emr.service.ArchiveCodeTaskService;
import com.his.emr.vo.ArchiveCodeTaskStatsVO;
import com.his.emr.vo.ArchiveCodeTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病案编码任务控制器
 */
@Tag(name = "病案编码任务池")
@RestController
@RequestMapping("/charge/codeTask")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('emr:codeTask:list')")
public class CodeTaskController {

    private final ArchiveCodeTaskService archiveCodeTaskService;

    @Operation(summary = "分页查询编码任务")
    @PostMapping("/listPage")
    public Result<PageResult<ArchiveCodeTaskVO>> listPage(@Valid @RequestBody CodeTaskQueryPageDTO queryDTO) {
        return Result.success(archiveCodeTaskService.page(queryDTO));
    }

    @Operation(summary = "编码任务详情")
    @GetMapping("/getDetailById")
    public Result<ArchiveCodeTaskVO> getDetailById(@RequestParam Long id) {
        return Result.success(archiveCodeTaskService.getDetailById(id));
    }

    @Operation(summary = "工作台统计（待编码/已提交/已完成/已退修）")
    @GetMapping("/stats")
    public Result<ArchiveCodeTaskStatsVO> stats() {
        return Result.success(archiveCodeTaskService.stats());
    }

    @PreAuthorize("hasAuthority('emr:codeTask:edit')")
    @Operation(summary = "同步任务池（为尚无任务的待归档/已归档病历各建一条待编码任务，幂等）")
    @PostMapping("/sync")
    public Result<Integer> sync() {
        int created = archiveCodeTaskService.syncTasks();
        return Result.success("新建 " + created + " 条任务", created);
    }

    @PreAuthorize("hasAuthority('emr:codeTask:edit')")
    @Operation(summary = "分配编码员")
    @PostMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody CodeTaskAssignUpsertDTO dto) {
        archiveCodeTaskService.assign(dto);
        return Result.success("分配成功", null);
    }

    @PreAuthorize("hasAuthority('emr:codeTask:add')")
    @Operation(summary = "提交编码（待编码/已退修 → 已提交）")
    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody CodeTaskSubmitDTO dto) {
        archiveCodeTaskService.submit(dto);
        return Result.success("提交成功", null);
    }

    @PreAuthorize("hasAuthority('emr:codeTask:edit')")
    @Operation(summary = "审核（通过→已完成；退修→已退修并提醒编码员）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody CodeTaskAuditDTO dto) {
        archiveCodeTaskService.audit(dto);
        return Result.success("审核完成", null);
    }
}
