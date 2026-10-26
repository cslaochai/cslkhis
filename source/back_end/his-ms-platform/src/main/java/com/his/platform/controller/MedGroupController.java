package com.his.platform.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.platform.dto.MedGroupQueryDTO;
import com.his.platform.dto.MedGroupUpsertDTO;
import com.his.platform.service.SysMedGroupService;
import com.his.platform.vo.MedGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医疗组管理控制器。
 *
 * <p>医疗组是科室下的医师分组，对应《三级医院评审标准(2022年版)》要求的
 * 「科主任领导下组建医疗团队（主诊医师制/医疗组长制）→ 三级查房」。
 */
@Tag(name = "医疗组管理")
@RestController
@RequestMapping("/system/medGroup")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedGroupController {

    private final SysMedGroupService sysMedGroupService;

    /**
     * 分页查询医疗组。
     */
    @Operation(summary = "分页查询医疗组列表")
    @PostMapping("/listPage")
    public Result<PageResult<MedGroupVO>> listPage(@Valid @RequestBody MedGroupQueryDTO queryDTO) {
        return Result.success(sysMedGroupService.listPage(queryDTO));
    }

    /**
     * 按同样条件取全部医疗组，不分页。
     */
    @Operation(summary = "查询医疗组列表（不分页）")
    @PostMapping("/list")
    public Result<List<MedGroupVO>> list(@Valid @RequestBody MedGroupQueryDTO queryDTO) {
        return Result.success(sysMedGroupService.list(queryDTO));
    }

    /**
     * 医疗组下拉。传 deptId 只返回该科室的医疗组，供医生编辑表单选组。
     */
    @Operation(summary = "医疗组下拉（deptId 为空取全部启用医疗组）")
    @GetMapping("/selectList")
    public Result<List<MedGroupVO>> selectList(@RequestParam(required = false) Long deptId) {
        return Result.success(sysMedGroupService.selectList(deptId));
    }

    /**
     * 取医疗组详情。
     */
    @Operation(summary = "获取医疗组详情")
    @GetMapping("/getById")
    public Result<MedGroupVO> getInfo(@RequestParam Long id) {
        return Result.success(sysMedGroupService.getInfo(id));
    }

    /**
     * 新增或修改医疗组，传主键即改、不传即建。
     */
    @Operation(summary = "新增或修改医疗组")
    @PreAuthorize("hasAuthority('org:medGroup:add')")
    @PostMapping("/medGroupUpsert")
    public Result<Void> medGroupUpsert(@Valid @RequestBody MedGroupUpsertDTO upsertDTO) {
        return Result.success(sysMedGroupService.upsert(upsertDTO), null);
    }

    /**
     * 删除医疗组。组内还有医师时会被拦下。
     */
    @Operation(summary = "删除医疗组")
    @PreAuthorize("hasAuthority('org:medGroup:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long id) {
        sysMedGroupService.delete(id);
        return Result.success("删除成功", null);
    }
}
