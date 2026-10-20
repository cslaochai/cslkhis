package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.service.BiService;
import com.his.medicaltech.vo.BiNationalVO;
import com.his.medicaltech.vo.BiOverviewVO;
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

    /**
     * 全院运营总览：当日挂号、在院、出院、收入、药占比、床位使用与近 7 日趋势、科室收入前五。
     * 用于 BI 驾驶舱页面首屏取数，一次拿全、页面不二次拼装。
     */
    @Operation(summary = "驾驶舱总览（今日挂号/在院/出院/收入/药占比/床位/7日趋势/科室TOP5）")
    @GetMapping("/overview")
    public Result<BiOverviewVO> overview() {
        return Result.success(biService.overview());
    }

    /**
     * 国考四项指标近 30 日结果（平均住院日、床位周转、耗占比、CMI），连同各指标分母一并返回。
     * 用于 BI 驾驶舱页面的国考指标区块，与总览并发拉取。
     */
    @Operation(summary = "国考四指标（近30日：平均住院日/床位周转/耗占比/CMI，分母一并返回）")
    @PreAuthorize("hasAuthority('report:bi:list')")
    @GetMapping("/nationalMetrics")
    public Result<BiNationalVO> nationalMetrics() {
        return Result.success(biService.nationalMetrics());
    }
}
