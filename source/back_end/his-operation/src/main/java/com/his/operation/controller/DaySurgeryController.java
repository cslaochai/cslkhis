package com.his.operation.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.operation.dto.*;
import com.his.operation.service.DaySurgeryService;
import com.his.operation.vo.DaySurgeryApplyVO;
import com.his.operation.vo.DaySurgeryItemVO;
import com.his.operation.vo.DaySurgeryStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日间手术（准入目录 → 预约 → 术前评估 → 安排 → 完成 → 出院 / 转住院 → 24h 随访）。
 */
@Tag(name = "日间手术")
@RestController
@RequestMapping("/patient/daySurgery")
@RequiredArgsConstructor
public class DaySurgeryController {

    private final DaySurgeryService daySurgeryService;

    // 准入目录

    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "准入目录分页")
    @PostMapping("/itemListPage")
    public Result<PageResult<DaySurgeryItemVO>> itemListPage(@Valid @RequestBody DaySurgeryItemPageQueryDTO dto) {
        return Result.success(daySurgeryService.itemListPage(dto));
    }

    /**
     * 启用中的准入术式下拉，可按科室收窄。
     * 日间手术页的术式查询条件与预约登记弹框的术式选择共用这一份数据，只含启用中的术式。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "启用中的术式下拉（预约用；停用术式不可新预约）")
    @GetMapping("/itemSelectList")
    public Result<List<DaySurgeryItemVO>> itemSelectList(@RequestParam(required = false) Long deptId) {
        return Result.success(daySurgeryService.itemSelectList(deptId));
    }

    /**
     * 录入或修订准入术式（新增与修改同一入口，按 id 分支）。
     * 用于「日间手术准入目录」页的术式弹框保存。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "准入目录新增 / 修改")
    @PostMapping("/itemUpsert")
    public Result<DaySurgeryItemVO> itemUpsert(@Valid @RequestBody DaySurgeryItemUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "术式已录入" : "修改成功", daySurgeryService.itemUpsert(dto));
    }

    /**
     * 准入术式启停。
     * 用于准入目录页行内的启用/停用开关；停用只拦后续新预约，存量在途登记单不受影响。
     *
     * @param status 启用状态（1-启用 0-停用）
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "术式启停（停用后不可新预约，存量单不受影响）")
    @PostMapping("/itemUpdateStatus")
    public Result<DaySurgeryItemVO> itemUpdateStatus(@RequestParam Long id, @RequestParam Integer status) {
        return Result.success(status != null && status == 1 ? "已启用" : "已停用", daySurgeryService.itemUpdateStatus(id, status));
    }

    // 登记单

    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "日间手术登记单分页")
    @PostMapping("/listPage")
    public Result<PageResult<DaySurgeryApplyVO>> listPage(@Valid @RequestBody DaySurgeryPageQueryDTO dto) {
        return Result.success(daySurgeryService.listPage(dto));
    }

    /**
     * 登记单全量字段，附带历次随访台账。
     * 日间手术页的详情弹框与预约编辑弹框回显都取这一份。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "登记单详情（含随访台账）")
    @GetMapping("/getDetailById")
    public Result<DaySurgeryApplyVO> getDetailById(@RequestParam Long id) {
        return Result.success(daySurgeryService.getDetailById(id));
    }

    /**
     * 日间手术预约登记与修改（同一入口，按 id 分支）。
     * 用于日间手术页的预约弹框保存；术式必须落在启用中的准入目录里，改单只在待评估阶段开放。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "预约登记 / 修改（仅待评估可改；术式必须在启用中的目录里）")
    @PostMapping("/applyUpsert")
    public Result<DaySurgeryApplyVO> applyUpsert(@Valid @RequestBody DaySurgeryApplyUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "预约登记成功" : "修改成功", daySurgeryService.applyUpsert(dto));
    }

    /**
     * 登记术前评估结论与评估意见。
     * 用于日间手术页行内的「术前评估」弹框；意见是禁忌筛查的留痕，不通过的单后续安排手术会被拦。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "术前评估（不通过不得安排手术）")
    @PostMapping("/evaluate")
    public Result<DaySurgeryApplyVO> evaluate(@Valid @RequestBody DaySurgeryEvalDTO dto) {
        return Result.success("评估已登记", daySurgeryService.evaluate(dto));
    }

    /**
     * 为评估通过的登记单排定手术时间、手术间、麻醉方式与主刀。
     * 用于日间手术页行内的「安排手术」弹框。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "安排手术（评估通过 → 已安排）")
    @PostMapping("/arrange")
    public Result<DaySurgeryApplyVO> arrange(@Valid @RequestBody DaySurgeryArrangeDTO dto) {
        return Result.success("已安排", daySurgeryService.arrange(dto));
    }

    /**
     * 回报手术结束并把登记单推进到术后观察。
     * 用于日间手术页行内的「完成手术」动作，可带实际结束时刻。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "完成手术（已安排 → 术后观察）")
    @PostMapping("/finishSurgery")
    public Result<DaySurgeryApplyVO> finishSurgery(@Valid @RequestBody DaySurgeryFinishDTO dto) {
        return Result.success("手术已完成，进入术后观察", daySurgeryService.finishSurgery(dto));
    }

    /**
     * 登记离院方式与离院时间，登记单走到已出院。
     * 用于日间手术页行内的「离院」弹框，是按时离院率统计的事实来源。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "离院登记（术后观察 → 已出院）")
    @PostMapping("/discharge")
    public Result<DaySurgeryApplyVO> discharge(@Valid @RequestBody DaySurgeryDischargeDTO dto) {
        return Result.success("离院已登记", daySurgeryService.discharge(dto));
    }

    /**
     * 把术后观察中的患者转为住院，登记单进终态。
     * 用于日间手术页行内的「转住院」弹框；转入的住院单与原因是必填项，这一步是医保与病案口径的分界。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "转住院（术后观察 → 已转住院，住院号必填，终态）")
    @PostMapping("/transferToIpd")
    public Result<DaySurgeryApplyVO> transferToIpd(@Valid @RequestBody DaySurgeryTransferDTO dto) {
        return Result.success("已转住院", daySurgeryService.transferToIpd(dto));
    }

    /**
     * 取消登记单并置终态。
     * 用于日间手术页行内的「取消」弹框，取消原因必填。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "取消（原因必填，终态）")
    @PostMapping("/cancel")
    public Result<DaySurgeryApplyVO> cancel(@Valid @RequestBody DaySurgeryActionDTO dto) {
        return Result.success("已取消", daySurgeryService.cancel(dto));
    }

    /**
     * 追加一次随访登记（方式、结果与内容）。
     * 用于日间手术页行内的「随访」弹框；离院或转住院后 24 小时内必须完成一次，超期口径由服务端算。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "登记随访（已出院 / 已转住院后，24h 内必访一次）")
    @PostMapping("/follow")
    public Result<DaySurgeryApplyVO> follow(@Valid @RequestBody DaySurgeryFollowDTO dto) {
        return Result.success("随访已登记", daySurgeryService.follow(dto));
    }

    /**
     * 软删一张登记单。
     * 用于日间手术页行内的「删除」动作，只删还没评估且没有随访台账的单。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "删除（软删；仅待评估且无随访）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long id) {
        boolean ok = daySurgeryService.deleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    /**
     * 日间手术看板指标：状态分布、超期与随访完成情况、非计划再入院、按时离院率、术式排行。
     * 用于日间手术页顶部统计卡，一次请求拿齐全部指标，超期与随访时限在这里收口计算。
     */
    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "统计（状态分布/超期/应随访未随访/非计划再入院/按时离院率/术式TOP）")
    @GetMapping("/stat")
    public Result<DaySurgeryStatVO> stat() {
        return Result.success(daySurgeryService.stat());
    }
}
