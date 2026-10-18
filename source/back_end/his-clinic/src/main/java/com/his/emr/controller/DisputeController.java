package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.DisputeService;
import com.his.emr.vo.DisputeCaseVO;
import com.his.emr.vo.DisputeStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医疗纠纷 / 投诉登记（登记 → 受理 → 调查处理 → 结案 / 撤销，病历封存联动）。
 */
@Tag(name = "医疗纠纷与投诉登记")
@RestController
@RequestMapping("/emr/dispute")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    /**
     * 纠纷/投诉分页：按状态、类型、等级、被诉科室筛出登记过的事件清单。
     * 用于「医疗纠纷与投诉」页查询卡下方的台账表格。
     */
    @PreAuthorize("hasAuthority('qc:dispute:list')")
    @Operation(summary = "纠纷/投诉分页")
    @PostMapping("/listPage")
    public Result<PageResult<DisputeCaseVO>> listPage(@Valid @RequestBody DisputePageQueryDTO dto) {
        return Result.success(disputeService.listPage(dto));
    }

    /**
     * 单据详情：诉求内容、责任认定结论连同逐次处理跟踪台账。
     * 用于列表行点开详情弹框时按单据编号取数。
     */
    @PreAuthorize("hasAuthority('qc:dispute:list')")
    @Operation(summary = "纠纷/投诉详情（含处理跟踪台账）")
    @GetMapping("/getDetailById")
    public Result<DisputeCaseVO> getDetailById(@RequestParam Long id) {
        return Result.success(disputeService.getDetailById(id));
    }

    /**
     * 登记或修改一张纠纷/投诉单：编号为空走登记，非空只允许改还没受理的。
     * 用于「医疗纠纷与投诉」页登记与修改共用的弹框保存。
     */
    @PreAuthorize("hasAuthority('qc:dispute:add')")
    @Operation(summary = "登记 / 修改（仅待受理可改）")
    @PostMapping("/caseUpsert")
    public Result<DisputeCaseVO> caseUpsert(@Valid @RequestBody DisputeCaseUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "登记成功" : "修改成功", disputeService.caseUpsert(dto));
    }

    /**
     * 受理事件：转入调查中，登记时勾选需封存病历的同步封存其已归档病历。
     * 用于列表行受理操作，是投诉从登记进入调查流程的唯一入口。
     */
    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "受理（待受理→调查中；需封存者联动封存已归档病历）")
    @PostMapping("/accept")
    public Result<DisputeCaseVO> accept(@Valid @RequestBody DisputeActionDTO dto) {
        return Result.success("已受理", disputeService.accept(dto));
    }

    /**
     * 追加一条处理跟踪流水：可把事件从调查中推进到处理中，但不允许借此直接结案。
     * 用于处理跟踪弹框逐次记录谈话、鉴定、协商进展。
     */
    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "登记处理跟踪（追加流水；可推进到调查中/处理中，不得直接结案）")
    @PostMapping("/follow")
    public Result<DisputeCaseVO> follow(@Valid @RequestBody DisputeFollowDTO dto) {
        return Result.success("处理跟踪已登记", disputeService.follow(dto));
    }

    /**
     * 补封存病历：受理时病历还没归档、当时封不成的单据，归档后回来补封。
     * 用于列表上标记待归档后封存的单据的补封按钮。
     */
    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "补封存病历（受理时暂无已归档病历的单据，归档后回来补封）")
    @PostMapping("/sealNow")
    public Result<DisputeCaseVO> sealNow(@Valid @RequestBody DisputeActionDTO dto) {
        return Result.success("病历已封存", disputeService.sealNow(dto));
    }

    /**
     * 结案：处理途径、责任认定、赔偿金额、结论四项齐了才允许提交，结案即终态。
     * 用于结案弹框一次性录入处置结论并终结该单。
     */
    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "结案（途径+责任+赔偿+结论 四项必填，终态）")
    @PostMapping("/close")
    public Result<DisputeCaseVO> close(@Valid @RequestBody DisputeCloseDTO dto) {
        return Result.success("已结案", disputeService.close(dto));
    }

    /**
     * 撤销事件：未结案的单据凭原因作废，撤销即终态。
     * 用于列表行撤销操作，误登记或诉求不成立时收回。
     */
    @PreAuthorize("hasAuthority('qc:dispute:edit')")
    @Operation(summary = "撤销（非终态→已撤销，原因必填，终态）")
    @PostMapping("/revoke")
    public Result<DisputeCaseVO> revoke(@Valid @RequestBody DisputeActionDTO dto) {
        return Result.success("已撤销", disputeService.revoke(dto));
    }

    /**
     * 删除单据：只清掉还没受理且一条跟踪流水都没记的误录单。
     * 用于列表行删除按钮；已进入调查流程的必须走撤销留痕，不许直接抹掉。
     */
    @PreAuthorize("hasAuthority('qc:dispute:add')")
    @Operation(summary = "删除（软删；仅待受理且无跟踪流水）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long id) {
        boolean ok = disputeService.deleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    /**
     * 区间统计：状态与类型分布、被诉科室排名、赔偿合计、平均结案天数。
     * 用于「医疗纠纷与投诉」页顶部统计卡，随查询时间范围一同刷新。
     */
    @PreAuthorize("hasAuthority('qc:dispute:list')")
    @Operation(summary = "统计（状态分布/类型分布/科室TOP/赔偿合计/平均结案天数）")
    @GetMapping("/stat")
    public Result<DisputeStatVO> stat(@RequestParam(required = false) String dateFrom,
                                      @RequestParam(required = false) String dateTo) {
        return Result.success(disputeService.stat(dateFrom, dateTo));
    }
}
