package com.mis.controller;

import com.mis.common.base.Result;
import com.mis.service.SuperviseStatsService;
import com.mis.vo.SuperviseStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基金监管 - 监管工作台
 */
@RestController
@RequestMapping("/supervise/dashboard")
@RequiredArgsConstructor
public class SuperviseDashboardController {

    private final SuperviseStatsService superviseStatsService;

    @GetMapping("/stats")
    public Result<SuperviseStatsVO> stats() {
        return Result.success(superviseStatsService.stats());
    }
}
