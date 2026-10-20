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
 * 门诊日志控制器：门诊接诊法规台账的只读查询与传染病应报未报筛查入口。
 */
@RestController
@RequestMapping("/emr/outpatientLog")
@RequiredArgsConstructor
@Tag(name = "门诊日志（法规台账）")
public class OutpatientLogController {

    private final OutpatientLogService outpatientLogService;

    /**
     * 门诊日志分页：一行 = 一次有诊断事实的接诊，可报/已报查询时现算不落状态列。
     * 用于「门诊日志」页查询卡，做行政/感控回溯核查；本页只读，报卡写动作走传染病报告卡。
     */
    @Operation(summary = "日志分页（跨科室回溯，不按登录科室收窄——口径与就诊总览一致）")
    @GetMapping("/listPage")
    @PreAuthorize("hasAuthority('opd:outpatientLog:list')")
    public Result<PageResult<OutpatientLogListVO>> listPage(@Valid OutpatientLogQueryDTO query) {
        return Result.success(outpatientLogService.listPage(query));
    }

    /**
     * 门诊日志统计条：接诊数、发热、可报、应报未报、已报五段计数。
     * 用于「门诊日志」页顶部统计，与分页共用同一套筛选条件。
     */
    @Operation(summary = "统计条（与分页同一套筛选条件）")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('opd:outpatientLog:list')")
    public Result<OutpatientLogStatsVO> stats(@Valid OutpatientLogQueryDTO query) {
        return Result.success(outpatientLogService.stats(query));
    }

    /**
     * 本人应报未报分页：服务端强制只取登录医生的行，前端传的医生/科室筛选一律被覆盖。
     * 用于「我的未报」自查清单，命中法定传染病却还没建卡的接诊逐条列出。
     */
    @Operation(summary = "我的应报未报分页（医生自查入口，服务端强制本人——菜单 2950，sql/238）")
    @GetMapping("/myPending")
    @PreAuthorize("hasAuthority('opd:outpatientLog:mine')")
    public Result<PageResult<OutpatientLogListVO>> myPending(@Valid OutpatientLogQueryDTO query) {
        return Result.success(outpatientLogService.myPendingPage(query));
    }

    /**
     * 本人应报未报的统计计数，口径与上面那份自查清单逐字一致。
     * 用于「我的未报」页顶部统计条。
     */
    @Operation(summary = "我的应报未报统计条（与 myPending 同口径）")
    @GetMapping("/myPendingStats")
    @PreAuthorize("hasAuthority('opd:outpatientLog:mine')")
    public Result<OutpatientLogStatsVO> myPendingStats(@Valid OutpatientLogQueryDTO query) {
        return Result.success(outpatientLogService.myPendingStats(query));
    }
}
