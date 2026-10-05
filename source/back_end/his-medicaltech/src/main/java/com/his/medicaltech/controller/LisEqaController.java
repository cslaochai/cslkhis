package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.LisEqaDTO;
import com.his.medicaltech.service.LisEqaService;
import com.his.medicaltech.vo.LisEqaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * LIS 室间质评（EQA）接口（URL 前缀 /medicaltech/lisEqa）
 *
 * <p>一句话区别于室内质控（/medicaltech/lisQc）：室内质控的靶值是本室自己定的，
 * EQA 的靶值要从组织方回报回来才有 —— 所以判定口发生在「成绩回报」这一步，而不是检测录入。
 *
 * <p>SDI / 偏倚 / PT 得分 / 互差全部服务端算好落库，前端只显示。
 * 每个方法单独标 @PreAuthorize（类级注解会静默覆盖所有没写注解的方法，G5b 已踩过）。
 */
@Tag(name = "LIS室间质评EQA")
@RestController
@RequestMapping("/medicaltech/lisEqa")
@RequiredArgsConstructor
public class LisEqaController {

    private final LisEqaService lisEqaService;

    // 批次

    @PreAuthorize("hasAuthority('medtech:lisEqa:list')")
    @Operation(summary = "质评批次分页")
    @PostMapping("/planListPage")
    public Result<PageResult<LisEqaVO.PlanVO>> planListPage(@Valid @RequestBody LisEqaDTO.PlanQuery query) {
        return Result.success(lisEqaService.planPage(query));
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:add')")
    @Operation(summary = "质评批次新增/修改")
    @PostMapping("/planUpsert")
    public Result<String> planUpsert(@Valid @RequestBody LisEqaDTO.PlanUpsert dto) {
        String planNo = lisEqaService.planUpsert(dto);
        return Result.success("质评批次 " + planNo + " 已保存", planNo);
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "质评批次归档（不合格项必须整改并复核完）")
    @PostMapping("/planArchive")
    public Result<Void> planArchive(@Valid @RequestBody LisEqaDTO.PlanArchive dto) {
        lisEqaService.archive(dto);
        return Result.success("批次已归档，本次质评结案", null);
    }

    // 盲样台账

    @PreAuthorize("hasAuthority('medtech:lisEqa:list')")
    @Operation(summary = "盲样台账分页")
    @PostMapping("/sampleListPage")
    public Result<PageResult<LisEqaVO.SampleVO>> sampleListPage(@Valid @RequestBody LisEqaDTO.SampleQuery query) {
        return Result.success(lisEqaService.samplePage(query));
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:add')")
    @Operation(summary = "批量生成盲样台账（样品序号 × 项目 × 仪器）")
    @PostMapping("/sampleGenerate")
    public Result<Integer> sampleGenerate(@Valid @RequestBody LisEqaDTO.SampleGenerate dto) {
        int n = lisEqaService.sampleGenerate(dto);
        return Result.success(n == 0 ? "所选组合已全部登记，无新增" : "已新增 " + n + " 条盲样台账", n);
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:add')")
    @Operation(summary = "盲样登记新增/修改")
    @PostMapping("/sampleUpsert")
    public Result<String> sampleUpsert(@Valid @RequestBody LisEqaDTO.SampleUpsert dto) {
        String sampleNo = lisEqaService.sampleUpsert(dto);
        return Result.success("盲样 " + sampleNo + " 已登记", sampleNo);
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:delete')")
    @Operation(summary = "删除盲样台账（未上报才能删，物理删）")
    @DeleteMapping("/sampleDeleteById")
    public Result<Void> sampleDeleteById(@RequestParam Long sampleId) {
        lisEqaService.sampleDeleteById(sampleId);
        return Result.success("盲样台账已删除", null);
    }

    // 检测 / 上报 / 回报

    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "录入本室检测结果")
    @PostMapping("/sampleTest")
    public Result<LisEqaVO.SampleVO> sampleTest(@Valid @RequestBody LisEqaDTO.TestInput dto) {
        return Result.success("检测结果已录入", lisEqaService.test(dto));
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "向组织方上报结果（逾期会记 flag，不拦截）")
    @PostMapping("/sampleReport")
    public Result<String> sampleReport(@Valid @RequestBody LisEqaDTO.ReportInput dto) {
        return Result.success(lisEqaService.report(dto), null);
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "成绩回报（靶值/SD/TEa）并服务端判定")
    @PostMapping("/returnScore")
    public Result<LisEqaVO.JudgeVO> returnScore(@Valid @RequestBody LisEqaDTO.ScoreReturn dto) {
        LisEqaVO.JudgeVO vo = lisEqaService.returnScore(dto);
        return Result.success("PT 得分 " + (vo.getPtScore() == null ? "—" : vo.getPtScore() + "%")
                + "，本次" + vo.getPassFlagText(), vo);
    }

    // 室间差

    @PreAuthorize("hasAuthority('medtech:lisEqa:list')")
    @Operation(summary = "仪器间比对（室间差）分页")
    @PostMapping("/compareListPage")
    public Result<PageResult<LisEqaVO.CompareVO>> compareListPage(@Valid @RequestBody LisEqaDTO.CompareQuery query) {
        return Result.success(lisEqaService.comparePage(query));
    }

    // 不合格整改

    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "不合格整改（原因 + 纠正措施）")
    @PostMapping("/rectify")
    public Result<Void> rectify(@Valid @RequestBody LisEqaDTO.Rectify dto) {
        lisEqaService.rectify(dto);
        return Result.success("已整改，待第二人复核", null);
    }

    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "整改复核（复核人不得是整改人本人）")
    @PostMapping("/rectifyReview")
    public Result<Void> rectifyReview(@Valid @RequestBody LisEqaDTO.RectifyReview dto) {
        lisEqaService.rectifyReview(dto);
        return Result.success("复核通过，不合格项已闭环", null);
    }

    // 统计

    @PreAuthorize("hasAuthority('medtech:lisEqa:list')")
    @Operation(summary = "室间质评概览统计")
    @GetMapping("/stats")
    public Result<LisEqaVO.StatsVO> stats() {
        return Result.success(lisEqaService.stats());
    }
}
