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

    /**
     * 启用状态的器械包模板下拉，停用的模板不出候选。
     * 用于回收登记弹框选模板带出灭菌方式与组成清单，也用于消毒供应页与模板页的筛选项。
     */
    @Operation(summary = "模板下拉（仅启用）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<java.util.List<CssdPackTemplateSelectListVO>> selectList() {
        return Result.success(cssdTemplateService.selectList());
    }

    /**
     * 器械包模板目录分页。
     * 用于「器械包模板」页的查询卡与模板表格。
     */
    @Operation(summary = "模板分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<CssdPackTemplateVO>> listPage(@Valid @RequestBody CssdDTO.TemplatePageQuery dto) {
        var page = cssdTemplateService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 按模板ID取模板档案及其器械组成明细（名称、规格、数量）。
     * 用于模板编辑弹框的回显，以及回收登记选中模板后带出默认灭菌方式与清单。
     */
    @Operation(summary = "模板详情（含组成明细）")
    @GetMapping("/getDetailById")
    public Result<CssdPackTemplateVO> getDetailById(@RequestParam Long templateId) {
        return Result.success(cssdTemplateService.getDetailById(templateId));
    }

    /**
     * 器械名称下拉：把已启用模板的组成明细去重汇总，附带规格与单位。
     * 用于模板编辑弹框添加组成行时选器械。
     */
    @Operation(summary = "器械名称下拉（启用模板明细去重汇总，带规格单位）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/item/selectList")
    public Result<List<CssdPackTemplateItemSelectListVO>> itemSelectList() {
        return Result.success(cssdTemplateService.itemSelectList());
    }

    /**
     * 新增或修改器械包模板（模板编码全表查重；组成明细整组替换，顺序即保存顺序）。
     * 用于「器械包模板」页的新增/修改弹框保存。
     */
    @Operation(summary = "模板新增/修改（明细整删重插）")
    @PostMapping("/templateUpsert")
    public Result<CssdPackTemplateVO> templateUpsert(@Valid @RequestBody CssdDTO.TemplateUpsert dto) {
        return Result.success("保存成功", cssdTemplateService.upsert(dto));
    }

    /**
     * 停用并删除一条器械包模板：历史器械包已记录的追溯链不受影响，只让模板不再出现在下拉里。
     * 用于「器械包模板」页行内删除确认。
     */
    @Operation(summary = "模板删除（软删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long templateId) {
        cssdTemplateService.deleteById(templateId);
        return Result.success("已删除", null);
    }
}
