package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.dto.RadioTemplateUpsertDTO;
import com.his.medicaltech.service.RadiologyReportService;
import com.his.medicaltech.vo.RadioReportTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 放射报告模板控制器：诊断报告模板的下拉候选与模板库维护的接口入口。
 */
@Tag(name = "放射报告模板")
@RestController
@RequestMapping("/medicaltech/radiology/template")
@RequiredArgsConstructor
public class RadioTemplateController {

    private final RadiologyReportService radiologyReportService;

    /**
     * 取书写时可套用的模板候选：按检查模态收窄，公用与个人模板随登录身份区分。
     * 报告书写台弹框打开时加载模板下拉。
     */
    @Operation(summary = "模板下拉（按模态过滤；未登录员工ID时只返回公用模板）")
    @GetMapping("/selectList")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<List<RadioReportTemplateVO>> selectList(@RequestParam(required = false) Integer modality) {
        return Result.success(radiologyReportService.templateSelectList(modality));
    }

    /**
     * 清点模板库现状：含停用中的模板，供维护视角全量查看。
     * 放射报告模板维护环节使用。
     */
    @Operation(summary = "模板列表（维护用，含停用）")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:tplEdit')")
    public Result<List<RadioReportTemplateVO>> list() {
        return Result.success(radiologyReportService.templateList());
    }

    /**
     * 新建或覆盖模板：不带ID为新增，带ID为修改。
     * 放射报告模板维护环节保存模板正文与适用范围时调用。
     */
    @Operation(summary = "新增/修改模板")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:tplEdit')")
    public Result<RadioReportTemplateVO> upsert(@Valid @RequestBody RadioTemplateUpsertDTO dto) {
        return Result.success("已保存", radiologyReportService.templateUpsert(dto));
    }

    /**
     * 按ID移除模板：连行物理删除，腾出的模板编码可再次启用。
     * 放射报告模板维护环节下架作废模板时调用。
     */
    @Operation(summary = "删除模板（物理删：uk_template_code 不含 del_flag，软删会让同编码再也建不出来）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:tplEdit')")
    public Result<Void> deleteById(@RequestParam Long id) {
        boolean ok = radiologyReportService.templateDeleteById(id);
        return ok ? Result.success("已删除", null) : Result.error("模板不存在或已被删除");
    }
}
