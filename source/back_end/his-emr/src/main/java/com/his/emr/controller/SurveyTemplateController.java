package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.SurveyTemplateQueryPageDTO;
import com.his.emr.dto.SurveyTemplateUpsertDTO;
import com.his.emr.service.SurveyTemplateService;
import com.his.emr.vo.SurveyTemplateSelectListVO;
import com.his.emr.vo.SurveyTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 满意度问卷模板
 */
@Tag(name = "满意度问卷模板")
@RestController
@RequestMapping("/survey/template")
@RequiredArgsConstructor
public class SurveyTemplateController {

    private final SurveyTemplateService surveyTemplateService;

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "问卷模板分页")
    @PostMapping("/listPage")
    public Result<PageResult<SurveyTemplateVO>> listPage(@Valid @RequestBody SurveyTemplateQueryPageDTO dto) {
        return Result.success(surveyTemplateService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "启用中的问卷下拉（scene 可空=全部）")
    @GetMapping("/selectList")
    public Result<List<SurveyTemplateSelectListVO>> selectList(@RequestParam(required = false) Integer scene) {
        return Result.success(surveyTemplateService.selectEnabled(scene));
    }

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "问卷详情（含题目清单）")
    @GetMapping("/getDetailById")
    public Result<SurveyTemplateVO> getDetailById(@RequestParam Long id) {
        return Result.success(surveyTemplateService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "新增 / 修改问卷（整卷覆盖题目）")
    @PostMapping("/templateUpsert")
    public Result<SurveyTemplateVO> templateUpsert(@Valid @RequestBody SurveyTemplateUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "问卷已创建" : "问卷已保存", surveyTemplateService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "删除问卷（已被发放引用时拒绝，请改停用）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long id) {
        return Result.success("问卷已删除", surveyTemplateService.deleteById(id));
    }
}
