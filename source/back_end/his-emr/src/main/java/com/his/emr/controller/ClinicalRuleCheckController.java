package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.RuleCheckExecuteDTO;
import com.his.emr.dto.RuleCheckHandleDTO;
import com.his.emr.dto.RuleCheckPageQueryDTO;
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

    /**
     * 校验记录分页：按患者、规则类型、处理状态筛出历次临床规则校验结果。
     * 供病历审核环节回看规则命中台账，按人追溯某次就诊被拦下的问题。
     */
    @Operation(summary = "分页查询校验记录")
    @PostMapping("/listPage")
    public Result<PageResult<BizClinicalRuleCheckVO>> listPage(@Valid @RequestBody RuleCheckPageQueryDTO queryDTO) {
        return Result.success(clinicalRuleCheckService.listPage(queryDTO));
    }

    /**
     * 单条校验详情：命中的规则内容与错误级别、处理建议。
     * 供病历审核环节处置某条问题前看全上下文。
     */
    @Operation(summary = "获取校验详情")
    @GetMapping("/getById")
    public Result<BizClinicalRuleCheckVO> getById(@RequestParam Long id) {
        return Result.success(clinicalRuleCheckService.getCheckDetail(id));
    }

    /**
     * 对一份病历按指定规则类型跑一次校验并留一条记录，覆盖配伍禁忌、检验诊断关联性、用药合理性三类。
     * 用于病历审核环节发起规则检查时建档留痕。
     */
    @PreAuthorize("hasAuthority('emr:medicalReview:edit')")
    @Operation(summary = "执行临床规则校验")
    @PostMapping("/executeCheck")
    public Result<BizClinicalRuleCheckVO> executeCheck(@Valid @RequestBody RuleCheckExecuteDTO actionDTO) {
        return Result.success(clinicalRuleCheckService.executeCheck(actionDTO.getRecordId(), actionDTO.getRuleType(),
                actionDTO.getCheckBy()));
    }

    /**
     * 处置一条待处理的校验问题：按建议整改后确认处理，或带说明忽略。
     * 用于病历审核环节对未通过项回写结论，已处置的结论不可重复改写。
     */
    @PreAuthorize("hasAuthority('emr:medicalReview:edit')")
    @Operation(summary = "处理校验问题")
    @PostMapping("/handleCheck")
    public Result<Void> handleCheck(@Valid @RequestBody RuleCheckHandleDTO actionDTO) {
        boolean success = clinicalRuleCheckService.handleCheck(actionDTO.getId(), actionDTO.getIgnore(), actionDTO.getRemark());
        return success ? Result.success("处理成功", null) : Result.error("处理失败");
    }
}
