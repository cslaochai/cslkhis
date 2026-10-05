package com.his.report.controller;

import com.his.common.base.Result;
import com.his.report.service.BiService;
import com.his.report.vo.BiNationalVO;
import com.his.report.vo.BiOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BI 驾驶舱控制器：一个总览接口全量返回，页面不二次拼装。
 */
@Tag(name = "BI 驾驶舱")
@RestController
@RequestMapping("/report/bi")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:bi:list')")
public class BiController {

    private final BiService biService;

    @Operation(summary = "驾驶舱总览（今日挂号/在院/出院/收入/药占比/床位/7日趋势/科室TOP5）")
    @GetMapping("/overview")
    public Result<BiOverviewVO> overview() {
        return Result.success(biService.overview());
    }

    @Operation(summary = "国考四指标（近30日：平均住院日/床位周转/耗占比/CMI，分母一并返回）")
    @PreAuthorize("hasAuthority('report:bi:list')")
    @GetMapping("/nationalMetrics")
    public Result<BiNationalVO> nationalMetrics() {
        return Result.success(biService.nationalMetrics());
    }
}
