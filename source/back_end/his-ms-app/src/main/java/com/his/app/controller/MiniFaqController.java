package com.his.app.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.app.dto.FaqFeedbackDTO;
import com.his.app.dto.FaqPageQueryDTO;
import com.his.app.service.MiniFaqService;
import com.his.app.vo.MiniFaqCategoryListVO;
import com.his.app.vo.MiniFaqListVO;
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

    /**
     * 常见问题分类：患者端可见的分类及其启用条数。
     * 用于小程序「客服中心」页的分类导航条。
     */
    @Operation(summary = "常见问题分类（带条数）")
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<MiniFaqCategoryListVO>> categories() {
        return Result.success(miniFaqService.categories());
    }

    /**
     * 常见问题检索：关键词切词命中答案正文，可按分类过滤后分页返回。
     * 用于小程序「客服中心」页的分类翻页与搜索框结果列表。
     */
    @Operation(summary = "常见问题检索（关键词切词匹配 + 分类过滤）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PageResult<MiniFaqListVO>> listPage(@RequestBody @Valid FaqPageQueryDTO dto) {
        return Result.success(miniFaqService.search(dto));
    }

    /**
     * 常见问题详情：取一条问答全文，同时累计该条的查看次数。
     * 用于小程序「客服中心」页展开某条问题的答案时调用。
     */
    @Operation(summary = "常见问题详情（累计查看次数）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<MiniFaqListVO> getById(@RequestParam Long faqId) {
        return Result.success(miniFaqService.getById(faqId));
    }

    /**
     * 热门问题：按查看次数排序的问答条目，条数上限可空（由服务端给默认值）。
     * 用于小程序「客服中心」页首屏的推荐问题清单。
     */
    @Operation(summary = "热门问题（客服页首屏）")
    @GetMapping("/hotList")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<MiniFaqListVO>> hotList(@RequestParam(required = false) Integer limit) {
        return Result.success(miniFaqService.hotList(limit));
    }

    /**
     * 常见问题有用反馈：记录患者对某条答案「有帮助 / 没帮助」的投票。
     * 用于小程序「客服中心」页答案卡片下方的反馈按钮。
     */
    @Operation(summary = "有用反馈（helpful=1 有帮助，0 没帮助）")
    @PostMapping("/feedback")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> feedback(@RequestBody @Valid FaqFeedbackDTO dto) {
        return Result.success(miniFaqService.feedback(dto.getFaqId(), dto.getHelpful()));
    }
}
