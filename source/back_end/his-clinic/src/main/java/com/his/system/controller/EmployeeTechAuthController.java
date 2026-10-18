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
 * 医疗技术临床应用授权控制器：手术分级授权台账的登记、委员会审批、收回与急诊越权确认的接口入口。
 */
@Tag(name = "医疗技术授权")
@RestController
@RequestMapping("/system/techAuth")
@RequiredArgsConstructor
public class EmployeeTechAuthController {

    private final EmployeeTechAuthService employeeTechAuthService;

    /**
     * 授权台账分页：按医师、授权类别、级别上限、授权状态筛选。
     * 用于「技术授权」页「授权台账」查询卡与表格。
     */
    @Operation(summary = "分页查询授权台账")
    @PreAuthorize("hasAuthority('org:techAuth:list')")
    @PostMapping("/listPage")
    public Result<PageResult<EmployeeTechAuthVO>> listPage(@Valid @RequestBody TechAuthPageQueryDTO query) {
        return Result.success(employeeTechAuthService.listPage(query));
    }

    /**
     * 单条授权详情：含授权依据与审批留痕。
     * 供委员会复核某一条授权的完整材料时取数。
     */
    @Operation(summary = "授权详情")
    @PreAuthorize("hasAuthority('org:techAuth:list')")
    @GetMapping("/getById")
    public Result<EmployeeTechAuthVO> getById(@RequestParam Long id) {
        return Result.success(employeeTechAuthService.getById(id));
    }

    /**
     * 按医师查其授权集合。
     * 供员工档案的授权区块展示与开单、术式申请时的权限校验取数。
     */
    @Operation(summary = "按人查授权（员工档案与开单提示共用）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listByEmployee")
    public Result<List<EmployeeTechAuthVO>> listByEmployee(@RequestParam Long employeeId) {
        return Result.success(employeeTechAuthService.listByEmployee(employeeId));
    }

    /**
     * 查当前登录人自己的授权。
     * 用于「技术授权」页「我的授权」页签，医师自查可做到哪一级。
     */
    @Operation(summary = "当前登录人的授权")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mine")
    public Result<List<EmployeeTechAuthVO>> mine() {
        return Result.success(employeeTechAuthService.mine());
    }

    /**
     * 登记或修改授权：新增后进入待审批；已生效的记录不允许改字段，只能走收回。
     * 用于「技术授权」页登记/编辑弹框保存。
     */
    @Operation(summary = "登记或修改授权（仅待审批/已驳回可改）")
    @PreAuthorize("hasAuthority('org:techAuth:add')")
    @PostMapping("/techAuthUpsert")
    public Result<Void> techAuthUpsert(@RequestBody @Valid TechAuthUpsertDTO dto) {
        employeeTechAuthService.upsert(dto);
        return Result.success(dto.getId() == null ? "登记成功，待委员会审批" : "修改成功", null);
    }

    /**
     * 委员会审批：通过即授权生效，驳回则退回申请人修改。
     * 用于「技术授权」页台账行的审批操作。
     */
    @Operation(summary = "审批（通过/驳回）")
    @PreAuthorize("hasAuthority('org:techAuth:edit')")
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody @Valid TechAuthApproveDTO dto) {
        employeeTechAuthService.approve(dto);
        return Result.success(Boolean.TRUE.equals(dto.getApproved()) ? "已授权" : "已驳回", null);
    }

    /**
     * 收回已生效的授权（动态调整，原因必填）。
     * 用于「技术授权」页台账行的收回操作。
     */
    @Operation(summary = "收回授权（动态调整）")
    @PreAuthorize("hasAuthority('org:techAuth:edit')")
    @PostMapping("/revoke")
    public Result<Void> revoke(@RequestBody @Valid TechAuthRevokeDTO dto) {
        employeeTechAuthService.revoke(dto);
        return Result.success("已收回", null);
    }

    /**
     * 删除授权记录：仅限未生效的待审批/已驳回记录，物理删。
     * 用于「技术授权」页台账行的删除操作。
     */
    @Operation(summary = "删除授权记录（仅待审批/已驳回，物理删）")
    @PreAuthorize("hasAuthority('org:techAuth:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        employeeTechAuthService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 急诊越权登记分页：按越权状态与来源单据筛选。
     * 用于「技术授权」页「急诊越权登记」页签表格。
     */
    @Operation(summary = "分页查询急诊越权登记")
    @PreAuthorize("hasAuthority('org:techAuth:list')")
    @PostMapping("/overrideListPage")
    public Result<PageResult<TechAuthOverrideVO>> overrideListPage(@Valid @RequestBody TechAuthOverridePageQueryDTO query) {
        return Result.success(employeeTechAuthService.overrideListPage(query));
    }

    /**
     * 上级确认急诊越权登记：本人确认自己被拦下，后端拒绝自批。
     * 用于「技术授权」页越权登记行的确认操作。
     */
    @Operation(summary = "越权登记上级确认")
    @PreAuthorize("hasAuthority('org:techAuth:override')")
    @PostMapping("/overrideConfirm")
    public Result<Void> overrideConfirm(@RequestBody @Valid TechAuthOverrideConfirmDTO dto) {
        employeeTechAuthService.confirmOverride(dto);
        return Result.success("已确认", null);
    }
}
