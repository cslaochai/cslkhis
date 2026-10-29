package com.his.inpatient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.inpatient.dto.*;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.ConsultCategoryEnum;
import com.his.common.utils.UserUtils;
import com.his.inpatient.service.InpatientConsultationService;
import com.his.inpatient.service.NutritionScreenService;
import com.his.inpatient.service.NutritionStatsService;
import com.his.inpatient.vo.ConsultationVO;
import com.his.inpatient.vo.NutritionOverviewVO;
import com.his.inpatient.vo.NutritionScreenVO;
import com.his.inpatient.vo.NutritionStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营养风险筛查 / 营养会诊 / 营养指标监测
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

    /**
     * 分页查询住院患者的营养风险筛查评定单，可只看已到期未复筛的一批。
     * 用于营养风险筛查页的查询卡与待筛表格。
     */
    @Operation(summary = "筛查评定分页（dueOnly=1 只看到期未复筛）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @PostMapping("/screenListPage")
    public Result<PageResult<NutritionScreenVO>> screenListPage(@Valid @RequestBody NutritionScreenPageQueryDTO query) {
        return Result.success(nutritionScreenService.screenListPage(query));
    }

    /**
     * 取某次住院期间的全部筛查评定，按筛查时间倒序。
     * 供追溯单名患者本次住院的历次复筛轨迹时取数；筛查页列表走分页接口。
     */
    @Operation(summary = "某次住院的筛查历史（按筛查时间倒序）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screen')")
    @GetMapping("/screenListByAdmission")
    public Result<List<NutritionScreenVO>> screenListByAdmission(@RequestParam Long admissionId) {
        return Result.success(nutritionScreenService.screenListByAdmission(admissionId));
    }

    /**
     * 登记或修改一份营养风险筛查评定，总分、风险判定、体重指数与下次复筛日期均由服务端按分项算出。
     * 用于营养风险筛查页的评定弹框保存。
     */
    @Operation(summary = "登记/修改筛查评定（总分、风险判定、BMI、复筛日期全部服务端算）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screenEdit')")
    @PostMapping("/screenUpsert")
    public Result<NutritionScreenVO> screenUpsert(@Valid @RequestBody NutritionScreenUpsertDTO dto) {
        return Result.success("筛查记录已保存", nutritionScreenService.screenUpsert(dto));
    }

    /**
     * 删除一条筛查评定记录。
     * 用于营养风险筛查页行操作「删除」的二次确认之后，只针对误录的单据。
     */
    @Operation(summary = "删除筛查记录（仅限误录）")
    @PreAuthorize("hasAuthority('ipd:nutrition:screenDelete')")
    @DeleteMapping("/screenDeleteById")
    public Result<Integer> screenDeleteById(@RequestParam Long id) {
        return Result.success("已删除", nutritionScreenService.screenDeleteById(id));
    }

    // 营养会诊（复用住院会诊闭环，类别钉死为营养）

    @Operation(summary = "营养会诊分页（consultCategory=2 或会诊科室=本科室；不接受前端改类别）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consult')")
    @PostMapping("/consultListPage")
    public Result<PageResult<ConsultationVO>> consultListPage(@Valid @RequestBody ConsultationPageQueryDTO query) {
        query.setConsultCategory(ConsultCategoryEnum.NUTRITION.getCode());
        // 发到本科室的普通会诊也要进营养工作台：医生在会诊管理里选「营养科」时不会知道要先标类别，
        // 只按类别过滤会让这单在营养科的工作台上凭空消失（谁来接诊都找不到）。
        CurrentUser operator = UserUtils.getCurrentUser();
        query.setOrToDeptId(operator == null ? null : operator.getDeptId());
        IPage<ConsultationVO> page = inpatientConsultationService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 取一次营养会诊的申请、应答与结论全文。
     * 营养会诊详情弹框按会诊 id 自拉。
     */
    @Operation(summary = "营养会诊详情")
    @PreAuthorize("hasAuthority('ipd:nutrition:consult')")
    @GetMapping("/consultGetById")
    public Result<ConsultationVO> consultGetById(@RequestParam Long consultationId) {
        return Result.success(inpatientConsultationService.getDetailById(consultationId));
    }

    /**
     * 发起一张营养科会诊申请，会诊类别由服务端钉死，不接受前端指定。
     * 用于营养会诊页的发起弹框，以及筛查页对阳性患者一键转会诊。
     */
    @Operation(summary = "发起营养会诊（筛查阳性患者一键发起；类别服务端写死为 2）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultApply')")
    @PostMapping("/consultApply")
    public Result<String> consultApply(@RequestBody @Valid ConsultationUpsertDTO dto) {
        dto.setId(null);
        dto.setConsultCategory(ConsultCategoryEnum.NUTRITION.getCode());
        return Result.success("营养会诊已申请", inpatientConsultationService.save(dto));
    }

    /**
     * 应答一张待接诊的营养会诊，接诊人取当前登录用户。
     * 用于营养会诊页行操作「接诊」。
     */
    @Operation(summary = "应答营养会诊（接诊人 = 当前登录用户）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultAccept")
    public Result<Void> consultAccept(@RequestBody @Valid ConsultationAcceptDTO dto) {
        inpatientConsultationService.accept(dto);
        return Result.success("已接诊", null);
    }

    /**
     * 完成一张营养会诊并填写结论，结论同步回写该次住院病历。
     * 用于营养会诊页的完成会诊弹框提交。
     */
    @Operation(summary = "完成营养会诊（必须带结论，完成即回写住院病历，返回病历号）")
    @PreAuthorize("hasAuthority('ipd:nutrition:consultEdit')")
    @PostMapping("/consultFinish")
    public Result<String> consultFinish(@RequestBody @Valid ConsultationFinishDTO dto) {
        return Result.success("会诊已完成并回写病历", inpatientConsultationService.finish(dto));
    }

    /**
     * 取消一张尚未完成的营养会诊并留存原因。
     * 用于营养会诊页行操作「取消」填写原因后提交。
     */
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

    /**
     * 按月份与统计范围生成营养指标快照，同月同范围重复生成即覆盖旧值。
     * 用于营养指标监测页的生成快照弹框。
     */
    @Operation(summary = "生成/重算月度营养指标快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('ipd:nutrition:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<NutritionStatsVO>> generateStats(@Valid @RequestBody NutritionStatsGenerateDTO dto) {
        List<NutritionStatsVO> rows = nutritionStatsService.generateStats(dto);
        return Result.success("已生成 " + rows.size() + " 条快照", rows);
    }

    /**
     * 分页查询已生成的月度营养指标快照。
     * 用于营养指标监测页的历史快照表格。
     */
    @Operation(summary = "已生成的营养指标分页")
    @PreAuthorize("hasAuthority('ipd:nutrition:stats')")
    @PostMapping("/statsListPage")
    public Result<PageResult<NutritionStatsVO>> statsListPage(@Valid @RequestBody NutritionStatsPageQueryDTO query) {
        return Result.success(nutritionStatsService.statsListPage(query));
    }

    /**
     * 按当前筛选条件把营养指标快照导出成表格文件，取数上限由服务端收口。
     * 用于营养指标监测页「导出」按钮。
     */
    @Operation(summary = "导出营养指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('ipd:nutrition:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@Valid @RequestBody NutritionStatsPageQueryDTO query) {
        return Result.success(nutritionStatsService.statsExportCsv(query));
    }
}
