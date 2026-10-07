package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DepartmentQueryDTO;
import com.his.system.dto.DepartmentSelectDTO;
import com.his.system.dto.DepartmentUpsertDTO;
import com.his.system.service.SysDepartmentService;
import com.his.system.vo.DepartmentSelectListVO;
import com.his.system.vo.DepartmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室管理控制器
 */
@Tag(name = "科室管理")
@RestController
@RequestMapping("/system/department")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DepartmentController {

    private final SysDepartmentService sysDepartmentService;

    @Operation(summary = "查询科室树")
    @GetMapping("/tree")
    public Result<List<DepartmentVO>> tree() {
        return Result.success(sysDepartmentService.tree());
    }

    @Operation(summary = "分页查询科室列表")
    @PostMapping("/listPage")
    public Result<PageResult<DepartmentVO>> listPage(@Valid @RequestBody DepartmentQueryDTO queryDTO) {
        return Result.success(sysDepartmentService.listPage(queryDTO));
    }

    @Operation(summary = "查询科室列表（不分页）")
    @PostMapping("/list")
    public Result<List<DepartmentVO>> list(@Valid @RequestBody DepartmentQueryDTO queryDTO) {
        return Result.success(sysDepartmentService.list(queryDTO));
    }

    /**
     * 科室下拉统一入口。
     *
     * <p><b>为什么用 GET 而不是 POST</b>：这是纯查询、无请求体业务语义，
     * 且默认数据范围要走"不传就是安全默认值"，GET 的参数缺省最自然。
     * 入参少且都是短标量，放 query string 不违和。
     *
     * <p>范围收口口径见 service：不传即按当前人过滤，只有显式索取全部才放开。
     */
    @Operation(summary = "科室下拉（scope 控制是否按当前人过滤，默认按当前人）")
    @GetMapping("/selectList")
    public Result<List<DepartmentSelectListVO>> selectList(@Valid DepartmentSelectDTO selectDTO) {
        return Result.success(sysDepartmentService.selectList(selectDTO));
    }

    @Operation(summary = "获取科室详情")
    @GetMapping("/getById")
    public Result<DepartmentVO> getInfo(@RequestParam Long deptId) {
        return Result.success(sysDepartmentService.getInfo(deptId));
    }

    @Operation(summary = "新增或修改科室")
    @PreAuthorize("hasAuthority('org:dept:add')")
    @PostMapping("/departmentUpsert")
    public Result<Void> departmentUpsert(@Valid @RequestBody DepartmentUpsertDTO upsertDTO) {
        return Result.success(sysDepartmentService.upsert(upsertDTO), null);
    }

    @Operation(summary = "删除科室")
    @PreAuthorize("hasAuthority('org:dept:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long deptId) {
        sysDepartmentService.delete(deptId);
        return Result.success("删除成功", null);
    }
}
