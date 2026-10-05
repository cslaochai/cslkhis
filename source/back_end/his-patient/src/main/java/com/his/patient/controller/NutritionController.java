package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.InpatientConsultationService;
import com.his.patient.service.NutritionScreenService;
import com.his.patient.service.NutritionStatsService;
import com.his.patient.support.ConsultationLabels;
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
 *
 * <p>权限：筛查页读 {@code ipd:nutrition:screen}，登记 {@code :screenEdit}、删除 {@code :screenDelete}；
 * 会诊页读 {@code ipd:nutrition:consult}，发起 {@code :consultApply}、应答/完成/取消 {@code :consultEdit}；
 * 指标页读 {@code ipd:nutrition:stats}，生成 {@code :statGenerate}、导出 {@code :statExport}。
 *
 * <p><b>为什么营养会诊要在这里另开一套接口</b>：闭环（申请→应答→完成/取消）确实是复用
 * {@link InpatientConsultationService}，但它挂在 {@code ipd:consultation:*} 上，营养师岗位没有这些码 ——
 * 直接让营养会诊页调老接口，营养师一进页面就 403。这里用营养自己的按钮码包一层，
 * 并把 {@code consultCategory} 钉死为 2：类别决定这单在哪个工作台出现，不能让请求体自选。
 *
 * <p>总分与"有无营养风险"一律服务端算（见 {@link com.his.patient.support.NutritionRules}）：
 * 前端只提交分项，判定决定要不要开膳食医嘱与发起会诊，是能凑指标的那一手。
 */
@Tag(name = "营养风险筛查与会诊")
@RestController
@RequestMapping("/patient/inpatient/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionScreenService screenService;
    private final NutritionStatsService statsService;
    private final InpatientConsultationService consultationService;

    // 筛查

    @Operation(summary = "营养膳食看板（在院未筛 / 有风险 / 到期复筛 / 今日订餐进度 / 未完成会诊）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @GetMapping("/overview")
    public Result<NutritionOverviewVO> overview() {
        return Result.success(statsService.overview());
    }

    @Operation(summary = "筛查评定分页（dueOnly=1 只看到期未复筛）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @PostMapping("/screenListPage")
    public Result<PageResult<NutritionScreenVO>> screenListPage(@Valid @RequestBody NutritionScreenQueryPageDTO query) {
        return Result.success(screenService.screenListPage(query));
    }

    @Operation(summary = "某次住院的筛查历史（按筛查时间倒序）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @GetMapping("/screenListByAdmission")
    public Result<List<NutritionScreenVO>> screenListByAdmission(@RequestParam Long admissionId) {
        return Result.success(screenService.screenListByAdmission(admissionId));
    }

    @Operation(summary = "登记/修改筛查评定（总分、风险判定、BMI、复筛日期全部服务端算）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screenEdit')")
    @PostMapping("/screenUpsert")
    public Result<NutritionScreenVO> screenUpsert(@Valid @RequestBody NutritionScreenUpsertDTO dto) {
        return Result.success("筛查记录已保存", screenService.screenUpsert(dto));
    }

    @Operation(summary = "删除筛查记录（仅限误录）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screenDelete')")
    @DeleteMapping("/screenDeleteById")
    public Result<Integer> screenDeleteById(@RequestParam Long id) {
        return Result.success("已删除", screenService.screenDeleteById(id));
    }

    // 营养会诊（复用住院会诊闭环，类别钉死为营养）

    @Operation(summary = "营养会诊分页（只给 consultCategory=2，不接受前端改类别）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consult')")
    @PostMapping("/consultListPage")
    public Result<PageResult<ConsultationVO>> consultListPage(@Valid @RequestBody ConsultationQueryPageDTO query) {
        query.setConsultCategory(ConsultationLabels.CATEGORY_NUTRITION);
        IPage<ConsultationVO> page = consultationService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "营养会诊详情")
    @PreAuthorize("hasAuthority('ipd:nutrition:consult')")
    @GetMapping("/consultGetById")
    public Result<ConsultationVO> consultGetById(@RequestParam Long consultationId) {
        return Result.success(consultationService.getDetailById(consultationId));
    }

    @Operation(summary = "发起营养会诊（筛查阳性患者一键发起；类别服务端写死为 2）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultApply')")
    @PostMapping("/consultApply")
    public Result<String> consultApply(@RequestBody @Valid ConsultationUpsertDTO dto) {
        dto.setId(null);
        dto.setConsultCategory(ConsultationLabels.CATEGORY_NUTRITION);
        return Result.success("营养会诊已申请", consultationService.save(dto));
    }

    @Operation(summary = "应答营养会诊（接诊人 = 当前登录用户）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultAccept")
    public Result<Void> consultAccept(@RequestBody @Valid ConsultationAcceptDTO dto) {
        consultationService.accept(dto);
        return Result.success("已接诊", null);
    }

    @Operation(summary = "完成营养会诊（必须带结论，完成即回写住院病历）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultFinish")
    public Result<String> consultFinish(@RequestBody @Valid ConsultationFinishDTO dto) {
        return Result.success("会诊已完成并回写病历", consultationService.finish(dto));
    }

    @Operation(summary = "取消营养会诊（必填原因）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultCancel")
    public Result<Void> consultCancel(@RequestBody @Valid ConsultationCancelDTO dto) {
        consultationService.cancel(dto);
        return Result.success("已取消", null);
    }

    // 月度指标

    @Operation(summary = "实时试算（不落库；报数以快照为准）")
    @PreAuthorize("hasAuthority('ipd:nutrition:stats')")
    @GetMapping("/previewStats")
    public Result<NutritionStatsVO> previewStats(@RequestParam String statMonth) {
        return Result.success(statsService.previewStats(statMonth));
    }

    @Operation(summary = "生成/重算月度营养指标快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('ipd:nutrition:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<NutritionStatsVO>> generateStats(@Valid @RequestBody NutritionStatsGenerateDTO dto) {
        List<NutritionStatsVO> rows = statsService.generateStats(dto);
        return Result.success("已生成 " + rows.size() + " 条快照", rows);
    }

    @Operation(summary = "已生成的营养指标分页")
    @PreAuthorize("hasAuthority('ipd:nutrition:stats')")
    @PostMapping("/statsListPage")
    public Result<PageResult<NutritionStatsVO>> statsListPage(@Valid @RequestBody NutritionStatsQueryPageDTO query) {
        return Result.success(statsService.statsListPage(query));
    }

    @Operation(summary = "导出营养指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('ipd:nutrition:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@Valid @RequestBody NutritionStatsQueryPageDTO query) {
        return Result.success(statsService.statsExportCsv(query));
    }
}
