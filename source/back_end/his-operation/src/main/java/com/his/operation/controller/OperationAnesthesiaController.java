package com.his.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.operation.dto.*;
import com.his.operation.service.AnesthesiaRecordService;
import com.his.operation.service.AnesthesiaVisitService;
import com.his.operation.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 手术麻醉链端点（术前访视 → 麻醉记录单 → 计费联动）。
 */
@Tag(name = "手术麻醉")
@RestController
@RequestMapping("/patient/inpatient/anesthesia")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:anesthesia:list')")
public class OperationAnesthesiaController {

    private final AnesthesiaVisitService anesthesiaVisitService;
    private final AnesthesiaRecordService anesthesiaRecordService;

    // 一、术前访视

    @Operation(summary = "术前访视分页（住院/手术/结论/关键字）")
    @GetMapping("/visitListPage")
    public Result<IPage<AnesthesiaVisitVO>> visitListPage(@Valid AnesthesiaVisitQueryPageDTO query) {
        return Result.success(anesthesiaVisitService.listPage(query));
    }

    @Operation(summary = "术前访视详情")
    @GetMapping("/visitGetDetailById")
    public Result<AnesthesiaVisitVO> visitGetDetailById(@RequestParam Long visitId) {
        return Result.success(anesthesiaVisitService.getDetailById(visitId));
    }

    @Operation(summary = "某台手术的术前访视（没有则 data 为 null）")
    @GetMapping("/visitGetByApply")
    public Result<AnesthesiaVisitVO> visitGetByApply(@RequestParam Long applyId) {
        return Result.success(anesthesiaVisitService.getByApply(applyId));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "保存术前访视（新增/修改草稿），返回访视单号")
    @PostMapping("/visitSave")
    public Result<String> visitSave(@RequestBody @Valid AnesthesiaVisitUpsertDTO dto) {
        return Result.success("术前访视已保存（尚未给出结论，不能作为麻醉依据）", anesthesiaVisitService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "完成术前访视（结论出账；结论非「可施行麻醉」必须写说明）")
    @PostMapping("/visitFinish")
    public Result<Void> visitFinish(@RequestBody @Valid AnesthesiaVisitFinishDTO dto) {
        anesthesiaVisitService.finish(dto);
        return Result.success("术前访视已完成", null);
    }

    @Operation(summary = "已完成但没有合格术前访视的手术台数（急诊超前麻醉的待补账）")
    @GetMapping("/countFinishedWithoutVisit")
    public Result<Long> countFinishedWithoutVisit() {
        return Result.success(anesthesiaVisitService.countFinishedWithoutVisit());
    }

    // 二、麻醉记录单

    @Operation(summary = "麻醉记录单分页（住院/手术/麻醉医师/状态/关键字/未计费）")
    @GetMapping("/recordListPage")
    public Result<IPage<AnesthesiaRecordVO>> recordListPage(@Valid AnesthesiaRecordQueryPageDTO query) {
        return Result.success(anesthesiaRecordService.listPage(query));
    }

    @Operation(summary = "麻醉记录单详情（含生命体征与用药）")
    @GetMapping("/recordGetDetailById")
    public Result<AnesthesiaRecordVO> recordGetDetailById(@RequestParam Long recordId) {
        return Result.success(anesthesiaRecordService.getDetailById(recordId));
    }

    @Operation(summary = "某台手术的麻醉记录单（没有则 data 为 null）")
    @GetMapping("/recordGetByApply")
    public Result<AnesthesiaRecordVO> recordGetByApply(@RequestParam Long applyId) {
        return Result.success(anesthesiaRecordService.getByApply(applyId));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "开立麻醉记录单（需先有「可施行麻醉」的术前访视），返回麻醉记录单号")
    @PostMapping("/recordCreate")
    public Result<String> recordCreate(@RequestBody @Valid AnesthesiaRecordUpsertDTO dto) {
        return Result.success("麻醉记录单已开立（记录中）", anesthesiaRecordService.create(dto));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "更新麻醉记录单（仅「记录中」可改）")
    @PostMapping("/recordUpdate")
    public Result<Void> recordUpdate(@RequestBody @Valid AnesthesiaRecordUpdateUpsertDTO dto) {
        anesthesiaRecordService.update(dto);
        return Result.success("麻醉记录已更新", null);
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "追加一条生命体征（仅「记录中」可加；同一时刻不允许两条）")
    @PostMapping("/addVital")
    public Result<Void> addVital(@RequestBody @Valid AnesthesiaVitalUpsertDTO dto) {
        anesthesiaRecordService.addVital(dto);
        return Result.success("生命体征已记录", null);
    }

    @Operation(summary = "麻醉生命体征列表（按采样时刻升序）")
    @GetMapping("/listVitals")
    public Result<List<AnesthesiaVitalVO>> listVitals(@RequestParam Long recordId) {
        return Result.success(anesthesiaRecordService.listVitals(recordId));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "追加一条麻醉用药（仅「记录中」可加）")
    @PostMapping("/addMed")
    public Result<Void> addMed(@RequestBody @Valid AnesthesiaMedUpsertDTO dto) {
        anesthesiaRecordService.addMed(dto);
        return Result.success("麻醉用药已记录", null);
    }

    @Operation(summary = "麻醉用药列表（按给药时刻升序）")
    @GetMapping("/listMeds")
    public Result<List<AnesthesiaMedVO>> listMeds(@RequestParam Long recordId) {
        return Result.success(anesthesiaRecordService.listMeds(recordId));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "提交麻醉记录（记录中→已提交；体征与用药之后锁死；自动联动计费）")
    @PostMapping("/recordSubmit")
    public Result<OperationChargeSummaryVO> recordSubmit(@RequestBody @Valid AnesthesiaActionDTO dto) {
        OperationChargeSummaryVO summary = anesthesiaRecordService.submit(dto);
        return Result.success(summary.hasFailure()
                        ? "麻醉记录已提交，但计费存在失败项（详见 messages）"
                        : "麻醉记录已提交",
                summary);
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "审核麻醉记录（已提交→已审核）")
    @PostMapping("/recordAudit")
    public Result<Void> recordAudit(@RequestBody @Valid AnesthesiaActionDTO dto) {
        anesthesiaRecordService.audit(dto);
        return Result.success("麻醉记录已审核", null);
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "麻醉计费（失败项重试；已成功的项幂等跳过）")
    @PostMapping("/recordCharge")
    public Result<OperationChargeSummaryVO> recordCharge(@RequestBody @Valid AnesthesiaActionDTO dto) {
        return Result.success(anesthesiaRecordService.charge(dto));
    }

    @Operation(summary = "尚未计费的麻醉记录单数（收费对账入口）")
    @GetMapping("/countUncharged")
    public Result<Long> countUncharged() {
        return Result.success(anesthesiaRecordService.countUncharged());
    }

    @Operation(summary = "某台手术的计费明细（每项一行；status=2 的行就是「该收但没计上」）")
    @GetMapping("/listChargeItems")
    public Result<List<OperationChargeItemVO>> listChargeItems(@RequestParam Long applyId) {
        return Result.success(anesthesiaRecordService.listChargeItems(applyId));
    }
}
