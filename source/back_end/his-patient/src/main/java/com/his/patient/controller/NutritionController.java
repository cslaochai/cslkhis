package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.enums.ConsultCategoryEnum;
import com.his.patient.service.InpatientConsultationService;
import com.his.patient.service.NutritionScreenService;
import com.his.patient.service.NutritionStatsService;
import com.his.patient.vo.ConsultationVO;
import com.his.patient.vo.NutritionOverviewVO;
import com.his.patient.vo.NutritionScreenVO;
import com.his.patient.vo.NutritionStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营养风险筛查 / 营养会诊 / 营养指标监测（sql/168，菜单 424、427、428）。
 */
@Tag(name = "营养风险筛查与会诊")
@RestController
@RequestMapping("/patient/inpatient/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionScreenService nutritionScreenService;
    private final NutritionStatsService nutritionStatsService;
    private final InpatientConsultationService inpatientConsultationService;

    // 筛查

    @Operation(summary = "营养膳食看板（在院未筛 / 有风险 / 到期复筛 / 今日订餐进度 / 未完成会诊）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @GetMapping("/overview")
    public Result<NutritionOverviewVO> overview() {
        return Result.success(nutritionStatsService.overview());
    }

    @Operation(summary = "筛查评定分页（dueOnly=1 只看到期未复筛）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @PostMapping("/screenListPage")
    public Result<PageResult<NutritionScreenVO>> screenListPage(@Valid @RequestBody NutritionScreenQueryPageDTO query) {
        return Result.success(nutritionScreenService.screenListPage(query));
    }

    @Operation(summary = "某次住院的筛查历史（按筛查时间倒序）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @GetMapping("/screenListByAdmission")
    public Result<List<NutritionScreenVO>> screenListByAdmission(@RequestParam Long admissionId) {
        return Result.success(nutritionScreenService.screenListByAdmission(admissionId));
    }

    @Operation(summary = "登记/修改筛查评定（总分、风险判定、BMI、复筛日期全部服务端算）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screenEdit')")
    @PostMapping("/screenUpsert")
    public Result<NutritionScreenVO> screenUpsert(@Valid @RequestBody NutritionScreenUpsertDTO dto) {
        return Result.success("筛查记录已保存", nutritionScreenService.screenUpsert(dto));
    }

    @Operation(summary = "删除筛查记录（仅限误录）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screenDelete')")
    @DeleteMapping("/screenDeleteById")
    public Result<Integer> screenDeleteById(@RequestParam Long id) {
        return Result.success("已删除", nutritionScreenService.screenDeleteById(id));
    }

    // 营养会诊（复用住院会诊闭环，类别钉死为营养）

    @Operation(summary = "营养会诊分页（只给 consultCategory=2，不接受前端改类别）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consult')")
    @PostMapping("/consultListPage")
    public Result<PageResult<ConsultationVO>> consultListPage(@Valid @RequestBody ConsultationQueryPageDTO query) {
        query.setConsultCategory(ConsultCategoryEnum.NUTRITION.getCode());
        IPage<ConsultationVO> page = inpatientConsultationService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "营养会诊详情")
    @PreAuthorize("hasAuthority('ipd:nutrition:consult')")
    @GetMapping("/consultGetById")
    public Result<ConsultationVO> consultGetById(@RequestParam Long consultationId) {
        return Result.success(inpatientConsultationService.getDetailById(consultationId));
    }

    @Operation(summary = "发起营养会诊（筛查阳性患者一键发起；类别服务端写死为 2）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultApply')")
    @PostMapping("/consultApply")
    public Result<String> consultApply(@RequestBody @Valid ConsultationUpsertDTO dto) {
        dto.setId(null);
        dto.setConsultCategory(ConsultCategoryEnum.NUTRITION.getCode());
        return Result.success("营养会诊已申请", inpatientConsultationService.save(dto));
    }

    @Operation(summary = "应答营养会诊（接诊人 = 当前登录用户）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultAccept")
    public Result<Void> consultAccept(@RequestBody @Valid ConsultationAcceptDTO dto) {
        inpatientConsultationService.accept(dto);
        return Result.success("已接诊", null);
    }

    @Operation(summary = "完成营养会诊（必须带结论，完成即回写住院病历）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultFinish")
    public Result<String> consultFinish(@RequestBody @Valid ConsultationFinishDTO dto) {
        return Result.success("会诊已完成并回写病历", inpatientConsultationService.finish(dto));
    }

    @Operation(summary = "取消营养会诊（必填原因）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultCancel")
    public Result<Void> consultCancel(@RequestBody @Valid ConsultationCancelDTO dto) {
        inpatientConsultationService.cancel(dto);
        return Result.success("已取消", null);
    }

    // 月度指标

    @Operation(summary = "实时试算（不落库；报数以快照为准）")
    @PreAuthorize("hasAuthority('ipd:nutrition:stats')")
    @GetMapping("/previewStats")
    public Result<NutritionStatsVO> previewStats(@RequestParam String statMonth) {
        return Result.success(nutritionStatsService.previewStats(statMonth));
    }

    @Operation(summary = "生成/重算月度营养指标快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('ipd:nutrition:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<NutritionStatsVO>> generateStats(@Valid @RequestBody NutritionStatsGenerateDTO dto) {
        List<NutritionStatsVO> rows = nutritionStatsService.generateStats(dto);
        return Result.success("已生成 " + rows.size() + " 条快照", rows);
    }

    @Operation(summary = "已生成的营养指标分页")
    @PreAuthorize("hasAuthority('ipd:nutrition:stats')")
    @PostMapping("/statsListPage")
    public Result<PageResult<NutritionStatsVO>> statsListPage(@Valid @RequestBody NutritionStatsQueryPageDTO query) {
        return Result.success(nutritionStatsService.statsListPage(query));
    }

    @Operation(summary = "导出营养指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('ipd:nutrition:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@Valid @RequestBody NutritionStatsQueryPageDTO query) {
        return Result.success(nutritionStatsService.statsExportCsv(query));
    }
}
