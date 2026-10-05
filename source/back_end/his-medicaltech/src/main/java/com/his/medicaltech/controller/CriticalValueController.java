package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.CriticalValueHandleDTO;
import com.his.medicaltech.dto.CriticalValueQueryPageDTO;
import com.his.medicaltech.dto.CriticalValueReceiveDTO;
import com.his.medicaltech.service.CriticalValueService;
import com.his.medicaltech.vo.BizCriticalValueVO;
import com.his.medicaltech.vo.CriticalValueStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医技管理 - 检验危急值控制器。
 * <p>
 * 补的是前端 {@code CriticalValueView} 长期没有的后端：该页面此前展示的是
 * {@code WJ-2026-0901} 这类硬编码演示数据，没有任何接口。
 */
@Tag(name = "医技管理-危急值")
@RestController
@RequestMapping("/medicaltech/criticalValue")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:criticalValue:list')")
public class CriticalValueController {

    private final CriticalValueService criticalValueService;

    @Operation(summary = "分页查询危急值")
    @PostMapping("/listPage")
    public Result<PageResult<BizCriticalValueVO>> listPage(@Valid @RequestBody CriticalValueQueryPageDTO queryDTO) {
        return Result.success(criticalValueService.listPage(queryDTO));
    }

    @Operation(summary = "根据ID获取危急值详情")
    @GetMapping("/getById")
    public Result<BizCriticalValueVO> getById(@RequestParam Long criticalValueId) {
        return Result.success(criticalValueService.getById(criticalValueId));
    }

    @Operation(summary = "危急值统计（列表页卡片）")
    @GetMapping("/stats")
    public Result<CriticalValueStatsVO> stats() {
        return Result.success(criticalValueService.stats());
    }

    @PreAuthorize("hasAuthority('medtech:criticalValue:edit')")
    @Operation(summary = "确认接收危急值")
    @PostMapping("/receive")
    public Result<Boolean> receive(@Valid @RequestBody CriticalValueReceiveDTO dto) {
        return Result.success(criticalValueService.receive(dto));
    }

    @PreAuthorize("hasAuthority('medtech:criticalValue:edit')")
    @Operation(summary = "记录危急值处置措施")
    @PostMapping("/handle")
    public Result<Boolean> handle(@Valid @RequestBody CriticalValueHandleDTO dto) {
        return Result.success(criticalValueService.handle(dto));
    }

    @PreAuthorize("hasAuthority('medtech:criticalValue:edit')")
    @Operation(summary = "超时升级补跑（验证/运维用；定时任务每 5 分钟自动扫）")
    @PostMapping("/escalateOverdue")
    public Result<Integer> escalateOverdue() {
        return Result.success(criticalValueService.escalateOverdue());
    }

    @PreAuthorize("hasAuthority('medtech:criticalValue:delete')")
    @Operation(summary = "删除危急值")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long criticalValueId) {
        return Result.success(criticalValueService.deleteById(criticalValueId));
    }
}
