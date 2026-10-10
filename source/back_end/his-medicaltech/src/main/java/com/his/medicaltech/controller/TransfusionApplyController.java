package com.his.medicaltech.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.medicaltech.dto.*;
import com.his.medicaltech.service.TransfusionApplyService;
import com.his.medicaltech.vo.TransfusionApplyVO;
import com.his.patient.vo.CodeOptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院输血闭环端点（P4.4）。
 */
@Tag(name = "住院输血闭环")
@RestController
@RequestMapping("/patient/inpatient/transfusionApply")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:transfusion:list')")
public class TransfusionApplyController {

    private final TransfusionApplyService transfusionApplyService;

    @Operation(summary = "输血申请分页（状态/配血状态/品种/血型/反应/日期范围/关键字）")
    @GetMapping("/listPage")
    public Result<IPage<TransfusionApplyVO>> listPage(@Valid TransfusionApplyQueryPageDTO query) {
        return Result.success(transfusionApplyService.listPage(query));
    }

    @Operation(summary = "输血申请详情（含血袋明细）")
    @GetMapping("/getDetailById")
    public Result<TransfusionApplyVO> getDetailById(@RequestParam Long applyId) {
        return Result.success(transfusionApplyService.getDetailById(applyId));
    }

    @Operation(summary = "某次住院的全部输血申请（按发生顺序升序）")
    @GetMapping("/listByAdmission")
    public Result<List<TransfusionApplyVO>> listByAdmission(@RequestParam Long admissionId) {
        return Result.success(transfusionApplyService.listByAdmission(admissionId));
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:add')")
    @Operation(summary = "发起/修改输血申请（返回输血申请单号；修改仅限「待配血」）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid TransfusionApplyUpsertDTO dto) {
        return Result.success("输血申请已提交（等待用血审批）", transfusionApplyService.save(dto));
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    @Operation(summary = "用血分级审批（通过/驳回；驳回必填原因；急诊补审同走此口）")
    @PostMapping("/approve")
    public Result<String> approve(@RequestBody @Valid TransfusionApproveDTO dto) {
        transfusionApplyService.approve(dto);
        return Result.success(Integer.valueOf(2).equals(dto.getApproveResult())
                ? "已驳回，申请人修改后可重新提交" : "审批通过，可进入配血", null);
    }

    @Operation(summary = "某单的审批流水（逐级链，时间正序）")
    @GetMapping("/approveListByApply")
    public Result<List<TransfusionApplyVO.ApproveRecord>> approveListByApply(@RequestParam Long applyId) {
        return Result.success(transfusionApplyService.approveListByApply(applyId));
    }

    @Operation(summary = "审批统计（按状态 + 按级别）")
    @GetMapping("/approveStats")
    public Result<TransfusionApplyVO.ApproveStats> approveStats() {
        return Result.success(transfusionApplyService.approveStats());
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    @Operation(summary = "配血（逐袋录入；ABO/Rh 不相容整批拒绝，配血不合则落库并提示不可发血）")
    @PostMapping("/crossmatch")
    public Result<Void> crossmatch(@RequestBody @Valid TransfusionCrossmatchDTO dto) {
        // 【坑】配血结果是给人看的结论（全部相合 / N 袋不合不能发血 / 已配 x/y 袋），
        // 必须放进 message。写成单参 Result.success(x) 会命中 success(T data) 重载，
        // 结论被塞进 data、message 恒为"操作成功" —— 调用方读 message 永远读不到
        // "到底配成没有"。同理适用于任何"返回值本身就是结论"的写操作。
        return Result.success(transfusionApplyService.crossmatch(dto), null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    @Operation(summary = "发血（已配血且全部相合 → 已发血）")
    @PostMapping("/issue")
    public Result<Void> issue(@RequestBody @Valid TransfusionIssueDTO dto) {
        transfusionApplyService.issue(dto);
        return Result.success("已发血，请病区双人核对后输注", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    @Operation(summary = "开始输注（含双人核对；6 项必核项缺一不可）")
    @PostMapping("/startInfusion")
    public Result<Void> startInfusion(@RequestBody @Valid TransfusionStartDTO dto) {
        transfusionApplyService.startInfusion(dto);
        return Result.success("已开始输注（双人核对完成）", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    @Operation(summary = "输血完成（回写输血记录病历 + 病案首页是否输血标志）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestBody @Valid TransfusionFinishDTO dto) {
        transfusionApplyService.finish(dto);
        return Result.success("输血已完成（已回写输血记录病历）", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    @Operation(summary = "输血反应上报（仅「已完成」且尚未上报；不回改历史状态）")
    @PostMapping("/reportReaction")
    public Result<Void> reportReaction(@RequestBody @Valid TransfusionReactionDTO dto) {
        transfusionApplyService.reportReaction(dto);
        return Result.success("输血反应已上报", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:delete')")
    @Operation(summary = "取消用血（仅「待配血/已配血/已发血」；输注中与已完成不可取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid TransfusionCancelDTO dto) {
        transfusionApplyService.cancel(dto);
        return Result.success("用血申请已取消", null);
    }

    @Operation(summary = "未完成输血数（工作台角标）")
    @GetMapping("/countUnfinished")
    public Result<Long> countUnfinished(@RequestParam(required = false) Long admissionId) {
        return Result.success(transfusionApplyService.countUnfinished(admissionId));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "血液品种字典（下拉候选）")
    @GetMapping("/component/selectList")
    public Result<List<CodeOptionVO>> selectComponentList() {
        return Result.success(transfusionApplyService.componentOptions());
    }

    @Operation(summary = "输血前核对要点字典（前端渲染勾选框）")
    @GetMapping("/checkItemList")
    public Result<List<TransfusionApplyVO.CheckItem>> checkItemList() {
        return Result.success(transfusionApplyService.checkItems());
    }

    @Operation(summary = "输血反应类型字典（受控字典，前端只能选不能填）")
    @GetMapping("/reactionTypeList")
    public Result<List<String>> reactionTypeList() {
        return Result.success(transfusionApplyService.reactionTypes());
    }
}
