package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.ExecutionQueryPageDTO;
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
 * 医技执行管理控制器
 */
@Tag(name = "医技执行管理")
@RestController
@RequestMapping("/charge/execution")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedTechExecutionController {

    private final MedTechExecutionService executionService;

    @Operation(summary = "分页查询待执行列表")
    @PostMapping("/listPage")
    public Result<PageResult<BizMedTechExecutionVO>> listPage(@Valid @RequestBody ExecutionQueryPageDTO queryDTO) {
        return Result.success(executionService.listPageVO(queryDTO.getPatientId(), queryDTO.getApplyType(),
                queryDTO.getExecutionStatus(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "开始执行")
    @PostMapping("/startExecution")
    public Result<Void> startExecution(@Valid @RequestBody ExecutionStartDTO actionDTO) {
        boolean success = executionService.startExecution(actionDTO.getId(), actionDTO.getExecutorId(), actionDTO.getExecutorName());
        return success ? Result.success("开始执行", null) : Result.error("操作失败");
    }

    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "完成执行")
    @PostMapping("/completeExecution")
    public Result<Void> completeExecution(@RequestParam Long id) {
        boolean success = executionService.completeExecution(id);
        return success ? Result.success("执行完成", null) : Result.error("操作失败");
    }

    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "审核执行")
    @PostMapping("/reviewExecution")
    public Result<Void> reviewExecution(@Valid @RequestBody ExecutionReviewDTO actionDTO) {
        boolean success = executionService.reviewExecution(actionDTO.getId(), actionDTO.getReviewerId(), actionDTO.getReviewerName());
        return success ? Result.success("审核通过", null) : Result.error("审核失败");
    }
}
