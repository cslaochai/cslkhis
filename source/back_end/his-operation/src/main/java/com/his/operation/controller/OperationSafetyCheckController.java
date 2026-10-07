package com.his.operation.controller;

import com.his.common.base.Result;
import com.his.operation.dto.SafetyCheckSignDTO;
import com.his.operation.service.OperationSafetyCheckService;
import com.his.operation.vo.SafetyCheckVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 手术安全核查端点（sql/134：三方 × 三时段）。
 *
 * <p>只有两个口子：看三张核查卡、签一个时段。没有修改和删除 ——
 * 核查记录一经签下就是事实，允许改就等于允许伪造（数据库唯一键同一条纪律的兜底）。
 *
 * <p>三方（手术医师/麻醉医师/手术室护士）来自不同岗位的角色，签单口子同时放行
 * {@code ipd:surgery:edit} 与 {@code ipd:anesthesia:edit}（麻醉医师通常没有手术排期页的编辑码）。
 */
@Tag(name = "手术安全核查")
@RestController
@RequestMapping("/patient/inpatient/safetyCheck")
@RequiredArgsConstructor
public class OperationSafetyCheckController {

    private final OperationSafetyCheckService operationSafetyCheckService;

    @PreAuthorize("hasAnyAuthority('ipd:surgery:list', 'ipd:anesthesia:list')")
    @Operation(summary = "某台手术的三张核查卡（核查项字典 + 已签行 + 能否签与原因）")
    @GetMapping("/cardsByApply")
    public Result<List<SafetyCheckVO.PhaseCard>> cardsByApply(@RequestParam Long applyId) {
        return Result.success(operationSafetyCheckService.cardsByApply(applyId));
    }

    @PreAuthorize("hasAnyAuthority('ipd:surgery:edit', 'ipd:anesthesia:edit')")
    @Operation(summary = "签某一阶段（三方签名齐 + 必核项齐 + 时段顺序对），返回核查单号")
    @PostMapping("/sign")
    public Result<String> sign(@RequestBody @Valid SafetyCheckSignDTO dto) {
        return Result.success("该时段三方核查已完成", operationSafetyCheckService.sign(dto));
    }
}
