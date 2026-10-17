package com.his.appoint.controller;

import com.his.appoint.dto.RevisitFeePolicyQueryPageDTO;
import com.his.appoint.dto.RevisitFeePolicyUpsertDTO;
import com.his.appoint.service.RevisitFeePolicyService;
import com.his.appoint.vo.RevisitFeePolicyVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 复诊收费策略配置控制器
 */
@Tag(name = "复诊收费策略配置")
@RestController
@RequestMapping("/appoint/revisitFeePolicy")
@RequiredArgsConstructor
public class RevisitFeePolicyController {

    private final RevisitFeePolicyService revisitFeePolicyService;

    @PreAuthorize("hasAuthority('opd:revisitPolicy:list')")
    @Operation(summary = "分页查询复诊收费策略")
    @PostMapping("/listPage")
    public Result<PageResult<RevisitFeePolicyVO>> listPage(@Valid @RequestBody RevisitFeePolicyQueryPageDTO queryDTO) {
        return Result.success(revisitFeePolicyService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('opd:revisitPolicy:list')")
    @Operation(summary = "获取策略详情")
    @GetMapping("/getById")
    public Result<RevisitFeePolicyVO> getById(@RequestParam Long id) {
        return Result.success(revisitFeePolicyService.detail(id));
    }

    @PreAuthorize("hasAuthority('opd:revisitPolicy:add')")
    @Operation(summary = "新增或修改策略")
    @PostMapping("/revisitFeePolicyUpsert")
    public Result<Void> revisitFeePolicyUpsert(@Valid @RequestBody RevisitFeePolicyUpsertDTO upsertDTO) {
        revisitFeePolicyService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", null);
    }

    @PreAuthorize("hasAuthority('opd:revisitPolicy:delete')")
    @Operation(summary = "删除策略")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        revisitFeePolicyService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
