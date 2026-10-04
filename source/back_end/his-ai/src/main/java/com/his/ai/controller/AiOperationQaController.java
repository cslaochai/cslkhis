package com.his.ai.controller;

import com.his.ai.dto.OperationQaAskDTO;
import com.his.ai.service.OperationQaCapability;
import com.his.ai.vo.OperationQaResultVO;
import com.his.ai.vo.OperationSchemaVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 运营问数接口。读者是院领导与管理员，回答经营统计问题；
 * 模型只产 SELECT，安全闸门与执行细节见 docs/AI能力施工手册.md §6。
 */
@Tag(name = "AI 能力-运营问数")
@RestController
@RequestMapping("/ai/operationQa")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiOperationQaController {

    private final OperationQaCapability operationQaCapability;

    @PreAuthorize("hasAuthority('ai:operationQa:ask')")
    @Operation(summary = "自然语言问数（白名单表受控 SELECT，只读）")
    @PostMapping("/ask")
    public Result<OperationQaResultVO> ask(@RequestBody @Valid OperationQaAskDTO askDTO) {
        return Result.success(operationQaCapability.ask(askDTO));
    }

    @PreAuthorize("hasAuthority('ai:operationQa:list')")
    @Operation(summary = "可查询的数据域（白名单表清单）")
    @GetMapping("/schema")
    public Result<List<OperationSchemaVO>> schema() {
        return Result.success(operationQaCapability.schema());
    }
}
