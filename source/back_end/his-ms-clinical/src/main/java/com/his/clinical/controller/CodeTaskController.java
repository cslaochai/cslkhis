package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.CodeTaskAssignUpsertDTO;
import com.his.clinical.dto.CodeTaskAuditDTO;
import com.his.clinical.dto.CodeTaskPageQueryDTO;
import com.his.clinical.dto.CodeTaskSubmitDTO;
import com.his.clinical.service.ArchiveCodeTaskService;
import com.his.clinical.vo.ArchiveCodeTaskStatsVO;
import com.his.clinical.vo.ArchiveCodeTaskVO;
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

    /**
     * 编码任务分页：按任务状态、编码员、任务号或患者关键字筛出待办与已办的编码任务。
     * 用于「编码任务池」页查询卡下方的任务表格。
     */
    @Operation(summary = "分页查询编码任务")
    @PostMapping("/listPage")
    public Result<PageResult<ArchiveCodeTaskVO>> listPage(@Valid @RequestBody CodeTaskPageQueryDTO queryDTO) {
        return Result.success(archiveCodeTaskService.page(queryDTO));
    }

    /**
     * 编码任务详情：病案基本信息、已编码值与审核退修意见。
     * 用于任务列表行点开详情弹框时按任务编号取数。
     */
    @Operation(summary = "编码任务详情")
    @GetMapping("/getDetailById")
    public Result<ArchiveCodeTaskVO> getDetailById(@RequestParam Long id) {
        return Result.success(archiveCodeTaskService.getDetailById(id));
    }

    /**
     * 编码工作台汇总：待编码、已提交、已完成、已退修四类任务量。
     * 用于「编码任务池」页顶部统计卡。
     */
    @Operation(summary = "工作台统计（待编码/已提交/已完成/已退修）")
    @GetMapping("/stats")
    public Result<ArchiveCodeTaskStatsVO> stats() {
        return Result.success(archiveCodeTaskService.stats());
    }

    /**
     * 补齐任务池：给还没建任务的待归档与已归档病案各生成一条待编码任务，返回新建条数。
     * 用于「编码任务池」页的同步按钮；重复点不会生出重复任务，任务只能由这一步灌入。
     */
    @PreAuthorize("hasAuthority('emr:codeTask:edit')")
    @Operation(summary = "同步任务池（为尚无任务的待归档/已归档病历各建一条待编码任务，幂等）")
    @PostMapping("/sync")
    public Result<Integer> sync() {
        int created = archiveCodeTaskService.syncTasks();
        return Result.success("新建 " + created + " 条任务", created);
    }

    /**
     * 把任务分配给指定编码员。
     * 用于「编码任务池」页对未领取或需转派的任务派活。
     */
    @PreAuthorize("hasAuthority('emr:codeTask:edit')")
    @Operation(summary = "分配编码员")
    @PostMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody CodeTaskAssignUpsertDTO dto) {
        archiveCodeTaskService.assign(dto);
        return Result.success("分配成功", null);
    }

    /**
     * 提交编码结果：待编码或被退修的任务编完疾病码后交回审核。
     * 用于编码员在「编码任务池」页填写编码后提交。
     */
    @PreAuthorize("hasAuthority('emr:codeTask:add')")
    @Operation(summary = "提交编码（待编码/已退修 → 已提交）")
    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody CodeTaskSubmitDTO dto) {
        archiveCodeTaskService.submit(dto);
        return Result.success("提交成功", null);
    }

    /**
     * 审核编码：通过则任务终态，退修则打回编码员并给出原因、同时提醒本人。
     * 用于编码质控岗在「编码任务池」页复核已提交的编码。
     */
    @PreAuthorize("hasAuthority('emr:codeTask:edit')")
    @Operation(summary = "审核（通过→已完成；退修→已退修并提醒编码员）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody CodeTaskAuditDTO dto) {
        archiveCodeTaskService.audit(dto);
        return Result.success("审核完成", null);
    }
}
