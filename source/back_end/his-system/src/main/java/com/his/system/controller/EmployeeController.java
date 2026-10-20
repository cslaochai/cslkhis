package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.EmployeeQueryDTO;
import com.his.system.dto.EmployeeUpsertDTO;
import com.his.system.service.SysEmployeeService;
import com.his.system.vo.EmployeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工档案控制器：员工名册查询与档案维护的接口入口；身份（角色×科室）由岗位配置写入，本域只维护人的基本事实。
 */
@Tag(name = "员工管理")
@RestController
@RequestMapping("/system/employee")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeController {

    private final SysEmployeeService sysEmployeeService;

    /**
     * 员工档案分页：按姓名、工号、科室、在职状态筛选。
     * 用于「员工管理」页查询卡与名册表格。
     */
    @Operation(summary = "分页查询员工列表")
    @GetMapping("/listPage")
    public Result<PageResult<EmployeeVO>> listPage(@Valid EmployeeQueryDTO queryDTO) {
        return Result.success(sysEmployeeService.listPage(queryDTO));
    }

    /**
     * 员工下拉候选：一次取回可用名册，不分页。
     * 用于排班、手术、预约、值班、转诊等页面选「哪位员工」的下拉。
     */
    @Operation(summary = "分页查询员工列表")
    @GetMapping("/selectList")
    public Result<List<EmployeeVO>> selectList(@Valid EmployeeQueryDTO queryDTO) {
        return Result.success(sysEmployeeService.selectList(queryDTO));
    }

    /**
     * 单条员工档案详情：字段明文回显，供表单回填后整对象提交。
     * 用于「员工管理」编辑弹框回显与医生档案页取数。
     */
    @Operation(summary = "获取员工详情")
    @GetMapping("/getById")
    public Result<EmployeeVO> getInfo(@RequestParam Long id) {
        return Result.success(sysEmployeeService.getInfo(id));
    }

    /**
     * 新增或修改员工档案：主岗位科室快照随岗位配置一并归一。
     * 用于「员工管理」页新增/编辑弹框保存。
     */
    @Operation(summary = "新增或修改员工")
    @PreAuthorize("hasAuthority('org:employee:add')")
    @PostMapping("/employeeUpsert")
    public Result<Void> employeeUpsert(@RequestBody @Valid EmployeeUpsertDTO upsertDTO) {
        return Result.success(sysEmployeeService.upsert(upsertDTO), null);
    }

    /**
     * 删除员工档案。
     * 用于「员工管理」页操作列删除。
     */
    @Operation(summary = "删除员工")
    @PreAuthorize("hasAuthority('org:employee:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long id) {
        sysEmployeeService.delete(id);
        return Result.success("删除成功", null);
    }
}
