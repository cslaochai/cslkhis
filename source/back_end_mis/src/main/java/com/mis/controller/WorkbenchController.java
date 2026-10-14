package com.mis.controller;

import com.mis.common.base.Result;
import com.mis.service.WorkbenchService;
import com.mis.vo.StatsVO;
import com.mis.vo.WorkbenchVO;
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
