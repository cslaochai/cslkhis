package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.CheckupDTO;
import com.his.patient.service.CheckupService;
import com.his.patient.vo.CheckupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 体检控制器（套餐 / 登记 / 结果 / 总检）。
 */
@Tag(name = "体检管理")
@RestController
@RequestMapping("/patient/checkup")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('checkup:manage:list')")
public class CheckupController {

    private final CheckupService checkupService;

    @PreAuthorize("hasAuthority('checkup:manage:add')")
    @Operation(summary = "套餐保存（新增/更新，整单替换明细）")
    @PostMapping("/package/save")
    public Result<CheckupVO.PackageVO> savePackage(@Valid @RequestBody CheckupDTO.PackageSave dto) {
        return Result.success("套餐已保存", checkupService.savePackage(dto));
    }

    @Operation(summary = "套餐分页")
    @PostMapping("/package/listPage")
    public Result<PageResult<CheckupVO.PackageVO>> packagePage(@Valid @RequestBody CheckupDTO.PackageQuery dto) {
        var page = checkupService.packagePage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "套餐详情")
    @GetMapping("/package/getDetailById")
    public Result<CheckupVO.PackageVO> getPackage(@RequestParam Long id) {
        return Result.success(checkupService.getPackage(id));
    }

    @PreAuthorize("hasAuthority('checkup:manage:edit')")
    @Operation(summary = "套餐停用")
    @PostMapping("/package/disable")
    public Result<Void> disablePackage(@RequestParam Long id) {
        checkupService.disablePackage(id);
        return Result.success("套餐已停用", null);
    }

    @PreAuthorize("hasAuthority('checkup:manage:add')")
    @Operation(summary = "体检登记（按套餐项目预生成结果空行）")
    @PostMapping("/record/create")
    public Result<CheckupVO.RecordVO> createRecord(@Valid @RequestBody CheckupDTO.RecordCreate dto) {
        return Result.success("体检登记成功", checkupService.createRecord(dto));
    }

    @Operation(summary = "体检登记分页")
    @PostMapping("/record/listPage")
    public Result<PageResult<CheckupVO.RecordVO>> recordPage(@Valid @RequestBody CheckupDTO.RecordQuery dto) {
        var page = checkupService.recordPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "体检登记详情（含结果明细；顺带判定明细录完→已完成）")
    @GetMapping("/record/getDetailById")
    public Result<CheckupVO.RecordVO> getRecord(@RequestParam Long recordId) {
        return Result.success(checkupService.refreshFinishStatus(recordId));
    }

    @PreAuthorize("hasAuthority('checkup:manage:edit')")
    @Operation(summary = "开始体检（1→2）")
    @PostMapping("/record/start")
    public Result<Void> start(@RequestParam Long recordId) {
        checkupService.startCheckup(recordId);
        return Result.success("已开始体检", null);
    }

    @PreAuthorize("hasAuthority('checkup:manage:add')")
    @Operation(summary = "单项结果录入")
    @PostMapping("/result/save")
    public Result<CheckupVO.ResultVO> saveResult(@Valid @RequestBody CheckupDTO.ResultSave dto) {
        return Result.success("结果已保存", checkupService.saveResult(dto));
    }

    @PreAuthorize("hasAuthority('checkup:manage:edit')")
    @Operation(summary = "总检出报告（3→4）")
    @PostMapping("/record/conclude")
    public Result<CheckupVO.RecordVO> conclude(@Valid @RequestBody CheckupDTO.Conclusion dto) {
        return Result.success("体检报告已出", checkupService.conclude(dto));
    }

    @PreAuthorize("hasAuthority('checkup:manage:delete')")
    @Operation(summary = "删除登记（仅已登记可删）")
    @PostMapping("/record/deleteById")
    public Result<Void> deleteRecord(@RequestParam Long recordId) {
        checkupService.deleteRecord(recordId);
        return Result.success("登记已删除", null);
    }
}
