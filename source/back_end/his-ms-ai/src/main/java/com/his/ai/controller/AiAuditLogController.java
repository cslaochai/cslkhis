package com.his.ai.controller;

import com.his.ai.dto.AiAuditLogPageQueryDTO;
import com.his.ai.service.AiAuditQueryService;
import com.his.ai.vo.AiAuditLogVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 调用审计查询接口。
 */
@Tag(name = "AI 能力-调用审计")
@RestController
@RequestMapping("/ai/auditLog")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiAuditLogController {

    private final AiAuditQueryService aiAuditQueryService;

    /**
     * 分页查询 AI 能力调用审计，可按能力、状态、业务单据与操作人收窄。
     * 用于系统管理「日志中心」的 AI 审计签页排查模型调用耗时与降级熔断原因。
     */
    @Operation(summary = "分页查询 AI 调用审计（可追溯模型、提示词版本、耗时与降级原因）")
    @PostMapping("/listPage")
    public Result<PageResult<AiAuditLogVO>> listPage(@Valid @RequestBody AiAuditLogPageQueryDTO pageQueryDTO) {
        return Result.success(aiAuditQueryService.listPage(pageQueryDTO));
    }
}
