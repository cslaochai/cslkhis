package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.AiDraftDiffQueryPageDTO;
import com.his.emr.service.AiDraftDiffService;
import com.his.emr.vo.AiDraftDiffListVO;
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
 * 病历草稿 AI 留痕查询（AI 管理台「草稿留痕」签页）。
 */
@Tag(name = "AI草稿留痕")
@RestController
@RequestMapping("/emr/draftDiff")
@RequiredArgsConstructor
public class AiDraftDiffController {

    private final AiDraftDiffService aiDraftDiffService;

    @Operation(summary = "草稿留痕分页（AI 管理台）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('ai:admin:list')")
    public Result<PageResult<AiDraftDiffListVO>> listPage(@Valid @RequestBody AiDraftDiffQueryPageDTO queryDTO) {
        return Result.success(aiDraftDiffService.listPage(queryDTO));
    }
}
