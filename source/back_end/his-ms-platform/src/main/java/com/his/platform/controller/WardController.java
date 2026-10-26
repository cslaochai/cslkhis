package com.his.platform.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.platform.dto.WardQueryDTO;
import com.his.platform.dto.WardUpsertDTO;
import com.his.platform.service.SysWardService;
import com.his.platform.vo.WardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病区管理控制器：病区主数据维护 + 病区与科室的多对多关系。
 *
 * <p>与科室管理的区别：科室是行政建制与成本核算单元，病区是护理单元与物理空间。
 * 一个病区可以收多个科室的患者（跨科收治 / 全院一张床），所以这里的 deptId 只表示「本位科室」。
 */
@Tag(name = "病区管理")
@RestController
@RequestMapping("/system/ward")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WardController {

    private final SysWardService sysWardService;

    /**
     * 分页查询病区。deptId 过滤走多对多，含跨科收治的病区，不只本位科室。
     */
    @Operation(summary = "分页查询病区列表")
    @PostMapping("/listPage")
    public Result<PageResult<WardVO>> listPage(@Valid @RequestBody WardQueryDTO queryDTO) {
        return Result.success(sysWardService.listPage(queryDTO));
    }

    /**
     * 按同样条件取全部病区，不分页。
     */
    @Operation(summary = "查询病区列表（不分页）")
    @PostMapping("/list")
    public Result<List<WardVO>> list(@Valid @RequestBody WardQueryDTO queryDTO) {
        return Result.success(sysWardService.list(queryDTO));
    }

    /**
     * 病区下拉统一入口。
     * 传 deptId 时返回该科室可收治的病区（多对多口径），不传返回全部启用的病区。
     */
    @Operation(summary = "病区下拉（deptId 为空取全部启用病区）")
    @GetMapping("/selectList")
    public Result<List<WardVO>> selectList(@RequestParam(required = false) Long deptId) {
        return Result.success(sysWardService.selectList(deptId));
    }

    /**
     * 取病区详情，含收治科室清单与专科优先床数，供编辑弹框回填。
     */
    @Operation(summary = "获取病区详情（含收治科室）")
    @GetMapping("/getById")
    public Result<WardVO> getInfo(@RequestParam Long wardId) {
        return Result.success(sysWardService.getInfo(wardId));
    }

    /**
     * 新增或修改病区，传主键即改、不传即建。同时维护收治科室集合。
     */
    @Operation(summary = "新增或修改病区")
    @PreAuthorize("hasAuthority('org:ward:add')")
    @PostMapping("/wardUpsert")
    public Result<Void> wardUpsert(@Valid @RequestBody WardUpsertDTO upsertDTO) {
        return Result.success(sysWardService.upsert(upsertDTO), null);
    }

    /**
     * 删除病区。有病床、有在院患者或有人挂在该病区时会被拦下，提示改为停用。
     */
    @Operation(summary = "删除病区")
    @PreAuthorize("hasAuthority('org:ward:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long wardId) {
        sysWardService.delete(wardId);
        return Result.success("删除成功", null);
    }
}
