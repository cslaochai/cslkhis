package com.his.outpatient.controller;

import com.his.common.base.Result;
import com.his.outpatient.dto.DutyTypeUpsertDTO;
import com.his.outpatient.service.DutyTypeService;
import com.his.outpatient.vo.DutyTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 值班类型（值班线）：医疗总值班 / 护理总值班 / 行政总值班 / 院领导带班 …… 院办自助维护。
 */
@Tag(name = "值班类型")
@RestController
@RequestMapping("/system/dutyType")
@RequiredArgsConstructor
public class DutyTypeController {

    private final DutyTypeService dutyTypeService;

    /**
     * 全部值班类型（含停用）：排班页的类型筛选与登记弹框类型下拉都取它。
     */
    @Operation(summary = "值班类型列表（含 manageable 标记：当前岗位能否排这条线）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public Result<List<DutyTypeVO>> list() {
        return Result.success(dutyTypeService.listVO());
    }

    /**
     * 新增/修改值班类型
     */
    @Operation(summary = "新增/修改值班类型")
    @PreAuthorize("hasAuthority('org:dutyType:edit')")
    @PostMapping("/dutyTypeUpsert")
    public Result<Long> upsert(@Valid @RequestBody DutyTypeUpsertDTO upsertDTO) {
        Long id = dutyTypeService.upsert(upsertDTO);
        return Result.success(null == upsertDTO.getId() ? "新增成功" : "修改成功", id);
    }

    /**
     * 删除值班类型（仍挂着点位/已有值班记录的类型由服务层拦下，只可停用）
     */
    @Operation(summary = "删除值班类型")
    @PreAuthorize("hasAuthority('org:dutyType:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyTypeService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
