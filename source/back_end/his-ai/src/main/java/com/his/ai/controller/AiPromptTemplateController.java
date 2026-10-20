package com.his.ai.controller;

import com.his.ai.dto.AiPromptTemplatePageQueryDTO;
import com.his.ai.dto.AiPromptTemplateUpsertDTO;
import com.his.ai.service.AiPromptTemplateService;
import com.his.ai.vo.AiPromptTemplateVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/promptTemplate")
@RequiredArgsConstructor
public class AiPromptTemplateController {

    private final AiPromptTemplateService promptTemplateService;

    @GetMapping("/listPage")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<PageResult<AiPromptTemplateVO>> listPage(@Valid AiPromptTemplatePageQueryDTO query) {
        return Result.success(promptTemplateService.listPage(query));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<List<AiPromptTemplateVO>> history(@RequestParam String code) {
        return Result.success(promptTemplateService.history(code));
    }

    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<String> upsert(@RequestBody @Valid AiPromptTemplateUpsertDTO dto) {
        return Result.success(promptTemplateService.upsert(dto));
    }

    /**
     * 版本回退：把指定历史版本切为生效版
     */
    @PostMapping("/activate")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<String> activate(@RequestParam Long id) {
        return Result.success(promptTemplateService.activate(id));
    }
}
