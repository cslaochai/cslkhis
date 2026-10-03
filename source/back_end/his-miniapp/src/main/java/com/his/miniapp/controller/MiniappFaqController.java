package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.FaqSearchDTO;
import com.his.miniapp.service.MiniappFaqService;
import com.his.miniapp.vo.FaqCategoryVO;
import com.his.miniapp.vo.FaqListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端常见问题（客服台自助入口）。
 *
 * <p>答案是人工维护的固定文本，不走模型 —— 患者问「门诊几点上班」，
 * 需要一个确定且可追溯的答案，不是一段听起来合理的生成文本。
 */
@Tag(name = "患者端-常见问题")
@RestController
@RequestMapping("/miniapp/faq")
@RequiredArgsConstructor
public class MiniappFaqController {

    private final MiniappFaqService faqService;

    @Operation(summary = "常见问题分类（带条数）")
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<FaqCategoryVO>> categories() {
        return Result.success(faqService.categories());
    }

    @Operation(summary = "常见问题检索（关键词切词匹配 + 分类过滤）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PageResult<FaqListVO>> listPage(@RequestBody @Valid FaqSearchDTO dto) {
        return Result.success(faqService.search(dto));
    }

    @Operation(summary = "常见问题详情（累计查看次数）")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<FaqListVO> getById(@RequestParam String faqId) {
        return Result.success(faqService.getById(parseId(faqId)));
    }

    @Operation(summary = "热门问题（客服页首屏）")
    @GetMapping("/hotList")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<FaqListVO>> hotList(@RequestParam(required = false) Integer limit) {
        return Result.success(faqService.hotList(limit));
    }

    @Operation(summary = "有用反馈（helpful=1 有帮助，0 没帮助）")
    @PostMapping("/feedback")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> feedback(@RequestBody @Valid FeedbackDTO dto) {
        return Result.success(faqService.feedback(parseId(dto.getFaqId()), dto.getHelpful()));
    }

    /**
     * 雪花ID 只按字符串收：前端若按 JSON 数字传，超过 2^53 就开始丢精度，
     * 现象是「点某条 FAQ 详情返回不存在」，而且没有任何报错。
     */
    private static Long parseId(String value) {
        if (!StringUtils.hasText(value) || !value.matches("\\d{1,20}")) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Data
    public static class FeedbackDTO {
        @NotBlank(message = "faqId不能为空")
        private String faqId;

        /** 1-有帮助 0-没帮助 */
        private Integer helpful;
    }
}
