package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.RuleQueryDTO;
import com.mis.dto.RuleStatusDTO;
import com.mis.dto.RuleUpsertDTO;
import com.mis.entity.MssAuditRule;
import com.mis.service.AuditRuleService;
import com.mis.vo.ActionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基金监管 - 规则配置
 */
@RestController
@RequestMapping("/supervise/rule")
@RequiredArgsConstructor
public class SuperviseRuleController {

    private final AuditRuleService auditRuleService;

    @GetMapping("/listPage")
    public Result<PageVO<MssAuditRule>> listPage(RuleQueryDTO query) {
        return Result.success(auditRuleService.rulePage(query));
    }

    @PostMapping("/upsert")
    public Result<ActionVO> upsert(@RequestBody @Valid RuleUpsertDTO dto) {
        return Result.success(auditRuleService.upsert(dto));
    }

    @PostMapping("/status")
    public Result<ActionVO> status(@RequestBody @Valid RuleStatusDTO dto) {
        return Result.success(auditRuleService.changeStatus(dto));
    }

    @GetMapping("/selectList")
    public Result<List<MssAuditRule>> selectList() {
        return Result.success(auditRuleService.enabledRules());
    }
}
