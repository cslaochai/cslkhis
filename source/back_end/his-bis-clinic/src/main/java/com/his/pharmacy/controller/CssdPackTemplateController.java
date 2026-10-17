package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.service.CssdTemplateService;
import com.his.pharmacy.vo.CssdPackTemplateItemSelectListVO;
import com.his.pharmacy.vo.CssdPackTemplateSelectListVO;
import com.his.pharmacy.vo.CssdPackTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CSSD 器械包模板目录控制器。
 */
@Tag(name = "CSSD器械包模板目录")
@RestController
@RequestMapping("/cssd/template")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:cssd:list')")
public class CssdPackTemplateController {

    private final CssdTemplateService cssdTemplateService;

    @Operation(summary = "模板下拉（仅启用）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<java.util.List<CssdPackTemplateSelectListVO>> selectList() {
        return Result.success(cssdTemplateService.selectList());
    }

    @Operation(summary = "模板分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<CssdPackTemplateVO>> listPage(@Valid @RequestBody CssdDTO.TemplateQueryPage dto) {
        var page = cssdTemplateService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "模板详情（含组成明细）")
    @GetMapping("/getDetailById")
    public Result<CssdPackTemplateVO> getDetailById(@RequestParam Long templateId) {
        return Result.success(cssdTemplateService.getDetailById(templateId));
    }

    @Operation(summary = "器械名称下拉（启用模板明细去重汇总，带规格单位）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/item/selectList")
    public Result<List<CssdPackTemplateItemSelectListVO>> itemSelectList() {
        return Result.success(cssdTemplateService.itemSelectList());
    }

    @Operation(summary = "模板新增/修改（明细整删重插）")
    @PostMapping("/templateUpsert")
    public Result<CssdPackTemplateVO> templateUpsert(@Valid @RequestBody CssdDTO.TemplateUpsert dto) {
        return Result.success("保存成功", cssdTemplateService.upsert(dto));
    }

    @Operation(summary = "模板删除（软删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long templateId) {
        cssdTemplateService.deleteById(templateId);
        return Result.success("已删除", null);
    }
}
