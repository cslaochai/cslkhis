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
 * 员工管理控制器
 */
@Tag(name = "员工管理")
@RestController
@RequestMapping("/system/employee")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeController {

    private final SysEmployeeService sysEmployeeService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/listPage")
    public Result<PageResult<EmployeeVO>> listPage(@Valid EmployeeQueryDTO queryDTO) {
        return Result.success(sysEmployeeService.listPage(queryDTO));
    }

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/selectList")
    public Result<List<EmployeeVO>> selectList(@Valid EmployeeQueryDTO queryDTO) {
        return Result.success(sysEmployeeService.selectList(queryDTO));
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/getById")
    public Result<EmployeeVO> getInfo(@RequestParam Long id) {
        return Result.success(sysEmployeeService.getInfo(id));
    }

    @Operation(summary = "新增或修改员工")
    @PreAuthorize("hasAuthority('org:employee:add')")
    @PostMapping("/employeeUpsert")
    public Result<Void> employeeUpsert(@RequestBody @Valid EmployeeUpsertDTO upsertDTO) {
        return Result.success(sysEmployeeService.upsert(upsertDTO), null);
    }

    @Operation(summary = "删除员工")
    @PreAuthorize("hasAuthority('org:employee:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long id) {
        sysEmployeeService.delete(id);
        return Result.success("删除成功", null);
    }
}
