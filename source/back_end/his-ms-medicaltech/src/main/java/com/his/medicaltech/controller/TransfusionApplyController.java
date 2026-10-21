package com.his.medicaltech.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.medicaltech.dto.*;
import com.his.medicaltech.service.TransfusionApplyService;
import com.his.medicaltech.vo.TransfusionApplyVO;
import com.his.common.vo.CodeOptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院输血闭环控制器：输血申请、分级审批、配血、发血、双人核对输注、完成与反应上报的接口入口。
 */
@Tag(name = "住院输血闭环")
@RestController
@RequestMapping("/patient/inpatient/transfusionApply")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:transfusion:list')")
public class TransfusionApplyController {

    private final TransfusionApplyService transfusionApplyService;

    /**
     * 输血申请单分页：可按流程状态、配血状态、品种、血型、有无反应、申请日期区间与关键字组合筛选。
     * 用于输血管理页查询卡。
     */
    @Operation(summary = "输血申请分页（状态/配血状态/品种/血型/反应/日期范围/关键字）")
    @GetMapping("/listPage")
    public Result<IPage<TransfusionApplyVO>> listPage(@Valid TransfusionApplyPageQueryDTO query) {
        return Result.success(transfusionApplyService.listPage(query));
    }

    /**
     * 单张输血申请全量详情，含名下血袋明细。
     * 输血管理页打开详情弹框时按申请标识加载。
     */
    @Operation(summary = "输血申请详情（含血袋明细）")
    @GetMapping("/getDetailById")
    public Result<TransfusionApplyVO> getDetailById(@RequestParam Long applyId) {
        return Result.success(transfusionApplyService.getDetailById(applyId));
    }

    /**
     * 取某次住院的全部输血申请，按发生先后升序。
     * 供住院诊疗过程回看该次住院输血史时列出全部单据。
     */
    @Operation(summary = "某次住院的全部输血申请（按发生顺序升序）")
    @GetMapping("/listByAdmission")
    public Result<List<TransfusionApplyVO>> listByAdmission(@RequestParam Long admissionId) {
        return Result.success(transfusionApplyService.listByAdmission(admissionId));
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:add')")
    /**
     * 发起新申请或修订尚未配血的申请，回执输血申请单号后进入分级审批流转。
     * 用于输血管理页申请弹框提交（新增与修改共用此口）。
     */
    @Operation(summary = "发起/修改输血申请（返回输血申请单号；修改仅限「待配血」）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid TransfusionApplyUpsertDTO dto) {
        return Result.success("输血申请已提交（等待用血审批）", transfusionApplyService.save(dto));
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    /**
     * 对输血申请作出通过或驳回结论，驳回必带原因；急诊先输血后补审也走此口。
     * 用于输血管理页审批弹框由审批人逐级签批。
     */
    @Operation(summary = "用血分级审批（通过/驳回；驳回必填原因；急诊补审同走此口）")
    @PostMapping("/approve")
    public Result<String> approve(@RequestBody @Valid TransfusionApproveDTO dto) {
        transfusionApplyService.approve(dto);
        return Result.success(Integer.valueOf(2).equals(dto.getApproveResult())
                ? "已驳回，申请人修改后可重新提交" : "审批通过，可进入配血", null);
    }

    /**
     * 查询单张申请的逐级审批流水，时间正序回放整条审批链。
     * 供用血审批环节追溯该单由谁在何时签核。
     */
    @Operation(summary = "某单的审批流水（逐级链，时间正序）")
    @GetMapping("/approveListByApply")
    public Result<List<TransfusionApplyVO.ApproveRecord>> approveListByApply(@RequestParam Long applyId) {
        return Result.success(transfusionApplyService.approveListByApply(applyId));
    }

    /**
     * 汇总输血申请在审批维度的数量分布（按状态与按级别）。
     * 供用血审批环节掌握待审与已审批量。
     */
    @Operation(summary = "审批统计（按状态 + 按级别）")
    @GetMapping("/approveStats")
    public Result<TransfusionApplyVO.ApproveStats> approveStats() {
        return Result.success(transfusionApplyService.approveStats());
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    /**
     * 分批提交逐袋交叉配血结果，累计不超过申请袋数；致死性不相容整批拒绝，配血结论随响应文本回显。
     * 用于输血管理页配血弹框按血站到货批次录入配血。
     */
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
    /**
     * 将配血全部相合的申请置为已发血，血液出库交接病区。
     * 用于输血管理页发血弹框确认发血。
     */
    @Operation(summary = "发血（已配血且全部相合 → 已发血）")
    @PostMapping("/issue")
    public Result<Void> issue(@RequestBody @Valid TransfusionIssueDTO dto) {
        transfusionApplyService.issue(dto);
        return Result.success("已发血，请病区双人核对后输注", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    /**
     * 双人核对全部必核项通过后开始输注，两名核对护士不得为同一人。
     * 用于输血管理页输注启动弹框在床旁核对后执行。
     */
    @Operation(summary = "开始输注（含双人核对；6 项必核项缺一不可）")
    @PostMapping("/startInfusion")
    public Result<Void> startInfusion(@RequestBody @Valid TransfusionStartDTO dto) {
        transfusionApplyService.startInfusion(dto);
        return Result.success("已开始输注（双人核对完成）", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    /**
     * 结束一次输血，并在同一事务回写输血记录病历与病案首页输血标志。
     * 用于输血管理页完成弹框登记输注结束。
     */
    @Operation(summary = "输血完成（回写输血记录病历 + 病案首页是否输血标志）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestBody @Valid TransfusionFinishDTO dto) {
        transfusionApplyService.finish(dto);
        return Result.success("输血已完成（已回写输血记录病历）", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:edit')")
    /**
     * 为已完成的输血补登不良反应，不改动历史流程状态。
     * 用于输血管理页反应上报弹框登记输血反应。
     */
    @Operation(summary = "输血反应上报（仅「已完成」且尚未上报；不回改历史状态）")
    @PostMapping("/reportReaction")
    public Result<Void> reportReaction(@RequestBody @Valid TransfusionReactionDTO dto) {
        transfusionApplyService.reportReaction(dto);
        return Result.success("输血反应已上报", null);
    }

    @PreAuthorize("hasAuthority('medtech:transfusion:delete')")
    /**
     * 撤销用血申请：血尚未进入患者体内（待配血、已配血、已发血）时可取消。
     * 用于输血管理页终止不再需要的用血，输注中与已完成行拒绝此操作。
     */
    @Operation(summary = "取消用血（仅「待配血/已配血/已发血」；输注中与已完成不可取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid TransfusionCancelDTO dto) {
        transfusionApplyService.cancel(dto);
        return Result.success("用血申请已取消", null);
    }

    /**
     * 统计尚未走完闭环的输血申请数量，可按住院就诊收窄。
     * 输血管理页加载时取数渲染未完成角标。
     */
    @Operation(summary = "未完成输血数（工作台角标）")
    @GetMapping("/countUnfinished")
    public Result<Long> countUnfinished(@RequestParam(required = false) Long admissionId) {
        return Result.success(transfusionApplyService.countUnfinished(admissionId));
    }

    @PreAuthorize("isAuthenticated()")
    /**
     * 血液品种下拉候选。
     * 输血管理页加载品种筛选选项与申请弹框选择品种时取数。
     */
    @Operation(summary = "血液品种字典（下拉候选）")
    @GetMapping("/component/selectList")
    public Result<List<CodeOptionVO>> selectComponentList() {
        return Result.success(transfusionApplyService.componentOptions());
    }

    /**
     * 取输血前核对要点清单，含必核标识。
     * 输注启动弹框渲染双人核对勾选项时取数。
     */
    @Operation(summary = "输血前核对要点字典（前端渲染勾选框）")
    @GetMapping("/checkItemList")
    public Result<List<TransfusionApplyVO.CheckItem>> checkItemList() {
        return Result.success(transfusionApplyService.checkItems());
    }

    /**
     * 取输血反应类型受控候选清单，只能选不能自填。
     * 反应上报弹框加载类型下拉时取数。
     */
    @Operation(summary = "输血反应类型字典（受控字典，前端只能选不能填）")
    @GetMapping("/reactionTypeList")
    public Result<List<String>> reactionTypeList() {
        return Result.success(transfusionApplyService.reactionTypes());
    }
}
