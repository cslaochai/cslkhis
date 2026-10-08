package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.OutpatientLogQueryDTO;
import com.his.emr.service.OutpatientLogService;
import com.his.emr.vo.OutpatientLogListVO;
import com.his.emr.vo.OutpatientLogStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门诊日志（法规台账）——《门诊日志管理规定》口径：以病历为基表的接诊事实台账，
 */
@RestController
@RequestMapping("/emr/outpatientLog")
@RequiredArgsConstructor
@Tag(name = "门诊日志（法规台账）")
public class OutpatientLogController {

    private final OutpatientLogService outpatientLogService;

    @Operation(summary = "日志分页（跨科室回溯，不按登录科室收窄——口径与就诊总览一致）")
    @GetMapping("/listPage")
    @PreAuthorize("hasAuthority('opd:outpatientLog:list')")
    public Result<PageResult<OutpatientLogListVO>> listPage(@Valid OutpatientLogQueryDTO query) {
        return Result.success(outpatientLogService.listPage(query));
    }

    @Operation(summary = "统计条（与分页同一套筛选条件）")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('opd:outpatientLog:list')")
    public Result<OutpatientLogStatsVO> stats(@Valid OutpatientLogQueryDTO query) {
        return Result.success(outpatientLogService.stats(query));
    }
}
