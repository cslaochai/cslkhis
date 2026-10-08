package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.FaqFeedbackDTO;
import com.his.miniapp.dto.FaqPageQueryDTO;
import com.his.miniapp.service.MiniFaqService;
import com.his.miniapp.vo.MiniFaqCategoryListVO;
import com.his.miniapp.vo.MiniFaqListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端常见问题（客服台自助入口）。
 */
@Tag(name = "患者端-常见问题")
@RestController
@RequestMapping("/miniapp/faq")
@RequiredArgsConstructor
public class MiniFaqController {

    private final MiniFaqService miniFaqService;

    @Operation(summary = "常见问题分类（带条数）")
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<MiniFaqCategoryListVO>> categories() {
        return Result.success(miniFaqService.categories());
    }

    @Operation(summary = "常见问题检索（关键词切词匹配 + 分类过滤）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PageResult<MiniFaqListVO>> listPage(@RequestBody @Valid FaqPageQueryDTO dto) {
        return Result.success(miniFaqService.search(dto));
    }

    @Operation(summary = "常见问题详情（累计查看次数）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<MiniFaqListVO> getById(@RequestParam Long faqId) {
        return Result.success(miniFaqService.getById(faqId));
    }

    @Operation(summary = "热门问题（客服页首屏）")
    @GetMapping("/hotList")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<MiniFaqListVO>> hotList(@RequestParam(required = false) Integer limit) {
        return Result.success(miniFaqService.hotList(limit));
    }

    @Operation(summary = "有用反馈（helpful=1 有帮助，0 没帮助）")
    @PostMapping("/feedback")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> feedback(@RequestBody @Valid FaqFeedbackDTO dto) {
        return Result.success(miniFaqService.feedback(dto.getFaqId(), dto.getHelpful()));
    }
}
