package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.TreatmentDTO;
import com.his.emr.service.TreatmentService;
import com.his.emr.vo.TreatmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门诊治疗站（G19）：治疗申请（疗程）→ 排期 → 按次打卡 → 按次计费。
 */
@Tag(name = "门诊治疗站")
@RestController
@RequestMapping("/emr/treatment")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:treatmentStation:list')")
public class TreatmentController {

    private final TreatmentService treatmentService;

    // 疗程（申请单）

    @PreAuthorize("hasAuthority('opd:treatmentStation:add')")
    @Operation(summary = "分页查询治疗申请（疗程）")
    @PostMapping("/applyListPage")
    public Result<PageResult<TreatmentVO.ApplyVO>> applyListPage(@Valid @RequestBody TreatmentDTO.ApplyQuery query) {
        return Result.success(treatmentService.listPageApplies(query));
    }

    @Operation(summary = "疗程详情（含全部按次流水）")
    @GetMapping("/getDetailById")
    public Result<TreatmentVO.ApplyDetailVO> getDetailById(@RequestParam Long applyId) {
        return Result.success(treatmentService.getDetail(applyId));
    }

    @Operation(summary = "治疗项目下拉（开单选项目用，带单价）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/item/selectList")
    public Result<List<TreatmentVO.ItemSelectListVO>> itemSelectList(@RequestParam(required = false) String keyword,
                                                                     @RequestParam(required = false) Integer limit) {
        return Result.success(treatmentService.itemSelectList(keyword, limit));
    }

    @PreAuthorize("hasAuthority('opd:treatmentStation:add')")
    @Operation(summary = "开单/重排疗程（仅在一次卡都没打过时允许重排）")
    @PostMapping("/applyUpsert")
    public Result<TreatmentVO.ApplyDetailVO> applyUpsert(@Valid @RequestBody TreatmentDTO.ApplyUpsert dto) {
        TreatmentVO.ApplyDetailVO detail = treatmentService.upsertApply(dto);
        return Result.success("疗程已排期：" + detail.getApply().getItemName()
                + " 共 " + detail.getApply().getTotalTimes() + " 次", detail);
    }

    @PreAuthorize("hasAuthority('opd:treatmentStation:delete')")
    @Operation(summary = "取消疗程（连带取消未执行的次数，不退已计费的账）")
    @PostMapping("/cancelApply")
    public Result<Integer> cancelApply(@Valid @RequestBody TreatmentDTO.ApplyCancel dto) {
        int n = treatmentService.cancelApply(dto);
        return Result.success("疗程已取消，连带取消未执行 " + n + " 次", n);
    }

    @PreAuthorize("hasAuthority('opd:treatmentStation:delete')")
    @Operation(summary = "删除疗程（仅一次都未执行时允许）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long applyId) {
        treatmentService.deleteApply(applyId);
        return Result.success("疗程及其排期已删除", true);
    }

    // 按次流水

    @Operation(summary = "分页查询按次执行流水（治疗台/台账共用）")
    @PostMapping("/execListPage")
    public Result<PageResult<TreatmentVO.ExecVO>> execListPage(@Valid @RequestBody TreatmentDTO.ExecQuery query) {
        return Result.success(treatmentService.listPageExecs(query));
    }

    @Operation(summary = "流水状态分布（与分页同口径，但不带上被统计的那一维）")
    @PostMapping("/execStatusCount")
    public Result<List<TreatmentVO.StatusCountVO>> execStatusCount(@Valid @RequestBody TreatmentDTO.ExecQuery query) {
        return Result.success(treatmentService.statusCount(query));
    }

    @Operation(summary = "治疗台看板统计")
    @GetMapping("/stats")
    public Result<TreatmentVO.StatsVO> stats() {
        return Result.success(treatmentService.stats());
    }

    @PreAuthorize("hasAuthority('opd:treatmentStation:edit')")
    @Operation(summary = "单次改期（只挪未执行的那一次）")
    @PostMapping("/execReschedule")
    public Result<TreatmentVO.ExecVO> execReschedule(@Valid @RequestBody TreatmentDTO.ExecReschedule dto) {
        TreatmentVO.ExecVO vo = treatmentService.rescheduleExec(dto);
        return Result.success("第 " + vo.getExecSeq() + " 次计划日期已改为 " + vo.getPlanDate(), vo);
    }

    @PreAuthorize("hasAuthority('opd:treatmentStation:edit')")
    @Operation(summary = "按次打卡（打卡成功即按次计费；计费失败只留痕，不回滚打卡）")
    @PostMapping("/execExecute")
    public Result<TreatmentVO.ExecVO> execExecute(@Valid @RequestBody TreatmentDTO.ExecExecute dto) {
        TreatmentVO.ExecVO vo = treatmentService.executeExec(dto);
        return Result.success(vo.getChargeNotice(), vo);
    }

    @PreAuthorize("hasAuthority('opd:treatmentStation:edit')")
    @Operation(summary = "计费补记（已执行但没记上账的行重试一次）")
    @PostMapping("/execRetryCharge")
    public Result<TreatmentVO.ExecVO> execRetryCharge(@Valid @RequestBody TreatmentDTO.ExecRetryCharge dto) {
        TreatmentVO.ExecVO vo = treatmentService.retryCharge(dto.getRecordId());
        return Result.success(vo.getChargeNotice(), vo);
    }
}
