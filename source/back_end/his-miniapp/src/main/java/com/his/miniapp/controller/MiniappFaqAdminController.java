package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.FaqDeleteDTO;
import com.his.miniapp.dto.FaqPageQueryDTO;
import com.his.miniapp.dto.FaqUpsertDTO;
import com.his.miniapp.service.MiniappFaqService;
import com.his.miniapp.vo.FaqListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 常见问题后台维护（院内运营入口）。
 */
@Tag(name = "院内-常见问题维护")
@RestController
@RequestMapping("/miniapp/faq/admin")
@RequiredArgsConstructor
public class MiniappFaqAdminController {

    private final MiniappFaqService miniappFaqService;

    @Operation(summary = "常见问题列表（含停用）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('patient:faq:list')")
    public Result<PageResult<FaqListVO>> listPage(@Valid @RequestBody FaqPageQueryDTO pageQueryDTO) {
        return Result.success(miniappFaqService.adminPage(pageQueryDTO));
    }

    @Operation(summary = "常见问题详情")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('patient:faq:list')")
    public Result<FaqListVO> getById(@RequestParam Long faqId) {
        return Result.success(miniappFaqService.adminGetById(faqId));
    }

    @Operation(summary = "新增或修改常见问题")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('patient:faq:upsert')")
    public Result<String> upsert(@RequestBody @Valid FaqUpsertDTO upsertDTO) {
        return Result.success(miniappFaqService.adminUpsert(upsertDTO));
    }

    @Operation(summary = "删除常见问题")
    @PostMapping("/deleteById")
    @PreAuthorize("hasAuthority('patient:faq:delete')")
    public Result<Integer> deleteById(@RequestBody @Valid FaqDeleteDTO deleteDTO) {
        miniappFaqService.adminDelete(deleteDTO.getId());
        return Result.success(1);
    }
}
