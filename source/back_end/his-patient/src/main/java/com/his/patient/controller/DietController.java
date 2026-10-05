package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.DietPlanService;
import com.his.patient.service.MealOrderService;
import com.his.patient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 膳食医嘱执行与订餐配送（sql/168，菜单 425、426）。
 *
 * <p>权限：方案页读 {@code ipd:diet:plan}，登记/修改/停餐/删除 {@code ipd:diet:edit}、
 * 接收/退回 {@code ipd:diet:confirm}；订餐页读 {@code ipd:meal:list}，批量生成 {@code ipd:meal:generate}、
 * 配餐/配送/签收/退订/删除 {@code ipd:meal:status}。
 *
 * <p>两条不在这里但必须知道的口径：
 * <ol>
 *   <li>膳食方案的<b>主来源是医嘱校对</b>：orderClass=10 的医嘱被护士校对（1→2）时同事务派生，
 *       停医嘱/作废医嘱同步停/作废方案（见 InpatientOrderServiceImpl）。本控制器的 planUpsert 只服务
 *       营养科手工登记（source=2）与改目标量。</li>
 *   <li><b>订餐只收口服途径</b>（route=1）：管饲制剂由营养科发放、肠外营养走静配与医嘱执行链，
 *       给肠外营养的患者配一份"饭"是错的。</li>
 * </ol>
 */
@Tag(name = "膳食医嘱与订餐配送")
@RestController
@RequestMapping("/patient/inpatient/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietPlanService dietPlanService;
    private final MealOrderService mealOrderService;

    // 膳食方案

    @Operation(summary = "饮食类型下拉（目录唯一来源 NutritionRules.DIETS，含热量/蛋白/餐次/是否订餐）")
    @PreAuthorize("hasAuthority('ipd:diet:plan')")
    @GetMapping("/dietTypeOptions")
    public Result<List<DietTypeOptionVO>> dietTypeOptions() {
        return Result.success(dietPlanService.dietTypeOptions());
    }

    @Operation(summary = "病区下拉（参照数据，订餐台按病区选生成范围）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/wardSelectList")
    public Result<List<WardVO>> wardSelectList() {
        return Result.success(dietPlanService.wardOptions());
    }

    @Operation(summary = "膳食方案分页（待接收的排前面）")
    @PreAuthorize("hasAuthority('ipd:diet:plan')")
    @PostMapping("/planListPage")
    public Result<PageResult<DietPlanVO>> planListPage(@Valid @RequestBody DietPlanQueryPageDTO query) {
        return Result.success(dietPlanService.planListPage(query));
    }

    @Operation(summary = "某次住院的膳食方案")
    @PreAuthorize("hasAuthority('ipd:diet:plan')")
    @GetMapping("/planListByAdmission")
    public Result<List<DietPlanVO>> planListByAdmission(@RequestParam Long admissionId) {
        return Result.success(dietPlanService.planListByAdmission(admissionId));
    }

    @Operation(summary = "手工登记/修改膳食方案（饮食类别与给食途径由目录带出，不接受前端传）")
    @PreAuthorize("hasAuthority('ipd:diet:edit')")
    @PostMapping("/planUpsert")
    public Result<DietPlanVO> planUpsert(@Valid @RequestBody DietPlanUpsertDTO dto) {
        return Result.success("膳食方案已保存", dietPlanService.planUpsert(dto));
    }

    @Operation(summary = "营养科批量接收/退回（退回必填原因；待指定的饮食不允许接收）")
    @PreAuthorize("hasAuthority('ipd:diet:confirm')")
    @PostMapping("/planConfirm")
    public Result<Integer> planConfirm(@Valid @RequestBody DietConfirmDTO dto) {
        return Result.success(dto.getAccept() ? "已接收" : "已退回", dietPlanService.planConfirm(dto));
    }

    @Operation(summary = "停止膳食方案（同步作废该日之后的未配送订餐）")
    @PreAuthorize("hasAuthority('ipd:diet:edit')")
    @PostMapping("/planStop")
    public Result<DietPlanVO> planStop(@Valid @RequestBody DietPlanStopDTO dto) {
        return Result.success("已停餐", dietPlanService.planStop(dto));
    }

    @Operation(summary = "删除膳食方案（已产生配送记录的方案禁止删除）")
    @PreAuthorize("hasAuthority('ipd:diet:edit')")
    @DeleteMapping("/planDeleteById")
    public Result<Integer> planDeleteById(@RequestParam Long id) {
        return Result.success("已删除", dietPlanService.planDeleteById(id));
    }

    // 订餐配送

    @Operation(summary = "订餐明细分页（按日期/病区/餐次/状态）")
    @PreAuthorize("hasAuthority('ipd:meal:list')")
    @PostMapping("/mealListPage")
    public Result<PageResult<MealOrderVO>> mealListPage(@Valid @RequestBody MealOrderQueryPageDTO query) {
        return Result.success(mealOrderService.mealListPage(query));
    }

    @Operation(summary = "某方案产生的订餐明细")
    @PreAuthorize("hasAuthority('ipd:meal:list')")
    @GetMapping("/mealListByPlan")
    public Result<List<MealOrderVO>> mealListByPlan(@RequestParam Long dietPlanId) {
        return Result.success(mealOrderService.mealListByPlan(dietPlanId));
    }

    @Operation(summary = "按日期批量生成订餐（只收执行中的口服方案；重生成只覆盖未配送行）")
    @PreAuthorize("hasAuthority('ipd:meal:generate')")
    @PostMapping("/mealGenerate")
    public Result<MealGenerateVO> mealGenerate(@Valid @RequestBody MealGenerateDTO dto) {
        MealGenerateVO vo = mealOrderService.mealGenerate(dto);
        return Result.success("生成 " + vo.getGeneratedCount() + " 条，跳过 " + vo.getSkippedCount() + " 条", vo);
    }

    @Operation(summary = "推进配餐状态机（待配餐→已配餐→已配送→已签收；退订必填原因）")
    @PreAuthorize("hasAuthority('ipd:meal:status')")
    @PostMapping("/mealStatus")
    public Result<Integer> mealStatus(@Valid @RequestBody MealStatusDTO dto) {
        return Result.success("状态已更新", mealOrderService.mealStatus(dto));
    }

    @Operation(summary = "删除订餐明细（仅限误生成，已配送的不可删）")
    @PreAuthorize("hasAuthority('ipd:meal:status')")
    @DeleteMapping("/mealDeleteById")
    public Result<Integer> mealDeleteById(@RequestParam Long id) {
        return Result.success("已删除", mealOrderService.mealDeleteById(id));
    }
}
