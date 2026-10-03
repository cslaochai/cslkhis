package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.EmployeeQualificationUpsertDTO;
import com.his.system.service.EmployeeQualificationService;
import com.his.system.vo.EmployeeQualificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工资格证书控制器（员工档案页的内嵌资源，见 sql/112）。
 *
 * <p>权限复用员工页的三个动作码（sql/100 已铺底到员工管理页的授权角色）：
 * 证书是「本页面独有的业务数据」，不另设菜单按钮码。
 */
@Tag(name = "员工资格证书")
@RestController
@RequestMapping("/system/employee/qualification")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeQualificationController {

    private final EmployeeQualificationService qualificationService;

    @Operation(summary = "查询员工的资格证书列表")
    @PreAuthorize("hasAuthority('org:employee:list')")
    @GetMapping("/listByEmployee")
    public Result<List<EmployeeQualificationVO>> listByEmployee(@RequestParam Long employeeId) {
        return Result.success(qualificationService.listByEmployee(employeeId));
    }

    @Operation(summary = "新增或修改资格证书")
    @PreAuthorize("hasAuthority('org:employee:add')")
    @PostMapping("/qualificationUpsert")
    public Result<Void> qualificationUpsert(@RequestBody @Valid EmployeeQualificationUpsertDTO upsertDTO) {
        qualificationService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", null);
    }

    @Operation(summary = "删除资格证书")
    @PreAuthorize("hasAuthority('org:employee:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        qualificationService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
