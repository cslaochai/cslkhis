package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.ExamFilmQueryPageDTO;
import com.his.medicaltech.dto.ExamFilmSpecUpsertDTO;
import com.his.medicaltech.dto.ExamFilmUpsertDTO;
import com.his.medicaltech.service.ExamFilmService;
import com.his.medicaltech.vo.ExamFilmVO;
import com.his.medicaltech.vo.FilmSpecSelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检查胶片量方与发放（sql/138，菜单 415）。
 *
 * <p>岗位落点：这个页面给**检查技师**（拍片的人），不给诊断医师 ——
 * 打几张片、发给谁，是拍片这一岗的事。诊断医师那边只在报告详情里看得到
 * 「本次已打 N 张胶片」这个数字（报告单.film_count），改不了。
 */
@Tag(name = "检查胶片量方")
@RestController
@RequestMapping("/medicaltech/examFilm")
@RequiredArgsConstructor
public class ExamFilmController {

    private final ExamFilmService examFilmService;

    @Operation(summary = "胶片用量分页列表")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('medtech:examFilm:list')")
    public Result<PageResult<ExamFilmVO>> listPage(@Valid @RequestBody ExamFilmQueryPageDTO query) {
        return Result.success(examFilmService.listPage(query));
    }

    @Operation(summary = "某一次检查的全部胶片")
    @GetMapping("/listByRecordId")
    @PreAuthorize("hasAuthority('medtech:examFilm:list')")
    public Result<List<ExamFilmVO>> listByRecordId(@RequestParam Long recordId) {
        return Result.success(examFilmService.listByRecordId(recordId));
    }

    @Operation(summary = "胶片用量汇总（张数 / 金额 / 已记账金额）")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('medtech:examFilm:list')")
    public Result<ExamFilmVO.FilmStats> stats(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate) {
        return Result.success(examFilmService.stats(startDate, endDate));
    }

    @Operation(summary = "登记胶片用量（金额服务端按 单价×张数 现算）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('medtech:examFilm:add')")
    public Result<ExamFilmVO> upsert(@Valid @RequestBody ExamFilmUpsertDTO dto) {
        return Result.success("胶片用量已登记", examFilmService.upsert(dto));
    }

    @Operation(summary = "生成记账（调收费模块，幂等；同一行重复点不会记两次）")
    @PostMapping("/charge")
    @PreAuthorize("hasAuthority('medtech:examFilm:charge')")
    public Result<ExamFilmVO> charge(@RequestParam Long filmId) {
        return Result.success("已生成记账，患者可在收费台结算", examFilmService.charge(filmId));
    }

    @Operation(summary = "标记已打印")
    @PostMapping("/markPrinted")
    @PreAuthorize("hasAuthority('medtech:examFilm:deliver')")
    public Result<ExamFilmVO> markPrinted(@RequestParam Long filmId) {
        return Result.success("已标记打印", examFilmService.markPrinted(filmId));
    }

    @Operation(summary = "标记已发放（交给患者/病区）")
    @PostMapping("/deliver")
    @PreAuthorize("hasAuthority('medtech:examFilm:deliver')")
    public Result<ExamFilmVO> deliver(@RequestParam Long filmId) {
        return Result.success("已标记发放", examFilmService.deliver(filmId));
    }

    @Operation(summary = "作废胶片（已记账的不允许作废，那笔钱要走红冲）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('medtech:examFilm:delete')")
    public Result<Void> deleteById(@RequestParam Long filmId,
                                   @RequestParam(required = false) String reason) {
        boolean ok = examFilmService.deleteById(filmId, reason);
        return ok ? Result.success("已作废（操作已留痕）", null) : Result.error("作废失败");
    }

    // 规格价目

    @Operation(summary = "胶片规格下拉（带单价）")
    @GetMapping("/specSelectList")
    @PreAuthorize("hasAuthority('medtech:examFilm:list')")
    public Result<List<FilmSpecSelectListVO>> specSelectList() {
        return Result.success(examFilmService.specSelectList());
    }

    @Operation(summary = "新增/修改胶片规格价目")
    @PostMapping("/specUpsert")
    @PreAuthorize("hasAuthority('medtech:examFilm:add')")
    public Result<FilmSpecSelectListVO> specUpsert(@Valid @RequestBody ExamFilmSpecUpsertDTO dto) {
        return Result.success("规格已保存", examFilmService.upsertSpec(dto));
    }

    @Operation(summary = "删除胶片规格（已被引用过的不能删，只能停用）")
    @DeleteMapping("/specDeleteById")
    @PreAuthorize("hasAuthority('medtech:examFilm:add')")
    public Result<Void> specDeleteById(@RequestParam Long id) {
        boolean ok = examFilmService.deleteSpec(id);
        return ok ? Result.success("规格已删除", null) : Result.error("规格不存在或已被删除");
    }
}
