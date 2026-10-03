package com.his.charge.controller;

import com.his.charge.dto.*;
import com.his.charge.service.YbDeductNoticeService;
import com.his.charge.vo.DeductNoticeDetailVO;
import com.his.charge.vo.DeductNoticeListVO;
import com.his.charge.vo.DeductSummaryVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医保扣款通知单（申诉 → 确认追责 → 缴回闭环，菜单 1010，sql/163）。
 */
@Tag(name = "医保扣款与飞检-扣款通知")
@RestController
@RequestMapping("/charge/ybDeduct")
@RequiredArgsConstructor
public class YbDeductNoticeController {

    private final YbDeductNoticeService noticeService;

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "扣款通知分页（附超期展示态）")
    @GetMapping("/listPage")
    public Result<PageResult<DeductNoticeListVO>> listPage(@Valid DeductNoticeQueryPageDTO queryDTO) {
        return Result.success(noticeService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "扣款通知详情（含全过程留痕）")
    @GetMapping("/getDetailById")
    public Result<DeductNoticeDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(noticeService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "台账汇总（待确认/申诉中/待缴/已缴回/超期/金额）")
    @GetMapping("/summary")
    public Result<DeductSummaryVO> summary() {
        return Result.success(noticeService.summary());
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:add')")
    @Operation(summary = "通知单新增/修改（仅待确认可改）")
    @PostMapping("/upsert")
    public Result<DeductNoticeListVO> upsert(@Valid @RequestBody DeductNoticeUpsertDTO dto) {
        return Result.success(noticeService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "发起申诉（待确认 → 申诉中）")
    @PostMapping("/appeal")
    public Result<Void> appeal(@Valid @RequestBody DeductAppealDTO dto) {
        noticeService.appeal(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "录入申诉结果（申诉中 → 申诉成功 / 待缴）")
    @PostMapping("/appealResult")
    public Result<Void> appealResult(@Valid @RequestBody DeductAppealResultDTO dto) {
        noticeService.appealResult(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "确认扣款并追责（→ 维持扣款待缴）")
    @PostMapping("/confirm")
    public Result<Void> confirm(@Valid @RequestBody DeductConfirmDTO dto) {
        noticeService.confirm(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "录入缴回（待缴 → 已缴回，金额须等于扣款金额）")
    @PostMapping("/payback")
    public Result<Void> payback(@Valid @RequestBody DeductPaybackDTO dto) {
        noticeService.payback(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "作废（仅待确认可作废）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody YbCancelDTO dto) {
        noticeService.cancel(dto);
        return Result.success(null);
    }
}
