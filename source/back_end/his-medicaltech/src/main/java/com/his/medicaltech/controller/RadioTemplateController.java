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
 * 放射报告模板（sql/138，菜单 414 下的「模板维护」按钮）。
 *
 * <p>可见性与过滤口径（个人模板只对本人可见）在 {@code RadiologyReportService} 里收口。
 */
@Tag(name = "放射报告模板")
@RestController
@RequestMapping("/medicaltech/radiology/template")
@RequiredArgsConstructor
public class RadioTemplateController {

    private final RadiologyReportService radiologyReportService;

    @Operation(summary = "模板下拉（按模态过滤；未登录员工ID时只返回公用模板）")
    @GetMapping("/selectList")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<List<RadioReportTemplateVO>> selectList(@RequestParam(required = false) Integer modality) {
        return Result.success(radiologyReportService.templateSelectList(modality));
    }

    @Operation(summary = "模板列表（维护用，含停用）")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:tplEdit')")
    public Result<List<RadioReportTemplateVO>> list() {
        return Result.success(radiologyReportService.templateList());
    }

    @Operation(summary = "新增/修改模板")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:tplEdit')")
    public Result<RadioReportTemplateVO> upsert(@Valid @RequestBody RadioTemplateUpsertDTO dto) {
        return Result.success("已保存", radiologyReportService.templateUpsert(dto));
    }

    @Operation(summary = "删除模板（物理删：uk_template_code 不含 del_flag，软删会让同编码再也建不出来）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:tplEdit')")
    public Result<Void> deleteById(@RequestParam Long id) {
        boolean ok = radiologyReportService.templateDeleteById(id);
        return ok ? Result.success("已删除", null) : Result.error("模板不存在或已被删除");
    }
}
