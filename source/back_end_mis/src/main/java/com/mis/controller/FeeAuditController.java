package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.AuditQueryDTO;
import com.mis.dto.IdDTO;
import com.mis.entity.MssFeeAudit;
import com.mis.service.FeeAuditService;
import com.mis.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 费用合规性审核
 */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class FeeAuditController {

    private final FeeAuditService feeAuditService;

    @GetMapping("/listPage")
    public Result<PageVO<MssFeeAudit>> listPage(AuditQueryDTO query) {
        return Result.success(feeAuditService.auditPage(query));
    }

    @PostMapping("/pass")
    public Result<ActionVO> pass(@RequestBody IdDTO dto) {
        return Result.success(feeAuditService.pass(Long.valueOf(dto.getId())));
    }

    @PostMapping("/reject")
    public Result<ActionVO> reject(@RequestBody IdDTO dto) {
        return Result.success(feeAuditService.reject(Long.valueOf(dto.getId())));
    }
}
