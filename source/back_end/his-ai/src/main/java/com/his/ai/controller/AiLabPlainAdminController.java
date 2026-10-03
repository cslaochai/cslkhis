package com.his.ai.controller;

import com.his.ai.dto.LabPlainItemSearchDTO;
import com.his.ai.dto.LabPlainItemUpsertDTO;
import com.his.ai.service.LabPlainItemAdminService;
import com.his.ai.vo.LabPlainCoverageVO;
import com.his.ai.vo.LabPlainItemAdminVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 检验项目白话词典 · 院内维护入口。
 *
 * <p><b>为什么必须配这个入口</b>：患者端报告解读的每一句白话都来自这张表。
 * 它建完不维护就会烂 —— 检验科一加新项目，患者端就多一个只有数值、没有解释的条目，
 * 而运营没有任何途径知道该补哪一条。{@code /coverage} 就是解决这个的：
 * 直接列出「库里出现过、但词典没配」的项目名，按出现次数倒序。
 *
 * <p>落 his-ai 而不是业务模块：表属于 AI 的规则层地基，实体与 Mapper 都在这里，
 * 挪到别处会让 his-ai 反过来依赖业务模块（依赖方向不允许）。
 */
@Tag(name = "院内-检验项目白话词典维护")
@RestController
@RequestMapping("/ai/admin/labPlain")
@RequiredArgsConstructor
public class AiLabPlainAdminController {

    private final LabPlainItemAdminService labPlainItemAdminService;

    @Operation(summary = "词典列表（含停用）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('lab:plain:list')")
    public Result<PageResult<LabPlainItemAdminVO>> listPage(@RequestBody LabPlainItemSearchDTO dto) {
        return Result.success(labPlainItemAdminService.adminPage(dto == null ? new LabPlainItemSearchDTO() : dto));
    }

    @Operation(summary = "词典详情")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('lab:plain:list')")
    public Result<LabPlainItemAdminVO> getById(@RequestParam String id) {
        return Result.success(labPlainItemAdminService.adminGetById(parseId(id)));
    }

    @Operation(summary = "覆盖率自检：库内出现过的检验项目还有哪些没配白话")
    @PostMapping("/coverage")
    @PreAuthorize("hasAuthority('lab:plain:list')")
    public Result<LabPlainCoverageVO> coverage() {
        return Result.success(labPlainItemAdminService.coverage());
    }

    @Operation(summary = "新增或修改词条")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('lab:plain:upsert')")
    public Result<String> upsert(@RequestBody @Valid LabPlainItemUpsertDTO dto) {
        return Result.success(labPlainItemAdminService.adminUpsert(dto));
    }

    @Operation(summary = "删除词条（物理删，uk_item_name 唯一键不含 del_flag）")
    @PostMapping("/deleteById")
    @PreAuthorize("hasAuthority('lab:plain:delete')")
    public Result<Integer> deleteById(@RequestBody @Valid IdDTO dto) {
        labPlainItemAdminService.adminDelete(parseId(dto.getId()));
        return Result.success(1);
    }

    private static Long parseId(String value) {
        if (!StringUtils.hasText(value) || !value.matches("\\d{1,20}")) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Data
    public static class IdDTO {
        @NotBlank(message = "id不能为空")
        private String id;
    }
}
