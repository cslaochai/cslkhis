package com.his.charge.controller;


import com.his.charge.dto.*;
import com.his.charge.service.YbChronicService;
import com.his.charge.vo.ChronicCatalogVO;
import com.his.charge.vo.ChronicRegListVO;
import com.his.charge.vo.ChronicRegSummaryVO;
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
 * 门诊慢特病病种目录 + 人员备案（菜单 1011，sql/163）。
 */
@Tag(name = "慢特病人员备案")
@RestController
@RequestMapping("/charge/ybChronic")
@RequiredArgsConstructor
public class YbChronicController {

    private final YbChronicService ybChronicService;

    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "病种目录分页")
    @GetMapping("/catalogListPage")
    public Result<PageResult<ChronicCatalogVO>> catalogListPage(@Valid ChronicCatalogQueryPageDTO queryDTO) {
        return Result.success(ybChronicService.catalogListPage(queryDTO));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "启用中的病种目录（备案表单下拉，参照数据不配权限码）")
    @GetMapping("/catalogSelectList")
    public Result<List<ChronicCatalogVO>> catalogSelectList() {
        return Result.success(ybChronicService.selectCatalogList());
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:add')")
    @Operation(summary = "病种目录新增/修改（编码唯一，只启停不删）")
    @PostMapping("/catalogUpsert")
    public Result<ChronicCatalogVO> catalogUpsert(@Valid @RequestBody ChronicCatalogUpsertDTO dto) {
        return Result.success(ybChronicService.catalogUpsert(dto));
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:add')")
    @Operation(summary = "病种目录启停")
    @PostMapping("/changeCatalogStatus")
    public Result<Void> changeCatalogStatus(@RequestParam Long id, @RequestParam Integer status) {
        ybChronicService.changeCatalogStatus(id, status);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "备案台账分页（展示态含已过期）")
    @GetMapping("/regListPage")
    public Result<PageResult<ChronicRegListVO>> regListPage(@Valid ChronicRegQueryPageDTO queryDTO) {
        return Result.success(ybChronicService.regListPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "备案详情")
    @GetMapping("/regGetById")
    public Result<ChronicRegListVO> regGetById(@RequestParam Long id) {
        return Result.success(ybChronicService.regGetById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "患者在用门特资格（医生站/收费判断能否走门特）")
    @GetMapping("/regActiveOfPatient")
    public Result<List<ChronicRegListVO>> regActiveOfPatient(@RequestParam Long patientId) {
        return Result.success(ybChronicService.regActiveOfPatient(patientId));
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "备案汇总（有效/待续备/已注销/已驳回）")
    @GetMapping("/regSummary")
    public Result<ChronicRegSummaryVO> regSummary() {
        return Result.success(ybChronicService.regSummary());
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:add')")
    @Operation(summary = "备案新增/修改（经办人默认当前登录人，仅有效可改）")
    @PostMapping("/regUpsert")
    public Result<ChronicRegListVO> regUpsert(@Valid @RequestBody ChronicRegUpsertDTO dto) {
        return Result.success(ybChronicService.regUpsert(dto));
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:cancel')")
    @Operation(summary = "备案注销（终态不可逆）")
    @PostMapping("/regCancel")
    public Result<Void> regCancel(@Valid @RequestBody ChronicRegTerminalDTO dto) {
        ybChronicService.regCancel(dto);
        return Result.success(null);
    }

    @PreAuthorize("hasAuthority('finance:insuranceChronic:cancel')")
    @Operation(summary = "备案驳回（终态不可逆，补材料后另起新单）")
    @PostMapping("/regReject")
    public Result<Void> regReject(@Valid @RequestBody ChronicRegTerminalDTO dto) {
        ybChronicService.regReject(dto);
        return Result.success(null);
    }
}
