package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.FaqDeleteDTO;
import com.his.miniapp.dto.FaqPageQueryDTO;
import com.his.miniapp.dto.FaqUpsertDTO;
import com.his.miniapp.service.MiniappFaqService;
import com.his.miniapp.vo.FaqAdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 常见问题后台维护（院内运营入口）。
 *
 * <p><b>为什么必须配这个入口</b>：语料是人工维护的，患者端每点一次「没帮助」都是在告诉运营
 * 「这条答案该改了」。没有维护页，改一条答案要找 DBA 写 SQL ——
 * 那样 FAQ 表三个月就会变成没人敢碰、也没人更新的死数据，
 * 患者端客服台也就跟着一起烂掉。
 *
 * <p>与患者端接口分开是为了鉴权分开：患者端只认 PATIENT，这里只认院内权限码。
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
    public Result<PageResult<FaqAdminVO>> listPage(@Valid @RequestBody FaqPageQueryDTO pageQueryDTO) {
        return Result.success(miniappFaqService.adminPage(pageQueryDTO));
    }

    @Operation(summary = "常见问题详情")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('patient:faq:list')")
    public Result<FaqAdminVO> getById(@RequestParam Long faqId) {
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
