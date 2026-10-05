package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.DischargeDrugDTO;
import com.his.patient.service.DischargeDrugService;
import com.his.patient.vo.DischargeDrugSelectListVO;
import com.his.patient.vo.DischargeDrugVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出院带药控制器。
 *
 * <p>流程：医生/护士在出院办理前开带药单（挂入院次）→ 药房批量发药 → 已发药留痕不可改删。
 */
@Tag(name = "出院带药")
@RestController
@RequestMapping("/patient/dischargeDrug")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('inpatient:dischargeDrug:list')")
public class DischargeDrugController {

    private final DischargeDrugService dischargeDrugService;

    @PreAuthorize("hasAuthority('inpatient:dischargeDrug:add')")
    @Operation(summary = "开带药单 / 修改（仅待发药可改）")
    @PostMapping("/upsert")
    public Result<DischargeDrugVO> upsert(@Valid @RequestBody DischargeDrugDTO.Upsert dto) {
        return Result.success("带药单已保存", dischargeDrugService.upsert(dto));
    }

    @Operation(summary = "分页查询带药单")
    @PostMapping("/listPage")
    public Result<PageResult<DischargeDrugVO>> listPage(@Valid @RequestBody DischargeDrugDTO.QueryPage dto) {
        var page = dischargeDrugService.listPage(dto == null ? new DischargeDrugDTO.QueryPage() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按入院次列全部带药单")
    @GetMapping("/selectList")
    public Result<List<DischargeDrugSelectListVO>> selectList(@RequestParam Long admissionId) {
        return Result.success(dischargeDrugService.listByAdmission(admissionId));
    }

    @Operation(summary = "带药单详情")
    @GetMapping("/getDetailById")
    public Result<DischargeDrugVO> getDetailById(@RequestParam Long id) {
        return Result.success(dischargeDrugService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('inpatient:dischargeDrug:edit')")
    @Operation(summary = "批量发药（药房岗，单向）")
    @PostMapping("/dispense")
    public Result<List<DischargeDrugVO>> dispense(@Valid @RequestBody DischargeDrugDTO.Dispense dto) {
        return Result.success("发药完成", dischargeDrugService.dispense(dto));
    }

    @PreAuthorize("hasAuthority('inpatient:dischargeDrug:delete')")
    @Operation(summary = "删除带药单（仅待发药）")
    @PostMapping("/deleteById")
    public Result<Void> deleteById(@Valid @RequestBody DischargeDrugDTO.Delete dto) {
        dischargeDrugService.deleteById(dto.getId());
        return Result.success("带药单已删除", null);
    }
}
