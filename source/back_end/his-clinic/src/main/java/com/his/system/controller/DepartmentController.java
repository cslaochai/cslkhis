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
 * 科室管理控制器：组织架构科室的树形展示、分页维护与跨页面选科室的取数入口。
 */
@Tag(name = "科室管理")
@RestController
@RequestMapping("/system/department")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DepartmentController {

    private final SysDepartmentService sysDepartmentService;

    /**
     * 返回带层级的完整科室树。
     * 用于科室管理页左栏的树形导航，以及员工、用户、医生、诊室等页面选择所属科室时的树形下拉。
     */
    @Operation(summary = "查询科室树")
    @GetMapping("/tree")
    public Result<List<DepartmentVO>> tree() {
        return Result.success(sysDepartmentService.tree());
    }

    /**
     * 按名称、类型等条件分页查询科室。
     * 用于科室管理页查询卡下方表格的取数与翻页。
     */
    @Operation(summary = "分页查询科室列表")
    @PostMapping("/listPage")
    public Result<PageResult<DepartmentVO>> listPage(@Valid @RequestBody DepartmentQueryDTO queryDTO) {
        return Result.success(sysDepartmentService.listPage(queryDTO));
    }

    /**
     * 按同样的条件一次取全部科室，不分页。
     * 用于需要完整科室清单才好做对照或整表渲染的维护环节；表格分页仍走上一个接口。
     */
    @Operation(summary = "查询科室列表（不分页）")
    @PostMapping("/list")
    public Result<List<DepartmentVO>> list(@Valid @RequestBody DepartmentQueryDTO queryDTO) {
        return Result.success(sysDepartmentService.list(queryDTO));
    }

    /**
     * 科室下拉统一入口。
     */
    @Operation(summary = "科室下拉（scope 控制是否按当前人过滤，默认按当前人）")
    @GetMapping("/selectList")
    public Result<List<DepartmentSelectListVO>> selectList(@Valid DepartmentSelectDTO selectDTO) {
        return Result.success(sysDepartmentService.selectList(selectDTO));
    }

    /**
     * 按科室主键取单条科室信息。
     * 用于科室详情弹框的只读展示，以及编辑弹框保存前的整条回填。
     */
    @Operation(summary = "获取科室详情")
    @GetMapping("/getById")
    public Result<DepartmentVO> getInfo(@RequestParam Long deptId) {
        return Result.success(sysDepartmentService.getInfo(deptId));
    }

    /**
     * 新增或修改科室，传主键即改、不传即建。
     * 用于科室管理页的新增与编辑弹框保存。「编辑」按钮复用新增权限码是有意为之，前后端同一个写入口。
     */
    @Operation(summary = "新增或修改科室")
    @PreAuthorize("hasAuthority('org:dept:add')")
    @PostMapping("/departmentUpsert")
    public Result<Void> departmentUpsert(@Valid @RequestBody DepartmentUpsertDTO upsertDTO) {
        return Result.success(sysDepartmentService.upsert(upsertDTO), null);
    }

    /**
     * 按科室主键删除科室。
     * 用于科室管理页表格行或左栏树节点的删除确认；顶级部门与挂了下级科室的部门会被拦下。
     */
    @Operation(summary = "删除科室")
    @PreAuthorize("hasAuthority('org:dept:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long deptId) {
        sysDepartmentService.delete(deptId);
        return Result.success("删除成功", null);
    }
}
