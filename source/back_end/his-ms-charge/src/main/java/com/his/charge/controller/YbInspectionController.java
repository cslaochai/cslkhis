package com.his.charge.controller;


import com.his.charge.dto.YbCancelDTO;
import com.his.charge.dto.YbInspectConcludeDTO;
import com.his.charge.dto.YbInspectionPageQueryDTO;
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

    private final YbInspectionService ybInspectionService;

    /**
     * 分页查询医保飞检/专项审核批次，附各批次名下扣款单数与金额。
     * 用于医保扣款与飞检页的批次列表 tab 检索。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "批次分页（附名下扣款单数与金额）")
    @GetMapping("/listPage")
    public Result<PageResult<YbInspectionListVO>> listPage(@Valid YbInspectionPageQueryDTO queryDTO) {
        return Result.success(ybInspectionService.listPage(queryDTO));
    }

    /**
     * 取单个飞检批次详情。
     * 用于批次列表点开某批次查看基本信息与名下扣款归集。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "批次详情")
    @GetMapping("/getById")
    public Result<YbInspectionListVO> getById(@RequestParam Long id) {
        return Result.success(ybInspectionService.getById(id));
    }

    /**
     * 取进行中批次的下拉候选。
     * 用于新建扣款通知时把单据挂到对应飞检批次。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "进行中批次下拉（新建扣款通知时挂批次）")
    @GetMapping("/selectList")
    public Result<List<YbInspectionListVO>> selectList() {
        return Result.success(ybInspectionService.selectRunningList());
    }

    /**
     * 新增或修改飞检批次，仅进行中批次可改。
     * 用于登记一次飞检/专项审核活动或修正其信息。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:add')")
    @Operation(summary = "批次新增/修改（仅进行中可改）")
    @PostMapping("/upsert")
    public Result<YbInspectionListVO> upsert(@Valid @RequestBody YbInspectionUpsertDTO dto) {
        return Result.success(ybInspectionService.upsert(dto));
    }

    /**
     * 飞检批次结项，结论必填，结项后不可再挂新扣款单。
     * 用于一次审核结束时固化结论并关闭批次。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "批次结项（结论必填）")
    @PostMapping("/conclude")
    public Result<Void> conclude(@Valid @RequestBody YbInspectConcludeDTO dto) {
        ybInspectionService.conclude(dto);
        return Result.success(null);
    }

    /**
     * 作废飞检批次，名下已有扣款通知时禁止作废。
     * 用于批次登记错误或撤销整次审核的环节。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "批次作废（名下有扣款通知时禁止）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody YbCancelDTO dto) {
        ybInspectionService.cancel(dto);
        return Result.success(null);
    }
}
