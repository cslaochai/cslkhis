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

import java.util.List;

/**
 * 检验项目白话词典 · 院内维护入口。
 *
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

    @Operation(summary = "现有分组清单（筛选下拉）")
    @GetMapping("/groupNameSelectList")
    @PreAuthorize("hasAuthority('lab:plain:list')")
    public Result<List<String>> groupNameSelectList() {
        return Result.success(labPlainItemAdminService.selectGroupNames());
    }

    @Operation(summary = "词典详情")
    @GetMapping("/getById")
    @PreAuthorize("hasAuthority('lab:plain:list')")
    public Result<LabPlainItemAdminVO> getById(@RequestParam String id) {
        return Result.success(labPlainItemAdminService.adminGetById(parseId(id)));
    }

    @Operation(summary = "覆盖率自检：库内出现过的检验项目还有哪些没配白话（前端维护页已不展示，供巡检/脚本直接取）")
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
