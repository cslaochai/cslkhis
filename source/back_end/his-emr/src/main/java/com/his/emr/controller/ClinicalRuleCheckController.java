package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.RuleCheckExecuteDTO;
import com.his.emr.dto.RuleCheckHandleDTO;
import com.his.emr.dto.RuleCheckQueryPageDTO;
import com.his.emr.service.ClinicalRuleCheckService;
import com.his.emr.vo.BizClinicalRuleCheckVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 临床规则校验控制器
 */
@Tag(name = "临床规则校验")
@RestController
@RequestMapping("/charge/ruleCheck")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ClinicalRuleCheckController {

    private final ClinicalRuleCheckService clinicalRuleCheckService;

    @Operation(summary = "分页查询校验记录")
    @PostMapping("/listPage")
    public Result<PageResult<BizClinicalRuleCheckVO>> listPage(@Valid @RequestBody RuleCheckQueryPageDTO queryDTO) {
        return Result.success(clinicalRuleCheckService.listPage(queryDTO));
    }

    @Operation(summary = "获取校验详情")
    @GetMapping("/getById")
    public Result<BizClinicalRuleCheckVO> getById(@RequestParam Long id) {
        return Result.success(clinicalRuleCheckService.getCheckDetail(id));
    }

    @PreAuthorize("hasAuthority('emr:medicalReview:edit')")
    @Operation(summary = "执行临床规则校验")
    @PostMapping("/executeCheck")
    public Result<BizClinicalRuleCheckVO> executeCheck(@Valid @RequestBody RuleCheckExecuteDTO actionDTO) {
        return Result.success(clinicalRuleCheckService.executeCheck(actionDTO.getRecordId(), actionDTO.getRuleType(),
                actionDTO.getCheckBy()));
    }

    @PreAuthorize("hasAuthority('emr:medicalReview:edit')")
    @Operation(summary = "处理校验问题")
    @PostMapping("/handleCheck")
    public Result<Void> handleCheck(@Valid @RequestBody RuleCheckHandleDTO actionDTO) {
        boolean success = clinicalRuleCheckService.handleCheck(actionDTO.getId(), actionDTO.getIgnore(), actionDTO.getRemark());
        return success ? Result.success("处理成功", null) : Result.error("处理失败");
    }
}
