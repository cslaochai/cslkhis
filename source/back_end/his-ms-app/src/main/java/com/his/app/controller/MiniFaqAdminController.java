package com.his.app.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.app.dto.FaqDeleteDTO;
import com.his.app.dto.FaqPageQueryDTO;
import com.his.app.dto.FaqUpsertDTO;
import com.his.app.service.MiniFaqService;
import com.his.app.vo.MiniFaqListVO;
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
public class MiniFaqAdminController {

    private final MiniFaqService miniFaqService;

    /**
     * 常见问题分页：院内维护视角，停用条目同样在列。
     * 用于院内「患者常见问题」管理页的列表查询卡。
     */
    @Operation(summary = "常见问题列表（含停用）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('patient:faq:list')")
    public Result<PageResult<MiniFaqListVO>> listPage(@Valid @RequestBody FaqPageQueryDTO pageQueryDTO) {
        return Result.success(miniFaqService.adminPage(pageQueryDTO));
    }

    /**
     * 常见问题单条详情：取一条问答的完整正文，供编辑前载入。
     * 用于院内「患者常见问题」页编辑弹框的表单回显。
     */
    @Operation(summary = "常见问题详情")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('patient:faq:list')")
    public Result<MiniFaqListVO> getById(@RequestParam Long faqId) {
        return Result.success(miniFaqService.adminGetById(faqId));
    }

    /**
     * 常见问题新增或修改：带 ID 走更新、不带 ID 走新建。
     * 用于院内「患者常见问题」页新增与编辑弹框的保存动作。
     */
    @Operation(summary = "新增或修改常见问题")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('patient:faq:upsert')")
    public Result<String> upsert(@RequestBody @Valid FaqUpsertDTO upsertDTO) {
        return Result.success(miniFaqService.adminUpsert(upsertDTO));
    }

    /**
     * 常见问题删除：按 ID 移除一条问答。
     * 用于院内「患者常见问题」页列表行的删除操作。
     */
    @Operation(summary = "删除常见问题")
    @PostMapping("/deleteById")
    @PreAuthorize("hasAuthority('patient:faq:delete')")
    public Result<Integer> deleteById(@RequestBody @Valid FaqDeleteDTO deleteDTO) {
        miniFaqService.adminDelete(deleteDTO.getId());
        return Result.success(1);
    }
}
