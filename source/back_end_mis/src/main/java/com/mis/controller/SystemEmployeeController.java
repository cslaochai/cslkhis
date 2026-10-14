package com.mis.controller;

import com.mis.common.base.PageResult;
import com.mis.common.base.Result;
import com.mis.dto.EmployeeQueryPageDTO;
import com.mis.dto.EmployeeUpsertDTO;
import com.mis.dto.QualificationBatchUpsertDTO;
import com.mis.entity.MssEmployee;
import com.mis.entity.MssEmployeeQualification;
import com.mis.service.SystemEmployeeService;
import com.mis.vo.EmployeeVO;
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
 * 员工管理（HIS 契约：/system/employee/**）
 */
@RestController
@RequestMapping("/system/employee")
@RequiredArgsConstructor
public class SystemEmployeeController {

    private final SystemEmployeeService employeeService;

    @GetMapping("/listPage")
    public Result<PageResult<EmployeeVO>> listPage(@Valid EmployeeQueryPageDTO dto) {
        return Result.success(employeeService.page(dto));
    }

    @GetMapping("/getById")
    public Result<EmployeeVO> getById(@RequestParam(required = false) Long id) {
        return Result.success(employeeService.detail(id));
    }

    @PostMapping("/employeeUpsert")
    public Result<MssEmployee> employeeUpsert(@RequestBody @Valid EmployeeUpsertDTO dto) {
        MssEmployee emp = employeeService.upsert(dto);
        return Result.success("保存成功", emp);
    }

    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam(required = false) Long id) {
        employeeService.deleteById(id);
        return Result.success("删除成功", null);
    }

    // ---------- 资格证书 ----------

    @GetMapping("/qualification/listByEmployee")
    public Result<List<MssEmployeeQualification>> qualificationList(@RequestParam(required = false) Long employeeId) {
        return Result.success(employeeService.qualificationList(employeeId));
    }

    @PostMapping("/qualification/qualificationBatchUpsert")
    public Result<Void> qualificationBatchUpsert(@RequestBody @Valid QualificationBatchUpsertDTO dto) {
        employeeService.qualificationBatchUpsert(dto);
        return Result.success("保存成功", null);
    }

    @DeleteMapping("/qualification/deleteById")
    public Result<Void> qualificationDelete(@RequestParam(required = false) Long id) {
        employeeService.qualificationDelete(id);
        return Result.success("删除成功", null);
    }
}
