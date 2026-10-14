package com.mis.controller;

import com.mis.common.base.Result;
import com.mis.dto.DepartmentUpsertDTO;
import com.mis.entity.MssDepartment;
import com.mis.service.SystemDeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 经办科室（岗位配置下拉 + HIS 契约 /system/department/**）
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SystemDeptController {

    private final SystemDeptService deptService;

    @GetMapping("/system/dept/selectList")
    public Result<List<MssDepartment>> selectList() {
        return Result.success(deptService.selectList());
    }

    @GetMapping("/system/department/tree")
    public Result<List<MssDepartment>> departmentTree() {
        return Result.success(deptService.tree());
    }

    @GetMapping("/system/department/selectList")
    public Result<List<MssDepartment>> departmentSelectList() {
        return Result.success(deptService.selectList());
    }

    @GetMapping("/system/department/getById")
    public Result<MssDepartment> departmentGetById(@RequestParam(required = false) Long deptId) {
        return Result.success(deptService.getById(deptId));
    }

    @PostMapping("/system/department/departmentUpsert")
    public Result<MssDepartment> departmentUpsert(@RequestBody @Valid DepartmentUpsertDTO dto) {
        MssDepartment dept = deptService.upsert(dto);
        return Result.success("保存成功", dept);
    }

    @DeleteMapping("/system/department/deleteById")
    public Result<Void> departmentDeleteById(@RequestParam(required = false) Long deptId) {
        deptService.deleteById(deptId);
        return Result.success("删除成功", null);
    }
}
