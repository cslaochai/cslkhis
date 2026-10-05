package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.AiDraftDiffQueryPageDTO;
import com.his.emr.service.AiDraftDiffService;
import com.his.emr.vo.AiDraftDiffListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 病历草稿 AI 留痕查询（AI 管理台「草稿留痕」签页）。
 *
 * <p>表归病历域（写入方是病历保存流），查询接口跟着表走，不进 his-ai；
 * 权限码沿用 AI 管理台的 ai:admin:list，不新增菜单。
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
    public Result<PageResult<AiDraftDiffListVO>> listPage(@RequestBody(required = false) AiDraftDiffQueryPageDTO queryDTO) {
        return Result.success(aiDraftDiffService.listPage(queryDTO));
    }
}
