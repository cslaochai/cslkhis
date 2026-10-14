package com.mss.controller;

import com.mss.common.base.Result;
import com.mss.service.WorkbenchService;
import com.mss.vo.StatsVO;
import com.mss.vo.WorkbenchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 经办工作台 + 统计报表
 */
@RestController
@RequestMapping("/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @GetMapping("/summary")
    public Result<WorkbenchVO> summary() {
        return Result.success(workbenchService.summary());
    }

    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.success(workbenchService.stats());
    }
}
