package com.his.charge.controller;


import com.his.charge.dto.ArrearsBoardQueryDTO;
import com.his.charge.dto.ArrearsPolicyUpsertDTO;
import com.his.charge.service.ArrearsControlService;
import com.his.charge.vo.ArrearsPatientVO;
import com.his.charge.vo.ArrearsPolicyVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 欠费管控控制器。
 */
@Tag(name = "住院欠费管控")
@RestController
@RequestMapping("/charge/inpatient/arrears")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('charge:arrearsControl:list')")
public class ArrearsControlController {

    private final ArrearsControlService arrearsControlService;

    /**
     * 读取全院欠费管控策略（阈值、提醒与阻断规则，全局单行）。
     * 用于住院欠费管控页加载当前生效的管控配置。
     */
    @Operation(summary = "读管控策略（单行）")
    @GetMapping("/policy")
    public Result<ArrearsPolicyVO> policy() {
        return Result.success(arrearsControlService.getPolicy());
    }

    /**
     * 保存欠费管控策略，覆盖当前生效的阈值与阻断规则。
     * 用于住院欠费管控页调整欠费认定与临床拦截口径后的回写。
     */
    @PreAuthorize("hasAuthority('charge:arrearsControl:add')")
    @Operation(summary = "更新管控策略")
    @PostMapping("/policyUpsert")
    public Result<ArrearsPolicyVO> policyUpsert(@Valid @RequestBody ArrearsPolicyUpsertDTO dto) {
        return Result.success("策略已保存", arrearsControlService.upsertPolicy(dto));
    }

    /**
     * 在院患者欠费榜分页，按欠费额倒序排列。
     * 用于住院欠费管控页展示需重点催缴与阻断的在院患者。
     */
    @Operation(summary = "在院欠费患者榜（按欠费额倒序）")
    @PostMapping("/board")
    public Result<PageResult<ArrearsPatientVO>> board(@Valid @RequestBody ArrearsBoardQueryDTO q) {
        var page = arrearsControlService.arrearsBoard(q);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}
