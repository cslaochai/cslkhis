package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.EmployeeQualificationBatchUpsertDTO;
import com.his.system.dto.EmployeeQualificationUpsertDTO;
import com.his.system.service.EmployeeQualificationService;
import com.his.system.vo.EmployeeQualificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工资格证书控制器
 */
@Tag(name = "员工资格证书")
@RestController
@RequestMapping("/system/employee/qualification")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeQualificationController {

    private final EmployeeQualificationService employeeQualificationService;

    /**
     * 查一名员工的资格证书清单。
     * 用于「员工管理」编辑弹框内证书表格的回显。
     */
    @Operation(summary = "查询员工的资格证书列表")
    @PreAuthorize("hasAuthority('org:employee:list')")
    @GetMapping("/listByEmployee")
    public Result<List<EmployeeQualificationVO>> listByEmployee(@RequestParam Long employeeId) {
        return Result.success(employeeQualificationService.listByEmployee(employeeId));
    }

    /**
     * 登记或修改单本资格证书。
     * 供证书逐条补录、单独修订有效期时回写一条记录。
     */
    @Operation(summary = "新增或修改资格证书")
    @PreAuthorize("hasAuthority('org:employee:add')")
    @PostMapping("/qualificationUpsert")
    public Result<Void> qualificationUpsert(@RequestBody @Valid EmployeeQualificationUpsertDTO upsertDTO) {
        employeeQualificationService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", null);
    }

    /**
     * 批量保存一名员工的多本证书：展开、查重与逐条校验在一次事务内完成。
     * 用于「员工管理」编辑弹框保存时把证书草稿行一次提交。
     */
    @Operation(summary = "批量保存资格证书（整批一个事务）")
    @PreAuthorize("hasAuthority('org:employee:add')")
    @PostMapping("/qualificationBatchUpsert")
    public Result<Void> qualificationBatchUpsert(@RequestBody @Valid EmployeeQualificationBatchUpsertDTO batchDTO) {
        employeeQualificationService.batchUpsert(batchDTO);
        return Result.success("已保存 " + batchDTO.getItems().size() + " 本证书", null);
    }

    /**
     * 删除一本资格证书。
     * 用于「员工管理」编辑弹框移除证书行。
     */
    @Operation(summary = "删除资格证书")
    @PreAuthorize("hasAuthority('org:employee:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        employeeQualificationService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
