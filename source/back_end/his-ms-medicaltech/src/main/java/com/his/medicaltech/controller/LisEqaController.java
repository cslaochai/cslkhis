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
 * LIS 室间质评（EQA）控制器：质评批次、盲样台账、结果上报、成绩判定与不合格整改的接口入口。
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

    /**
     * 新增或修改室间质评批次（组织方、项目、上报截止与成绩回报日）。
     * 用于室间质评页「质评批次」标签的批次登记与调整。
     */
    @PreAuthorize("hasAuthority('medtech:lisEqa:add')")
    @Operation(summary = "质评批次新增/修改")
    @PostMapping("/planUpsert")
    public Result<String> planUpsert(@Valid @RequestBody LisEqaDTO.PlanUpsert dto) {
        String planNo = lisEqaService.planUpsert(dto);
        return Result.success("质评批次 " + planNo + " 已保存", planNo);
    }

    /**
     * 归档质评批次并结案；仍有未闭环的不合格整改时不允许归档。
     * 用于室间质评页批次成绩判定完成后的归档动作。
     */
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

    /**
     * 按样品序号 × 检验项目 × 仪器批量生成盲样台账，已登记的组合不重复生成。
     * 用于室间质评页收到组织方盲样后的一次性批量登记。
     */
    @PreAuthorize("hasAuthority('medtech:lisEqa:add')")
    @Operation(summary = "批量生成盲样台账（样品序号 × 项目 × 仪器）")
    @PostMapping("/sampleGenerate")
    public Result<Integer> sampleGenerate(@Valid @RequestBody LisEqaDTO.SampleGenerate dto) {
        int n = lisEqaService.sampleGenerate(dto);
        return Result.success(n == 0 ? "所选组合已全部登记，无新增" : "已新增 " + n + " 条盲样台账", n);
    }

    /**
     * 登记或修改单条盲样台账。
     * 用于室间质评页「盲样台账」标签的补录与纠正。
     */
    @PreAuthorize("hasAuthority('medtech:lisEqa:add')")
    @Operation(summary = "盲样登记新增/修改")
    @PostMapping("/sampleUpsert")
    public Result<String> sampleUpsert(@Valid @RequestBody LisEqaDTO.SampleUpsert dto) {
        String sampleNo = lisEqaService.sampleUpsert(dto);
        return Result.success("盲样 " + sampleNo + " 已登记", sampleNo);
    }

    /**
     * 删除盲样台账，仅限尚未上报的盲样。
     * 用于室间质评页盲样登记录错后的撤销。
     */
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

    /**
     * 向组织方上报本室盲样检测结果；超过上报截止只标记逾期、不拦截。
     * 用于室间质评页盲样检测完成后的上报环节。
     */
    @PreAuthorize("hasAuthority('medtech:lisEqa:edit')")
    @Operation(summary = "向组织方上报结果（逾期会记 flag，不拦截）")
    @PostMapping("/sampleReport")
    public Result<String> sampleReport(@Valid @RequestBody LisEqaDTO.ReportInput dto) {
        return Result.success(lisEqaService.report(dto), null);
    }

    /**
     * 回报组织方下发的靶值、组 SD 与 TEa，由服务端判定 PT 得分与是否合格。
     * 用于室间质评页「成绩回报与判定」标签的录入判定。
     */
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

    /**
     * 复核不合格项的整改，复核人不得是整改人本人，通过后该不合格项闭环。
     * 用于室间质评页「成绩回报与判定」标签里的整改复核。
     */
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
