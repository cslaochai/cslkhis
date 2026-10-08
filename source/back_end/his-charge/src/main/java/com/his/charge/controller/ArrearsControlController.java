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

    @Operation(summary = "读管控策略（单行）")
    @GetMapping("/policy")
    public Result<ArrearsPolicyVO> policy() {
        return Result.success(arrearsControlService.getPolicy());
    }

    @PreAuthorize("hasAuthority('charge:arrearsControl:add')")
    @Operation(summary = "更新管控策略")
    @PostMapping("/policyUpsert")
    public Result<ArrearsPolicyVO> policyUpsert(@Valid @RequestBody ArrearsPolicyUpsertDTO dto) {
        return Result.success("策略已保存", arrearsControlService.upsertPolicy(dto));
    }

    @Operation(summary = "在院欠费患者榜（按欠费额倒序）")
    @PostMapping("/board")
    public Result<PageResult<ArrearsPatientVO>> board(@Valid @RequestBody(required = false) ArrearsBoardQueryDTO q) {
        ArrearsBoardQueryDTO query = q == null ? new ArrearsBoardQueryDTO() : q;
        var page = arrearsControlService.arrearsBoard(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}
