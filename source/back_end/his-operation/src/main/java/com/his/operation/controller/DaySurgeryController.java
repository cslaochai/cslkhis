package com.his.operation.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.operation.dto.DaySurgeryActionDTO;
import com.his.operation.dto.DaySurgeryApplyUpsertDTO;
import com.his.operation.dto.DaySurgeryArrangeDTO;
import com.his.operation.dto.DaySurgeryDischargeDTO;
import com.his.operation.dto.DaySurgeryEvalDTO;
import com.his.operation.dto.DaySurgeryFinishDTO;
import com.his.operation.dto.DaySurgeryFollowDTO;
import com.his.operation.dto.DaySurgeryItemQueryPageDTO;
import com.his.operation.dto.DaySurgeryItemUpsertDTO;
import com.his.operation.dto.DaySurgeryQueryPageDTO;
import com.his.operation.dto.DaySurgeryTransferDTO;
import com.his.operation.service.DaySurgeryService;
import com.his.operation.vo.DaySurgeryApplyVO;
import com.his.operation.vo.DaySurgeryItemVO;
import com.his.operation.vo.DaySurgeryStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 日间手术（准入目录 → 预约 → 术前评估 → 安排 → 完成 → 出院 / 转住院 → 24h 随访）。
 *
 * <p>按项目规范 @PreAuthorize 全部标到方法；按钮可用性与超期判定由后端 VO 给，
 * 前端不按 status 码值 switch、也不自己算时间差。
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
    public Result<PageResult<DaySurgeryItemVO>> itemListPage(@RequestBody DaySurgeryItemQueryPageDTO dto) {
        return Result.success(daySurgeryService.itemListPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "启用中的术式下拉（预约用；停用术式不可新预约）")
    @GetMapping("/itemSelectList")
    public Result<List<DaySurgeryItemVO>> itemSelectList(@RequestParam(required = false) Long deptId) {
        return Result.success(daySurgeryService.itemSelectList(deptId));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "准入目录新增 / 修改")
    @PostMapping("/itemUpsert")
    public Result<DaySurgeryItemVO> itemUpsert(@Valid @RequestBody DaySurgeryItemUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "术式已录入" : "修改成功", daySurgeryService.itemUpsert(dto));
    }

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
    public Result<PageResult<DaySurgeryApplyVO>> listPage(@RequestBody DaySurgeryQueryPageDTO dto) {
        return Result.success(daySurgeryService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "登记单详情（含随访台账）")
    @GetMapping("/getDetailById")
    public Result<DaySurgeryApplyVO> getDetailById(@RequestParam Long id) {
        return Result.success(daySurgeryService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "预约登记 / 修改（仅待评估可改；术式必须在启用中的目录里）")
    @PostMapping("/applyUpsert")
    public Result<DaySurgeryApplyVO> applyUpsert(@Valid @RequestBody DaySurgeryApplyUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "预约登记成功" : "修改成功", daySurgeryService.applyUpsert(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "术前评估（不通过不得安排手术）")
    @PostMapping("/evaluate")
    public Result<DaySurgeryApplyVO> evaluate(@Valid @RequestBody DaySurgeryEvalDTO dto) {
        return Result.success("评估已登记", daySurgeryService.evaluate(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "安排手术（评估通过 → 已安排）")
    @PostMapping("/arrange")
    public Result<DaySurgeryApplyVO> arrange(@Valid @RequestBody DaySurgeryArrangeDTO dto) {
        return Result.success("已安排", daySurgeryService.arrange(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "完成手术（已安排 → 术后观察）")
    @PostMapping("/finishSurgery")
    public Result<DaySurgeryApplyVO> finishSurgery(@Valid @RequestBody DaySurgeryFinishDTO dto) {
        return Result.success("手术已完成，进入术后观察", daySurgeryService.finishSurgery(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "离院登记（术后观察 → 已出院）")
    @PostMapping("/discharge")
    public Result<DaySurgeryApplyVO> discharge(@Valid @RequestBody DaySurgeryDischargeDTO dto) {
        return Result.success("离院已登记", daySurgeryService.discharge(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "转住院（术后观察 → 已转住院，住院号必填，终态）")
    @PostMapping("/transferToIpd")
    public Result<DaySurgeryApplyVO> transferToIpd(@Valid @RequestBody DaySurgeryTransferDTO dto) {
        return Result.success("已转住院", daySurgeryService.transferToIpd(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "取消（原因必填，终态）")
    @PostMapping("/cancel")
    public Result<DaySurgeryApplyVO> cancel(@Valid @RequestBody DaySurgeryActionDTO dto) {
        return Result.success("已取消", daySurgeryService.cancel(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:edit')")
    @Operation(summary = "登记随访（已出院 / 已转住院后，24h 内必访一次）")
    @PostMapping("/follow")
    public Result<DaySurgeryApplyVO> follow(@Valid @RequestBody DaySurgeryFollowDTO dto) {
        return Result.success("随访已登记", daySurgeryService.follow(dto));
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:add')")
    @Operation(summary = "删除（软删；仅待评估且无随访）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long id) {
        boolean ok = daySurgeryService.deleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    @PreAuthorize("hasAuthority('ipd:daySurgery:list')")
    @Operation(summary = "统计（状态分布/超期/应随访未随访/非计划再入院/按时离院率/术式TOP）")
    @GetMapping("/stat")
    public Result<DaySurgeryStatVO> stat() {
        return Result.success(daySurgeryService.stat());
    }
}
