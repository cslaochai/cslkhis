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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 员工管理控制器
 */
@Tag(name = "员工管理")
@RestController
@RequestMapping("/system/employee")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeController {

    private final SysEmployeeService employeeService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/listPage")
    public Result<PageResult<EmployeeVO>> listPage(EmployeeQueryDTO queryDTO) {
        return Result.success(employeeService.listPage(queryDTO));
    }

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/selectList")
    public Result<List<EmployeeVO>> selectList(EmployeeQueryDTO queryDTO) {
        return Result.success(employeeService.selectList(queryDTO));
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/getById")
    public Result<EmployeeVO> getInfo(@RequestParam Long id) {
        return Result.success(employeeService.getInfo(id));
    }

    @Operation(summary = "新增或修改员工")
    @PreAuthorize("hasAuthority('org:employee:add')")
    @PostMapping("/employeeUpsert")
    public Result<Void> employeeUpsert(@RequestBody @Valid EmployeeUpsertDTO upsertDTO) {
        return Result.success(employeeService.upsert(upsertDTO), null);
    }

    @Operation(summary = "删除员工")
    @PreAuthorize("hasAuthority('org:employee:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long id) {
        employeeService.delete(id);
        return Result.success("删除成功", null);
    }
}
