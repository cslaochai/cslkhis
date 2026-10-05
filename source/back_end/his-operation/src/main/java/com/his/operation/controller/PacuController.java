package com.his.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.operation.dto.*;
import com.his.operation.service.PacuService;
import com.his.operation.vo.OperationChargeSummaryVO;
import com.his.operation.vo.PacuRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PACU 麻醉后监测治疗端点（G15 第三环）。
 *
 * <p>单独一个前缀是因为它是一个独立岗位：复苏护士只看自己的队列，
 * 把它塞进「麻醉记录单」那一页里，等于强迫护士在麻醉医师的工作台上干活。
 */
@Tag(name = "PACU 麻醉复苏")
@RestController
@RequestMapping("/patient/inpatient/pacu")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:anesthesia:list')")
public class PacuController {

    private final PacuService pacuService;

    @Operation(summary = "PACU 复苏记录分页（住院/手术/麻醉记录/在室状态/关键字）")
    @GetMapping("/listPage")
    public Result<IPage<PacuRecordVO>> listPage(@Valid PacuQueryPageDTO query) {
        return Result.success(pacuService.listPage(query));
    }

    @Operation(summary = "PACU 复苏记录详情")
    @GetMapping("/getDetailById")
    public Result<PacuRecordVO> getDetailById(@RequestParam Long pacuId) {
        return Result.success(pacuService.getDetailById(pacuId));
    }

    @Operation(summary = "某条麻醉记录的 PACU 复苏单（没有则 data 为 null）")
    @GetMapping("/getByRecord")
    public Result<PacuRecordVO> getByRecord(@RequestParam Long recordId) {
        return Result.success(pacuService.getByRecord(recordId));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "入 PACU 登记（需麻醉记录已提交），返回复苏单号")
    @PostMapping("/enter")
    public Result<String> enter(@RequestBody @Valid PacuEnterDTO dto) {
        return Result.success("已登记入 PACU", pacuService.enter(dto));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "Aldrete 评分（总分由服务端逐项相加，不接收前端传总分）")
    @PostMapping("/score")
    public Result<Void> score(@Valid @RequestBody PacuScoreDTO dto) {
        pacuService.score(dto);
        return Result.success("Aldrete 评分已记录", null);
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "出 PACU（Aldrete≥9 或写明原因且去向非回病房；自动联动计费）")
    @PostMapping("/leave")
    public Result<OperationChargeSummaryVO> leave(@RequestBody @Valid PacuLeaveDTO dto) {
        OperationChargeSummaryVO summary = pacuService.leave(dto);
        return Result.success(summary.hasFailure()
                        ? "已出 PACU，但计费存在失败项（详见 messages）"
                        : "已出 PACU",
                summary);
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "PACU 计费（失败项重试）")
    @PostMapping("/charge")
    public Result<OperationChargeSummaryVO> charge(@RequestBody @Valid AnesthesiaActionDTO dto) {
        return Result.success(pacuService.charge(dto));
    }

    @Operation(summary = "在室人数（复苏室床位看板）")
    @GetMapping("/countInRoom")
    public Result<Long> countInRoom() {
        return Result.success(pacuService.countInRoom());
    }
}
