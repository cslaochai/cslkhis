package com.his.charge.controller;

import com.his.charge.dto.YbCancelDTO;
import com.his.charge.dto.YbInspectConcludeDTO;
import com.his.charge.dto.YbInspectionQueryPageDTO;
import com.his.charge.dto.YbInspectionUpsertDTO;
import com.his.charge.service.YbInspectionService;
import com.his.charge.vo.YbInspectionListVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医保飞检/专项审核批次（菜单 1010，sql/163）。
 */
@Tag(name = "医保扣款与飞检-飞检批次")
@RestController
@RequestMapping("/charge/ybInspection")
@RequiredArgsConstructor
public class YbInspectionController {

    private final YbInspectionService inspectionService;

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "批次分页（附名下扣款单数与金额）")
    @GetMapping("/listPage")
    public Result<PageResult<YbInspectionListVO>> listPage(@Valid YbInspectionQueryPageDTO queryDTO) {
        return Result.success(inspectionService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "批次详情")
    @GetMapping("/getById")
    public Result<YbInspectionListVO> getById(@RequestParam Long id) {
        return Result.success(inspectionService.getById(id));
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "进行中批次下拉（新建扣款通知时挂批次）")
    @GetMapping("/selectList")
    public Result<List<YbInspectionListVO>> selectList() {
        return Result.success(inspectionService.selectRunningList());
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:add')")
    @Operation(summary = "批次新增/修改（仅进行中可改）")
    @PostMapping("/upsert")
    public Result<YbInspectionListVO> upsert(@Valid @RequestBody YbInspectionUpsertDTO dto) {
        return Result.success(inspectionService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "批次结项（结论必填）")
    @PostMapping("/conclude")
    public Result<Void> conclude(@Valid @RequestBody YbInspectConcludeDTO dto) {
        inspectionService.conclude(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "批次作废（名下有扣款通知时禁止）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody YbCancelDTO dto) {
        inspectionService.cancel(dto);
        return Result.success(null);
    }
}
