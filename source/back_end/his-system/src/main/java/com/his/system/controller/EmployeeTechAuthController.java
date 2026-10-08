package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.*;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.vo.EmployeeTechAuthVO;
import com.his.system.vo.TechAuthOverrideVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医疗技术临床应用授权控制器（台账见 sql/155，页面 805 技术授权）。
 */
@Tag(name = "医疗技术授权")
@RestController
@RequestMapping("/system/techAuth")
@RequiredArgsConstructor
public class EmployeeTechAuthController {

    private final EmployeeTechAuthService employeeTechAuthService;

    @Operation(summary = "分页查询授权台账")
    @PreAuthorize("hasAuthority('org:techAuth:list')")
    @PostMapping("/listPage")
    public Result<PageResult<EmployeeTechAuthVO>> listPage(@Valid @RequestBody TechAuthQueryPageDTO query) {
        return Result.success(employeeTechAuthService.listPage(query));
    }

    @Operation(summary = "授权详情")
    @PreAuthorize("hasAuthority('org:techAuth:list')")
    @GetMapping("/getById")
    public Result<EmployeeTechAuthVO> getById(@RequestParam Long id) {
        return Result.success(employeeTechAuthService.getById(id));
    }

    @Operation(summary = "按人查授权（员工档案与开单提示共用）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listByEmployee")
    public Result<List<EmployeeTechAuthVO>> listByEmployee(@RequestParam Long employeeId) {
        return Result.success(employeeTechAuthService.listByEmployee(employeeId));
    }

    @Operation(summary = "当前登录人的授权")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mine")
    public Result<List<EmployeeTechAuthVO>> mine() {
        return Result.success(employeeTechAuthService.mine());
    }

    @Operation(summary = "登记或修改授权（仅待审批/已驳回可改）")
    @PreAuthorize("hasAuthority('org:techAuth:add')")
    @PostMapping("/techAuthUpsert")
    public Result<Void> techAuthUpsert(@RequestBody @Valid TechAuthUpsertDTO dto) {
        employeeTechAuthService.upsert(dto);
        return Result.success(dto.getId() == null ? "登记成功，待委员会审批" : "修改成功", null);
    }

    @Operation(summary = "审批（通过/驳回）")
    @PreAuthorize("hasAuthority('org:techAuth:edit')")
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody @Valid TechAuthApproveDTO dto) {
        employeeTechAuthService.approve(dto);
        return Result.success(Boolean.TRUE.equals(dto.getApproved()) ? "已授权" : "已驳回", null);
    }

    @Operation(summary = "收回授权（动态调整）")
    @PreAuthorize("hasAuthority('org:techAuth:edit')")
    @PostMapping("/revoke")
    public Result<Void> revoke(@RequestBody @Valid TechAuthRevokeDTO dto) {
        employeeTechAuthService.revoke(dto);
        return Result.success("已收回", null);
    }

    @Operation(summary = "删除授权记录（仅待审批/已驳回，物理删）")
    @PreAuthorize("hasAuthority('org:techAuth:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        employeeTechAuthService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "分页查询急诊越权登记")
    @PreAuthorize("hasAuthority('org:techAuth:list')")
    @PostMapping("/overrideListPage")
    public Result<PageResult<TechAuthOverrideVO>> overrideListPage(@Valid @RequestBody TechAuthOverrideQueryPageDTO query) {
        return Result.success(employeeTechAuthService.overrideListPage(query));
    }

    @Operation(summary = "越权登记上级确认")
    @PreAuthorize("hasAuthority('org:techAuth:override')")
    @PostMapping("/overrideConfirm")
    public Result<Void> overrideConfirm(@RequestBody @Valid TechAuthOverrideConfirmDTO dto) {
        employeeTechAuthService.confirmOverride(dto);
        return Result.success("已确认", null);
    }
}
