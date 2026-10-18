package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.ExecutionPageQueryDTO;
import com.his.medicaltech.dto.ExecutionReviewDTO;
import com.his.medicaltech.dto.ExecutionStartDTO;
import com.his.medicaltech.service.MedTechExecutionService;
import com.his.medicaltech.vo.BizMedTechExecutionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医技执行控制器：检查与检验申请执行单的接单、完成、审核这条执行链的接口入口。
 */
@Tag(name = "医技执行管理")
@RestController
@RequestMapping("/charge/execution")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedTechExecutionController {

    private final MedTechExecutionService medTechExecutionService;

    /**
     * 按患者、申请类型与执行状态分页查询医技执行单。
     * 供医技科室筛选本人待办执行任务时取数。
     */
    @Operation(summary = "分页查询待执行列表")
    @PostMapping("/listPage")
    public Result<PageResult<BizMedTechExecutionVO>> listPage(@Valid @RequestBody ExecutionPageQueryDTO queryDTO) {
        return Result.success(medTechExecutionService.listPageVO(queryDTO.getPatientId(), queryDTO.getApplyType(),
                queryDTO.getExecutionStatus(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 把执行单从待执行置为执行中，并落执行人与执行时刻。
     * 用于医技人员开始实施检查或检验时的接单动作。
     */
    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "开始执行")
    @PostMapping("/startExecution")
    public Result<Void> startExecution(@Valid @RequestBody ExecutionStartDTO actionDTO) {
        boolean success = medTechExecutionService.startExecution(actionDTO.getId(), actionDTO.getExecutorId(), actionDTO.getExecutorName());
        return success ? Result.success("开始执行", null) : Result.error("操作失败");
    }

    /**
     * 把执行中的执行单置为已完成，并落完成时刻。
     * 用于检查或检验操作做完后的收尾登记。
     */
    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "完成执行")
    @PostMapping("/completeExecution")
    public Result<Void> completeExecution(@RequestParam Long id) {
        boolean success = medTechExecutionService.completeExecution(id);
        return success ? Result.success("执行完成", null) : Result.error("操作失败");
    }

    /**
     * 审核已完成的执行单，审核人取当前登录人并落审核时刻。
     * 用于执行链最后一步的第二人确认。
     */
    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "审核执行")
    @PostMapping("/reviewExecution")
    public Result<Void> reviewExecution(@Valid @RequestBody ExecutionReviewDTO actionDTO) {
        boolean success = medTechExecutionService.reviewExecution(actionDTO.getId());
        return success ? Result.success("审核通过", null) : Result.error("审核失败");
    }
}
