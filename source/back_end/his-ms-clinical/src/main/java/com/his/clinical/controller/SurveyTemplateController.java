package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.SurveyTemplatePageQueryDTO;
import com.his.clinical.dto.SurveyTemplateUpsertDTO;
import com.his.clinical.service.SurveyTemplateService;
import com.his.clinical.vo.SurveyTemplateSelectListVO;
import com.his.clinical.vo.SurveyTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 满意度问卷模板控制器：问卷与题目的增删改查及启用清单的接口入口。
 */
@Tag(name = "满意度问卷模板")
@RestController
@RequestMapping("/survey/template")
@RequiredArgsConstructor
public class SurveyTemplateController {

    private final SurveyTemplateService surveyTemplateService;

    /**
     * 问卷模板分页：按名称关键字、适用场景、启停状态筛出问卷清单。
     * 用于「满意度评价」页「问卷模板」页签的模板台账表格。
     */
    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "问卷模板分页")
    @PostMapping("/listPage")
    public Result<PageResult<SurveyTemplateVO>> listPage(@Valid @RequestBody SurveyTemplatePageQueryDTO dto) {
        return Result.success(surveyTemplateService.listPage(dto));
    }

    /**
     * 启用中的问卷下拉，按适用场景收口；场景不传表示不限场景。
     * 供问卷发放环节挑选要发哪张问卷，停用的问卷不出现在候选里。
     */
    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "启用中的问卷下拉（scene 可空=全部）")
    @GetMapping("/selectList")
    public Result<List<SurveyTemplateSelectListVO>> selectList(@RequestParam(required = false) Integer scene) {
        return Result.success(surveyTemplateService.selectEnabled(scene));
    }

    /**
     * 问卷详情：连题目清单一起返回，题目按序给出题型与是否必答。
     * 用于「问卷模板」页签的编辑弹框回显与启停开关保存前的整卷取数，答卷代填弹框也靠它渲染题目。
     */
    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "问卷详情（含题目清单）")
    @GetMapping("/getDetailById")
    public Result<SurveyTemplateVO> getDetailById(@RequestParam Long id) {
        return Result.success(surveyTemplateService.getDetailById(id));
    }

    /**
     * 新建或修改问卷，题目清单整卷覆盖（至少一题）；只改启停也要回带完整题目，否则题目会被清空。
     * 用于「问卷模板」页签的编辑弹框保存，以及台账上的启用/停用开关。
     */
    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "新增 / 修改问卷（整卷覆盖题目）")
    @PostMapping("/templateUpsert")
    public Result<SurveyTemplateVO> templateUpsert(@Valid @RequestBody SurveyTemplateUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "问卷已创建" : "问卷已保存", surveyTemplateService.upsert(dto));
    }

    /**
     * 删除问卷，已被发放记录引用的拒绝删除（历史答卷要能追溯到题目，应改停用）。
     * 用于「问卷模板」页签操作列的删除。
     */
    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "删除问卷（已被发放引用时拒绝，请改停用）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long id) {
        return Result.success("问卷已删除", surveyTemplateService.deleteById(id));
    }
}
